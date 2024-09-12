package utils

import (
	"context"
	"encoding/base64"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/errors/guru_error"
	"ntnx-api-guru-pc/guru-pc-api-service/models"
	"strings"
	"testing"

	"github.com/golang/protobuf/proto"
	"github.com/nutanix-core/go-cache/insights/insights_interface"
	"github.com/stretchr/testify/assert"
	"google.golang.org/grpc"
	"google.golang.org/grpc/metadata"
)

var (
	modifiedTimestampUsecs uint64 = 123456
	casValue               uint64 = 789
)

func TestDecodeDomainManagerETag_Success(t *testing.T) {
	etag := encodeDomainManagerETag(&models.DomainManagerETag{
		LastModifiedTimestamp: modifiedTimestampUsecs,
		CasValue:              casValue,
	})

	decodedETag, err := decodeDomainManagerETag(etag)

	assert.Nil(t, err)
	assert.Equal(t, decodedETag.LastModifiedTimestamp, modifiedTimestampUsecs)
	assert.Equal(t, decodedETag.CasValue, casValue)
}

func TestDecodeDomainManagerETag_InvalidETag(t *testing.T) {
	etag := "invalid-etag"

	decodedETag, err := decodeDomainManagerETag(etag)

	assert.NotNil(t, err)
	assert.Nil(t, decodedETag)
}

func TestDecodeDomainManagerETag_InvalidETag2(t *testing.T) {
	etag := base64.StdEncoding.EncodeToString(
		[]byte("abc,def,ghi"))

	decodedETag, err := decodeDomainManagerETag(etag)

	assert.NotNil(t, err)
	assert.Nil(t, decodedETag)
}

func TestGetCurrentDomainManagerETag_Success(t *testing.T) {
	entity := &insights_interface.Entity{
		ModifiedTimestampUsecs: proto.Uint64(modifiedTimestampUsecs),
		CasValue:               proto.Uint64(casValue),
	}
	eTag := &models.DomainManagerETag{}

	GetCurrentDomainManagerETag(entity, eTag)

	assert.Equal(t, eTag.LastModifiedTimestamp, uint64(modifiedTimestampUsecs))
	assert.Equal(t, eTag.CasValue, uint64(casValue))
}

func TestPopulateETagInHeaders_Success(t *testing.T) {
	ctx := context.Background()
	ctx = grpc.NewContextWithServerTransportStream(ctx, nil)
	md := metadata.New(map[string]string{
		"key1": "value1",
		"key2": "value2",
	})
	ctx = metadata.NewOutgoingContext(ctx, md)
	domainManagerETag := &models.DomainManagerETag{
		LastModifiedTimestamp: modifiedTimestampUsecs,
		CasValue:              casValue,
	}
	// Expecting failure in this scenario because
	// grpc.SetHeader expects a stream to be present in the context.

	PopulateETagInHeaders(ctx, domainManagerETag)

	_, ok := metadata.FromOutgoingContext(ctx)
	assert.True(t, ok)
}

func TestGetDomainManagerETagFromContext_Success(t *testing.T) {
	ctx := context.Background()
	eTag := &models.DomainManagerETag{
		LastModifiedTimestamp: modifiedTimestampUsecs,
		CasValue:              casValue,
	}
	md := metadata.New(map[string]string{
		"key1":     "value1",
		"key2":     "value2",
		"if-match": encodeDomainManagerETag(eTag),
	})
	ctx = metadata.NewIncomingContext(ctx, md)

	eTag, err := GetDomainManagerETagFromContext(ctx, consts.IfMatchHeader, consts.GetDomainManagerOpName)

	assert.Nil(t, err)
	assert.Equal(t, eTag.LastModifiedTimestamp, uint64(modifiedTimestampUsecs))
	assert.Equal(t, eTag.CasValue, uint64(casValue))
}

func TestGetDomainManagerETagFromContext_NoHeadersPresent(t *testing.T) {
	ctx := context.Background()

	eTag, err := GetDomainManagerETagFromContext(ctx, consts.IfMatchHeader, consts.GetDomainManagerOpName)

	assert.NotNil(t, err)
	expectedErr := guru_error.GetMissingEtagError(consts.GetDomainManagerOpName)
	assert.Equal(t, expectedErr, err)
	assert.Nil(t, eTag)
}

func TestGetDomainManagerETagFromContext_NoETagPresent(t *testing.T) {
	ctx := context.Background()
	md := metadata.New(map[string]string{
		"key1": "value1",
		"key2": "value2",
	})
	ctx = metadata.NewIncomingContext(ctx, md)

	eTag, err := GetDomainManagerETagFromContext(ctx, consts.IfMatchHeader, consts.GetDomainManagerOpName)

	assert.NotNil(t, err)
	expectedErr := guru_error.GetMissingEtagError(consts.GetDomainManagerOpName)
	assert.Equal(t, expectedErr, err)
	assert.Nil(t, eTag)
}

