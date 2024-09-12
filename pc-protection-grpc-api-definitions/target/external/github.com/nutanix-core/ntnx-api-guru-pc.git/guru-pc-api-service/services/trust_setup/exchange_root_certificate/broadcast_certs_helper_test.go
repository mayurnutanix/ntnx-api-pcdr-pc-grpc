package exchange_root_certificate

import (
	"encoding/base64"
	"encoding/json"
	"errors"
	"fmt"
	"net/http"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	test_consts "ntnx-api-guru-pc/guru-pc-api-service/services/register_aos/test-consts"
	"ntnx-api-guru-pc/guru-pc-api-service/test"
	"ntnx-api-guru-pc/guru-pc-api-service/utils"
	"testing"

	zk "github.com/nutanix-core/go-cache/go-zookeeper"
	genesisSvc "github.com/nutanix-core/go-cache/infrastructure/cluster/client/genesis"

	"github.com/golang/mock/gomock"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/mock"
)

func TestGetV3PayloadCreateV3JwtError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	old := utils.OsReadFileFunc
	defer func() { utils.OsReadFileFunc = old }()
	utils.OsReadFileFunc = func(path string) ([]byte, error) {
		return nil, errors.New("error")
	}
	cert := "123"

	payload, err := getV3Payload(&cert)

	assert.Nil(t, payload)
	assert.Equal(t, errors.New("error"), err)
}

func TestGetV3PayloadGetCAChainError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	old := utils.OsReadFileFunc
	defer func() { utils.OsReadFileFunc = old }()
	privateKeyBytes := []byte(test_consts.TestPrivateKey)
	utils.OsReadFileFunc = func(path string) ([]byte, error) {
		if path == consts.EntityCertPath {
			return nil, errors.New("error")
		}
		return privateKeyBytes, nil
	}
	cert := "123"

	payload, err := getV3Payload(&cert)

	assert.Nil(t, payload)
	assert.Equal(t, "reading entity cert: error", err.Error())
}

func TestGetV3Payload(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	old := utils.OsReadFileFunc
	defer func() { utils.OsReadFileFunc = old }()
	privateKeyBytes := []byte(test_consts.TestPrivateKey)
	utils.OsReadFileFunc = func(path string) ([]byte, error) {
		return privateKeyBytes, nil
	}
	cert := "123"

	payload, err := getV3Payload(&cert)

	assert.NotNil(t, payload)
	assert.Nil(t, err)
}

func TestFanoutCertToPeSuccess(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	pcUuid := "123"
	certString := "789"
	old := utils.OsReadFileFunc
	defer func() { utils.OsReadFileFunc = old }()
	privateKeyBytes := []byte(test_consts.TestPrivateKey)
	utils.OsReadFileFunc = func(path string) ([]byte, error) {
		return privateKeyBytes, nil
	}
	peUuid := "456"
	url := fmt.Sprintf(V3CertApiPath, pcUuid)
	code := http.StatusOK
	m.RemoteRestClient.EXPECT().FanoutProxy(http.MethodPost,
		gomock.Any(), http.MethodPost, peUuid, url).Return([]byte{}, &code, nil)

	err := fanoutCertToPe(&certString, pcUuid, peUuid)

	assert.Nil(t, err)
}

func TestStartRootCertBroadcastGetRegisteredPeListError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	m.ZkSession.On("Children", consts.AOSClusterCES, true).Return(nil,
		nil, errors.New("error"))

	err := startRootCertBroadcast("")

	assert.Equal(t, errors.New("error"), err)
}

func TestStartRootCertBroadcastError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	args := genesisSvc.GetCACertificateArg{
		ClusterUuid: []byte("123"),
	}
	certString := "789"
	cert := base64.StdEncoding.EncodeToString([]byte(certString))
	ret := genesisSvc.GetCACertificateRet{
		CaCert: &genesisSvc.CACert{
			CaCertificate: []byte(cert),
		},
	}
	m.GenesisClient.EXPECT().GetCACertificate(&args).Return(&ret, nil)
	m.ZkSession.On("Get", BroadcastZkPath+"123", true).Return(nil, nil, errors.New("error"))
	m.ZkSession.On("Children", consts.AOSClusterCES, true).Return(nil,
		nil, nil)

	err := startRootCertBroadcast("123")

	assert.Equal(t, errors.New("error"), err)
}

