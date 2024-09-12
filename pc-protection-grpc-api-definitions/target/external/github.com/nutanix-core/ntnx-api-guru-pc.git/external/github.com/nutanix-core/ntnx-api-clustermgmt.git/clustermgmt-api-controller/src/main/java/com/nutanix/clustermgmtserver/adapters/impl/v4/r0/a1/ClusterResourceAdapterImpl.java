/*
 * Copyright (c) 2021 Nutanix Inc. All rights reserved.
 *
 * Author: rakesh.falak@nutanix.com
 */
package com.nutanix.clustermgmtserver.adapters.impl.v4.r0.a1;

import com.google.common.collect.ImmutableMap;
import com.nutanix.api.utils.type.DateUtils;
import com.nutanix.infrastructure.cluster.genesis.GenesisInterfaceProto;
import dp1.clu.common.v1.config.IPAddress;
import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import com.nutanix.clustermgmtserver.adapters.api.ClusterResourceAdapter;
import com.nutanix.clustermgmtserver.adapters.impl.BaseClusterResourceAdapterImpl;
import com.nutanix.clustermgmtserver.utils.ClustermgmtUtils;
import com.nutanix.insights.ifc.InsightsInterfaceProto;
import com.nutanix.prism.adapter.service.ZeusConfiguration;
import com.nutanix.zeus.protobuf.Configuration.ConfigurationProto;
import com.nutanix.zeus.protobuf.Configuration.ConfigurationProto.*;
import com.nutanix.zeus.protobuf.DomainFaultToleranceStateProto.DomainFaultToleranceState;
import com.nutanix.zeus.protobuf.DomainFaultToleranceStateProto.DomainFaultToleranceState.Domain.DomainType;
import com.nutanix.zeus.protobuf.MessageEntityProto;
import dp1.clu.clustermgmt.v4.config.FaultToleranceState;
import dp1.clu.clustermgmt.v4.config.RackableUnit;
import dp1.clu.clustermgmt.v4.config.*;
import dp1.clu.common.v1.config.IPAddressOrFQDN;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component(ClusterResourceAdapterImpl.ADAPTER_VERSION)
public class ClusterResourceAdapterImpl extends BaseClusterResourceAdapterImpl {
  @Autowired
  public ClusterResourceAdapterImpl(
    @Qualifier(
      com.nutanix.clustermgmtserver.adapters.impl.v4.r0.a2.ClusterResourceAdapterImpl.ADAPTER_VERSION
    ) final ClusterResourceAdapter nextChainAdapter) {
    this.nextChainAdapter = nextChainAdapter;
  }

  public static final String ADAPTER_VERSION = "v4.0.a1-clustermgmt-adapter";

  @Override
  public String getVersionOfAdapter() {
    return ADAPTER_VERSION;
  }

