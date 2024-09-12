/*
 * Copyright (c) 2023 Nutanix Inc. All rights reserved.
 * Author: harsh.seta@nutanix.com
 */

package models

type ClusterTrustSetupDetailsDTO struct {
	ClusterCertificateSigningRequest    string   `json:"clusterCertificateSigningRequest"`
	SignedCertificate                   string   `json:"signedCertificate"`
	IntermediateCertificate             string   `json:"intermediateCertificate"`
	RootCertificate                     string   `json:"rootCertificate"`
	CertificateOwnerClusterUUID         string   `json:"certificateOwnerClusterUUID"`
	CertificateOwnerClusterExternalIp   string   `json:"certificateOwnerClusterExternalIp"`
	CertificateOwnerClusterExternalFqdn string   `json:"certificateOwnerClusterExternalFqdn"`
	CertificateOwnerSvmIpList           []string `json:"certificateOwnerSvmIpList"`
}
