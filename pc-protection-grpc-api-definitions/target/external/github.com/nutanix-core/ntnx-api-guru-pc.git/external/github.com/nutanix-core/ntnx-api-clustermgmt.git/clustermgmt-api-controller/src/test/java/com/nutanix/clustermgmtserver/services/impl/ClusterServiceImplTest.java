package com.nutanix.clustermgmtserver.services.impl;

import com.nutanix.api.processor.message.ProcessorUtils;
import com.nutanix.api.utils.message.MessageBus;
import com.nutanix.clustermgmtserver.adapters.impl.BaseClusterResourceAdapterImpl;
import com.nutanix.clustermgmtserver.adapters.impl.ClustermgmtResourceAdapterImpl;
import com.nutanix.clustermgmtserver.odata.ClustermgmtOdataParser;
import com.nutanix.clustermgmtserver.proxy.CMSProxyImpl;
import com.nutanix.clustermgmtserver.proxy.EntityDbProxyImpl;
import com.nutanix.clustermgmtserver.proxy.ErgonProxyImpl;
import com.nutanix.clustermgmtserver.proxy.GenesisProxyImpl;
import com.nutanix.clustermgmtserver.utils.*;
import com.nutanix.odata.core.csdl.CsdlEntityBinding;
import com.nutanix.odata.core.edm.EdmProvider;
import com.nutanix.odata.core.service.ServiceMetadataProvider;
import com.nutanix.prism.adapter.service.ApplianceConfiguration;
import com.nutanix.prism.commands.multicluster.MulticlusterZeusConfigurationManagingZkImpl;
import dp1.clu.edm.clustermgmt.v4.config.*;
import dp1.clu.edm.clustermgmt.v4.stats.ClustermgmtStatsSelectClusterStats;
import dp1.clu.edm.clustermgmt.v4.stats.ClustermgmtStatsSelectHostStats;
import dp1.clu.edm.common.v1.config.ClustermgmtSelectIPAddress;
import dp1.clu.edm.common.v1.config.ClustermgmtSelectIPv4Address;
import dp1.clu.edm.common.v1.config.ClustermgmtSelectIPv6Address;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.testng.annotations.BeforeMethod;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.powermock.api.mockito.PowerMockito.mock;

/*
This is parent class for writing UTs for functions defined in ClusterServiceImpl.java
Please don't add any UTs here. Create a new file or add to existing ones as per entity segregation.
Add entity bindings here or mock service and add to cluster service in class constructor.
 */

public class ClusterServiceImplTest {
  static protected EntityDbProxyImpl entityDbProxy;
  static protected MulticlusterZeusConfigurationManagingZkImpl multiClusterZeusConfig;
  static protected BaseClusterResourceAdapterImpl clusterResourceAdapter;
  static protected ClustermgmtResourceAdapterImpl clustermgmtResourceAdapter;
  static protected ApplianceConfiguration applianceConfiguration;
  static protected ClustermgmtOdataParser clustermgmtOdataParser;
  static protected ErgonProxyImpl ergonProxy;
  static protected GenesisProxyImpl genesisProxy;
  static protected CMSProxyImpl cmsProxy;
  static protected ProcessorUtils processorUtils;
  static protected MessageBus messageBus;
  static protected ClusterServiceImpl clusterService;
  static protected StatsGatewayServiceImpl statsGatewayService;

  @Mock
  private ApplicationContext context;