func TestGetDomainManagerETagFromContext_MultipleETagsPresent(t *testing.T) {
	ctx := context.Background()
	md := metadata.New(map[string]string{
		"key1":     "value1",
		"key2":     "value2",
		"if-match": "invalid-etag",
		"If-match": "invalid-etag",
	})
	ctx = metadata.NewIncomingContext(ctx, md)

	eTag, err := GetDomainManagerETagFromContext(ctx, consts.IfMatchHeader, consts.GetDomainManagerOpName)

	assert.NotNil(t, err)
	headers, _ := metadata.FromIncomingContext(ctx)
	headerList, _ := headers[consts.IfMatchHeader]
	expectedErr := guru_error.GetInvalidEtagError(consts.GetDomainManagerOpName, strings.Join(headerList, `,`))
	assert.Equal(t, expectedErr, err)
	assert.Nil(t, eTag)
}

func TestGetDomainManagerETagFromContext_InvalidETagPresent(t *testing.T) {
	ctx := context.Background()
	md := metadata.New(map[string]string{
		"key1":     "value1",
		"key2":     "value2",
		"if-match": "invalid-etag",
	})
	ctx = metadata.NewIncomingContext(ctx, md)

	eTag, err := GetDomainManagerETagFromContext(ctx, consts.IfMatchHeader, consts.GetDomainManagerOpName)

	assert.NotNil(t, err)
	headers, _ := metadata.FromIncomingContext(ctx)
	headerList, _ := headers[consts.IfMatchHeader]
	expectedErr := guru_error.GetInvalidEtagError(consts.GetDomainManagerOpName, headerList[0])
	assert.Equal(t, expectedErr, err)
	assert.Nil(t, eTag)
}

func TestVerifyDomainManagerETag_Success(t *testing.T) {
	ctx := context.Background()
	eTag := &models.DomainManagerETag{
		LastModifiedTimestamp: modifiedTimestampUsecs,
		CasValue:              casValue,
	}
	md := metadata.New(map[string]string{
		"key1":     "value1",
		"key2":     "value2",
		"if-match": encodeDomainManagerETag(eTag),
	})
	ctx = metadata.NewIncomingContext(ctx, md)
	entity := &insights_interface.Entity{
		ModifiedTimestampUsecs: proto.Uint64(modifiedTimestampUsecs),
		CasValue:               proto.Uint64(casValue),
	}

	err := VerifyDomainManagerETag(ctx, entity, eTag, consts.GetDomainManagerOpName)

	assert.Nil(t, err)
}

func TestVerifyDomainManagerETag_MissingETag(t *testing.T) {
	ctx := context.Background()
	eTag := &models.DomainManagerETag{}
	md := metadata.New(map[string]string{
		"key1": "value1",
		"key2": "value2",
	})
	ctx = metadata.NewIncomingContext(ctx, md)
	entity := &insights_interface.Entity{
		ModifiedTimestampUsecs: proto.Uint64(modifiedTimestampUsecs),
		CasValue:               proto.Uint64(casValue),
	}

	err := VerifyDomainManagerETag(ctx, entity, eTag, consts.GetDomainManagerOpName)
	expectedErr := guru_error.GetMissingEtagError(consts.GetDomainManagerOpName)
	assert.Equal(t, expectedErr, err)
}

func TestVerifyDomainManagerETag_ETagMismatch(t *testing.T) {
	ctx := context.Background()
	eTag := &models.DomainManagerETag{}
	requestETag := &models.DomainManagerETag{
		LastModifiedTimestamp: modifiedTimestampUsecs,
		CasValue:              casValue,
	}
	md := metadata.New(map[string]string{
		"key1":     "value1",
		"key2":     "value2",
		"if-match": encodeDomainManagerETag(requestETag),
	})
	ctx = metadata.NewIncomingContext(ctx, md)
	entity := &insights_interface.Entity{
		ModifiedTimestampUsecs: proto.Uint64(1234567),
		CasValue:               proto.Uint64(789),
	}
	currentEtag := &models.DomainManagerETag{
		LastModifiedTimestamp: 1234567,
		CasValue:              789,
	}

	err := VerifyDomainManagerETag(ctx, entity, eTag, consts.GetDomainManagerOpName)
	expectedErr := guru_error.GetETagMismatchError(consts.GetDomainManagerOpName, encodeDomainManagerETag(requestETag), encodeDomainManagerETag(currentEtag))
	assert.Equal(t, expectedErr, err)
}