func TestStartRootCertBroadcast(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	pcUuid := "123"
	args := genesisSvc.GetCACertificateArg{
		ClusterUuid: []byte("123"),
	}
	certString := "789"
	cert := base64.StdEncoding.EncodeToString([]byte(certString))
	ret := genesisSvc.GetCACertificateRet{
		CaCert: &genesisSvc.CACert{
			CaCertificate: []byte(cert),
		},
	}
	m.GenesisClient.EXPECT().GetCACertificate(&args).Return(&ret, nil)
	statusMap := broadcastStatusMap{
		PcUuid:    pcUuid,
		StatusMap: make(map[string]bool),
	}
	statusMap.StatusMap["pe1"] = true
	statusMapMarshal, _ := json.Marshal(statusMap)
	var registeredPeList []string
	registeredPeList = append(registeredPeList, "pe1")
	m.ZkSession.On("Get", BroadcastZkPath+pcUuid, true).Return(statusMapMarshal, nil, nil)
	m.ZkSession.On("Exist", BroadcastZkPath+pcUuid, true).Return(false, nil, errors.New("error")).Once()
	m.ZkSession.On("Children", consts.AOSClusterCES, true).Return(registeredPeList,
		nil, nil)

	err := startRootCertBroadcast(pcUuid)

	assert.Equal(t, "checking for zk node: error", err.Error())
}

func TestBroadCastCertToPeBroadCastListError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	args := genesisSvc.GetCACertificateArg{
		ClusterUuid: []byte("123"),
	}
	certString := "789"
	cert := base64.StdEncoding.EncodeToString([]byte(certString))
	ret := genesisSvc.GetCACertificateRet{
		CaCert: &genesisSvc.CACert{
			CaCertificate: []byte(cert),
		},
	}
	m.GenesisClient.EXPECT().GetCACertificate(&args).Return(&ret, nil)
	m.ZkSession.On("Get", BroadcastZkPath+"123", true).Return(nil, nil, errors.New("error"))

	err := broadcastCertToPE("123", nil)

	assert.Equal(t, errors.New("error"), err)
}

func TestBroadCastCertToPeBroadCastList(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	pcUuid := "123"
	args := genesisSvc.GetCACertificateArg{
		ClusterUuid: []byte("123"),
	}
	certString := "789"
	cert := base64.StdEncoding.EncodeToString([]byte(certString))
	ret := genesisSvc.GetCACertificateRet{
		CaCert: &genesisSvc.CACert{
			CaCertificate: []byte(cert),
		},
	}
	m.GenesisClient.EXPECT().GetCACertificate(&args).Return(&ret, nil)
	statusMap := broadcastStatusMap{
		PcUuid:    pcUuid,
		StatusMap: make(map[string]bool),
	}
	var registeredPeList []string
	registeredPeList = append(registeredPeList, "pe1")
	statusMap.StatusMap["pe1"] = true
	statusMapMarshal, _ := json.Marshal(statusMap)
	m.ZkSession.On("Get", BroadcastZkPath+pcUuid, true).Return(statusMapMarshal, nil, nil)
	m.ZkSession.On("Exist", BroadcastZkPath+pcUuid, true).Return(false, nil, errors.New("error")).Once()

	err := broadcastCertToPE(pcUuid, registeredPeList)

	assert.Equal(t, "checking for zk node: error", err.Error())
}

func TestGetBroadcastedPcList(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	var registeredPcList []string
	registeredPcList = append(registeredPcList, "pc1")
	m.ZkSession.On("Children", BroadcastZkPath, true).Return(registeredPcList, nil, nil)

	ret, err := getBroadcastedPcList()

	assert.Equal(t, registeredPcList, ret)
	assert.Nil(t, err)
}

func TestGetBroadcastedPcListError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	m.ZkSession.On("Children", BroadcastZkPath, true).Return(nil, nil, errors.New("error"))

	ret, err := getBroadcastedPcList()

	assert.Nil(t, ret)
	assert.NotNil(t, err)
}

func TestFanoutDeleteCertToPe(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)

	pcUuid := "123"
	peUuid := "456"
	url := fmt.Sprintf(V3CertApiPath, pcUuid)
	code := http.StatusOK
	m.RemoteRestClient.EXPECT().FanoutProxy(http.MethodDelete,
		gomock.Any(), http.MethodDelete, peUuid, url).Return([]byte{}, &code, nil)
	err := fanoutDeleteCertPe(pcUuid, peUuid)
	assert.Nil(t, err)
}

func TestFanoutDeleteCertToPeError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)

	pcUuid := "123"
	peUuid := "456"
	url := fmt.Sprintf(V3CertApiPath, pcUuid)
	code := http.StatusUnauthorized
	m.RemoteRestClient.EXPECT().FanoutProxy(http.MethodDelete,
		gomock.Any(), http.MethodDelete, peUuid, url).Return([]byte{}, &code, nil)
	err := fanoutDeleteCertPe(pcUuid, peUuid)
	assert.NotNil(t, err)
}

