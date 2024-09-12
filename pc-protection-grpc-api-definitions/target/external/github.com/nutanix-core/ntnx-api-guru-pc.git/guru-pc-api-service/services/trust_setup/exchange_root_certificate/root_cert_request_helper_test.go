package exchange_root_certificate

import (
	"encoding/base64"
	"errors"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/test"
	"ntnx-api-guru-pc/guru-pc-api-service/utils"
	"testing"

	"github.com/golang/mock/gomock"
	"github.com/nutanix-core/go-cache/ergon"
	genesisSvc "github.com/nutanix-core/go-cache/infrastructure/cluster/client/genesis"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/mock"
)

func TestSaveRootCertSuccess(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	pcUuid := "123"
	rootCertificate := "789"
	base64EncodedCertificate := base64.StdEncoding.EncodeToString([]byte(rootCertificate))
	args := genesisSvc.AddCACertificateArg{
		CaCert: &genesisSvc.CACert{
			CaCertificate:    []byte(base64EncodedCertificate),
			OwnerClusterUuid: []byte(pcUuid),
			Owner:            genesisSvc.CACert_kPC.Enum(),
		},
	}
	ret := genesisSvc.AddCACertificateRet{
		Error: &genesisSvc.GenesisGenericError{
			ErrorType: genesisSvc.GenesisGenericError_kNoError.Enum(),
		},
	}
	m.GenesisClient.EXPECT().AddCACertificate(&args).Return(&ret, nil)
	err := SaveRootCert(pcUuid, &rootCertificate)
	assert.Nil(t, err)
}

func TestSaveRootCertRetryError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	pcUuid := "123"
	rootCertificate := "789"
	base64EncodedCertificate := base64.StdEncoding.EncodeToString([]byte(rootCertificate))
	args := genesisSvc.AddCACertificateArg{
		CaCert: &genesisSvc.CACert{
			CaCertificate:    []byte(base64EncodedCertificate),
			OwnerClusterUuid: []byte(pcUuid),
			Owner:            genesisSvc.CACert_kPC.Enum(),
		},
	}
	ret := genesisSvc.AddCACertificateRet{
		Error: &genesisSvc.GenesisGenericError{
			ErrorType: genesisSvc.GenesisGenericError_kRetry.Enum(),
		},
	}
	m.GenesisClient.EXPECT().AddCACertificate(&args).Return(&ret, nil)
	err := SaveRootCert(pcUuid, &rootCertificate)
	assert.Equal(t, consts.ErrorGenesisRetry, err)
}

func TestSaveRootCertGenesisError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	pcUuid := "123"
	rootCertificate := "789"
	base64EncodedCertificate := base64.StdEncoding.EncodeToString([]byte(rootCertificate))
	args := genesisSvc.AddCACertificateArg{
		CaCert: &genesisSvc.CACert{
			CaCertificate:    []byte(base64EncodedCertificate),
			OwnerClusterUuid: []byte(pcUuid),
			Owner:            genesisSvc.CACert_kPC.Enum(),
		},
	}
	ret := genesisSvc.AddCACertificateRet{
		Error: &genesisSvc.GenesisGenericError{
			ErrorType: genesisSvc.GenesisGenericError_kNetworkError.Enum(),
		},
	}
	m.GenesisClient.EXPECT().AddCACertificate(&args).Return(&ret, nil)
	err := SaveRootCert(pcUuid, &rootCertificate)
	assert.Equal(t, consts.ErrorCertAddError, err)
}

func TestSaveRootCertError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	pcUuid := "123"
	rootCertificate := "789"
	base64EncodedCertificate := base64.StdEncoding.EncodeToString([]byte(rootCertificate))
	args := genesisSvc.AddCACertificateArg{
		CaCert: &genesisSvc.CACert{
			CaCertificate:    []byte(base64EncodedCertificate),
			OwnerClusterUuid: []byte(pcUuid),
			Owner:            genesisSvc.CACert_kPC.Enum(),
		},
	}
	m.GenesisClient.EXPECT().AddCACertificate(&args).Return(nil, errors.New("error"))
	err := SaveRootCert(pcUuid, &rootCertificate)
	assert.Equal(t, consts.ErrorCertAddError, err)
}

func TestDeleteRootCertSuccess(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	pcUuid := "123"
	args := genesisSvc.DeleteCACertificateArg{
		ClusterUuid: []byte(pcUuid),
	}
	ret := genesisSvc.DeleteCACertificateRet{
		Error: &genesisSvc.GenesisGenericError{
			ErrorType: genesisSvc.GenesisGenericError_kNoError.Enum(),
		},
	}
	m.GenesisClient.EXPECT().DeleteCACertificate(&args).Return(&ret, nil)
	err := DeleteRootCert(pcUuid)
	assert.Nil(t, err)
}

