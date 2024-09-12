/*
* Copyright (c) 2024 Nutanix Inc. All rights reserved.
*
* Author: harsh.seta@nutanix.com
*
* This file contains the ETag utility functions for the Domain Manager GET/PUT/DELETE APIs.
 */

package utils

import (
	"context"
	"encoding/base64"
	"fmt"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/errors/guru_error"
	"ntnx-api-guru-pc/guru-pc-api-service/models"
	"strings"

	"github.com/nutanix-core/go-cache/insights/insights_interface"
	log "github.com/sirupsen/logrus"
	"google.golang.org/grpc"
	"google.golang.org/grpc/metadata"
)

// Helper function to create an ETag for the Domain Manager
// It is a base64 encoded string of the last modified timestamp and the CAS value.
// To be used in the GET API handlers.
func encodeDomainManagerETag(domainManagerETag *models.DomainManagerETag) string {
	return base64.StdEncoding.EncodeToString(
		[]byte(fmt.Sprintf("%d,%d", domainManagerETag.LastModifiedTimestamp, domainManagerETag.CasValue)))
}

// Helper function to decode the ETag for the Domain Manager
// To be used in the PUT/DELETE API handlers.
func decodeDomainManagerETag(etagEncoding string) (*models.DomainManagerETag, error) {
	var lastModifiedTimestamp, casValue uint64
	etagByte, err := base64.StdEncoding.DecodeString(etagEncoding)
	if err != nil {
		return nil, fmt.Errorf(
			"failed to decode ETag %s to bytes: %w", etagEncoding, err)
	}
	parsedCount, err := fmt.Sscanf(
		string(etagByte), "%d,%d", &lastModifiedTimestamp, &casValue)
	if err != nil {
		return nil, fmt.Errorf(
			"failed to parse all the timestamps from ETag %s: %w",
			etagEncoding, err)
	}
	if parsedCount != 2 {
		return nil, fmt.Errorf(
			"failed to parse all the timestamps from ETag %s", etagEncoding)
	}
	return &models.DomainManagerETag{
		LastModifiedTimestamp: lastModifiedTimestamp,
		CasValue:              casValue,
	}, nil
}

// Helper function to create the current Domain Manager ETag from the IDF entity.
func GetCurrentDomainManagerETag(entity *insights_interface.Entity, eTag *models.DomainManagerETag) {
	eTag.LastModifiedTimestamp = entity.GetModifiedTimestampUsecs()
	eTag.CasValue = entity.GetCasValue()
}

// Helper function to populate the ETag in the response headers.
// To be used in the GET/PUT/DELETE API handlers.
func PopulateETagInHeaders(ctx context.Context, domainManagerETag *models.DomainManagerETag) {
	etagValue := encodeDomainManagerETag(domainManagerETag)
	log.Debugf("ETag header generated %s", etagValue)
	header := metadata.Pairs("etag", etagValue)
	err := grpc.SetHeader(ctx, header)
	if err != nil {
		log.Warnf("Failed to set ETag header in the response: %s", err)
	}
}

// Helper function to retrieve the corresponding Domain Manager ETag. Returns error on failure.
// To be used in the PUT/DELETE API handlers.
func GetDomainManagerETagFromContext(ctx context.Context, headerName string, operation string) (
	*models.DomainManagerETag, guru_error.GuruErrorInterface) {

	headers, ok := metadata.FromIncomingContext(ctx)
	// No headers present in the request context.
	if !ok {
		return nil, guru_error.GetMissingEtagError(operation)
	}

	// ETag is missing in the request context.
	headerList, present := headers[headerName]
	if !present || headerList == nil {
		return nil, guru_error.GetMissingEtagError(operation)
	}

	// Multiple ETags are not allowed in the request context.
	if len(headerList) > 1 {
		return nil, guru_error.GetInvalidEtagError(operation, strings.Join(headerList, `,`))
	}

	// Decode the ETag.
	domainManagerETag, err := decodeDomainManagerETag(headerList[0])
	if err != nil {
		return nil, guru_error.GetInvalidEtagError(operation, headerList[0])
	}

	return domainManagerETag, nil
}

// Helper function to verify the ETag in the request context with the current Domain Manager ETag.
// Returns error on ETag mismatch.
// To be used in the PUT/DELETE API handlers.
func VerifyDomainManagerETag(ctx context.Context,
	entity *insights_interface.Entity, currentETag *models.DomainManagerETag,
	operation string) guru_error.GuruErrorInterface {

	requestETag, err := GetDomainManagerETagFromContext(ctx, consts.IfMatchHeader, operation)
	if err != nil {
		return err
	}

	// Check for ETag mismatch.
	GetCurrentDomainManagerETag(entity, currentETag)
	if requestETag.LastModifiedTimestamp != currentETag.LastModifiedTimestamp ||
		requestETag.CasValue != currentETag.CasValue {
		return guru_error.GetETagMismatchError(operation, encodeDomainManagerETag(requestETag), encodeDomainManagerETag(currentETag))
	}
	return nil
}
