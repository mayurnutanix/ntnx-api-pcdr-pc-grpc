package exchange_root_certificate

import (
	"encoding/json"
	"flag"
	"fmt"
	"net/http"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/interfaces/external"
	"ntnx-api-guru-pc/guru-pc-api-service/utils"

	log "github.com/sirupsen/logrus"
)

const (
	BroadcastZkPath = "/appliance/physical/domain-manager/root-cert-exchange/"
	V3CertApiPath   = "clusters/%s/certificates/root"
)

var (
	PE_CERT_ZK_COMMIT_FREQUENCY = flag.Int("PE_CERT_ZK_COMMIT_FREQUENCY", 10,
		"This signifies at what frequency will the PE broadcast progress be commited to ZK")
)

type V3Certificate struct {
	Content *string `json:"content"`
}

type V3CertApiPayload struct {
	Certificate      V3Certificate   `json:"certificate"`
	CertificateChain []V3Certificate `json:"certificate_chain"`
	Jwt              *string         `json:"jwt"`
	LocalGatewayRole string          `json:"local_gateway_role"`
}

type V3Message struct {
	Message string `json:"message"`
	Reason  string `json:"reason"`
}

type V3ErrorResponse struct {
	Code        int         `json:"code"`
	State       string      `json:"state"`
	MessageList []V3Message `json:"message_list"`
}

// This starts root cert broadcast for all the registered PCs.
// Remote PCs root certs are broadcasted to all PEs registered to the local PC.
// Also deletes the certificates of the PCs which have been un-registered.
func startAllRootCertBroadcast() error {
	// fetch registered PC list
	registeredPcList, err := utils.GetRegisteredPcList()
	if err != nil {
		return err
	}

	for _, registeredPc := range registeredPcList {
		err := startRootCertBroadcast(registeredPc)
		if err != nil {
			log.Errorf("error broadcasting %s PC cert to registered PEs: %v", registeredPc, err)
		}
	}
	// fetch broadcasted pc list
	broadcastedPcList, err := getBroadcastedPcList()
	if err != nil {
		return err
	}
	// Check for the case where we want to delete certs
	for _, broadcastedPc := range broadcastedPcList {
		found := false
		for _, registeredPc := range registeredPcList {
			if broadcastedPc == registeredPc {
				found = true
				break
			}
		}
		if !found {
			// this pc is no more registered. Delete cert from leftover PEs
			err := deleteCertBroadcast(broadcastedPc)
			if err != nil {
				log.Errorf("error deleting %s PC cert from registered PEs: %v", broadcastedPc, err)
			}
		}
	}
	return nil
}

func startRootCertBroadcast(remotePcClusterUuid string) error {
	log.Infof("Starting PE cert broadcast for PC %s", remotePcClusterUuid)
	registeredPeList, err := utils.GetRegisteredPeList()
	if err != nil {
		log.Errorf("error fetching registered pe list: %v", err)
		return err
	}
	err = broadcastCertToPE(remotePcClusterUuid, registeredPeList)
	if err != nil {
		log.Errorf("error broadcasting pc %s cert to registered pe clusters: %v", remotePcClusterUuid, err)
		return err
	}
	log.Infof("Completed PE cert broadcast for PC %s", remotePcClusterUuid)
	return nil
}

func broadcastCertToPE(remotePCClusterUuid string, registeredPEList []string) error {
	pcRootCert, err := utils.GetRootCert(remotePCClusterUuid)
	if err != nil {
		log.Errorf("error fetching remote pc certificate: %v", err)
		return err
	}
	statusMap, err := GetBroadcastStatusMap(remotePCClusterUuid)
	if err != nil {
		log.Errorf("error fetching broadcast status from zk for pc %s: %v", remotePCClusterUuid, err)
		return err
	}
	err = statusMap.CommitProgressToZk(true, false, Post)
	if err != nil {
		log.Errorf("error fetching broadcast status from zk for pc %s: %v", remotePCClusterUuid, err)
		return err
	}
	var count int
	for count = 1; count <= len(registeredPEList); count++ {
		peUuid := registeredPEList[count-1]
		if !statusMap.WasBroadcasted(peUuid) {
			log.Debugf("fannig out request to pe : %s", peUuid)
			err := fanoutCertToPe(&pcRootCert, remotePCClusterUuid, peUuid)
			if err != nil {
				log.Errorf("error fanning out PC %s root cert to PE %s: %v", remotePCClusterUuid, peUuid, err)
				statusMap.UpdatePeStatus(peUuid, false)
			} else {
				log.Infof("fanned out pc %s root cert to pe %s", remotePCClusterUuid, peUuid)
				statusMap.UpdatePeStatus(peUuid, true)
			}
		}
		if count%(*PE_CERT_ZK_COMMIT_FREQUENCY) == 0 {
			err = statusMap.CommitProgressToZk(false, false, Post)
			if err != nil {
				log.Errorf("error commiting broadcast progress to zk: %v", err)
				// not returning from here
			}
		}
	}
	err = statusMap.CommitProgressToZk(false, true, Post)
	if err != nil {
		log.Errorf("error commiting broadcast progress to zk: %v", err)
	}

	return nil
}

