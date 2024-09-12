package utils

import (
	"errors"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/interfaces/external"
	"ntnx-api-guru-pc/guru-pc-api-service/mocks"
	test_consts "ntnx-api-guru-pc/guru-pc-api-service/services/register_aos/test-consts"
	"testing"

	"github.com/golang/mock/gomock"
	"github.com/nutanix-core/go-cache/infrastructure/cluster/client/genesis"
	"github.com/stretchr/testify/assert"
)

func TestGetRootCertGetCACertificateError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	mockGenesisService := mocks.NewMockIGenesisRpcClient(ctrl)
	external.SetSingletonServices(nil, nil, nil, nil,
		nil, nil, nil, nil, nil, nil, nil, mockGenesisService, nil, nil, nil, nil, nil)
	mockGenesisService.EXPECT().GetCACertificate(gomock.Any()).Return(nil, errors.New("error"))
	res, err := GetRootCert("cluster-uuid")
	assert.Equal(t, "", res)
	assert.Equal(t, errors.New("error"), err)
}

func TestGetRootCertSuccess(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	mockGenesisService := mocks.NewMockIGenesisRpcClient(ctrl)
	external.SetSingletonServices(nil, nil, nil, nil,
		nil, nil, nil, nil, nil, nil, nil, mockGenesisService, nil, nil, nil, nil, nil)
	getCaCertRet := genesis.GetCACertificateRet{
		Error:  nil,
		CaCert: nil,
	}
	mockGenesisService.EXPECT().GetCACertificate(gomock.Any()).Return(&getCaCertRet, nil)
	res, err := GetRootCert("cluster-uuid")
	assert.Equal(t, "", res)
	assert.Nil(t, err)
}

func TestReadFileError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	res, err := ReadFile("")
	assert.Equal(t, &consts.EmptyString, res)
	assert.NotNil(t, err)
}

func TestReadFileSuccess(t *testing.T) {
	old := OsReadFileFunc
	defer func() { OsReadFileFunc = old }()
	OsReadFileFunc = func(path string) ([]byte, error) {
		return nil, nil
	}
	res, err := ReadFile("")
	assert.NotNil(t, res)
	assert.Nil(t, err)
}

func TestGetCAChainentityCertReadFileError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	old := OsReadFileFunc
	defer func() { OsReadFileFunc = old }()
	OsReadFileFunc = func(path string) ([]byte, error) {
		return nil, errors.New("error")
	}
	rootCert, certChain, err := GetCAChain()
	assert.Equal(t, &consts.EmptyString, rootCert)
	assert.Nil(t, certChain)
	assert.Equal(t, "reading entity cert: error", err.Error())
}

func TestGetCAChainIcaCertReadFileError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	old := OsReadFileFunc
	certificateBytes := []byte(test_consts.TestCertificate)
	defer func() { OsReadFileFunc = old }()
	OsReadFileFunc = func(path string) ([]byte, error) {
		if path == consts.IcaCertPath {
			return nil, errors.New("error")
		}
		return certificateBytes, nil
	}
	rootCert, certChain, err := GetCAChain()
	assert.Equal(t, &consts.EmptyString, rootCert)
	assert.Nil(t, certChain)
	assert.Equal(t, "reading ica cert: error", err.Error())
}

func TestGetCAChainRootCertReadFileError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	old := OsReadFileFunc
	certificateBytes := []byte(test_consts.TestCertificate)
	defer func() { OsReadFileFunc = old }()
	OsReadFileFunc = func(path string) ([]byte, error) {
		if path == consts.RootCertPath {
			return nil, errors.New("error")
		}
		return certificateBytes, nil
	}
	rootCert, certChain, err := GetCAChain()
	assert.Equal(t, &consts.EmptyString, rootCert)
	assert.Nil(t, certChain)
	assert.Equal(t, "reading root cert: error", err.Error())
}
func TestGetCAChain(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	old := OsReadFileFunc
	defer func() { OsReadFileFunc = old }()
	certificateBytes := []byte(test_consts.TestCertificate)
	OsReadFileFunc = func(path string) ([]byte, error) {
		return certificateBytes, nil
	}
	rootCert, certChain, err := GetCAChain()
	assert.Equal(t, &test_consts.TestCertificate, rootCert)
	assert.NotNil(t, certChain)
	assert.Nil(t, err)
}

func TestCreateV3Jwt_ReadFileError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	old := OsReadFileFunc
	defer func() { OsReadFileFunc = old }()
	OsReadFileFunc = func(path string) ([]byte, error) {
		return nil, errors.New("error")
	}
	res, err := CreateV3Jwt("privateKeyPath")
	assert.Equal(t, "", res)
	assert.Equal(t, errors.New("error"), err)
}

func TestCreateV3Jwt_Success(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	old := OsReadFileFunc
	defer func() { OsReadFileFunc = old }()
	privateKeyBytes := []byte(test_consts.TestPrivateKey)
	OsReadFileFunc = func(path string) ([]byte, error) {
		return privateKeyBytes, nil
	}
	res, err := CreateV3Jwt("privateKeyPath")
	assert.NotNil(t, res)
	assert.Nil(t, err)
}

func TestCreateV3Jwt_BlockNilError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	old := OsReadFileFunc
	defer func() { OsReadFileFunc = old }()
	OsReadFileFunc = func(path string) ([]byte, error) {
		return nil, nil
	}
	res, err := CreateV3Jwt("privateKeyPath")
	assert.Equal(t, "", res)
	assert.NotNil(t, err)
	assert.Equal(t, consts.ErrorPrivateKeyDecode, err)
}

func TestDecodeJwtInvalidToken(t *testing.T) {
	token := "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ"
	response, err := DecodeJwt(token)
	assert.Errorf(t, err, "invalid service token")
	assert.Nil(t, response)
}

func TestDecodeJwtDecodeFails(t *testing.T) {
	token :=
		"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.IiwiaWF0IjoxNjI1MDg0NzU3LCJleHAiOjE2MjUwODgzNTd9.wODgzNTd9"
	response, err := DecodeJwt(token)
	assert.Errorf(t, err, "token decode failed")
	assert.Nil(t, response)
}

func TestDecodeJwtSuccess(t *testing.T) {
	token :=
		"eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE3MjE4MDk5NzAsImlhdCI6MTcyMTgwOTY3MCwiaXNzIjoiR3VydVNlcnZpY2UifQ" +
			".YzBZR0lychgSyBcod7KDriADjxM5xUbcxtp-Fewts6o-EIxRgtkruLakmuyqpZhjV2XUq4vct3NfkAU0AKZUmO7-ewJ6hrytvUyPmuPeMJ" +
			"9fxmrpCHTlBExhIf30a-oA9NT6H-nn-5wV7fQ8T3tTq5LU5o3Ed-ZWm3ZKX6EItLMyffmZxf1vljcVWa5vqFv3nVqoIv43ktcCYBllpiFyA" +
			"EiQzQ9G0YhUFUg6DKZ1cQMLPNi1MCmbJGqgbrSvistW39rgAW6Redeg5cjUXSc-Qv1M1piIav-3p3R1su7dEuxHP-Yo9565o4EZTbWoVi5" +
			"4w85tnLYmAfdO1jjnQ-jn9A"
	response, err := DecodeJwt(token)
	assert.Nil(t, err)
	assert.Equal(t, response["iss"].(string), "GuruService")
}