func TestDeleteRootCertBroadcastStartCommitError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	pcUuid := "123"
	statusMap := broadcastStatusMap{
		PcUuid:    pcUuid,
		StatusMap: make(map[string]bool),
	}
	statusMap.StatusMap["pe1"] = true
	statusMapMarshal, _ := json.Marshal(statusMap)
	var registeredPeList []string
	registeredPeList = append(registeredPeList, "pe1")
	m.ZkSession.On("Get", BroadcastZkPath+pcUuid, true).Return(statusMapMarshal, nil, nil)
	m.ZkSession.On("Exist", BroadcastZkPath+pcUuid, true).Return(false, nil, errors.New("error")).Once()
	m.ZkSession.On("Children", consts.AOSClusterCES, true).Return(registeredPeList,
		nil, nil)

	err := deleteCertBroadcast(pcUuid)

	assert.Equal(t, "checking for zk node: error", err.Error())
}

func TestDeleteRootCertBroadcastGetMapError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	pcUuid := "123"
	var registeredPeList []string
	registeredPeList = append(registeredPeList, "pe1")
	m.ZkSession.On("Get", BroadcastZkPath+pcUuid, true).Return(nil, nil, errors.New("error"))
	m.ZkSession.On("Children", consts.AOSClusterCES, true).Return(registeredPeList,
		nil, nil)

	err := deleteCertBroadcast(pcUuid)

	assert.Equal(t, "error", err.Error())
}

func TestDeleteRootCertBroadcast(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	pcUuid := "123"
	var registeredPeList []string
	registeredPeList = append(registeredPeList, "456")
	m.ZkSession.On("Get", BroadcastZkPath+pcUuid, true).Return(nil, nil, zk.ErrNoNode)
	m.ZkSession.On("Children", consts.AOSClusterCES, true).Return(registeredPeList, nil, nil)
	m.ZkSession.On("Exist", BroadcastZkPath+pcUuid, true).Return(false, nil, nil).Once()
	m.ZkSession.On("Create", BroadcastZkPath+pcUuid, mock.Anything, int32(0), zk.WorldACL(zk.PermAll), true).
		Return("", nil).Once()
	stat := zk.Stat{}
	m.ZkSession.On("Exist", BroadcastZkPath+pcUuid, true).Return(true, &stat, nil).Once()
	m.ZkSession.On("Delete", BroadcastZkPath+pcUuid, stat.Version, true).Return(nil).Once()

	url := fmt.Sprintf(V3CertApiPath, pcUuid)
	code := http.StatusOK
	m.RemoteRestClient.EXPECT().FanoutProxy(http.MethodDelete,
		gomock.Any(), http.MethodDelete, "456", url).Return([]byte{}, &code, nil)
	err := deleteCertBroadcast(pcUuid)

	assert.Nil(t, err)
}

func TestStartAllRootCertBroadcast(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	pcUuid := "123"
	args := genesisSvc.GetCACertificateArg{
		ClusterUuid: []byte("123"),
	}
	certString := "789"
	cert := base64.StdEncoding.EncodeToString([]byte(certString))
	ret := genesisSvc.GetCACertificateRet{
		CaCert: &genesisSvc.CACert{
			CaCertificate: []byte(cert),
		},
	}
	m.GenesisClient.EXPECT().GetCACertificate(&args).Return(&ret, nil)
	statusMap := broadcastStatusMap{
		PcUuid:    pcUuid,
		StatusMap: make(map[string]bool),
	}
	statusMap.StatusMap["pe1"] = true
	statusMapMarshal, _ := json.Marshal(statusMap)
	var registeredPeList []string
	registeredPeList = append(registeredPeList, "pe1")
	var registeredPcList []string
	registeredPcList = append(registeredPcList, pcUuid)
	m.ZkSession.On("Get", BroadcastZkPath+pcUuid, true).Return(statusMapMarshal, nil, nil)
	m.ZkSession.On("Exist", BroadcastZkPath+pcUuid, true).Return(false, nil, nil)
	m.ZkSession.On("Children", consts.AOSClusterCES, true).Return(registeredPeList,
		nil, nil).Once()
	m.ZkSession.On("Children", BroadcastZkPath, true).Return(registeredPcList,
		nil, nil).Once()
	m.ZkSession.On("Children", consts.DomainManagerCES, true).Return(registeredPcList,
		nil, nil).Once()
	m.ZkSession.On("Create", BroadcastZkPath+pcUuid, mock.Anything, int32(0), zk.WorldACL(zk.PermAll), true).
		Return("", nil)

	err := startAllRootCertBroadcast()

	assert.Nil(t, err)
}
