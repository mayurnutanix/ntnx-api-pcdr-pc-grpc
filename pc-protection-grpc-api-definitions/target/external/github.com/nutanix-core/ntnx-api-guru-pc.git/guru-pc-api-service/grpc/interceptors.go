/*
 * Copyright (c) 2024 Nutanix Inc. All rights reserved.
 * Author: kshitij.kumar@nutanix.com
 * This file contains utilities for unary logger interceptor which intercepts
 * incoming requests, extracts the context and other attributes and invoke
 * the access logger.
 */

package grpc

import (
	"context"
	"time"

	"github.com/golang/protobuf/proto"
	log "github.com/sirupsen/logrus"
	"google.golang.org/grpc"
	"google.golang.org/grpc/metadata"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/utils"
)

// UnaryLoggerInterceptor UnaryInterceptor logs details of unary RPC calls
func (server *GrpcServerImpl) UnaryLoggerInterceptor(
	ctx context.Context,
	req interface{},
	info *grpc.UnaryServerInfo,
	handler grpc.UnaryHandler,
) (interface{}, error) {
	hostName := utils.FetchHostName(ctx)
	username := utils.FetchUsername(ctx)
	contextId := utils.FetchRequestIdFromMetadata(ctx)
	startTime := time.Now()
	log.Debugf("Invoking the handler for request : %+v and context : %+v", req, ctx)
	responseSize := 0
	resp, err := handler(ctx, req)
	if err != nil {
		log.Errorf("Method %s failed with error: %v", info.FullMethod, err)
	}

	responseSize = proto.Size(resp.(proto.Message))
	server.accessLogger.WriteToLog(info.FullMethod, startTime, err, contextId, username, hostName, responseSize)
	log.Debugf("Returning response : %+v", resp)
	return resp, err
}

func fetchRequestIdFromMetadata(md metadata.MD) string {
	// Extract requestId from context metadata
	requestIdList, _ := md[consts.XRequestId]
	// Return if any type of the request-Id is populated, empty string if none is present.
	switch {
	case len(requestIdList) > 0:
		return requestIdList[0]
	default:
		return ""
	}
}