func TestDeleteRootCertRetryError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	pcUuid := "123"
	args := genesisSvc.DeleteCACertificateArg{
		ClusterUuid: []byte(pcUuid),
	}
	ret := genesisSvc.DeleteCACertificateRet{
		Error: &genesisSvc.GenesisGenericError{
			ErrorType: genesisSvc.GenesisGenericError_kRetry.Enum(),
		},
	}
	m.GenesisClient.EXPECT().DeleteCACertificate(&args).Return(&ret, nil)
	err := DeleteRootCert(pcUuid)
	assert.Equal(t, consts.ErrorGenesisRetry, err)
}

func TestDeleteRootCertGenesisError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	pcUuid := "123"
	args := genesisSvc.DeleteCACertificateArg{
		ClusterUuid: []byte(pcUuid),
	}
	ret := genesisSvc.DeleteCACertificateRet{
		Error: &genesisSvc.GenesisGenericError{
			ErrorType: genesisSvc.GenesisGenericError_kNetworkError.Enum(),
		},
	}
	m.GenesisClient.EXPECT().DeleteCACertificate(&args).Return(&ret, nil)
	err := DeleteRootCert(pcUuid)
	assert.Equal(t, consts.ErrorCertDeleteError, err)
}

func TestDeleteRootCertError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	pcUuid := "123"
	args := genesisSvc.DeleteCACertificateArg{
		ClusterUuid: []byte(pcUuid),
	}
	m.GenesisClient.EXPECT().DeleteCACertificate(&args).Return(nil, errors.New("error"))
	err := DeleteRootCert(pcUuid)
	assert.Equal(t, consts.ErrorCertDeleteError, err)
}

func TestProcessAddRootCertRequestSuccess(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	pcUuid := "123"
	rootCertificate := "789"
	base64EncodedCertificate := base64.StdEncoding.EncodeToString([]byte(rootCertificate))
	args := genesisSvc.AddCACertificateArg{
		CaCert: &genesisSvc.CACert{
			CaCertificate:    []byte(base64EncodedCertificate),
			OwnerClusterUuid: []byte(pcUuid),
			Owner:            genesisSvc.CACert_kPC.Enum(),
		},
	}
	ret := genesisSvc.AddCACertificateRet{
		Error: &genesisSvc.GenesisGenericError{
			ErrorType: genesisSvc.GenesisGenericError_kNoError.Enum(),
		},
	}
	m.GenesisClient.EXPECT().AddCACertificate(&args).Return(&ret, nil)
	old := utils.OsReadFileFunc
	defer func() { utils.OsReadFileFunc = old }()
	utils.OsReadFileFunc = func(path string) ([]byte, error) {
		return []byte{}, nil
	}
	var timestamp int64 = 1
	taskRet := ergon.Task{
		LogicalTimestamp: &timestamp,
	}
	m.ErgonService.On("TaskGet", []byte("456")).Return(&taskRet, nil)
	updateRet := ergon.TaskUpdateRet{}
	m.ErgonService.On("TaskUpdateBase", mock.Anything).Return(&updateRet, nil)
	err := ProcessAddRootCertRequest([]byte("456"), &rootCertificate, pcUuid)
	assert.Nil(t, err)
}

func TestProcessAddRootCertRequestSaveError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	pcUuid := "123"
	rootCertificate := "789"
	base64EncodedCertificate := base64.StdEncoding.EncodeToString([]byte(rootCertificate))
	args := genesisSvc.AddCACertificateArg{
		CaCert: &genesisSvc.CACert{
			CaCertificate:    []byte(base64EncodedCertificate),
			OwnerClusterUuid: []byte(pcUuid),
			Owner:            genesisSvc.CACert_kPC.Enum(),
		},
	}
	ret := genesisSvc.AddCACertificateRet{
		Error: &genesisSvc.GenesisGenericError{
			ErrorType: genesisSvc.GenesisGenericError_kRetry.Enum(),
		},
	}
	m.GenesisClient.EXPECT().AddCACertificate(&args).Return(&ret, nil)
	err := ProcessAddRootCertRequest([]byte("456"), &rootCertificate, pcUuid)
	assert.Equal(t, consts.ErrorGenesisRetry, err)
}

func TestProcessAddRootCertRequestGetCertError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	pcUuid := "123"
	rootCertificate := "789"
	base64EncodedCertificate := base64.StdEncoding.EncodeToString([]byte(rootCertificate))
	args := genesisSvc.AddCACertificateArg{
		CaCert: &genesisSvc.CACert{
			CaCertificate:    []byte(base64EncodedCertificate),
			OwnerClusterUuid: []byte(pcUuid),
			Owner:            genesisSvc.CACert_kPC.Enum(),
		},
	}
	ret := genesisSvc.AddCACertificateRet{
		Error: &genesisSvc.GenesisGenericError{
			ErrorType: genesisSvc.GenesisGenericError_kNoError.Enum(),
		},
	}
	m.GenesisClient.EXPECT().AddCACertificate(&args).Return(&ret, nil)
	old := utils.OsReadFileFunc
	defer func() { utils.OsReadFileFunc = old }()
	utils.OsReadFileFunc = func(path string) ([]byte, error) {
		return nil, errors.New("error")
	}
	err := ProcessAddRootCertRequest([]byte("456"), &rootCertificate, pcUuid)
	assert.Equal(t, errors.New("error"), err)
}