func fanoutCertToPe(pcRootCert *string, remotePcClusterUuid string, peClusterUuid string) error {
	apiUrl := fmt.Sprintf(V3CertApiPath, remotePcClusterUuid)
	payload, err := getV3Payload(pcRootCert)
	if err != nil {
		log.Errorf("error creating payload for v3 cert fanout call: %v", err)
		return err
	}
	response, code, err := external.Interfaces().RemoteRestClient().FanoutProxy(http.MethodPost,
		payload, http.MethodPost, peClusterUuid, apiUrl)

	if err != nil || *code != 200 {
		return fmt.Errorf("error calling v3 cert api using fanout: %d %v", *code, err)
	}

	// TODO : Check for a better way. Will probably require changes in v3 code.
	if len(response) == 0 || len(response) == 14 {
		log.Infof("Successfully fanned out remote pc %s root cert to pe %s", remotePcClusterUuid, peClusterUuid)
		return nil
	}

	v3ErrorResponse := &V3ErrorResponse{}
	err = json.Unmarshal(response, v3ErrorResponse)
	if err != nil {
		log.Errorf("error unmarshalling response: %v", err)
		return err
	}

	log.Errorf("error calling v3 cert api using fanout: %s", v3ErrorResponse.MessageList[0].Message)
	return fmt.Errorf("error fannig out cert to pe: %s", v3ErrorResponse.MessageList[0].Message)
}

func deleteCertBroadcast(remotePcUuid string) error {
	peList, err := utils.GetRegisteredPeList()
	if err != nil {
		log.Error("error fetching registered pe list")
		return err
	}
	statusMap, err := GetBroadcastStatusMap(remotePcUuid)
	if err != nil {
		log.Errorf("error fetching broadcast status from zk for pc %s: %v", remotePcUuid, err)
		return err
	}
	err = statusMap.CommitProgressToZk(true, false, Delete)
	if err != nil {
		log.Errorf("error fetching broadcast status from zk for pc %s: %v", remotePcUuid, err)
		return err
	}
	for _, pe := range peList {
		err = fanoutDeleteCertPe(remotePcUuid, pe)
		if err != nil {
			log.Errorf("error fanning out delete root cert request for pe %s :%v", pe, err)
		} else {
			statusMap.RemovePeStatus(pe)
		}
	}
	err = statusMap.CommitProgressToZk(false, true, Delete)
	if err != nil {
		log.Errorf("error fetching broadcast status from zk for pc %s: %v", remotePcUuid, err)
		return err
	}
	return nil
}

func fanoutDeleteCertPe(remotePcClusterUuid string, peClusterUuid string) error {
	apiUrl := fmt.Sprintf(V3CertApiPath, remotePcClusterUuid)
	_, code, err := external.Interfaces().RemoteRestClient().FanoutProxy(
		http.MethodDelete, nil, http.MethodDelete, peClusterUuid, apiUrl)
	if err != nil || *code != http.StatusOK {
		return fmt.Errorf("error calling v3 delete cert api using fanout: %d %v", *code, err)
	}
	return nil
}

func getV3Payload(remotePcRootCert *string) (*V3CertApiPayload, error) {
	payload := &V3CertApiPayload{}
	payload.LocalGatewayRole = "ACCEPTOR"

	payload.Certificate = V3Certificate{
		Content: remotePcRootCert,
	}

	jwt, err := utils.CreateV3Jwt(consts.EntityPrivateKey)
	if err != nil {
		log.Errorf("error creating jwt for payload: %v", err)
		return nil, err
	}

	payload.Jwt = &jwt

	_, certChain, err := utils.GetCAChain()
	if err != nil {
		log.Errorf("error fetching cert chain: %v", err)
		return nil, err
	}
	rootCertStruct := V3Certificate{
		Content: &certChain[2],
	}

	icaCertStruct := V3Certificate{
		Content: &certChain[1],
	}

	entityCertStruct := V3Certificate{
		Content: &certChain[0],
	}

	payload.CertificateChain = []V3Certificate{
		entityCertStruct, icaCertStruct, rootCertStruct,
	}

	return payload, nil
}

func getBroadcastedPcList() ([]string, error) {
	pairedPcList, _, err := external.Interfaces().ZkSession().Children(BroadcastZkPath, true)
	if err != nil {
		log.Errorf("error fetching broadcasted pc list from zk %s: %v", BroadcastZkPath, err)
		return nil, err
	}
	log.Debugf("fetched broadcasted pc list: %v", pairedPcList)
	return pairedPcList, nil
}
