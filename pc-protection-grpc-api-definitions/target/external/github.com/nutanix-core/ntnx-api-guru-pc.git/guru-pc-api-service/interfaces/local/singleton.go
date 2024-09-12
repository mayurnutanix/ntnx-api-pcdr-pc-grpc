/*
* Copyright (c) 2024 Nutanix Inc. All rights reserved.
*
* Author: kshitij.kumar@nutanix.com
*
* This file contains all interfaces required for guru
 */

package local

import (
	"ntnx-api-guru-pc/generated-code/target/dto/models/prism/v4/management"
	"ntnx-api-guru-pc/guru-pc-api-service/services/register"
	"ntnx-api-guru-pc/guru-pc-api-service/services/register/configure_connection"
	"ntnx-api-guru-pc/guru-pc-api-service/services/register/register_pc"
	"ntnx-api-guru-pc/guru-pc-api-service/services/register_aos/registration_common"
	"ntnx-api-guru-pc/guru-pc-api-service/services/register_aos/registration_precheck"
	"ntnx-api-guru-pc/guru-pc-api-service/services/register_aos/registration_proxy"
	"ntnx-api-guru-pc/guru-pc-api-service/services/trust_setup/cert_signing_trust_setup"
	"sync"

	log "github.com/sirupsen/logrus"
)

type InternalInterfaces interface {
	RegistrationPrecheckIfc() registration_precheck.RegistrationPrecheckIfc
	RegistrationProxyIfc() registration_proxy.RegistrationProxyIfc
	RegistrationCommonUtilIfc() registration_common.RegistrationCommonUtilIfc
	CertSigningTrustSetupIfc() cert_signing_trust_setup.CertSigningTrustSetupIfc

	RegisterSvc(management.ClusterType) register.Register
	UnregisterSvc(management.ClusterType) register.Unregister
	PCConnConfigSvc() configure_connection.ConnectionConfig
}

type singletonInternalServices struct {
	registrationPrecheck      registration_precheck.RegistrationPrecheckIfc
	registrationProxyIfc      registration_proxy.RegistrationProxyIfc
	registrationCommonUtilIfc registration_common.RegistrationCommonUtilIfc
	certSigningTrustSetupIfc  cert_signing_trust_setup.CertSigningTrustSetupIfc
	registerPcSvc             register.Register
	unregisterPcSvc           register.Unregister
	pcConfigureConnectionSvc  configure_connection.ConnectionConfig
}

var (
	// A singleton Instance through which to access all local singleton services.
	singletonInternalService     InternalInterfaces
	singletonInternalServiceOnce sync.Once
	registrationPrecheckOnce     sync.Once
	registrationProxyOnce        sync.Once
	registrationCommonOnce       sync.Once
	certSigningTrustSetupOnce    sync.Once
	registerPcSvcOnce            sync.Once
	unregisterPcSvcOnce          sync.Once
	rootCertManagerOnce          sync.Once
	pcConfigureConnectionOnce    sync.Once
)

func GetInternalInterfaces() InternalInterfaces {
	singletonInternalServiceOnce.Do(func() {
		if singletonInternalService == nil {
			singletonInternalService = &singletonInternalServices{}
		}
	})

	return singletonInternalService
}

func (s *singletonInternalServices) RegistrationPrecheckIfc() registration_precheck.RegistrationPrecheckIfc {
	registrationPrecheckOnce.Do(func() {
		if s.registrationPrecheck == nil {
			s.registrationPrecheck, _ = registration_precheck.New()
		}
	})

	return s.registrationPrecheck
}

func (s *singletonInternalServices) RegistrationProxyIfc() registration_proxy.RegistrationProxyIfc {
	registrationProxyOnce.Do(func() {
		if s.registrationProxyIfc == nil {
			s.registrationProxyIfc, _ = registration_proxy.New()
		}
	})

	return s.registrationProxyIfc
}

func (s *singletonInternalServices) RegistrationCommonUtilIfc() registration_common.RegistrationCommonUtilIfc {
	registrationCommonOnce.Do(func() {
		if s.registrationCommonUtilIfc == nil {
			s.registrationCommonUtilIfc, _ = registration_common.New()
		}
	})

	return s.registrationCommonUtilIfc
}
func (s *singletonInternalServices) CertSigningTrustSetupIfc() cert_signing_trust_setup.CertSigningTrustSetupIfc {
	certSigningTrustSetupOnce.Do(func() {
		if s.certSigningTrustSetupIfc == nil {
			s.certSigningTrustSetupIfc, _ = cert_signing_trust_setup.New()
		}
	})

	return s.certSigningTrustSetupIfc
}

func (s *singletonInternalServices) getRegisterPcSvc() register.Register {
	registerPcSvcOnce.Do(func() {
		if s.registerPcSvc == nil {
			s.registerPcSvc = register_pc.NewRegisterPcImpl()
		}
	})
	return s.registerPcSvc
}

func (s *singletonInternalServices) getUnregisterPcSvc() register.Unregister {
	unregisterPcSvcOnce.Do(func() {
		if s.unregisterPcSvc == nil {
			s.unregisterPcSvc = &register_pc.UnregisterPCImpl{
				ConfigureConnection: s.PCConnConfigSvc(),
				Precheck:            &register_pc.UnregisterPCPrecheck{},
			}
		}
	})
	return s.unregisterPcSvc
}

func (s *singletonInternalServices) RegisterSvc(remoteType management.ClusterType) register.Register {
	switch remoteType {
	// Add other cases for different remote entity types like
	case management.CLUSTERTYPE_DOMAIN_MANAGER:
		return s.getRegisterPcSvc()
	}
	log.Errorf("Unsupported remote entity type %v", remoteType)
	return nil
}

func (s *singletonInternalServices) UnregisterSvc(remoteType management.ClusterType) register.Unregister {
	switch remoteType {
	// Add other cases for different remote entity types like
	case management.CLUSTERTYPE_DOMAIN_MANAGER:
		return s.getUnregisterPcSvc()
	}
	log.Errorf("Unsupported remote entity type %v", remoteType)
	return nil
}

func (s *singletonInternalServices) PCConnConfigSvc() configure_connection.ConnectionConfig {
	pcConfigureConnectionOnce.Do(func() {
		if s.pcConfigureConnectionSvc == nil {
			s.pcConfigureConnectionSvc = configure_connection.New(management.CLUSTERTYPE_DOMAIN_MANAGER)
		}
	})
	return s.pcConfigureConnectionSvc
}

func SetSingletonServices(
	registrationPrecheck registration_precheck.RegistrationPrecheckIfc,
	registrationProxyIfc registration_proxy.RegistrationProxyIfc,
	registrationCommonUtilIfc registration_common.RegistrationCommonUtilIfc,
	certSigningTrustSetupIfc cert_signing_trust_setup.CertSigningTrustSetupIfc) {
	singletonInternalService = &singletonInternalServices{
		registrationPrecheck:      registrationPrecheck,
		registrationProxyIfc:      registrationProxyIfc,
		registrationCommonUtilIfc: registrationCommonUtilIfc,
		certSigningTrustSetupIfc:  certSigningTrustSetupIfc,
	}
}
