/*
 * Copyright (c) 2023 Nutanix Inc. All rights reserved.
 * Author: harsh.seta@nutanix.com
 */

package models

type ClusterExternalDetailsDTO struct {
	ClusterName                     string   `json:"clusterName"`
	IpAddresses                     []string `json:"ipAddresses"`
	ClusterAddresses                []string `json:"clusterAddresses"`
	ClusterFullyQualifiedDomainName string   `json:"clusterFullyQualifiedDomainName"`
	Multicluster                    bool     `json:"multicluster"`
	Username                        string   `json:"username"`
	Password                        string   `json:"password"`
	Jsessionid                      string   `json:"jsessionid"`
	PrcCluster                      string   `json:"prcCluster"`
	Reachable                       bool     `json:"reachable"`
	Port                            int      `json:"port"`
	TrustSetupOnly                  bool     `json:"trustSetupOnly"`
	UseTrust                        bool     `json:"useTrust"`
	ClusterUuid                     string   `json:"clusterUuid"`
}