  @BeforeMethod
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    entityDbProxy = mock(EntityDbProxyImpl.class);
    multiClusterZeusConfig = mock(MulticlusterZeusConfigurationManagingZkImpl.class);
    applianceConfiguration = mock(ApplianceConfiguration.class);
    clusterResourceAdapter = TestConstantUtils.clusterResourceAdapterV40A1;
    clusterResourceAdapter.setAdapterRegistry(TestConstantUtils.getAdapterRegistry(context));
    clustermgmtResourceAdapter = new ClustermgmtResourceAdapterImpl();
    List<CsdlEntityBinding> entityBindings = new ArrayList<>();
    entityBindings.add(new ClustermgmtConfigFilterCluster());
    entityBindings.add(new ClustermgmtConfigOrderByCluster());
    entityBindings.add(new ClustermgmtConfigFilterHost());
    entityBindings.add(new ClustermgmtConfigOrderByHost());
    entityBindings.add(new ClustermgmtConfigFilterHostGpu());
    entityBindings.add(new ClustermgmtConfigOrderByHostGpu());
    entityBindings.add(new ClustermgmtConfigFilterHostNic());
    entityBindings.add(new ClustermgmtConfigOrderByHostNic());
    entityBindings.add(new ClustermgmtConfigFilterVirtualNic());
    entityBindings.add(new ClustermgmtConfigOrderByVirtualNic());
    entityBindings.add(new ClustermgmtConfigFilterBuildReference());
    entityBindings.add(new ClustermgmtConfigFilterUpgradeStatus());
    entityBindings.add(new ClustermgmtConfigFilterClusterConfigReference());
    entityBindings.add(new ClustermgmtConfigFilterClusterReference());
    entityBindings.add(new ClustermgmtConfigFilterClusterNetworkReference());
    entityBindings.add(new ClustermgmtConfigFilterEncryptionOptionInfo());
    entityBindings.add(new ClustermgmtConfigFilterEncryptionScopeInfo());
    entityBindings.add(new ClustermgmtConfigFilterEncryptionStatus());
    entityBindings.add(new ClustermgmtConfigFilterHypervisorReference());
    entityBindings.add(new ClustermgmtConfigFilterHypervisorType());
    entityBindings.add(new ClustermgmtConfigFilterKeyManagementServerType());
    entityBindings.add(new ClustermgmtConfigOrderByBuildReference());
    entityBindings.add(new ClustermgmtConfigOrderByClusterConfigReference());
    entityBindings.add(new ClustermgmtConfigOrderByClusterReference());
    entityBindings.add(new ClustermgmtConfigOrderByClusterNetworkReference());
    entityBindings.add(new ClustermgmtConfigOrderByHostTypeEnum());
    entityBindings.add(new ClustermgmtConfigOrderByHypervisorReference());
    entityBindings.add(new ClustermgmtConfigOrderByHypervisorType());
    entityBindings.add(new ClustermgmtConfigOrderByKeyManagementServerType());
    entityBindings.add(new ClustermgmtConfigOrderByNodeReference());
    entityBindings.add(new ClustermgmtConfigOrderByUpgradeStatus());
    entityBindings.add(new ClustermgmtConfigApplyBuildReference());
    entityBindings.add(new ClustermgmtConfigApplyClusterConfigReference());
    entityBindings.add(new ClustermgmtConfigApplyCluster());
    entityBindings.add(new ClustermgmtConfigApplyClusterReference());
    entityBindings.add(new ClustermgmtConfigApplyHost());
    entityBindings.add(new ClustermgmtConfigApplyHypervisorReference());
    entityBindings.add(new ClustermgmtConfigApplyHypervisorType());
    entityBindings.add(new ClustermgmtConfigApplyNodeReference());
    entityBindings.add(new ClustermgmtConfigSelectHostNic());
    entityBindings.add(new ClustermgmtConfigSelectVirtualNic());
    entityBindings.add(new ClustermgmtConfigSelectHost());
    entityBindings.add(new ClustermgmtConfigSelectCluster());
    entityBindings.add(new ClustermgmtConfigSelectHostGpu());
    entityBindings.add(new ClustermgmtConfigSelectAcropolisConnectionState());
    entityBindings.add(new ClustermgmtConfigSelectBuildReference());
    entityBindings.add(new ClustermgmtConfigSelectClusterArchReference());
    entityBindings.add(new ClustermgmtConfigSelectClusterConfigReference());
    entityBindings.add(new ClustermgmtConfigSelectClusterFunctionRef());
    entityBindings.add(new ClustermgmtConfigSelectClusterNetworkReference());
    entityBindings.add(new ClustermgmtConfigApplyClusterReference());
    entityBindings.add(new ClustermgmtConfigApplyHypervisorReference());
    entityBindings.add(new ClustermgmtConfigSelectEncryptionOptionInfo());
    entityBindings.add(new ClustermgmtConfigSelectEncryptionScopeInfo());
    entityBindings.add(new ClustermgmtConfigSelectEncryptionStatus());
    entityBindings.add(new ClustermgmtConfigSelectHostTypeEnum());
    entityBindings.add(new ClustermgmtConfigSelectHypervisorState());
    entityBindings.add(new ClustermgmtConfigSelectHypervisorType());
    entityBindings.add(new ClustermgmtConfigSelectHypervisorReference());
    entityBindings.add(new ClustermgmtConfigSelectKeyManagementServerType());
    entityBindings.add(new ClustermgmtConfigSelectNodeReference());
    entityBindings.add(new ClustermgmtConfigSelectNodeStatus());
    entityBindings.add(new ClustermgmtConfigSelectOperationMode());
    entityBindings.add(new ClustermgmtConfigSelectUpgradeStatus());
    entityBindings.add(new ClustermgmtConfigSelectClusterReference());
    entityBindings.add(new ClustermgmtConfigSelectClusterProfile());
    entityBindings.add(new ClustermgmtSelectIPAddress());
    entityBindings.add(new ClustermgmtSelectIPv4Address());
    entityBindings.add(new ClustermgmtSelectIPv6Address());
    entityBindings.add(new ClustermgmtConfigExpandClusterReference());
    entityBindings.add(new ClustermgmtConfigExpandGpuMode());
    entityBindings.add(new ClustermgmtConfigExpandGpuType());
    entityBindings.add(new ClustermgmtConfigExpandVirtualGpuConfig());
    entityBindings.add(new ClustermgmtConfigExpandPhysicalGpuConfig());
    entityBindings.add(new ClustermgmtConfigExpandAcropolisConnectionState());
    entityBindings.add(new ClustermgmtConfigExpandBuildReference());
    entityBindings.add(new ClustermgmtConfigExpandClusterArchReference());
    entityBindings.add(new ClustermgmtConfigExpandClusterConfigReference());
    entityBindings.add(new ClustermgmtConfigExpandClusterNetworkReference());
    entityBindings.add(new ClustermgmtConfigExpandEncryptionStatus());
    entityBindings.add(new ClustermgmtConfigExpandOperationMode());
    entityBindings.add(new ClustermgmtConfigExpandNodeReference());
    entityBindings.add(new ClustermgmtConfigExpandKeyManagementServerType());
    entityBindings.add(new ClustermgmtConfigExpandHypervisorReference());
    entityBindings.add(new ClustermgmtConfigExpandHypervisorState());
    entityBindings.add(new ClustermgmtConfigExpandHypervisorType());
    entityBindings.add(new ClustermgmtConfigExpandClusterProfile());
    entityBindings.add(new ClustermgmtConfigExpandStorageSummary());
    entityBindings.add(new ClustermgmtConfigExpandCluster());
    entityBindings.add(new ClustermgmtStatsSelectClusterStats());
    entityBindings.add(new ClustermgmtStatsSelectHostStats());
    entityBindings.add(new ClustermgmtConfigSelectManagedCluster());
    entityBindings.add(new ClustermgmtConfigExpandManagedCluster());
    EdmProvider customEdmProvider = new EdmProvider(entityBindings);
    statsGatewayService = mock(StatsGatewayServiceImpl.class);
    ServiceMetadataProvider serviceMetadataProvider = new ServiceMetadataProvider(customEdmProvider);
    clustermgmtOdataParser = new ClustermgmtOdataParser(serviceMetadataProvider, statsGatewayService);
    ergonProxy = mock(ErgonProxyImpl.class);
    messageBus = mock(MessageBus.class);
    processorUtils = new ProcessorUtils(messageBus);
    genesisProxy = mock(GenesisProxyImpl.class);
    cmsProxy = mock(CMSProxyImpl.class);
    // mock request context for odata parser
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/api/clustermgmt/v4/config");
    request.addHeader(ClustermgmtUtils.FULL_VERSION_HEADER_NAME,
      ClustermgmtUtils.LATEST_FULL_VERSION_HEADER_VALUE);
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    this.clusterService = new ClusterServiceImpl(entityDbProxy,
      multiClusterZeusConfig, clustermgmtResourceAdapter, clusterResourceAdapter, applianceConfiguration,
      clustermgmtOdataParser, genesisProxy, ergonProxy, cmsProxy, processorUtils);
  }
}