  @Override
  public Cluster adaptIdfClusterMetricstoClusterEntity(final List<InsightsInterfaceProto.MetricData> metricDataList,
                                                             Cluster clusterEntity) {
    boolean setConfig = false;
    boolean setNetwork = false;
    ClusterConfigReference clusterConfigReference = new ClusterConfigReference();
    ClusterNetworkReference clusterNetworkReference = new ClusterNetworkReference();

    List<SoftwareMapReference> softwareMapReferences = new ArrayList<>();

    boolean setSmtp = false;
    SmtpServerRef smtpServerRef = new SmtpServerRef();
    SmtpNetwork smtpNetwork = new SmtpNetwork();

    for (final InsightsInterfaceProto.MetricData metricData : metricDataList) {
      if (!metricData.hasName() || metricData.getValueListCount() <= 0) {
        continue;
      }
      switch (metricData.getName()) {
        case "cluster_name":
          clusterEntity.setName(metricData.getValueList(0).getValue().getStrValue());
          break;
        case "external_data_services_ip":
          setNetwork = true;
          clusterNetworkReference.setExternalDataServiceIp(
            ClustermgmtUtils.createIpv4Ipv6Address(metricData.getValueList(0).getValue().getStrValue()));
          break;
        case "cluster_arch":
          setConfig = true;
          if (metricData.getValueList(0).getValue().getStrValue().equals("X86_64"))
            clusterConfigReference.setClusterArch(ClusterArchReference.X86_64);
          else
            clusterConfigReference.setClusterArch(ClusterArchReference.PPC64LE);
          break;
        case "service_list":
          setConfig = true;
          List<ClusterFunctionRef> clusterFunctions = new ArrayList<>();
          for (String function : metricData.getValueList(0).getValue().getStrList().getValueListList()) {
            clusterFunctions.add(ClustermgmtUtils.clusterFunctionRefHashMap.get(function));
          }
          clusterConfigReference.setClusterFunction(clusterFunctions);
          break;
        case "timezone":
          setConfig = true;
          clusterConfigReference.setTimezone(metricData.getValueList(0).getValue().getStrValue());
          break;
        case "hypervisor_types":
          setConfig = true;
          List<HypervisorType> hypervisorTypes = new ArrayList<>();
          for (String type : metricData.getValueList(0).getValue().getStrList().getValueListList()) {
            hypervisorTypes.add(ClustermgmtUtils.hypervisorTypeMap.get(type));
          }
          clusterConfigReference.setHypervisorTypes(hypervisorTypes);
          break;
        case "redundancy_factor":
          setConfig = true;
          clusterConfigReference.setRedundancyFactor(metricData.getValueList(0).getValue().getInt64Value());
          break;
        case "ncc_version":
          SoftwareMapReference softwareMapReference = new SoftwareMapReference();
          softwareMapReference.setSoftwareType(SoftwareTypeRef.NCC);
          softwareMapReference.setVersion(metricData.getValueList(0).getValue().getStrValue());
          softwareMapReferences.add(softwareMapReference);
          break;
        case "cluster_upgrade_status":
          clusterEntity.setUpgradeStatus(ClustermgmtUtils.upgradeStatusMap.get(metricData.getValueList(0).getValue().getStrValue()));
          break;
        case "num_vms":
          clusterEntity.setVmCount(metricData.getValueList(0).getValue().getInt64Value());
          break;
        case "capacity.inefficient_vm_num":
          clusterEntity.setInefficientVmCount(metricData.getValueList(0).getValue().getInt64Value());
          break;
        case "encryption_option":
          setConfig = true;
          List<EncryptionOptionInfo> encryptionOptionList = new ArrayList<>();
          for (String option : metricData.getValueList(0).getValue().getStrList().getValueListList()) {
            encryptionOptionList.add(ClustermgmtUtils.encryptionOptionMap.get(option));
          }
          clusterConfigReference.setEncryptionOption(encryptionOptionList);
          break;
        case "encryption_scope":
          setConfig = true;
          List<EncryptionScopeInfo> encryptionScopeList = new ArrayList<>();
          for (String scope : metricData.getValueList(0).getValue().getStrList().getValueListList()) {
            encryptionScopeList.add(ClustermgmtUtils.encryptionScopeMap.get(scope));
          }
          clusterConfigReference.setEncryptionScope(encryptionScopeList);
          break;
        case "key_management_server":
          setNetwork = true;
          clusterNetworkReference.setKeyManagementServerType(ClustermgmtUtils.keyManagementServerTypeMap.get(
            metricData.getValueList(0).getValue().getStrValue()));
          break;
        case "encryption_in_transit":
          setConfig = true;
          clusterConfigReference.setEncryptionInTransitStatus(ClustermgmtUtils.encryptionStatusMap.get(
            metricData.getValueList(0).getValue().getStrValue()));
          break;
        case "name_server_ip_list":
          setNetwork = true;
          List<IPAddressOrFQDN> nameServers = new ArrayList<>();
          for (String nameServer : metricData.getValueList(0).getValue().getStrList().getValueListList()) {
            nameServers.add(ClustermgmtUtils.createIpv4Ipv6OrFqdnAddress(nameServer));
          }
          clusterNetworkReference.setNameServerIpList(nameServers);
          break;
        case "ntp_server_ip_list":
          setNetwork = true;
          List<IPAddressOrFQDN> ntpServers = new ArrayList<>();
          for (String ntpServer : metricData.getValueList(0).getValue().getStrList().getValueListList()) {
            ntpServers.add(ClustermgmtUtils.createIpv4Ipv6OrFqdnAddress(ntpServer));
          }
          clusterNetworkReference.setNtpServerIpList(ntpServers);
          break;
        case "smtp_server.server.ip_address":
          setSmtp = true;
          setNetwork = true;
          smtpNetwork.setIpAddress(
            ClustermgmtUtils.createIpv4Ipv6OrFqdnAddress(metricData.getValueList(0).getValue().getStrValue()));
          break;
        case "smtp_server.server.port":
          setSmtp = true;
          setNetwork = true;
          smtpNetwork.setPort(Math.toIntExact(metricData.getValueList(0).getValue().getInt64Value()));
          break;
        case "smtp_server.server.user_name":
          setSmtp = true;
          setNetwork = true;
          smtpNetwork.setUsername(metricData.getValueList(0).getValue().getStrValue());
          break;
        case "smtp_server.email_address":
          setSmtp = true;
          setNetwork = true;
          smtpServerRef.setEmailAddress(metricData.getValueList(0).getValue().getStrValue());
          break;
        case "smtp_server.type":
          setSmtp = true;
          setNetwork = true;
          smtpServerRef.setType(
            ClustermgmtUtils.smtpStrTypeMap.get(metricData.getValueList(0).getValue().getStrValue()));
          break;
        default:
          break;
      }
    }
    if(setSmtp) {
      smtpServerRef.setServer(smtpNetwork);
      clusterNetworkReference.setSmtpServer(smtpServerRef);
    }
    if(setConfig)
      clusterEntity.setConfig(clusterConfigReference);
    if(setNetwork)
      clusterEntity.setNetwork(clusterNetworkReference);

    if (!softwareMapReferences.isEmpty())
      clusterEntity.getConfig().setClusterSoftwareMap(softwareMapReferences);
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptIdfClusterMetricstoClusterEntity(metricDataList, clusterEntity);
    }
    return clusterEntity;
  }

  @Override
  public Cluster adaptZeusEntriestoClusterEntity(ZeusConfiguration zkConfig, Cluster clusterEntity) {
    clusterEntity = init(clusterEntity);

    final ConfigurationProto configuration = zkConfig.getConfiguration();
    final Collection<Node> nodes = zkConfig.getCumulativeNodes();
    List<NodeListItemReference> nodeListItemReferences = new ArrayList<>();
    for (Node node : nodes) {
      NodeListItemReference nodeListItemReference = new NodeListItemReference();
      // UUID
      nodeListItemReference.setNodeUuid(node.getUuid());

      // CVM IP
      if (node.getSvmExternalIpListCount() > 0) {
        nodeListItemReference.setControllerVmIp(
          ClustermgmtUtils.createIpv4Ipv6Address(node.getSvmExternalIpList(0)));
      }
      else if (node.hasServiceVmExternalIp()) {
        nodeListItemReference.setControllerVmIp(
          ClustermgmtUtils.createIpv4Ipv6Address(node.getServiceVmExternalIp()));
      }

      // HOST IP
      if (node.getHostExternalIpListCount() > 0) {
        nodeListItemReference.setHostIp(
          ClustermgmtUtils.createIpv4Ipv6Address(node.getHostExternalIpList(0)));
      }
      else if (node.hasHypervisor()
        && (node.getHypervisor().getAddressListCount() > 0)) {
        final ConfigurationProto.NetworkEntity hypervisor =
          node.getHypervisor();
        nodeListItemReference.setHostIp(
          ClustermgmtUtils.createIpv4Ipv6Address(hypervisor.getAddressList(0)));
      }
      else if(node.hasHypervisorKey()) {
        nodeListItemReference.setHostIp(
          ClustermgmtUtils.createIpv4Ipv6Address(node.getHypervisorKey()));
      }
      nodeListItemReferences.add(nodeListItemReference);
    }

    // Node Reference
    NodeReference nodeReference = new NodeReference();
    nodeReference.setNumberOfNodes(nodes.size());
    clusterEntity.setNodes(nodeReference);
    clusterEntity.getNodes().setNodeList(nodeListItemReferences);

    // Build Reference
    if (configuration.getReleaseVersion() != null) {
      BuildReference buildReference = new BuildReference();
      final Matcher matcher =
        ClustermgmtUtils.NUTANIX_SW_VERSION.matcher(configuration.getReleaseVersion());
      if (matcher.matches()) {
        String version = matcher.group(ClustermgmtUtils.GROUP_IDX_VERSION);
        if (!version.equals("master")) {
          // Here version is of format "fraser-6.7-stable"
          Pattern pattern = Pattern.compile("\\d+\\.\\d+");
          Matcher versionMatcher = pattern.matcher(version);
          if (versionMatcher.find()) {
            version = versionMatcher.group();
          }
        }
        buildReference.setVersion(version);
        buildReference.setBuildType(matcher.group(ClustermgmtUtils.GROUP_IDX_BUILD_TYPE));
        buildReference.setShortCommitId(matcher.group(
          ClustermgmtUtils.GROUP_IDX_COMMIT_ID).substring(
          0, ClustermgmtUtils.SHORT_COMMIT_ID_END_IDX));
        buildReference.setCommitId(matcher.group(ClustermgmtUtils.GROUP_IDX_COMMIT_ID));
      }
      buildReference.setFullVersion(configuration.getReleaseVersion());
      clusterEntity.getConfig().setBuildInfo(buildReference);
    }

    // Public Key
    List<PublicKey> sshKeys = new ArrayList<>();
    for (final SSHKey sshKey : zkConfig.getAllSSHKeys()) {
      PublicKey publicKey = new PublicKey();
      publicKey.setName(sshKey.getKeyId());
      publicKey.setKey(sshKey.getPubKey());
      sshKeys.add(publicKey);
    }
    clusterEntity.getConfig().setAuthorizedPublicKeyList(sshKeys);

    // Fault Tolerance State
    if(configuration.hasClusterFaultToleranceState()) {
      FaultToleranceState faultToleranceState = new FaultToleranceState();
      if (zkConfig.getFaultToleranceState().hasCurrentMaxFaultTolerance())
        faultToleranceState.setCurrentMaxFaultTolerance(
          zkConfig.getFaultToleranceState().getCurrentMaxFaultTolerance());
      if (zkConfig.getFaultToleranceState().hasDesiredFaultToleranceLevel())
        faultToleranceState.setDesiredMaxFaultTolerance(
          zkConfig.getFaultToleranceState().getDesiredMaxFaultTolerance());
      // Domain Awareness
      final DomainType desiredDomain =
        zkConfig.getFaultToleranceState().hasDesiredFaultToleranceLevel() ?
          zkConfig.getFaultToleranceState().getDesiredFaultToleranceLevel() :
          DomainType.kNode;
      faultToleranceState.setDomainAwarenessLevel(
        ClustermgmtUtils.domainAwarenessMap.get(desiredDomain));

      clusterEntity.getConfig().setFaultToleranceState(faultToleranceState);
    }

    // Operation Mode
    if(configuration.hasClusterOperationMode())
    clusterEntity.getConfig().setOperationMode(
      ClustermgmtUtils.operationModeMap.get(configuration.getClusterOperationMode()));

    // isLTS
    if(configuration.hasIsLts())
      clusterEntity.getConfig().setIsLts(configuration.getIsLts());

    // passwordRemoteLoginEnabled
    if(configuration.hasPasswordRemoteLoginEnabled())
      clusterEntity.getConfig().setIsPasswordRemoteLoginEnabled(configuration.getPasswordRemoteLoginEnabled());

    // External Subnet
    if (configuration.hasExternalSubnet())
      clusterEntity.getNetwork().setExternalSubnet(configuration.getExternalSubnet());

    // Internal Subnet
    if (configuration.hasInternalSubnet())
      clusterEntity.getNetwork().setInternalSubnet(configuration.getInternalSubnet());

    // Nfs subnet whitelist
    if (configuration.hasNfsSubnetWhitelist()) {
      List<String> nfsWhitelist =
        new ArrayList<>(Arrays.asList(configuration.getNfsSubnetWhitelist().split(",")));
      clusterEntity.getNetwork().setNfsSubnetWhitelist(nfsWhitelist);
    }

    // ntp server list
    if(clusterEntity.getNetwork().getNtpServerIpList() == null && configuration.getNtpServerListCount() > 0) {
      List<IPAddressOrFQDN> ntpServers = new ArrayList<>();
      for (String ntpServer : configuration.getNtpServerListList()) {
        ntpServers.add(ClustermgmtUtils.createIpv4Ipv6OrFqdnAddress(ntpServer));
      }
      if(!ntpServers.isEmpty())
        clusterEntity.getNetwork().setNtpServerIpList(ntpServers);
    }

    // name server list
    if(clusterEntity.getNetwork().getNameServerIpList() == null && configuration.getNameServerIpListCount() > 0) {
      List<IPAddressOrFQDN> nameServers = new ArrayList<>();
      for (String nameServer : configuration.getNameServerIpListList()) {
        nameServers.add(ClustermgmtUtils.createIpv4Ipv6OrFqdnAddress(nameServer));
      }
      if(!nameServers.isEmpty())
        clusterEntity.getNetwork().setNameServerIpList(nameServers);
    }

    // smtp server
    final Aegis aegis = zkConfig.getAegis().get();
    if (clusterEntity.getNetwork().getSmtpServer() == null && aegis.hasSmtpServer()) {
      SmtpServerRef smtpServerRef = new SmtpServerRef();
      SmtpNetwork smtpNetwork = new SmtpNetwork();
      smtpNetwork.setIpAddress(ClustermgmtUtils.createIpv4Ipv6OrFqdnAddress(
        aegis.getSmtpServer().getAddressList(0)));
      smtpNetwork.setPort(aegis.getSmtpServer().getPort());
      if (aegis.getSmtpServer().hasUsername())
        smtpNetwork.setUsername(aegis.getSmtpServer().getUsername());

      smtpServerRef.setEmailAddress(aegis.getFromAddress());
      smtpServerRef.setServer(smtpNetwork);
      smtpServerRef.setType(ClustermgmtUtils.smtpServerTypeMap.get(aegis.getSmtpServerType()));
      clusterEntity.getNetwork().setSmtpServer(smtpServerRef);
    }

    // remote support
    if (aegis.hasRemoteSupport()) {
      clusterEntity.getConfig().setIsRemoteSupportEnabled(aegis.getRemoteSupport().getValue());
    }

    // Masqueradin Ip
    if(configuration.hasClusterMasqueradingIp())
      clusterEntity.getNetwork().setMasqueradingIp(ClustermgmtUtils.createIpv4Ipv6Address(configuration.getClusterMasqueradingIp()));

    // Masquerading Port
    if(configuration.hasClusterMasqueradingPort())
      clusterEntity.getNetwork().setMasqueradingPort(configuration.getClusterMasqueradingPort());

    // FQDN
    if(configuration.hasClusterFullyQualifiedDomainName())
      clusterEntity.getNetwork().setFqdn(configuration.getClusterFullyQualifiedDomainName());

    // Software Map
    if (configuration.getReleaseVersion() != null) {
      SoftwareMapReference softwareMapReference = new SoftwareMapReference();
      if (isPC(clusterEntity))
        softwareMapReference.setSoftwareType(SoftwareTypeRef.PRISM_CENTRAL);
      else
        softwareMapReference.setSoftwareType(SoftwareTypeRef.NOS);
      softwareMapReference.setVersion(configuration.getReleaseVersion());

      if(clusterEntity.getConfig().getClusterSoftwareMap() != null)
        clusterEntity.getConfig().getClusterSoftwareMap().add(softwareMapReference);
      else {
        List<SoftwareMapReference> softwareMapReferenceList = new ArrayList<>();
        softwareMapReferenceList.add(softwareMapReference);
        clusterEntity.getConfig().setClusterSoftwareMap(softwareMapReferenceList);
      }
    }

    // External address ipv4 and ipv6
    IPAddress ipAddress = new IPAddress();
    if (configuration.hasClusterExternalIp())
      ipAddress.setIpv4(ClustermgmtUtils.createIpv4Address(configuration.getClusterExternalIp()));
    if (configuration.hasClusterExternalIpv6())
      ipAddress.setIpv6(ClustermgmtUtils.createIpv6Address(configuration.getClusterExternalIpv6()));
    clusterEntity.getNetwork().setExternalAddress(ipAddress);

    // Incarnation id
    if (configuration.hasClusterIncarnationId())
      clusterEntity.getConfig().setIncarnationId(configuration.getClusterIncarnationId());

    // backplane network params
    BackplaneNetworkParams backplaneParams = new BackplaneNetworkParams();
    if(configuration.hasBackplaneNetworkConfig()) {
      ConfigurationProto.NetworkConfig backplaneNetworkConfig = configuration.getBackplaneNetworkConfig();
      if(backplaneNetworkConfig.hasVlanId()) {
        backplaneParams.setVlanTag(Long.valueOf(backplaneNetworkConfig.getVlanId()));
      }
      if(backplaneNetworkConfig.hasSubnet()) {
        //the subnet format string: subnet/netmask
        final String subnet = backplaneNetworkConfig.getSubnet();
        final List<String> backplaneIps = Arrays.asList(subnet.split("/"));
        backplaneParams.setSubnet(ClustermgmtUtils.createIpv4Address(backplaneIps.get(0)));
        backplaneParams.setNetmask(ClustermgmtUtils.createIpv4Address(backplaneIps.get(1)));
      }
      backplaneParams.setIsSegmentationEnabled(true);

    }
    else{
      backplaneParams.setIsSegmentationEnabled(false);
    }
    clusterEntity.getNetwork().setBackplane(backplaneParams);

    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptZeusEntriestoClusterEntity(zkConfig, clusterEntity);
    }

    return clusterEntity;
  }

  public Cluster init(Cluster clusterEntity) {
    if (clusterEntity.getConfig() == null)
      clusterEntity.setConfig(new ClusterConfigReference());
    if (clusterEntity.getNetwork() == null)
      clusterEntity.setNetwork(new ClusterNetworkReference());
    return clusterEntity;
  }

  public boolean isPC(Cluster clusterEntity) {
    if(clusterEntity.getConfig().getClusterFunction().contains(ClusterFunctionRef.PRISM_CENTRAL))
      return true;
    return false;
  }

  @Override
  public List<SnmpUser> getSnmpUsers(ZeusConfiguration zkConfig) {
    List<SnmpUser> snmpUsers = new ArrayList<>();
    SnmpInfo snmpInfo = zkConfig.getSnmpInfo().orNull();
    if (snmpInfo == null) {
      return snmpUsers;
    }
    for(final SnmpInfo.User user : snmpInfo.getUserListList()) {
      SnmpUser snmpUser = new SnmpUser();
      snmpUser.setUsername(user.getUsername());
      snmpUser.setExtId(user.getUuid());
      if (user.hasAuthKey())
        snmpUser.setAuthKey(user.getAuthKey());
      if (user.hasAuthType())
        snmpUser.setAuthType(ClustermgmtUtils.snmpAuthTypeMap.get(user.getAuthType()));
      if (user.hasPrivKey())
        snmpUser.setPrivKey(user.getPrivKey());
      if (user.hasPrivType())
        snmpUser.setPrivType(ClustermgmtUtils.snmpPrivTypeMap.get(user.getPrivType()));
      snmpUsers.add(snmpUser);
    }
    return snmpUsers;
  }

  @Override
  public List<SnmpTrap> getSnmpTraps(ZeusConfiguration zkConfig) {
    List<SnmpTrap> snmpTraps = new ArrayList<>();
    SnmpInfo snmpInfo = zkConfig.getSnmpInfo().orNull();
    if (snmpInfo == null) {
      return snmpTraps;
    }
    for(final SnmpInfo.TrapSink trapSink: snmpInfo.getTrapSinkListList()) {
      SnmpTrap snmpTrap = new SnmpTrap();
      snmpTrap.setVersion(ClustermgmtUtils.snmpTrapVersionMap.get(trapSink.getVersion()));
      snmpTrap.setExtId(trapSink.getUuid());
      if(trapSink.hasTrapAddress()) {
        if(trapSink.getTrapAddress().hasPort())
          snmpTrap.setPort(trapSink.getTrapAddress().getPort());
        snmpTrap.setAddress(ClustermgmtUtils.createIpv4Ipv6Address(trapSink.getTrapAddress().getAddressList(0)));
      }
      if(trapSink.hasTrapUsername()) {
        snmpTrap.setUsername(trapSink.getTrapUsername());
      }
      if(trapSink.hasProtocol()) {
        snmpTrap.setProtocol(ClustermgmtUtils.snmpProtocolMap.get(trapSink.getProtocol()));
      }
      if(trapSink.hasIsInform()) {
        snmpTrap.setShouldInform(trapSink.getIsInform());
      }
      if(trapSink.hasTrapEngineId()) {
        snmpTrap.setEngineId(trapSink.getTrapEngineId());
      }
      if(trapSink.hasReceiverName()) {
        snmpTrap.setRecieverName(trapSink.getReceiverName());
      }
      if(trapSink.hasCommunityString()) {
        snmpTrap.setCommunityString(trapSink.getCommunityString());
      }

      snmpTraps.add(snmpTrap);
    }
    return snmpTraps;
  }

  @Override
  public SnmpConfig adaptZeusEntriestoSnmpInfo(ZeusConfiguration zkConfig, SnmpConfig snmpConfig) {
    SnmpInfo snmpInfo = zkConfig.getSnmpInfo().orNull();
    if (snmpInfo == null) {
      return snmpConfig;
    }

    // Status
    if(snmpInfo.hasEnabled())
      snmpConfig.setIsEnabled(snmpInfo.getEnabled());

    // Users
    List<SnmpUser> snmpUsers = getSnmpUsers(zkConfig);
    snmpConfig.setUsers(snmpUsers);

    // Transport
    List<SnmpTransport> snmpTransports = new ArrayList<>();
    for(final SnmpInfo.Transport transport: snmpInfo.getTransportListList()) {
      SnmpTransport snmpTransport = new SnmpTransport();
      snmpTransport.setPort(transport.getPort());
      if (transport.hasProtocol()) {
        snmpTransport.setProtocol(ClustermgmtUtils.snmpProtocolMap.get(transport.getProtocol()));
      }
      snmpTransports.add(snmpTransport);
    }
    snmpConfig.setTransports(snmpTransports);

    // Trap
    List<SnmpTrap> snmpTraps = getSnmpTraps(zkConfig);
    snmpConfig.setTraps(snmpTraps);

    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptZeusEntriestoSnmpInfo(zkConfig, snmpConfig);
    }
    return snmpConfig;
  }

  @Override
  public List<RsyslogServer> adaptZeusEntriestoRsyslogServer(ZeusConfiguration zkConfig) {
    List<RsyslogServer> rsyslogServers = new ArrayList<>();
    final RSyslogConfig rSyslogConfig = zkConfig.getRSyslogConfig().orNull();
    if(!Objects.isNull(rSyslogConfig)) {
      for (RSyslogConfig.LogServerConfig logServerConfig : rSyslogConfig.getLogServerConfigListList()) {
        RsyslogServer rsyslogServer = new RsyslogServer();
        rsyslogServer.setExtId(logServerConfig.getUuid());
        if (logServerConfig.hasName())
          rsyslogServer.setServerName(logServerConfig.getName());
        if (logServerConfig.hasServer()) {
          if (logServerConfig.getServer().hasPort())
            rsyslogServer.setPort(logServerConfig.getServer().getPort());
          if (logServerConfig.getServer().hasProtocol()) {
            rsyslogServer.setNetworkProtocol(
              ClustermgmtUtils.rsyslogNetworkProtocolMap.get(logServerConfig.getServer().getProtocol()));
          }
          rsyslogServer.setIpAddress(
            ClustermgmtUtils.createIpv4Ipv6Address(logServerConfig.getServer().getAddressList(0)));
        }
        List<RsyslogModuleItem> rsyslogModuleItems = new ArrayList<>();
        for (RSyslogConfig.LogServerConfig.Module module : logServerConfig.getModulesList()) {
          RsyslogModuleItem rsyslogModuleItem = new RsyslogModuleItem();
          rsyslogModuleItem.setName(ClustermgmtUtils.rsyslogModuleNameMap.get(module.getName()));
          rsyslogModuleItem.setLogSeverityLevel(ClustermgmtUtils.rsyslogZeusConfigToSyslogMap.get(module.getPriority()));
          if (module.hasMonitor())
            rsyslogModuleItem.setShouldLogMonitorFiles(module.getMonitor());
          rsyslogModuleItems.add(rsyslogModuleItem);
        }
        rsyslogServer.setModules(rsyslogModuleItems);
        rsyslogServers.add(rsyslogServer);
      }
    }
    return rsyslogServers;
  }

  @Override
  public List<RackableUnit> adaptZeusEntriestoRackableUnits(ZeusConfiguration zkConfig) {
    List<RackableUnit> rackableUnits = new ArrayList<>();
    for (final ConfigurationProto.RackableUnit rackableUnit : zkConfig.getAllRackableUnits()) {
      RackableUnit rackable = adaptToRackableUnitFromProto(zkConfig, rackableUnit);
      rackableUnits.add(rackable);
    }
    return rackableUnits;
  }

  @Override
  public RackableUnit adaptZeusEntriestoRackableUnit(ZeusConfiguration zkConfig,
                                                     String rackableUnitUuid) {
    for (final ConfigurationProto.RackableUnit rackableUnit : zkConfig.getAllRackableUnits()) {
      if (rackableUnit.getRackableUnitUuid().equals(rackableUnitUuid)) {
        return adaptToRackableUnitFromProto(zkConfig, rackableUnit);
      }
    }
    return null;
  }

  @Override
  public List<DomainFaultTolerance> adaptZeusEntriestoDomainFaultTolerance(ZeusConfiguration zkConfig) {
    List<DomainFaultTolerance> domainFaultTolerances = new ArrayList<>();
    final DomainFaultToleranceState domainFaultToleranceState =
      zkConfig.getConfiguration().getDomainFaultToleranceState();
    for(final DomainFaultToleranceState.Domain domain : domainFaultToleranceState.getDomainsList()) {
      DomainFaultTolerance domainFaultTolerance = new DomainFaultTolerance();
      domainFaultTolerance.setType(ClustermgmtUtils.domainTypeMap.get(domain.getDomainType()));

      List<ComponentFaultTolerance> componentFaultTolerances = new ArrayList<>();
      for(final DomainFaultToleranceState.Domain.Component component : domain.getComponentsList()) {
        ComponentFaultTolerance componentFaultTolerance = new ComponentFaultTolerance();
        componentFaultTolerance.setType(ClustermgmtUtils.componentTypeMap.get(component.getComponentType()));
        componentFaultTolerance.setMaxFaultsTolerated(component.getMaxFaultsTolerated());
        componentFaultTolerance.setIsUnderComputation(component.getUnderComputation());
        componentFaultTolerance.setLastUpdatedTime(DateUtils.fromEpochMicros(component.getLastUpdateSecs()*1000000));
        if (component.hasToleranceDetailsMessage()) {
          MessageEntityProto.MessageEntity messageEntity = component.getToleranceDetailsMessage();
          ToleranceMessage toleranceMessage = new ToleranceMessage();
          toleranceMessage.setId(messageEntity.getMessageId());
          List<AttributeItem> attributeItemList = new ArrayList<>();
          for (MessageEntityProto.MessageEntity.AttributeList attributeList : messageEntity.getAttributeListList()) {
            AttributeItem attributeItem = new AttributeItem();
            attributeItem.setAttribute(attributeList.getAttribute());
            attributeItem.setValue(attributeList.getValue());
            attributeItemList.add(attributeItem);
          }
          toleranceMessage.setAttributeList(attributeItemList);
        }
        componentFaultTolerances.add(componentFaultTolerance);
      }
      domainFaultTolerance.setComponentStatus(componentFaultTolerances);
      domainFaultTolerances.add(domainFaultTolerance);
    }
    return domainFaultTolerances;
  }

  public RackableUnit adaptToRackableUnitFromProto(ZeusConfiguration zkConfig,
                                                   ConfigurationProto.RackableUnit rackableUnit) {
    RackableUnit rackable = new RackableUnit();
    rackable.setId(rackableUnit.getRackableUnitId());
    rackable.setExtId(rackableUnit.getRackableUnitUuid());
    if (rackableUnit.hasRackableUnitModelName())
      rackable.setModelName(rackableUnit.getRackableUnitModelName());
    if (rackableUnit.hasRackableUnitSerial())
      rackable.setSerial(rackableUnit.getRackableUnitSerial());
    if (rackableUnit.hasRackableUnitModel())
      rackable.setModel(ClustermgmtUtils.rackableUnitModelMap.get(rackableUnit.getRackableUnitModel()));
    if (rackableUnit.hasRackUuid()) {
      RackReference rackReference = new RackReference();
      rackReference.setUuid(rackableUnit.getRackUuid());
      if (rackableUnit.hasRackId())
        rackReference.setId(rackableUnit.getRackId());
      rackable.setRack(rackReference);
    }

    List<RackableUnitNode> rackableUnitNodes = new ArrayList<>();
    final Collection<Node> nodeList = zkConfig.getCumulativeNodes();
    for (final Node node : nodeList) {
      final boolean rackableUnitIdsMatch =
        node.getRackableUnitId() == rackableUnit.getRackableUnitId();
      if (rackableUnitIdsMatch && node.hasNodePosition()) {
        RackableUnitNode rackableUnitNode = new RackableUnitNode();
        rackableUnitNode.setPosition(node.getNodePosition());
        rackableUnitNode.setSvmId(node.getServiceVmId());
        rackableUnitNode.setUuid(node.getUuid());
        rackableUnitNodes.add(rackableUnitNode);
      }
    }
    rackable.setNodes(rackableUnitNodes);
    return rackable;
  }

  @Override
  public Host adaptIdfHostMetricstoHostEntity(final List<InsightsInterfaceProto.MetricData> metricDataList,
                                                    Host hostEntity) {
    ClusterReference clusterReference = new ClusterReference();
    HypervisorReference hypervisorReference = new HypervisorReference();
    boolean setClusterReference = false;
    boolean setHypervisorReference = false;

    for (final InsightsInterfaceProto.MetricData metricData : metricDataList) {
      if (!metricData.hasName() || metricData.getValueListCount() <= 0) {
        continue;
      }
      switch (metricData.getName()) {
        case "node_name":
          hostEntity.setHostName(metricData.getValueList(0).getValue().getStrValue());
          break;
        case "host_type":
          hostEntity.setHostType(ClustermgmtUtils.hostTypeMap.get(metricData.getValueList(0).getValue().getStrValue()));
          break;
        case "cpu_model":
          hostEntity.setCpuModel(metricData.getValueList(0).getValue().getStrValue());
          break;
        case "num_cpu_cores":
          hostEntity.setNumberOfCpuCores(metricData.getValueList(0).getValue().getInt64Value());
          break;
        case "num_cpu_sockets":
          hostEntity.setNumberOfCpuSockets(metricData.getValueList(0).getValue().getInt64Value());
          break;
        case "num_cpu_threads":
          hostEntity.setNumberOfCpuThreads(metricData.getValueList(0).getValue().getInt64Value());
          break;
        case "capacity.cpu_capacity_hz":
          hostEntity.setCpuCapacityHz(metricData.getValueList(0).getValue().getInt64Value());
          break;
        case "_cluster_uuid_":
          setClusterReference = true;
          clusterReference.setUuid(metricData.getValueList(0).getValue().getStrValue());
          break;
        case "cluster_name":
          setClusterReference = true;
          clusterReference.setName(metricData.getValueList(0).getValue().getStrValue());
          break;
        case "hypervisor_full_name":
          setHypervisorReference = true;
          hypervisorReference.setFullName(metricData.getValueList(0).getValue().getStrValue());
          break;
        case "hypervisor_type":
          setHypervisorReference = true;
          hypervisorReference.setType(ClustermgmtUtils.hypervisorTypeMap.get(metricData.getValueList(0).getValue().getStrValue()));
          break;
        case "num_vms":
          setHypervisorReference = true;
          hypervisorReference.setNumberOfVms(metricData.getValueList(0).getValue().getInt64Value());
          break;
        case "gpu_driver_version":
          hostEntity.setGpuDriverVersion(metricData.getValueList(0).getValue().getStrValue());
          break;
        case "host_gpu_list":
          List<String> hostGpus = new ArrayList<>();
          for (String gpu : metricData.getValueList(0).getValue().getStrList().getValueListList()) {
            hostGpus.add(gpu);
          }
          hostEntity.setGpuList(hostGpus);
          break;
        case "default_vhd_location":
          hostEntity.setDefaultVhdLocation(metricData.getValueList(0).getValue().getStrValue());
          break;
        case "default_vhd_container_uuid":
          hostEntity.setDefaultVhdContainerUuid(metricData.getValueList(0).getValue().getStrValue());
          break;
        case "default_vm_location":
          hostEntity.setDefaultVmLocation(metricData.getValueList(0).getValue().getStrValue());
          break;
        case "default_vm_container_uuid":
          hostEntity.setDefaultVmContainerUuid(metricData.getValueList(0).getValue().getStrValue());
          break;
        case "reboot_pending":
          hostEntity.setIsRebootPending(metricData.getValueList(0).getValue().getInt64Value() != 0);
          break;
        case "failover_cluster_fqdn":
          hostEntity.setFailoverClusterFqdn(metricData.getValueList(0).getValue().getStrValue());
          break;
        case "failover_cluster_node_status":
          hostEntity.setFailoverClusterNodeStatus(metricData.getValueList(0).getValue().getStrValue());
          break;
        case "boot_time_usecs":
          hostEntity.setBootTimeUsecs(metricData.getValueList(0).getValue().getInt64Value());
          break;
        case "memory_size_bytes":
          hostEntity.setMemorySizeBytes(metricData.getValueList(0).getValue().getInt64Value());
          break;
        case "cpu_frequency_hz":
          hostEntity.setCpuFrequencyHz(metricData.getValueList(0).getValue().getInt64Value());
          break;
        case "host_maintenance_state":
          hostEntity.setMaintenanceState(metricData.getValueList(0).getValue().getStrValue());
          break;
        case "block_serial":
          hostEntity.setBlockSerial(metricData.getValueList(0).getValue().getStrValue());
          break;
        case "block_model_name":
          hostEntity.setBlockModel(metricData.getValueList(0).getValue().getStrValue());
          break;
        case "node_status":
          hostEntity.setNodeStatus(ClustermgmtUtils.nodeStatusMap.get(
            metricData.getValueList(0).getValue().getStrValue()));
          break;
        default:
          log.debug("Default entry {} and value {}", metricData.getName(), metricData.getValueList(0).getValue());
          break;
      }
    }

    if(setClusterReference)
      hostEntity.setCluster(clusterReference);
    if(setHypervisorReference)
      hostEntity.setHypervisor(hypervisorReference);

    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptIdfHostMetricstoHostEntity(metricDataList, hostEntity);
    }

    return hostEntity;
  }

  @Override
  public Host adaptZeusEntriestoHostEntity(ZeusConfiguration zkConfig, Host hostEntity, String hostUuid) {
    ControllerVmReference controllerVmReference = new ControllerVmReference();
    if(hostEntity.getHypervisor() == null)
      hostEntity.setHypervisor(new HypervisorReference());

    Optional<Node> nodeOptional = zkConfig.getNodeByUuid(hostUuid);
    if (nodeOptional.isPresent()) {
      final Node node = nodeOptional.get();

      // CVM IP: Ipv4
      IPAddress ipAddress = new IPAddress();
      if(node.getSvmExternalIpListCount() > 0) {
        ipAddress.setIpv4(ClustermgmtUtils.createIpv4Address(
          node.getSvmExternalIpList(0)));
      }
      else if(node.hasServiceVmExternalIp()) {
        ipAddress.setIpv4(ClustermgmtUtils.createIpv4Address(
          node.getServiceVmExternalIp()));
      }

      // CVM IP: Ipv6
      if(node.getSvmExternalIpListCount() > 1) {
        ipAddress.setIpv6(ClustermgmtUtils.createIpv6Address(
          node.getSvmExternalIpList(1)));
      }
      controllerVmReference.setExternalAddress(ipAddress);

      // CVM ID
      controllerVmReference.setId(node.getServiceVmId());
      // CVM Backplane Address
      if(node.hasControllerVmBackplaneIp())
        controllerVmReference.setBackplaneAddress(ClustermgmtUtils.createIpv4Ipv6Address(node.getControllerVmBackplaneIp()));

      // CVM ipmi
      if (node.hasIpmi()) {
        final ConfigurationProto.NetworkEntity ipmiNE =
          node.getIpmi();
        if (ipmiNE.getAddressListCount() > 0) {
          IpmiReference ipmiReference = new IpmiReference();
          ipmiReference.setIp(ClustermgmtUtils.createIpv4Ipv6Address(ipmiNE.getAddressList(0)));
          ipmiReference.setUsername(ipmiNE.getUsername());
          controllerVmReference.setIpmi(ipmiReference);
        }
      }

      // CVM RdmaBackplane Ips
      if (!CollectionUtils.isEmpty(node.getRdmaBackplaneIpsList())) {
        List<IPAddress> rdmaBackplaneIps = new ArrayList<>();
        for (final String ip : node.getRdmaBackplaneIpsList()) {
          rdmaBackplaneIps.add(ClustermgmtUtils.createIpv4Ipv6Address(ip));
        }
        controllerVmReference.setRdmaBackplaneAddress(rdmaBackplaneIps);
      }

      // Cvm nat ip
      if (node.hasServiceVmNatIp()) {
        controllerVmReference.setNatIp(ClustermgmtUtils.createIpv4Ipv6Address(node.getServiceVmNatIp()));
      }

      // Cvm nat Port
      if (node.hasServiceVmNatPort()) {
        controllerVmReference.setNatPort(node.getServiceVmNatPort());
      }

      // Cvm maintenance mode
      if (node.hasMaintenanceMode()) {
        controllerVmReference.setIsInMaintenanceMode(node.getMaintenanceMode());
      }

      // rackable unit uuid
      if (node.hasRackableUnitUuid()) {
        controllerVmReference.setRackableUnitUuid(node.getRackableUnitUuid());
      }

      // Hypervisor IP: Ipv4
      IPAddress hostIpAddresses = new IPAddress();
      if(node.getHostExternalIpListCount() > 0) {
        hostIpAddresses.setIpv4(ClustermgmtUtils.createIpv4Address(
          node.getHostExternalIpList(0)));
      }
      else {
        if (node.hasHypervisor()
          && (node.getHypervisor().getAddressListCount() > 0)) {
          final ConfigurationProto.NetworkEntity hypervisor =
            node.getHypervisor();
          hostIpAddresses.setIpv4(ClustermgmtUtils.createIpv4Address(
            hypervisor.getAddressList(0)));
        }
        else if(node.hasHypervisorKey()){
          hostIpAddresses.setIpv4(ClustermgmtUtils.createIpv4Address(
            node.getHypervisorKey()));
        }
      }

      // Hypervisor IP: Ipv6
      if(node.getHostExternalIpListCount() > 1) {
        hostIpAddresses.setIpv6(ClustermgmtUtils.createIpv6Address(
          node.getHostExternalIpList(1)));
      }

      if (hostEntity.getHypervisor() != null)
        hostEntity.getHypervisor().setExternalAddress(hostIpAddresses);

      // Hypervisor Username
      if(node.hasHypervisor()) {
        final ConfigurationProto.NetworkEntity hypervisor = node.getHypervisor();
        if (hypervisor.hasUsername())
          hostEntity.getHypervisor().setUserName(hypervisor.getUsername());
      }

      // Disk
      final Collection<ConfigurationProto.Disk> disksInThisNode = zkConfig.getDisksByNodeUuid(hostUuid);
      List<DiskReference> diskList= new ArrayList<>();
      for (final ConfigurationProto.Disk disk :  disksInThisNode) {
        DiskReference diskReference = new DiskReference();
        diskReference.setUuid(disk.getDiskUuid());
        diskReference.setMountPath(disk.getMountPath());
        diskReference.setSerialId(disk.getDiskSerialId());
        diskReference.setSizeInBytes(disk.getDiskSize());
        diskReference.setStorageTier(ClustermgmtUtils.storageTierMap.get(disk.getStorageTier()));
        diskList.add(diskReference);
      }
      hostEntity.setDisk(diskList);

      // Degraded
      if(node.hasIsDegraded())
        hostEntity.setIsDegraded(node.getIsDegraded());

      // Secure Boot
      if(node.hasIsSecureBooted())
        hostEntity.setIsSecureBooted(node.getIsSecureBooted());

      // Hardware Virtualised
      hostEntity.setIsHardwareVirtualized(node.getAcropolisStatus().getIsHardwareVirtualized());

      // Csr
      hostEntity.setHasCsr(node.hasSvmCertificateSigningRequestZkpath());

      // Hypervisor State
      hostEntity.getHypervisor().setState(
        ClustermgmtUtils.hypervisorState.get(node.getAcropolisStatus().getNodeState()));

      // acropolisConnectionState
      hostEntity.getHypervisor().setAcropolisConnectionState(
        ClustermgmtUtils.acropolisConnectionState.get(node.getAcropolisStatus().getConnState()));

      // keyManagementDeviceToCertStatus
      // Sample certificate zkpath:
      // /appliance/logical/certs/node-id/svm_certs/<device-uuid>/randomString
      // <device-uuid> indicate the uuid of the key management server.
      final Set<String> certificateSet = Sets.newHashSet();
      for (final String path : node.getDigitalCertificateZkpathListList()) {
        final String[] tokens = path.split("/");
        certificateSet.add(tokens[6]);
      }
      final Set<String> testedCertificateSet = new HashSet<String>();
      for (final Node.DigitalCertificateMap aMap : node
        .getDigitalCertificateMapListList()) {
        final String[] tokens = aMap.getDigitalCertificateZkpath().split("/");
        testedCertificateSet.add(tokens[6]);
      }
      List<KeyManagementDeviceToCertStatusInfo> keyManagementDeviceToCertStatusList =
        new ArrayList<>();
      for (final ConfigurationProto.KeyManagementServer server :
        zkConfig.getAllKeyManagementServers()) {
        Boolean status = null;
        if (certificateSet.contains(server.getKeyManagementServerUuid())) {
          status = false;
        }
        if (testedCertificateSet.contains(
          server.getKeyManagementServerUuid())) {
          status = true;
        }
        KeyManagementDeviceToCertStatusInfo keyManagementDeviceToCertStatus =
          new KeyManagementDeviceToCertStatusInfo();
        keyManagementDeviceToCertStatus.setIsCertificatePresent(status);
        keyManagementDeviceToCertStatus.setKeyManagementServerName(
          server.getKeyManagementServerName());
        keyManagementDeviceToCertStatusList.add(keyManagementDeviceToCertStatus);
      }
      hostEntity.setKeyManagementDeviceToCertStatus(keyManagementDeviceToCertStatusList);
    }
    hostEntity.setControllerVm(controllerVmReference);

    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptZeusEntriestoHostEntity(zkConfig, hostEntity, hostUuid);
    }

    return hostEntity;
  }

  @Override
  public Cluster adaptManagementServerIntoClusterEntity(List<InsightsInterfaceProto.EntityWithMetric> entitiesWithMetric,
                                                              Cluster clusterEntity) {
    ManagementServerRef managementServerRef = new ManagementServerRef();
    managementServerRef.setIsRegistered(false);
    managementServerRef.setIsInUse(true); // By default true
    for (final InsightsInterfaceProto.EntityWithMetric entityWithMetric : entitiesWithMetric) {
      final List<InsightsInterfaceProto.MetricData> metricDataList =
        entityWithMetric.getMetricDataListList();
      for (final InsightsInterfaceProto.MetricData metricData : metricDataList) {
        if (!metricData.hasName() || metricData.getValueListCount() <= 0) {
          continue;
        }
        switch (metricData.getName()) {
          case "address":
            managementServerRef.setIp(
              ClustermgmtUtils.createIpv4Ipv6Address(metricData.getValueList(0).getValue().getStrValue()));
            break;
          case "extension_key":
            managementServerRef.setIsRegistered(true);
            break;
          case "management_server_type":
            managementServerRef.setType(ClustermgmtUtils.managementServerType.get(
              metricData.getValueList(0).getValue().getStrValue()));
            break;
          case "in_use":
            managementServerRef.setIsInUse(metricData.getValueList(0).getValue().getBoolValue());
            break;
          case "drs_enabled":
            managementServerRef.setIsDrsEnabled(metricData.getValueList(0).getValue().getBoolValue());
            break;
          default:
            break;
        }
      }
    }
    clusterEntity.getNetwork().setManagementServer(managementServerRef);

    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptManagementServerIntoClusterEntity(entitiesWithMetric, clusterEntity);
    }

    return clusterEntity;
  }

  public SnmpUser adaptSnmpUserProtoToSnmpUserEntity(String userExtId, GenesisInterfaceProto.SnmpUser snmpUser) {
    SnmpUser snmpUserEntity = new SnmpUser();
    if (!userExtId.isEmpty())
      snmpUserEntity.setExtId(userExtId);
    snmpUserEntity.setUsername(snmpUser.getUsername());
    if (snmpUser.hasAuthType())
      snmpUserEntity.setAuthType(SnmpAuthType.fromString(snmpUser.getAuthType()));
    if (snmpUser.hasPrivType())
      snmpUserEntity.setPrivType(SnmpPrivType.fromString(snmpUser.getPrivType()));
    log.debug("SnmpUser constructed from SnmpUserProto {}: {}", snmpUser, snmpUserEntity);
    return snmpUserEntity;
  }

  public SnmpTrap adaptSnmpTrapProtoToSnmpTrapEntity(String trapExtId, GenesisInterfaceProto.SnmpTrap snmpTrap) {
    SnmpTrap snmpTrapEntity = new SnmpTrap();
    if (!trapExtId.isEmpty())
      snmpTrapEntity.setExtId(trapExtId);
    if (snmpTrap.hasVersion())
      snmpTrapEntity.setVersion(SnmpTrapVersion.fromString(snmpTrap.getVersion()));
    if (snmpTrap.hasAddress())
      snmpTrapEntity.setAddress(ClustermgmtUtils.createIpv4Ipv6Address(snmpTrap.getAddress()));
    if (snmpTrap.hasPort())
      snmpTrapEntity.setPort(snmpTrap.getPort());
    if (snmpTrap.hasUsername())
      snmpTrapEntity.setUsername(snmpTrap.getUsername());
    if (snmpTrap.hasProtocol())
      snmpTrapEntity.setProtocol(SnmpProtocol.fromString(snmpTrap.getProtocol()));
    if (snmpTrap.hasIsInform())
      snmpTrapEntity.setShouldInform(snmpTrap.getIsInform());
    if (snmpTrap.hasEngineId())
      snmpTrapEntity.setEngineId(snmpTrap.getEngineId());
    if (snmpTrap.hasReceiverName())
      snmpTrapEntity.setRecieverName(snmpTrap.getReceiverName());
    if (snmpTrap.hasCommunityString())
      snmpTrapEntity.setCommunityString(snmpTrap.getCommunityString());

    log.debug("SnmpTrap constructed from SnmpTrapProto {}: {}", snmpTrap, snmpTrapEntity);
    return snmpTrapEntity;
  }

  public SnmpTransport adaptSnmpTransportProtoToSnmpTransportEntity(GenesisInterfaceProto.SnmpTransport snmpTransport) {
    SnmpTransport snmpTransportEntity = new SnmpTransport();
    if(snmpTransport.hasPort())
      snmpTransportEntity.setPort(snmpTransport.getPort());
    if(snmpTransport.hasProtocol())
      snmpTransportEntity.setProtocol(SnmpProtocol.fromString(snmpTransport.getProtocol()));

    log.debug("SnmpTransport constructed from SnmpTransportProto {}: {}", snmpTransport, snmpTransportEntity);
    return snmpTransportEntity;
  }

  public RsyslogServer adaptRemoteSyslogConfigurationProtoToRsyslogServer(
    String rsyslogServerExtId, GenesisInterfaceProto.RemoteSyslogConfiguration remoteSyslogConfigurationProto) {
    RsyslogServer rsyslogServer = new RsyslogServer();
    ImmutableMap<Integer, RsyslogModuleLogSeverityLevel> priorityNumberToSyslogLogSecurityLevel =
      ClustermgmtUtils.syslogMapToPriorityNumber
        .entrySet()
        .stream()
        .collect(ImmutableMap.toImmutableMap(
          Map.Entry::getValue,
          Map.Entry::getKey
        ));

    rsyslogServer.setExtId(rsyslogServerExtId);
    if (remoteSyslogConfigurationProto.hasServerName())
      rsyslogServer.setServerName(remoteSyslogConfigurationProto.getServerName());
    if (remoteSyslogConfigurationProto.hasIpAddress())
      rsyslogServer.setIpAddress(
        ClustermgmtUtils.createIpv4Ipv6Address(remoteSyslogConfigurationProto.getIpAddress())
      );
    if (remoteSyslogConfigurationProto.hasPort())
      rsyslogServer.setPort(remoteSyslogConfigurationProto.getPort());
    if (remoteSyslogConfigurationProto.hasNetworkProtocol())
      rsyslogServer.setNetworkProtocol(RsyslogNetworkProtocol.fromString(
        remoteSyslogConfigurationProto.getNetworkProtocol()));
    List<RsyslogModuleItem> rsyslogModuleItems = new ArrayList<>();
    for (GenesisInterfaceProto.RemoteSyslogConfiguration.SyslogModule syslogModule:
      remoteSyslogConfigurationProto.getModulesList()) {
      RsyslogModuleItem rsyslogModuleItem = new RsyslogModuleItem();
      rsyslogModuleItem.setName(RsyslogModuleName.fromString(syslogModule.getModuleName().toUpperCase(Locale.ROOT)));
      rsyslogModuleItem.setShouldLogMonitorFiles(syslogModule.getMonitor());
      rsyslogModuleItem.setLogSeverityLevel(
        priorityNumberToSyslogLogSecurityLevel.get(syslogModule.getLogSeverityLevel()));
      rsyslogModuleItems.add(rsyslogModuleItem);
    }
    rsyslogServer.setModules(rsyslogModuleItems);

    log.debug("RsyslogServer constructed from RsyslogServerProto {}: {}", remoteSyslogConfigurationProto, rsyslogServer);
    return rsyslogServer;
  }
}
