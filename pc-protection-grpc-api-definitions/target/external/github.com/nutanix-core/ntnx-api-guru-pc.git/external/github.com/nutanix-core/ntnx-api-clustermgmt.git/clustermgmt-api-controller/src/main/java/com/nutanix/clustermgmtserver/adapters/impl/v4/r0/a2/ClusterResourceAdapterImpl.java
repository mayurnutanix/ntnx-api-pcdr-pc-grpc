package com.nutanix.clustermgmtserver.adapters.impl.v4.r0.a2;

import com.google.protobuf.ByteString;
import com.nutanix.api.utils.type.DateUtils;
import com.nutanix.cluster_management.ClusterManagementInterfaceProto;
import com.nutanix.clustermgmtserver.adapters.api.ClusterResourceAdapter;
import com.nutanix.clustermgmtserver.adapters.impl.BaseClusterResourceAdapterImpl;
import com.nutanix.clustermgmtserver.utils.ClustermgmtUtils;
import com.nutanix.infrastructure.cluster.genesis.GenesisInterfaceProto;
import com.nutanix.prism.auth.util.XorCrypt;
import com.nutanix.insights.ifc.InsightsInterfaceProto;
import com.nutanix.prism.commands.validation.ValidationUtils;
import com.nutanix.prism.common.ConfigurationConstants;
import com.nutanix.zeus.protobuf.Configuration;
import com.nutanix.zeus.protobuf.DomainFaultToleranceStateProto;
import dp1.clu.clustermgmt.v4.config.*;
import dp1.clu.clustermgmt.v4.stats.ClusterStats;
import dp1.clu.clustermgmt.v4.stats.HostStats;
import dp1.clu.clustermgmt.v4.stats.TimeValuePair;
import dp1.clu.common.v1.config.IPAddress;
import dp1.clu.common.v1.config.IPAddressOrFQDN;
import dp1.clu.common.v1.config.IPv4Address;
import lombok.extern.slf4j.Slf4j;
import com.nutanix.api.utils.json.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.time.OffsetDateTime;
import java.util.*;

@Component(ClusterResourceAdapterImpl.ADAPTER_VERSION)
@Slf4j
public class ClusterResourceAdapterImpl extends BaseClusterResourceAdapterImpl {
  @Autowired
  public ClusterResourceAdapterImpl(
    @Qualifier(
      com.nutanix.clustermgmtserver.adapters.impl.v4.r0.b1.ClusterResourceAdapterImpl.ADAPTER_VERSION
    ) final ClusterResourceAdapter nextChainAdapter) {
    this.nextChainAdapter = nextChainAdapter;
  }
  public static final String ADAPTER_VERSION = "v4.0.a2-clustermgmt-adapter";

  @Override
  public String getVersionOfAdapter() {
    return ADAPTER_VERSION;
  }

  @Override
  public GenesisInterfaceProto.UpdateClusterArg adaptClusterEntityToClusterUpdateArg(Cluster body,
                                                                                     GenesisInterfaceProto.UpdateClusterArg.Builder updateClusterArg,
                                                                                     ByteString parentTaskUuid) {
    // cluster name
    if (body.getName() != null)
      updateClusterArg.setClusterName(body.getName());

    // cluster network
    if (body.getNetwork() != null) {
      ClusterNetworkReference network = body.getNetwork();
      // ntp servers

      if(network.getNtpServerIpList() != null) {
        updateClusterArg.setNtpServerList(
          GenesisInterfaceProto.UpdateClusterArg.ntp_server_msg.newBuilder()
            .addAllNtpServers(ClustermgmtUtils.getAllAddressList(network.getNtpServerIpList())).build());
      }

      // name servers
      if(network.getNameServerIpList() != null) {
        updateClusterArg.setNameServerList(
          GenesisInterfaceProto.UpdateClusterArg.name_server_msg.newBuilder().
            addAllNameServers(ClustermgmtUtils.getAllAddressList(network.getNameServerIpList())).build());
      }

      // nfs whitelist
      if(network.getNfsSubnetWhitelist() != null) {
        updateClusterArg.setNfsSubnetWhitelist(
          GenesisInterfaceProto.UpdateClusterArg.nfs_subnet_whitelist_msg.newBuilder().
            addAllNfsSubnets(network.getNfsSubnetWhitelist()).build());
      }

      // smtp server
      if(network.getSmtpServer() != null) {
        // type
        Configuration.ConfigurationProto.Aegis.SmtpServerType smtpServerType = null;
        if (network.getSmtpServer().getType() != null) {
          smtpServerType = ClustermgmtUtils.getKey(ClustermgmtUtils.smtpServerTypeMap, network.getSmtpServer().getType());
        }

        // server
        SmtpNetwork smtpNetwork = network.getSmtpServer().getServer();
        Configuration.ConfigurationProto.NetworkEntity.Builder networkEntitiy =
          Configuration.ConfigurationProto.NetworkEntity.newBuilder();
        networkEntitiy.addAddressList(ClustermgmtUtils.getIpAddressOrFqdn(smtpNetwork.getIpAddress()));
        if(smtpNetwork.getPort() != null)
          networkEntitiy.setPort(smtpNetwork.getPort());
        if(smtpNetwork.getUsername() != null)
          networkEntitiy.setUsername(smtpNetwork.getUsername());
        if(smtpNetwork.getPassword() != null)
          networkEntitiy.setPassword(smtpNetwork.getPassword());

        GenesisInterfaceProto.UpdateClusterArg.SmtpServer.Builder smtpServer =
          GenesisInterfaceProto.UpdateClusterArg.SmtpServer.newBuilder();
        if (smtpServerType != null)
          smtpServer.setType(smtpServerType);
        smtpServer.setEmailAddress(network.getSmtpServer().getEmailAddress());
        smtpServer.setServer(networkEntitiy);
        updateClusterArg.setSmtpServer(smtpServer);
      }

      //Cluster External Ipv4
      if (network.getExternalAddress() != null &&
        network.getExternalAddress().hasIpv4()) {
        updateClusterArg.setClusterExternalIpv4(
          ClustermgmtUtils.getIpv4Address(network.getExternalAddress()));
      }

      //Cluster External Ipv6
      if (network.getExternalAddress() != null &&
        network.getExternalAddress().hasIpv6()) {
        updateClusterArg.setClusterExternalIpv6(
          ClustermgmtUtils.getIpv6Address(network.getExternalAddress()));
      }

      //Cluster Data Service IP
      if (network.getExternalDataServiceIp() != null && network.getExternalDataServiceIp().hasIpv4()) {
        updateClusterArg.setClusterExternalDataServicesIp(
          ClustermgmtUtils.getAddress(network.getExternalDataServiceIp()));
      }

      //FQDN
      if(network.getFqdn() != null) {
        updateClusterArg.setClusterFullyQualifiedDomainName(network.getFqdn());
      }
    }

    // cluster config
    if(body.getConfig() != null) {
      ClusterConfigReference config = body.getConfig();
      // operation mode
      if (config.getOperationMode() != null) {
        Configuration.ConfigurationProto.OperationMode operationMode =
          ClustermgmtUtils.getKey(ClustermgmtUtils.operationModeMap, config.getOperationMode());
        updateClusterArg.setOperationMode(operationMode);
      }

      // redundancy factor
      if(config.getRedundancyFactor() != null)
        updateClusterArg.setRedundancyFactor(config.getRedundancyFactor());

      // domain awareness level
      if (config.getFaultToleranceState() != null
        && config.getFaultToleranceState().getDomainAwarenessLevel() != null) {
        DomainFaultToleranceStateProto.DomainFaultToleranceState.Domain.DomainType domainType =
          ClustermgmtUtils.getKey(ClustermgmtUtils.domainAwarenessMap, config.getFaultToleranceState().getDomainAwarenessLevel());
        updateClusterArg.setDomainAwarenessLevel(domainType);
      }

      // Authorised public key
      if (config.getAuthorizedPublicKeyList() != null && config.getAuthorizedPublicKeyList().size() > 0) {
        List<GenesisInterfaceProto.UpdateClusterArg.public_key_msg.ssh_keys> sshKeys
          = new ArrayList<>();
        for(PublicKey publicKey : config.getAuthorizedPublicKeyList()) {
          sshKeys.add(GenesisInterfaceProto.UpdateClusterArg.public_key_msg.ssh_keys.newBuilder()
            .setKeyName(publicKey.getName())
            .setKeyValue(publicKey.getKey()).build());
        }
        updateClusterArg.setAuthorisedPublicKeys(
          GenesisInterfaceProto.UpdateClusterArg.public_key_msg.newBuilder().addAllSshKeyList(sshKeys));
      }
    }

    // Parent Task uuid
    if (parentTaskUuid != null) {
      updateClusterArg.setParentTaskUuid(parentTaskUuid);
    }

    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptClusterEntityToClusterUpdateArg(body, updateClusterArg, parentTaskUuid);
    }

    return updateClusterArg.build();
  }

  @Override
  public GenesisInterfaceProto.AddSnmpTransportsArg adaptSnmpTransportsToAddTransportsArg(
    SnmpTransport snmpTransport, GenesisInterfaceProto.AddSnmpTransportsArg.Builder addTransportArg,
    ByteString parentTaskUuid) {
    final Configuration.ConfigurationProto.SnmpInfo.Transport.Builder transport = Configuration.ConfigurationProto.SnmpInfo.Transport.newBuilder();
    Configuration.ConfigurationProto.SnmpInfo.Protocol protocol = ClustermgmtUtils.getKey(
      ClustermgmtUtils.snmpProtocolMap, snmpTransport.getProtocol());

    transport.setProtocol(protocol);
    transport.setPort(snmpTransport.getPort());
    addTransportArg.addTransportList(transport);

    if (parentTaskUuid != null) {
      addTransportArg.setParentTaskUuid(parentTaskUuid);
    }

    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptSnmpTransportsToAddTransportsArg(snmpTransport, addTransportArg, parentTaskUuid);
    }

    return addTransportArg.build();
  }

  @Override
  public GenesisInterfaceProto.RemoveSnmpTransportsArg adaptSnmpTransportsToRemoveTransportsArg(
    SnmpTransport transportToRemove, GenesisInterfaceProto.RemoveSnmpTransportsArg.Builder removeTransportArg,
    ByteString parentTaskUuid) {
    final Configuration.ConfigurationProto.SnmpInfo.Transport.Builder transport = Configuration.ConfigurationProto.SnmpInfo.Transport.newBuilder();
    Configuration.ConfigurationProto.SnmpInfo.Protocol protocol = ClustermgmtUtils.getKey(
      ClustermgmtUtils.snmpProtocolMap, transportToRemove.getProtocol());

    transport.setProtocol(protocol);
    transport.setPort(transportToRemove.getPort());
    removeTransportArg.addTransportList(transport);

    if (parentTaskUuid != null) {
      removeTransportArg.setParentTaskUuid(parentTaskUuid);
    }

    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptSnmpTransportsToRemoveTransportsArg(transportToRemove, removeTransportArg,
                                                                       parentTaskUuid);
    }

    return removeTransportArg.build();
  }

  public Configuration.ConfigurationProto.SnmpInfo.User getSnmpUserZeusConfigObject(SnmpUser snmpUser) {
    final Configuration.ConfigurationProto.SnmpInfo.User.Builder user = Configuration.ConfigurationProto.SnmpInfo.User.newBuilder();
    if (snmpUser.getExtId() != null) // For update user call
      user.setUuid(snmpUser.getExtId());
    user.setUsername(snmpUser.getUsername());
    user.setAuthKey(XorCrypt.encrypt(snmpUser.getAuthKey(), null));
    Configuration.ConfigurationProto.SnmpInfo.User.AuthType authType = ClustermgmtUtils.getKey(
      ClustermgmtUtils.snmpAuthTypeMap, snmpUser.getAuthType());
    user.setAuthType(authType);
    if (snmpUser.getPrivKey() != null)
      user.setPrivKey(XorCrypt.encrypt(snmpUser.getPrivKey(), null));
    if (snmpUser.getPrivType() != null) {
      Configuration.ConfigurationProto.SnmpInfo.User.PrivType privType = ClustermgmtUtils.getKey(
        ClustermgmtUtils.snmpPrivTypeMap, snmpUser.getPrivType());
      user.setPrivType(privType);
    }
    return user.build();
  }

  @Override
  public GenesisInterfaceProto.AddSnmpUserArg adaptSnmpUserToAddSnmpUserArg(SnmpUser snmpUser,
                                                                            GenesisInterfaceProto.AddSnmpUserArg.Builder addSnmpUserArg,
                                                                            ByteString parentTaskUuid) {
    addSnmpUserArg.setSnmpUser(getSnmpUserZeusConfigObject(snmpUser));

    if (parentTaskUuid != null) {
      addSnmpUserArg.setParentTaskUuid(parentTaskUuid);
    }

    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptSnmpUserToAddSnmpUserArg(snmpUser, addSnmpUserArg, parentTaskUuid);
    }

    return addSnmpUserArg.build();
  }

  @Override
  public GenesisInterfaceProto.UpdateSnmpUserArg adaptSnmpUserToUpdateSnmpUserArg(SnmpUser snmpUser,
                                                                                  GenesisInterfaceProto.UpdateSnmpUserArg.Builder updateSnmpUserArg,
                                                                                  ByteString parentTaskUuid) {
    updateSnmpUserArg.setSnmpUser(getSnmpUserZeusConfigObject(snmpUser));

    if (parentTaskUuid != null) {
      updateSnmpUserArg.setParentTaskUuid(parentTaskUuid);
    }

    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptSnmpUserToUpdateSnmpUserArg(snmpUser, updateSnmpUserArg, parentTaskUuid);
    }

    return updateSnmpUserArg.build();
  }

  @Override
  public GenesisInterfaceProto.DeleteSnmpUserArg adaptSnmpUserToDeleteSnmpUserArg(String userExtId,
                                                                                  GenesisInterfaceProto.DeleteSnmpUserArg.Builder deleteSnmpUserArg,
                                                                                  ByteString parentTaskUuid) {
    deleteSnmpUserArg.setUserUuid(userExtId);

    if (parentTaskUuid != null) {
      deleteSnmpUserArg.setParentTaskUuid(parentTaskUuid);
    }

    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptSnmpUserToDeleteSnmpUserArg(userExtId, deleteSnmpUserArg, parentTaskUuid);
    }

    return deleteSnmpUserArg.build();
  }

  public Configuration.ConfigurationProto.SnmpInfo.TrapSink getSnmpTrapZeusConfigObject(SnmpTrap snmpTrap) {
    final Configuration.ConfigurationProto.SnmpInfo.TrapSink.Builder snmpTrapBuilder = Configuration.ConfigurationProto.SnmpInfo.TrapSink.newBuilder();
    if (snmpTrap.getExtId() != null)
      snmpTrapBuilder.setUuid(snmpTrap.getExtId());
    if (snmpTrap.getUsername() != null)
      snmpTrapBuilder.setTrapUsername(snmpTrap.getUsername());
    if (snmpTrap.getShouldInform() != null) {
      snmpTrapBuilder.setIsInform(snmpTrap.getShouldInform());
    }
    if (snmpTrap.getEngineId() != null) {
      snmpTrapBuilder.setTrapEngineId(snmpTrap.getEngineId());
    }
    if (snmpTrap.getProtocol() != null) {
      Configuration.ConfigurationProto.SnmpInfo.Protocol protocol = ClustermgmtUtils.getKey(
        ClustermgmtUtils.snmpProtocolMap, snmpTrap.getProtocol());
      snmpTrapBuilder.setProtocol(protocol);
    } else {
      snmpTrapBuilder.setProtocol(
        ConfigurationConstants.DEFAULT_ZEUS_PROTOCOL);
    }
    // Build the SNMP trap address configuration
    final Configuration.ConfigurationProto.NetworkEntity.Builder snmpTrapAddressBuilder =
      Configuration.ConfigurationProto.NetworkEntity.newBuilder();
    if (snmpTrap.getPort() != null && snmpTrap.getPort() > 0) {
      snmpTrapAddressBuilder.setPort(snmpTrap.getPort());
    } else {
      snmpTrapAddressBuilder.setPort(ConfigurationConstants.DEFAULT_PORT);
    }
    snmpTrapAddressBuilder.addAddressList(ClustermgmtUtils.getIpv4Address(snmpTrap.getAddress()));
    snmpTrapBuilder.setTrapAddress(snmpTrapAddressBuilder.build());
    if (snmpTrap.getVersion() != null) {
      Configuration.ConfigurationProto.SnmpInfo.SnmpVersion version = ClustermgmtUtils.getKey(
        ClustermgmtUtils.snmpTrapVersionMap, snmpTrap.getVersion());
      snmpTrapBuilder.setVersion(version);
    }
    if (snmpTrap.getVersion() == SnmpTrapVersion.V2) {
      final String communityString = snmpTrap.getCommunityString() != null ?
        snmpTrap.getCommunityString() : ClustermgmtUtils.DEFAULT_COMMUNITY_STRING;
      snmpTrapBuilder.setCommunityString(communityString);
    }
    if (snmpTrap.getRecieverName() != null) {
      snmpTrapBuilder.setReceiverName(snmpTrap.getRecieverName());
    }

    return snmpTrapBuilder.build();
  }

  @Override
  public GenesisInterfaceProto.AddSnmpTrapArg adaptSnmpTrapToAddSnmpTrapArg(SnmpTrap snmpTrap,
                                                                            GenesisInterfaceProto.AddSnmpTrapArg.Builder addSnmpTrapArg,
                                                                            ByteString parentTaskUuid) {
    addSnmpTrapArg.setSnmpTrap(getSnmpTrapZeusConfigObject(snmpTrap));

    if (parentTaskUuid != null) {
      addSnmpTrapArg.setParentTaskUuid(parentTaskUuid);
    }

    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptSnmpTrapToAddSnmpTrapArg(snmpTrap, addSnmpTrapArg, parentTaskUuid);
    }

    return addSnmpTrapArg.build();
  }

  @Override
  public GenesisInterfaceProto.UpdateSnmpTrapArg adaptSnmpTrapToUpdateSnmpTrapArg(SnmpTrap snmpTrap,
                                                                                  GenesisInterfaceProto.UpdateSnmpTrapArg.Builder updateSnmpTrapArg,
                                                                                  ByteString parentTaskUuid) {
    updateSnmpTrapArg.setSnmpTrap(getSnmpTrapZeusConfigObject(snmpTrap));

    if (parentTaskUuid != null) {
      updateSnmpTrapArg.setParentTaskUuid(parentTaskUuid);
    }

    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptSnmpTrapToUpdateSnmpTrapArg(snmpTrap, updateSnmpTrapArg, parentTaskUuid);
    }

    return updateSnmpTrapArg.build();
  }

  @Override
  public GenesisInterfaceProto.DeleteSnmpTrapArg adaptSnmpTrapToDeleteSnmpTrapArg(String trapExtId,
                                                                                  GenesisInterfaceProto.DeleteSnmpTrapArg.Builder deleteSnmpTrapArg,
                                                                                  ByteString parentTaskUuid) {
    deleteSnmpTrapArg.setTrapUuid(trapExtId);

    if (parentTaskUuid != null) {
      deleteSnmpTrapArg.setParentTaskUuid(parentTaskUuid);
    }

    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptSnmpTrapToDeleteSnmpTrapArg(trapExtId, deleteSnmpTrapArg, parentTaskUuid);
    }

    return deleteSnmpTrapArg.build();
  }

  public GenesisInterfaceProto.RemoteSyslogServer getRemoteSyslogServerFromRsyslogServer(RsyslogServer rsyslogServer) {
    GenesisInterfaceProto.RemoteSyslogServer.Builder remoteSyslogServer =
      GenesisInterfaceProto.RemoteSyslogServer.newBuilder();

    if(rsyslogServer.getExtId() != null)
      remoteSyslogServer.setUuid(rsyslogServer.getExtId());
    remoteSyslogServer.setServerName(rsyslogServer.getServerName());
    remoteSyslogServer.setPort(rsyslogServer.getPort());
    remoteSyslogServer.setIpAddress(ClustermgmtUtils.getAddress(rsyslogServer.getIpAddress()));
    String protocol = ClustermgmtUtils.rsyslogNetworkProtocolStringImmutableMap.get(rsyslogServer.getNetworkProtocol());
    remoteSyslogServer.setNetworkProtocol(protocol);

    GenesisInterfaceProto.RemoteSyslogServer.RemoteSyslogModule.Builder remoteSyslogModule =
      GenesisInterfaceProto.RemoteSyslogServer.RemoteSyslogModule.newBuilder();
    if (rsyslogServer.getModules() != null) {
      for (RsyslogModuleItem rsyslogModuleItem : rsyslogServer.getModules()) {
        GenesisInterfaceProto.RemoteSyslogServer.RemoteSyslogModule.SyslogModule.Builder syslogModule =
          GenesisInterfaceProto.RemoteSyslogServer.RemoteSyslogModule.SyslogModule.newBuilder();
        String name = ClustermgmtUtils.getKey(ClustermgmtUtils.rsyslogModuleNameMap,rsyslogModuleItem.getName());
        syslogModule.setModuleName(name);
        syslogModule.setLogSeverityLevel(ClustermgmtUtils.syslogMapToPriorityNumber.get(rsyslogModuleItem.getLogSeverityLevel()));
        final boolean monitor = rsyslogModuleItem.getShouldLogMonitorFiles() != null ?
          rsyslogModuleItem.getShouldLogMonitorFiles() : true;
        syslogModule.setMonitor(monitor).build();
        remoteSyslogModule.addModuleList(syslogModule);
      }
    }

    remoteSyslogServer.setRemoteSyslogModule(remoteSyslogModule);
    return remoteSyslogServer.build();
  }

  @Override
  public GenesisInterfaceProto.CreateRemoteSyslogServerArg adaptRsyslogServerToCreateRsyslogServerArg(RsyslogServer rsyslogServer,
                                                                                                      GenesisInterfaceProto.CreateRemoteSyslogServerArg.Builder createRemoteSyslogServerArg,
                                                                                                      ByteString parentTaskUuid) {
    GenesisInterfaceProto.RemoteSyslogServer remoteSyslogServer = getRemoteSyslogServerFromRsyslogServer(rsyslogServer);
    createRemoteSyslogServerArg.setRemoteSyslogServer(remoteSyslogServer).build();

    // Parent Task uuid
    if (parentTaskUuid != null) {
      createRemoteSyslogServerArg.setParentTaskUuid(parentTaskUuid);
    }

    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptRsyslogServerToCreateRsyslogServerArg(rsyslogServer, createRemoteSyslogServerArg, parentTaskUuid);
    }

    return createRemoteSyslogServerArg.build();
  }

  @Override
  public GenesisInterfaceProto.UpdateRemoteSyslogServerArg adaptRsyslogServerToUpdateRsyslogServerArg(RsyslogServer rsyslogServer,
                                                                                                      GenesisInterfaceProto.UpdateRemoteSyslogServerArg.Builder updateRemoteSyslogServerArg,
                                                                                                      ByteString parentTaskUuid) {
    GenesisInterfaceProto.RemoteSyslogServer remoteSyslogServer = getRemoteSyslogServerFromRsyslogServer(rsyslogServer);
    updateRemoteSyslogServerArg.setRemoteSyslogServer(remoteSyslogServer).build();

    // Parent Task uuid
    if (parentTaskUuid != null) {
      updateRemoteSyslogServerArg.setParentTaskUuid(parentTaskUuid);
    }

    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptRsyslogServerToUpdateRsyslogServerArg(rsyslogServer, updateRemoteSyslogServerArg, parentTaskUuid);
    }

    return updateRemoteSyslogServerArg.build();
  }

  @Override
  public GenesisInterfaceProto.DeleteRemoteSyslogServerArg adaptRsyslogServerNameToDeleteRsyslogServerArg(String rsyslogServerExtId,
                                                                                                          GenesisInterfaceProto.DeleteRemoteSyslogServerArg.Builder deleteRemoteSyslog,
                                                                                                          ByteString parentTaskUuid) {
    deleteRemoteSyslog.setUuid(rsyslogServerExtId);

    // Parent Task uuid
    if (parentTaskUuid != null) {
      deleteRemoteSyslog.setParentTaskUuid(parentTaskUuid);
    }

    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptRsyslogServerNameToDeleteRsyslogServerArg(rsyslogServerExtId, deleteRemoteSyslog, parentTaskUuid);
    }

    return deleteRemoteSyslog.build();
  }

  public static List<String> getIpFilterList(NodeDiscoveryParams discoverUnconfiguredNode) {
    if(discoverUnconfiguredNode.getAddressType() == AddressType.IPV6) {
      return ClustermgmtUtils.getIpv6AddressList(discoverUnconfiguredNode.getIpFilterList());
    }
    return ClustermgmtUtils.getIpv4AddressList(discoverUnconfiguredNode.getIpFilterList());
  }

  public String adaptDiscoverUnconfigureNodeToJsonRpcFormat(NodeDiscoveryParams discoverUnconfiguredNode, String pcTaskUuid) {
    ObjectNode objectNode = JsonUtils.getObjectNode();
    if(discoverUnconfiguredNode.getAddressType() != null)
      objectNode.put("address_type", ClustermgmtUtils.addressTypeMap.get(discoverUnconfiguredNode.getAddressType()));
    if(discoverUnconfiguredNode.getIpFilterList() != null)
      objectNode.set("ip_filter_list", JsonUtils.getObjectMapper().valueToTree(getIpFilterList(discoverUnconfiguredNode)));
    if(discoverUnconfiguredNode.getUuidFilterList() != null)
      objectNode.set("uuid_filter_list", JsonUtils.getObjectMapper().valueToTree(discoverUnconfiguredNode.getUuidFilterList()));
    if(discoverUnconfiguredNode.getTimeout() != null)
      objectNode.put("timeout", discoverUnconfiguredNode.getTimeout());
    if(discoverUnconfiguredNode.getInterfaceFilterList() != null)
      objectNode.set("interface_filter_list", JsonUtils.getObjectMapper().valueToTree(discoverUnconfiguredNode.getInterfaceFilterList()));
    if(discoverUnconfiguredNode.getIsManualDiscovery() != null)
      objectNode.put("do_manual", discoverUnconfiguredNode.getIsManualDiscovery());
    if (pcTaskUuid != null)
      objectNode.put("parent_task_uuid", pcTaskUuid);
    final String payLoad = MessageFormat.format(
      ClustermgmtUtils.GENESIS_REQUEST_PATTERN, ClustermgmtUtils.NODE_MANAGER,
      ClustermgmtUtils.GenesisOp.DISCOVER_NODES.getGenesisMethodName(),
      JsonUtils.toJsonString(objectNode));
    return payLoad;
  }

  @Override
  public String adaptNodeNetworkingDetailsToJsonRpcFormat(NodeDetails nodesNetworkingDetails,
                                                          String pcTaskUuid) {
    final ObjectNode paramDict = JsonUtils.getObjectNode();
    final ArrayNode nodeListParam =  JsonUtils.getArrayNode();
    if(nodesNetworkingDetails.getNodeList() != null) {
      for (NodeListNetworkingDetails node : nodesNetworkingDetails.getNodeList()) {
        final ObjectNode nodeItemParam = JsonUtils.getObjectNode();
        if (node.getCvmIp() != null && node.getCvmIp().hasIpv4())
          nodeItemParam.put(ClustermgmtUtils.CVM_IP, ClustermgmtUtils.getIpv4Address(node.getCvmIp()));
        if (node.getNodeUuid() != null)
          nodeItemParam.put("node_uuid", node.getNodeUuid());
        if (node.getHypervisorIp() != null && node.getHypervisorIp().hasIpv4())
          nodeItemParam.put(ClustermgmtUtils.HYPERVISOR_IP, ClustermgmtUtils.getIpv4Address(node.getHypervisorIp()));
        if (node.getIsLightCompute() != null)
          nodeItemParam.put("is_light_compute", node.getIsLightCompute());
        if (node.getIsComputeOnly() != null)
          nodeItemParam.put("is_compute_only", node.getIsComputeOnly());
        if (node.getBlockId() != null)
          nodeItemParam.put("block_id", node.getBlockId());
        if (node.getNodePosition() != null)
          nodeItemParam.put("node_position", node.getNodePosition());
        if (node.getIpmiIp() != null && node.getIpmiIp().hasIpv4())
          nodeItemParam.put(ClustermgmtUtils.IPMI_IP, ClustermgmtUtils.getIpv4Address(node.getIpmiIp()));
        if (node.getModel() != null)
          nodeItemParam.put("model", node.getModel());
        if (node.getHypervisorVersion() != null)
          nodeItemParam.put("hypervisor_version", node.getHypervisorVersion());
        if (node.getNosVersion() != null)
          nodeItemParam.put("nos_version", node.getNosVersion());
        if (node.getCurrentNetworkInterface() != null)
          nodeItemParam.put("current_network_interface", node.getCurrentNetworkInterface());
        if (node.getDigitalCertificateMapList() != null) {
          final ArrayNode jsonObjectList = JsonUtils.getArrayNode();
          for (DigitalCertificateMapReference digitalMap : node.getDigitalCertificateMapList()) {
            final ObjectNode digitalKeyValuePair = JsonUtils.getObjectNode();
            digitalKeyValuePair.put(digitalMap.getKey(), digitalMap.getValue());
            jsonObjectList.add(digitalKeyValuePair);
          }
          nodeItemParam.set("digital_certificate_map_list", jsonObjectList);
        }
        if (node.getHypervisorIp() != null && node.getHypervisorIp().hasIpv6())
          nodeItemParam.put(ClustermgmtUtils.HYPERVISOR_IPV6, ClustermgmtUtils.getIpv6Address(node.getHypervisorIp()));
        if (node.getCvmIp() != null && node.getCvmIp().hasIpv6())
          nodeItemParam.put(ClustermgmtUtils.CVM_IPV6, ClustermgmtUtils.getIpv6Address(node.getCvmIp()));
        if (node.getIpmiIp() != null && node.getIpmiIp().hasIpv6())
          nodeItemParam.put(ClustermgmtUtils.IPMI_IPV6, ClustermgmtUtils.getIpv6Address(node.getIpmiIp()));
        if (node.getHypervisorType() != null)
          nodeItemParam.put(ClustermgmtUtils.HYPERVISOR_TYPE, ClustermgmtUtils.hypervisorTypeToStringMap.get(node.getHypervisorType()));
        nodeListParam.add(nodeItemParam);
      }
      paramDict.set("node_list", nodeListParam);
    }
    if (nodesNetworkingDetails.getRequestType() != null)
      paramDict.put("request_type", nodesNetworkingDetails.getRequestType());
    if (pcTaskUuid != null)
      paramDict.put("parent_task_uuid", pcTaskUuid);
    final String payLoad = MessageFormat.format(
      ClustermgmtUtils.GENESIS_REQUEST_PATTERN, ClustermgmtUtils.CLUSTER_MANAGER,
      ClustermgmtUtils.GenesisOp.NODE_NETWORKING_DETAILS.getGenesisMethodName(),
      JsonUtils.toJsonString(paramDict));
    return payLoad;
  }

  public ObjectNode getNodeItem(NodeItem nodeItem) {
    final ObjectNode nodeItemJson = JsonUtils.getObjectNode();
    if(nodeItem.getBlockId() != null)
      nodeItemJson.put("block_id", nodeItem.getBlockId());
    if(nodeItem.getNodeUuid() != null)
      nodeItemJson.put("node_uuid", nodeItem.getNodeUuid());
    if(nodeItem.getNodePosition() != null)
      nodeItemJson.put("node_position", nodeItem.getNodePosition());
    if(nodeItem.getHypervisorType() != null)
      nodeItemJson.put(ClustermgmtUtils.HYPERVISOR_TYPE, ClustermgmtUtils.hypervisorTypeToStringMap.get(nodeItem.getHypervisorType()));
    if(nodeItem.getIsRoboMixedHypervisor() != null)
      nodeItemJson.put("robo_mixed_hypervisor", nodeItem.getIsRoboMixedHypervisor());
    if(nodeItem.getHypervisorHostname() != null)
      nodeItemJson.put("hypervisor_hostname", nodeItem.getHypervisorHostname());
    if(nodeItem.getHypervisorVersion() != null)
      nodeItemJson.put("hypervisor_version", nodeItem.getHypervisorVersion());
    if(nodeItem.getNosVersion() != null)
      nodeItemJson.put("nos_version", nodeItem.getNosVersion());
    if(nodeItem.getIsLightCompute() != null)
      nodeItemJson.put("is_light_compute", nodeItem.getIsLightCompute());
    if(nodeItem.getIpmiIp() != null && nodeItem.getIpmiIp().hasIpv4())
      nodeItemJson.put(ClustermgmtUtils.IPMI_IP, ClustermgmtUtils.getIpv4Address(nodeItem.getIpmiIp()));
    if(nodeItem.getHypervisorIp() != null && nodeItem.getHypervisorIp().hasIpv6())
      nodeItemJson.put(ClustermgmtUtils.HYPERVISOR_IPV6, ClustermgmtUtils.getIpv6Address(nodeItem.getHypervisorIp()));
    if(nodeItem.getCvmIp() != null && nodeItem.getCvmIp().hasIpv6())
      nodeItemJson.put(ClustermgmtUtils.CVM_IPV6, ClustermgmtUtils.getIpv6Address(nodeItem.getCvmIp()));
    if(nodeItem.getCvmIp() != null && nodeItem.getCvmIp().hasIpv4())
      nodeItemJson.put(ClustermgmtUtils.CVM_IP, ClustermgmtUtils.getIpv4Address(nodeItem.getCvmIp()));
    if(nodeItem.getHypervisorIp() != null && nodeItem.getHypervisorIp().hasIpv4())
      nodeItemJson.put(ClustermgmtUtils.HYPERVISOR_IP, ClustermgmtUtils.getIpv4Address(nodeItem.getHypervisorIp()));
    if(nodeItem.getModel() != null)
      nodeItemJson.put("model", nodeItem.getModel());
    if(nodeItem.getCurrentNetworkInterface() != null)
      nodeItemJson.put("current_network_interface", nodeItem.getCurrentNetworkInterface());
    if (nodeItem.getDigitalCertificateMapList() != null) {
      final ArrayNode jsonObjectList = JsonUtils.getArrayNode();
      for (DigitalCertificateMapReference digitalMap : nodeItem.getDigitalCertificateMapList()) {
        final ObjectNode digitalKeyValuePair = JsonUtils.getObjectNode();
        digitalKeyValuePair.put(digitalMap.getKey(), digitalMap.getValue());
        jsonObjectList.add(digitalKeyValuePair);
      }
      nodeItemJson.set("digital_certificate_map_list", jsonObjectList);
    }
    if(nodeItem.getNetworks() != null) {
      nodeItemJson.set("networks", getListOfUplinkNetworks(nodeItem.getNetworks()));
    }
    return nodeItemJson;
  }

  public ObjectNode getValidateBundleNodeItem(NodeInfo nodeItem) {
    final ObjectNode validateBundleNodeItemJson = JsonUtils.getObjectNode();
    if(nodeItem.getBlockId() != null)
      validateBundleNodeItemJson.put("block_id", nodeItem.getBlockId());
    if(nodeItem.getNodeUuid() != null)
      validateBundleNodeItemJson.put("node_uuid", nodeItem.getNodeUuid());
    if(nodeItem.getNodePosition() != null)
      validateBundleNodeItemJson.put("node_position", nodeItem.getNodePosition());
    if(nodeItem.getHypervisorType() != null)
      validateBundleNodeItemJson.put(ClustermgmtUtils.HYPERVISOR_TYPE, ClustermgmtUtils.hypervisorTypeToStringMap.get(nodeItem.getHypervisorType()));
    if(nodeItem.getIsRoboMixedHypervisor() != null)
      validateBundleNodeItemJson.put("robo_mixed_hypervisor", nodeItem.getIsRoboMixedHypervisor());
    if(nodeItem.getHypervisorHostname() != null)
      validateBundleNodeItemJson.put("hypervisor_hostname", nodeItem.getHypervisorHostname());
    if(nodeItem.getHypervisorVersion() != null)
      validateBundleNodeItemJson.put("hypervisor_version", nodeItem.getHypervisorVersion());
    if(nodeItem.getNosVersion() != null)
      validateBundleNodeItemJson.put("nos_version", nodeItem.getNosVersion());
    if(nodeItem.getIsLightCompute() != null)
      validateBundleNodeItemJson.put("is_light_compute", nodeItem.getIsLightCompute());
    if(nodeItem.getIpmiIp() != null && nodeItem.getIpmiIp().hasIpv4())
      validateBundleNodeItemJson.put(ClustermgmtUtils.IPMI_IP, ClustermgmtUtils.getIpv4Address(nodeItem.getIpmiIp()));
    if(nodeItem.getHypervisorIp() != null && nodeItem.getHypervisorIp().hasIpv6())
      validateBundleNodeItemJson.put(ClustermgmtUtils.HYPERVISOR_IPV6, ClustermgmtUtils.getIpv6Address(nodeItem.getHypervisorIp()));
    if(nodeItem.getCvmIp() != null && nodeItem.getCvmIp().hasIpv6())
      validateBundleNodeItemJson.put(ClustermgmtUtils.CVM_IPV6, ClustermgmtUtils.getIpv6Address(nodeItem.getCvmIp()));
    if(nodeItem.getCvmIp() != null && nodeItem.getCvmIp().hasIpv4())
      validateBundleNodeItemJson.put(ClustermgmtUtils.CVM_IP, ClustermgmtUtils.getIpv4Address(nodeItem.getCvmIp()));
    if(nodeItem.getHypervisorIp() != null && nodeItem.getHypervisorIp().hasIpv4())
      validateBundleNodeItemJson.put(ClustermgmtUtils.HYPERVISOR_IP, ClustermgmtUtils.getIpv4Address(nodeItem.getHypervisorIp()));
    if(nodeItem.getModel() != null)
      validateBundleNodeItemJson.put("model", nodeItem.getModel());
    if(nodeItem.getCurrentNetworkInterface() != null)
      validateBundleNodeItemJson.put("current_network_interface", nodeItem.getCurrentNetworkInterface());
    if (nodeItem.getDigitalCertificateMapList() != null) {
      final ArrayNode jsonObjectList = JsonUtils.getArrayNode();
      for (DigitalCertificateMapReference digitalMap : nodeItem.getDigitalCertificateMapList()) {
        final ObjectNode digitalKeyValuePair = JsonUtils.getObjectNode();
        digitalKeyValuePair.put(digitalMap.getKey(), digitalMap.getValue());
        jsonObjectList.add(digitalKeyValuePair);
      }
      validateBundleNodeItemJson.set("digital_certificate_map_list", jsonObjectList);
    }
    return validateBundleNodeItemJson;
  }

  @Override
  public String adaptAddNodeToJsonRpcFormat(ExpandClusterParams addNode, String pcTaskUuid) {
    final ObjectNode paramDict = JsonUtils.getObjectNode();
    if (addNode.getShouldSkipAddNode() != null)
      paramDict.put("skip_add_node", addNode.getShouldSkipAddNode());
    if (addNode.getShouldSkipPreExpandChecks() != null)
      paramDict.put("skip_pre_expand_checks", addNode.getShouldSkipPreExpandChecks());
    if (addNode.getNodeParams() != null) {
      final ObjectNode nodeParamJson = JsonUtils.getObjectNode();
      NodeParam nodeParam = addNode.getNodeParams();
      if (nodeParam.getNodeList() != null) {
        ArrayNode nodeJsonList = JsonUtils.getArrayNode();
        for (NodeItem nodeItem : nodeParam.getNodeList()) {
          final ObjectNode nodeItemJson = getNodeItem(nodeItem);
          nodeJsonList.add(nodeItemJson);
        }
        nodeParamJson.set("node_list", nodeJsonList);
      }
      if(nodeParam.getBlockList() != null) {
        ArrayNode blockJsonList = JsonUtils.getArrayNode();
        for (BlockItem blockItem : nodeParam.getBlockList()) {
          final ObjectNode blockItemJson = JsonUtils.getObjectNode();
          if(blockItem.getBlockId() != null)
            blockItemJson.put("block_id", blockItem.getBlockId());
          if(blockItem.getRackName() != null)
            blockItemJson.put("rack_name", blockItem.getRackName());
          blockJsonList.add(blockItemJson);
        }
        nodeParamJson.set("block_list", blockJsonList);
      }
      if(nodeParam.getHypervSku() != null)
        nodeParamJson.put("hyperv_sku", nodeParam.getHypervSku());
      if(nodeParam.getHypervisorIsos() != null) {
        final ObjectNode hypervisorIsoParamDict = JsonUtils.getObjectNode();
        for (HypervisorIsoMap hypervisorIsoMap: nodeParam.getHypervisorIsos()) {
          hypervisorIsoParamDict.put(ClustermgmtUtils.hypervisorTypeToStringMap.get(hypervisorIsoMap.getType()), hypervisorIsoMap.getMd5Sum());
        }
        nodeParamJson.set("hypervisor_isos", hypervisorIsoParamDict);
      }
      if(nodeParam.getBundleInfo() != null) {
        final ObjectNode bundleInfoParam = JsonUtils.getObjectNode();
        if (nodeParam.getBundleInfo().getName() != null)
          bundleInfoParam.put("name", nodeParam.getBundleInfo().getName());
        nodeParamJson.set("bundle_info", bundleInfoParam);
      }
      if(nodeParam.getShouldSkipHostNetworking() != null) {
        nodeParamJson.put("skip_host_networking", nodeParam.getShouldSkipHostNetworking());
      }
      if(nodeParam.getComputeNodeList() != null) {
        ArrayNode computeJsonList = JsonUtils.getArrayNode();
        for (ComputeNodeItem computeNodeItem : nodeParam.getComputeNodeList()) {
          final ObjectNode computeItemJson = JsonUtils.getObjectNode();
          if(computeNodeItem.getBlockId() != null)
            computeItemJson.put("block_id", computeNodeItem.getBlockId());
          if(computeNodeItem.getNodeUuid() != null)
            computeItemJson.put("node_uuid", computeNodeItem.getNodeUuid());
          if(computeNodeItem.getNodePosition() != null)
            computeItemJson.put("node_position", computeNodeItem.getNodePosition());
          if(computeNodeItem.getHypervisorIp() != null && computeNodeItem.getHypervisorIp().hasIpv4())
            computeItemJson.put(ClustermgmtUtils.HYPERVISOR_IP, ClustermgmtUtils.getIpv4Address(computeNodeItem.getHypervisorIp()));
          if(computeNodeItem.getIpmiIp() != null && computeNodeItem.getIpmiIp().hasIpv4())
            computeItemJson.put(ClustermgmtUtils.IPMI_IP, ClustermgmtUtils.getIpv4Address(computeNodeItem.getIpmiIp()));
          if(computeNodeItem.getHypervisorHostname() != null)
            computeItemJson.put("hypervisor_hostname", computeNodeItem.getHypervisorHostname());
          if(computeNodeItem.getModel() != null)
            computeItemJson.put("model", computeNodeItem.getModel());
          if (computeNodeItem.getDigitalCertificateMapList() != null) {
            final ArrayNode jsonObjectList = JsonUtils.getArrayNode();
            for (DigitalCertificateMapReference digitalMap : computeNodeItem.getDigitalCertificateMapList()) {
              final ObjectNode digitalKeyValuePair = JsonUtils.getObjectNode();
              digitalKeyValuePair.put(digitalMap.getKey(), digitalMap.getValue());
              jsonObjectList.add(digitalKeyValuePair);
            }
            computeItemJson.set("digital_certificate_map_list", jsonObjectList);
          }
          computeJsonList.add(computeItemJson);
        }
        nodeParamJson.set("compute_node_list", computeJsonList);
      }
      paramDict.set("node_params", nodeParamJson);
    }
    if(addNode.getConfigParams() != null) {
      ConfigParams extraParam = addNode.getConfigParams();
      final ObjectNode extraParamJson = JsonUtils.getObjectNode();
      if(extraParam.getIsComputeOnly() != null)
        extraParamJson.put("compute_only", extraParam.getIsComputeOnly());
      if(extraParam.getShouldSkipDiscovery() != null)
        extraParamJson.put("skip_discovery", extraParam.getShouldSkipDiscovery());
      if(extraParam.getShouldSkipImaging() != null)
        extraParamJson.put("skip_imaging", extraParam.getShouldSkipImaging());
      if(extraParam.getShouldValidateRackAwareness() != null)
        extraParamJson.put("validate_ra", extraParam.getShouldValidateRackAwareness());
      if(extraParam.getIsNosCompatible() != null)
        extraParamJson.put("is_nos_compatible", extraParam.getIsNosCompatible());
      if(extraParam.getIsNeverScheduleable() != null)
        extraParamJson.put("never_scheduleable", extraParam.getIsNeverScheduleable());
      if(extraParam.getTargetHypervisor() != null)
        extraParamJson.put("target_hypervisor", extraParam.getTargetHypervisor());
      if(extraParam.getHyperv() != null) {
        final ObjectNode hypervJsonDict = JsonUtils.getObjectNode();
        HypervCredentials hyperVCredAddNode = extraParam.getHyperv();
        if(hyperVCredAddNode.getDomainDetails() != null) {
          final ObjectNode domainDetailJsonDict = JsonUtils.getObjectNode();
          String userName = hyperVCredAddNode.getDomainDetails().getUserName();
          String password = hyperVCredAddNode.getDomainDetails().getPassword();
          domainDetailJsonDict.put("username", userName);
          domainDetailJsonDict.put("password", password);
          hypervJsonDict.set("domain_details", domainDetailJsonDict);
        }
        if(hyperVCredAddNode.getFailoverClusterDetails() != null) {
          final ObjectNode failoverDetailJsonDict = JsonUtils.getObjectNode();
          String userName = hyperVCredAddNode.getFailoverClusterDetails().getUserName();
          String password = hyperVCredAddNode.getFailoverClusterDetails().getPassword();
          String clusterName = hyperVCredAddNode.getFailoverClusterDetails().getClusterName();
          failoverDetailJsonDict.put("username", userName);
          failoverDetailJsonDict.put("password", password);
          failoverDetailJsonDict.put("cluster_name", clusterName);
          hypervJsonDict.set("failover_cluster_details", failoverDetailJsonDict);
        }
        extraParamJson.set("hyperv", hypervJsonDict);
      }
      paramDict.set("extra_params", extraParamJson);
    }
    if (pcTaskUuid != null)
      paramDict.put("parent_task_uuid", pcTaskUuid);
    final String payLoad = MessageFormat.format(
      ClustermgmtUtils.GENESIS_REQUEST_PATTERN, ClustermgmtUtils.CLUSTER_MANAGER,
      ClustermgmtUtils.GenesisOp.EXPAND_CLUSTER.getGenesisMethodName(),
      JsonUtils.toJsonString(paramDict));
    return payLoad;
  }

  @Override
  public String adaptHypervisorUploadParamToJsonRpcFormat(HypervisorUploadParam hypervisorUploadParam, String pcTaskUuid) {
    final ObjectNode paramDict = JsonUtils.getObjectNode();
    ArrayNode nodeJsonList = JsonUtils.getArrayNode();
    for(HypervisorUploadNodeListItem hypervisorUploadNodeListItem : hypervisorUploadParam.getNodeList()) {
      final ObjectNode nodeJsonItem = JsonUtils.getObjectNode();
      if(hypervisorUploadNodeListItem.getNodeUuid() != null)
        nodeJsonItem.put("node_uuid", hypervisorUploadNodeListItem.getNodeUuid());
      if(hypervisorUploadNodeListItem.getHypervisorVersion() != null)
        nodeJsonItem.put("hypervisor_version", hypervisorUploadNodeListItem.getHypervisorVersion());
      if(hypervisorUploadNodeListItem.getNosVersion() != null)
        nodeJsonItem.put("nos_version", hypervisorUploadNodeListItem.getNosVersion());
      if(hypervisorUploadNodeListItem.getModel() != null)
        nodeJsonItem.put("model", hypervisorUploadNodeListItem.getModel());
      if(hypervisorUploadNodeListItem.getBlockId() != null)
        nodeJsonItem.put("block_id", hypervisorUploadNodeListItem.getBlockId());
      if(hypervisorUploadNodeListItem.getIsLightCompute() != null)
        nodeJsonItem.put("is_light_compute", hypervisorUploadNodeListItem.getIsLightCompute());
      if(hypervisorUploadNodeListItem.getHypervisorType() != null)
        nodeJsonItem.put(ClustermgmtUtils.HYPERVISOR_TYPE, ClustermgmtUtils.hypervisorTypeToStringMap.get(hypervisorUploadNodeListItem.getHypervisorType()));
      if(hypervisorUploadNodeListItem.getIsRoboMixedHypervisor() != null)
        nodeJsonItem.put("robo_mixed_hypervisor", hypervisorUploadNodeListItem.getIsRoboMixedHypervisor());
      if(hypervisorUploadNodeListItem.getIsMinimumComputeNode() != null)
        nodeJsonItem.put("minimal_compute_node", hypervisorUploadNodeListItem.getIsMinimumComputeNode());
      nodeJsonList.add(nodeJsonItem);
    }
    paramDict.set("node_list", nodeJsonList);
    if (pcTaskUuid != null)
      paramDict.put("parent_task_uuid", pcTaskUuid);
    final String payLoad = MessageFormat.format(
      ClustermgmtUtils.GENESIS_REQUEST_PATTERN, ClustermgmtUtils.CLUSTER_MANAGER,
      ClustermgmtUtils.GenesisOp.IS_HYPERVISOR_UPLOAD_REQUIRED.getGenesisMethodName(),
      JsonUtils.toJsonString(paramDict));
    return payLoad;
  }

  @Override
  public String adaptIsBundleCompatibleToJsonRpcFormat(BundleParam validateBundleParam, String pcTaskUuid) {
    final ObjectNode paramDict = JsonUtils.getObjectNode();
    final ObjectNode nodeparamDict = JsonUtils.getObjectNode();
    if (validateBundleParam.getBundleInfo() != null) {
      final ObjectNode bundleInfoDict = JsonUtils.getObjectNode();
      BundleInfo validateBundleInfo = validateBundleParam.getBundleInfo();
      if (validateBundleInfo.getName() != null)
        bundleInfoDict.put("name", validateBundleInfo.getName());
      nodeparamDict.set("bundle_info", bundleInfoDict);
    }
    ArrayNode nodeParamDictList = JsonUtils.getArrayNode();
    if (validateBundleParam.getNodeList() != null) {
      for (NodeInfo nodeItem : validateBundleParam.getNodeList()) {
        final ObjectNode nodeItemJson = getValidateBundleNodeItem(nodeItem);
        nodeParamDictList.add(nodeItemJson);
      }
      nodeparamDict.set("node_list", nodeParamDictList);
    }
    paramDict.set("node_params", nodeparamDict);
    if (pcTaskUuid != null)
      paramDict.put("parent_task_uuid", pcTaskUuid);
    final String payLoad = MessageFormat.format(
      ClustermgmtUtils.GENESIS_REQUEST_PATTERN, ClustermgmtUtils.CLUSTER_MANAGER,
      ClustermgmtUtils.GenesisOp.IS_BUNDLE_COMPATIBLE.getGenesisMethodName(),
      JsonUtils.toJsonString(paramDict));
    return payLoad;
  }

  @Override
  public String adaptRemoveNodeToJsonRpcFormat(NodeRemovalParams removeNode, String pcTaskUuid) {
    final ObjectNode paramDict = JsonUtils.getObjectNode();
    if (removeNode.getShouldSkipRemove() != null)
      paramDict.put("skip_remove", removeNode.getShouldSkipRemove());
    if (removeNode.getShouldSkipPrechecks() != null)
      paramDict.put("skip_prechecks", removeNode.getShouldSkipPrechecks());
    if (removeNode.getNodeUuids() != null)
      paramDict.set("node_list", JsonUtils.getObjectMapper().valueToTree(removeNode.getNodeUuids()));
    if (removeNode.getExtraParams() != null) {
      final ObjectNode extraParam = JsonUtils.getObjectNode();
      NodeRemovalExtraParam removeNodeExtraParam = removeNode.getExtraParams();
      if(removeNodeExtraParam.getShouldSkipAddCheck() != null)
        extraParam.put("skip_add_check", removeNodeExtraParam.getShouldSkipAddCheck());
      if(removeNodeExtraParam.getShouldSkipSpaceCheck() != null)
        extraParam.put("skip_space_checks", removeNodeExtraParam.getShouldSkipSpaceCheck());
      if(removeNodeExtraParam.getShouldSkipUpgradeCheck() != null)
        extraParam.put("skip_upgrade_check", removeNodeExtraParam.getShouldSkipUpgradeCheck());
      paramDict.set("extra_params", extraParam);
    }
    if (pcTaskUuid != null)
      paramDict.put("parent_task_uuid", pcTaskUuid);
    final String payLoad = MessageFormat.format(
      ClustermgmtUtils.GENESIS_REQUEST_PATTERN, ClustermgmtUtils.CLUSTER_MANAGER,
      ClustermgmtUtils.GenesisOp.REMOVE_NODES.getGenesisMethodName(),
      JsonUtils.toJsonString(paramDict));
    return payLoad;
  }

  public ArrayNode getListOfUplinkNetworks(List<UplinkNetworkItem> uplinkNodeItemList) {
    ArrayNode networkParamDictList = JsonUtils.getArrayNode();
    for(UplinkNetworkItem networkItem : uplinkNodeItemList) {
      final ObjectNode networkParamDict = JsonUtils.getObjectNode();
      if(networkItem.getName() != null)
        networkParamDict.put("name", networkItem.getName());
      if(networkItem.getNetworks() != null) {
        networkParamDict.set("networks", JsonUtils.getObjectMapper().valueToTree(networkItem.getNetworks()));
      }
      if(networkItem.getUplinks() != null) {
        final ObjectNode uplinkDict = JsonUtils.getObjectNode();
        Uplinks uplinks = networkItem.getUplinks();
        if(uplinks.getActive() != null) {
          ArrayNode activeUplinkDictList = JsonUtils.getArrayNode();
          for(UplinksField uplinksField: uplinks.getActive()) {
            final ObjectNode uplinkFieldItem = JsonUtils.getObjectNode();
            if(uplinksField.getMac() != null)
              uplinkFieldItem.put("mac", uplinksField.getMac());
            if(uplinksField.getName() != null)
              uplinkFieldItem.put("name", uplinksField.getName());
            if(uplinksField.getValue() != null)
              uplinkFieldItem.put("value", uplinksField.getValue());
            activeUplinkDictList.add(uplinkFieldItem);
          }
          uplinkDict.set("active", activeUplinkDictList);
        }
        if(uplinks.getStandby() != null) {
          ArrayNode standByUplinkDictList = JsonUtils.getArrayNode();
          for(UplinksField uplinksField: uplinks.getStandby()) {
            final ObjectNode uplinkFieldItem = JsonUtils.getObjectNode();
            if(uplinksField.getMac() != null)
              uplinkFieldItem.put("mac", uplinksField.getMac());
            if(uplinksField.getName() != null)
              uplinkFieldItem.put("name", uplinksField.getName());
            if(uplinksField.getValue() != null)
              uplinkFieldItem.put("value", uplinksField.getValue());
            standByUplinkDictList.add(uplinkFieldItem);
          }
          uplinkDict.set("standby", standByUplinkDictList);
        }
        networkParamDict.set("uplinks", uplinkDict);
      }
      networkParamDictList.add(networkParamDict);
    }
    return networkParamDictList;
  }

  @Override
  public String adaptValidateUplinkToJsonRpcFormat(List<UplinkNode> nodeItems, String pcTaskUuid) {
    final ObjectNode paramDict = JsonUtils.getObjectNode();
    ArrayNode nodeParamDictList = JsonUtils.getArrayNode();
    for(UplinkNode nodeItem : nodeItems) {
      final ObjectNode nodeParamDict = JsonUtils.getObjectNode();
      if(nodeItem.getCvmIp() != null && nodeItem.getCvmIp().hasIpv4())
        nodeParamDict.put(ClustermgmtUtils.CVM_IP, ClustermgmtUtils.getIpv4Address(nodeItem.getCvmIp()));
      if(nodeItem.getHypervisorIp() != null && nodeItem.getHypervisorIp().hasIpv4())
        nodeParamDict.put(ClustermgmtUtils.HYPERVISOR_IP, ClustermgmtUtils.getIpv4Address(nodeItem.getHypervisorIp()));
      if(nodeItem.getNetworks() != null) {
        ArrayNode networkParamDictList = getListOfUplinkNetworks(nodeItem.getNetworks());
        nodeParamDict.set("networks", networkParamDictList);
      }
      nodeParamDictList.add(nodeParamDict);
    }
    paramDict.set("node_list", nodeParamDictList);

    if (pcTaskUuid != null)
      paramDict.put("parent_task_uuid", pcTaskUuid);
    final String payLoad = MessageFormat.format(
      ClustermgmtUtils.GENESIS_REQUEST_PATTERN, ClustermgmtUtils.CLUSTER_MANAGER,
      ClustermgmtUtils.GenesisOp.VALIDATE_UPLINKS.getGenesisMethodName(),
      JsonUtils.toJsonString(paramDict));
    return payLoad;
  }

  public List<String> convertArrayNodeToStringList(ArrayNode node) {
    List<String> ret = new ArrayList<>();
    for (JsonNode jsonNode : node) {
      ret.add(jsonNode.asText());
    }
    return ret;
  }

  @Override
  public UnconfigureNodeDetails adaptJsonResponsetoUnconfiguredNodes(JsonNode jsonObject,
                                                                     UnconfigureNodeDetails unconfigureNodesResponse) {
    ArrayNode undiscoveredNodes = (ArrayNode)jsonObject.get("discovered_nodes");
    List<UnconfiguredNodeListItem> unconfiguredNodeListItems = new ArrayList<>();
    for(JsonNode node : undiscoveredNodes) {
      UnconfiguredNodeListItem unconfiguredNodeListItem = new UnconfiguredNodeListItem();
      if (node.get("foundation_version") != null)
        unconfiguredNodeListItem.setFoundationVersion(node.get("foundation_version").asText());
      if (node.get("rackable_unit_serial") != null)
        unconfiguredNodeListItem.setRackableUnitSerial(node.get("rackable_unit_serial").asText());
      if (node.get("node_uuid") != null)
        unconfiguredNodeListItem.setNodeUuid(node.get("node_uuid").asText());
      if (node.get("rackable_unit_max_nodes") != null)
        unconfiguredNodeListItem.setRackableUnitMaxNodes(node.get("rackable_unit_max_nodes").asLong());
      if (node.get("current_network_interface") != null)
        unconfiguredNodeListItem.setCurrentNetworkInterface(node.get("current_network_interface").asText());
      if (node.get("node_position") != null)
        unconfiguredNodeListItem.setNodePosition(node.get("node_position").asText());
      if (node.get("ip") != null)
        unconfiguredNodeListItem.setInterfaceIpv6(node.get("ip").asText());
      if (node.get("current_cvm_vlan_tag") != null)
        unconfiguredNodeListItem.setCurrentCvmVlanTag(node.get("current_cvm_vlan_tag").asText());
      if (node.get("is_secure_booted") != null)
        unconfiguredNodeListItem.setIsSecureBooted(node.get("is_secure_booted").asBoolean());
      if (node.get("nos_version") != null)
        unconfiguredNodeListItem.setNosVersion(node.get("nos_version").asText());
      if (node.get("cluster_id") != null)
        unconfiguredNodeListItem.setClusterId(node.get("cluster_id").asText());
      if (node.get("hypervisor") != null) {
        unconfiguredNodeListItem.setHypervisorType(ClustermgmtUtils.getKey(ClustermgmtUtils.hypervisorTypeToStringMap,
          (node.get("hypervisor").asText())));
      }
      if (node.get("hypervisor_version") != null)
        unconfiguredNodeListItem.setHypervisorVersion(node.get("hypervisor_version").asText());
      if (node.get("rackable_unit_model") != null)
        unconfiguredNodeListItem.setRackableUnitModel(node.get("rackable_unit_model").asText());
      if (node.get("arch") != null)
        unconfiguredNodeListItem.setArch(node.get("arch").asText());
      if (node.get("cpu_type") != null)
        unconfiguredNodeListItem.setCpuType(convertArrayNodeToStringList((ArrayNode) node.get("cpu_type")));
      if (node.get("attributes") != null) {
        UnconfiguredNodeAttributeMap unconfiguredNodeAttributeMap = new UnconfiguredNodeAttributeMap();
        JsonNode attributeObj = node.get("attributes");
        if (attributeObj.get("lcm_family") != null)
          unconfiguredNodeAttributeMap.setLcmFamily(attributeObj.get("lcm_family").asText());
        if (attributeObj.get("is_model_supported") != null)
          unconfiguredNodeAttributeMap.setIsModelSupported(attributeObj.get("is_model_supported").asBoolean());
        if (attributeObj.get("default_workload") != null)
          unconfiguredNodeAttributeMap.setDefaultWorkload(attributeObj.get("default_workload").asText());
        if (attributeObj.get("robo_mixed_hypervisor") != null)
          unconfiguredNodeAttributeMap.setIsRoboMixedHypervisor(attributeObj.get("robo_mixed_hypervisor").asBoolean());
        if (attributeObj.get("maybe_1GbE_only") != null)
          unconfiguredNodeAttributeMap.setShouldWorkWith1GNic(attributeObj.get("maybe_1GbE_only").asBoolean());
        unconfiguredNodeListItem.setAttributes(unconfiguredNodeAttributeMap);
      }
      IPAddress hypervisorIp = new IPAddress();
      if (node.get(ClustermgmtUtils.HYPERVISOR_IP) != null)
        hypervisorIp.setIpv4(ClustermgmtUtils.createIpv4Address(node.get(ClustermgmtUtils.HYPERVISOR_IP).asText()));
      if (node.get(ClustermgmtUtils.HYPERVISOR_IPV6) != null)
        hypervisorIp.setIpv6(ClustermgmtUtils.createIpv6Address(node.get(ClustermgmtUtils.HYPERVISOR_IPV6).asText()));
      if(hypervisorIp.isValid())
        unconfiguredNodeListItem.setHypervisorIp(hypervisorIp);

      IPAddress ipmiIp = new IPAddress();
      if (node.get(ClustermgmtUtils.IPMI_IP) != null)
        ipmiIp.setIpv4(ClustermgmtUtils.createIpv4Address(node.get(ClustermgmtUtils.IPMI_IP).asText()));
      if (node.get(ClustermgmtUtils.IPMI_IPV6) != null)
        ipmiIp.setIpv6(ClustermgmtUtils.createIpv6Address(node.get(ClustermgmtUtils.IPMI_IPV6).asText()));
      if(ipmiIp.isValid())
        unconfiguredNodeListItem.setIpmiIp(ipmiIp);

      IPAddress cvmIp = new IPAddress();
      if (node.get("svm_ip") != null)
        cvmIp.setIpv4(ClustermgmtUtils.createIpv4Address(node.get("svm_ip").asText()));
      if (node.get(ClustermgmtUtils.CVM_IPV6) != null)
        cvmIp.setIpv6(ClustermgmtUtils.createIpv6Address(node.get(ClustermgmtUtils.CVM_IPV6).asText()));
      if(cvmIp.isValid())
        unconfiguredNodeListItem.setCvmIp(cvmIp);

      if (node.get("node_serial") != null)
        unconfiguredNodeListItem.setNodeSerialNumber(node.get("node_serial").asText());
      if (node.get("host_name") != null)
        unconfiguredNodeListItem.setHostName(node.get("host_name").asText());
      if (node.get("host_type") != null){
        String host_type = node.get("host_type").asText();
        HostTypeEnum hostType = HostTypeEnum.STORAGE_ONLY;
        if(host_type.equals("HYPER_CONVERGED"))
          hostType = HostTypeEnum.HYPER_CONVERGED;
        unconfiguredNodeListItem.setHostType(hostType); 
      }
      if (node.get("one_node_cluster_support") != null)
        unconfiguredNodeListItem.setIsOneNodeClusterSupported(Boolean.parseBoolean(node.get("one_node_cluster_support").asText()));
      if (node.get("two_node_cluster_support") != null)
        unconfiguredNodeListItem.setIsTwoNodeClusterSupported(Boolean.parseBoolean(node.get("two_node_cluster_support").asText()));

      unconfiguredNodeListItems.add(unconfiguredNodeListItem);
    }
    unconfigureNodesResponse.setNodeList(unconfiguredNodeListItems);
    return unconfigureNodesResponse;
  }

  @Override
  public NodeNetworkingDetails adaptJsonResponsetoNetworkingDetails(JsonNode jsonObject,
                                                                    NodeNetworkingDetails networkingDetailResponsee) {
    if(jsonObject.get("network_info") != null) {
      NetworkInfo networkInfoObj = new NetworkInfo();
      JsonNode networkInfo = jsonObject.get("network_info");
      if(networkInfo.get("HCI") != null) {
        List<NameNetworkRef> nameNetworkRefList = new ArrayList<>();
        ArrayNode HCInetworkInfo = (ArrayNode)networkInfo.get("HCI");
        for(JsonNode HCInetworkItem: HCInetworkInfo) {
          NameNetworkRef nameNetworkRef = new NameNetworkRef();
          if(HCInetworkItem.get(ClustermgmtUtils.HYPERVISOR_TYPE) != null)
            nameNetworkRef.setHypervisorType(ClustermgmtUtils.LongTohypervisorTypeMap.get(HCInetworkItem.get(ClustermgmtUtils.HYPERVISOR_TYPE).asLong()));
          if(HCInetworkItem.get("name") != null)
            nameNetworkRef.setName(HCInetworkItem.get("name").asText());
          if(HCInetworkItem.get("networks") != null)
            nameNetworkRef.setNetworks(convertArrayNodeToStringList((ArrayNode) HCInetworkItem.get("networks")));
          nameNetworkRefList.add(nameNetworkRef);
        }
        networkInfoObj.setHci(nameNetworkRefList);
      }

      if(networkInfo.get("SO") != null) {
        List<NameNetworkRef> nameNetworkRefList = new ArrayList<>();
        ArrayNode SOnetworkInfo = (ArrayNode)networkInfo.get("HCI");
        for(JsonNode SOnetworkItem: SOnetworkInfo) {
          NameNetworkRef nameNetworkRef = new NameNetworkRef();
          if(SOnetworkItem.get(ClustermgmtUtils.HYPERVISOR_TYPE) != null)
            nameNetworkRef.setHypervisorType(ClustermgmtUtils.LongTohypervisorTypeMap.get(SOnetworkItem.get(ClustermgmtUtils.HYPERVISOR_TYPE).asLong()));
          if(SOnetworkItem.get("name") != null)
            nameNetworkRef.setName(SOnetworkItem.get("name").asText());
          if(SOnetworkItem.get("networks") != null)
            nameNetworkRef.setNetworks(convertArrayNodeToStringList((ArrayNode) SOnetworkItem.get("networks")));
          nameNetworkRefList.add(nameNetworkRef);
        }
        networkInfoObj.setSo(nameNetworkRefList);
      }
      networkingDetailResponsee.setNetworkInfo(networkInfoObj);
    }
    if(jsonObject.get("uplinks") != null) {
      List<UplinkInfo> uplinkInfoList = new ArrayList<>();
      JsonNode uplinks = jsonObject.get("uplinks");
      Iterator<String> cvmIpList = uplinks.fieldNames();
      while (cvmIpList.hasNext()) {
        String cvmIp = cvmIpList.next();
        UplinkInfo uplinkInfo = new UplinkInfo();
        uplinkInfo.setCvmIp(ClustermgmtUtils.createIpv4Ipv6Address(cvmIp));
        ArrayNode values = (ArrayNode) uplinks.get(cvmIp);
        List<NameMacRef> nameMacRefList = new ArrayList<>();
        for (JsonNode value : values) {
          NameMacRef nameMacRef = new NameMacRef();
          if(value.get("name") != null)
            nameMacRef.setName(value.get("name").asText());
          if(value.get("mac") != null)
            nameMacRef.setMac(value.get("mac").asText());
          nameMacRefList.add(nameMacRef);
        }
        uplinkInfo.setUplinkList(nameMacRefList);
        uplinkInfoList.add(uplinkInfo);
      }
      networkingDetailResponsee.setUplinks(uplinkInfoList);
    }
    if(jsonObject.get("warnings") != null) {
      networkingDetailResponsee.setWarnings(convertArrayNodeToStringList((ArrayNode) jsonObject.get("warnings")));
    }
    return networkingDetailResponsee;
  }

  @Override
  public HypervisorUploadInfo adaptJsonResponsetoHypervisorIso(JsonNode jsonObject,
                                                                       HypervisorUploadInfo hypervisorUploadInfoResponse) {
    if(jsonObject.get("msg") != null)
      hypervisorUploadInfoResponse.setErrorMessage(jsonObject.get("msg").asText());
    if(jsonObject.get("node_list") != null){
      ArrayNode nodeList = (ArrayNode)jsonObject.get("node_list");
      List<UploadInfoNodeItem> uploadInfoNodeItems = new ArrayList<>();
      for(JsonNode node: nodeList) {
        UploadInfoNodeItem uploadInfoNodeItem = new UploadInfoNodeItem();
        if (node.get("hypervisor_upload_required") != null)
          uploadInfoNodeItem.setIsHypervisorUploadRequired(node.get("hypervisor_upload_required").asBoolean());
        if (node.get("is_imaging_mandatory") != null)
          uploadInfoNodeItem.setIsImagingMandatory(node.get("is_imaging_mandatory").asBoolean());
        if (node.get("is_node_compatible") != null)
          uploadInfoNodeItem.setIsNodeCompatible(node.get("is_node_compatible").asBoolean());
        if (node.get("node_uuid") != null)
          uploadInfoNodeItem.setNodeUuid(node.get("node_uuid").asText());
        if (node.get("available_hypervisor_iso_error") != null)
          uploadInfoNodeItem.setAvailableHypervisorIsoError(node.get("available_hypervisor_iso_error").asText());
        if (node.get("required_hypervisor") != null) {
          uploadInfoNodeItem.setRequiredHypervisorType(ClustermgmtUtils.getKey(ClustermgmtUtils.hypervisorTypeToStringMap,
            node.get("required_hypervisor").asText()));
        }
        if (node.get("hypervisor_md5sum") != null)
          uploadInfoNodeItem.setMd5Sum(node.get("hypervisor_md5sum").asText());
        if (node.get("bundle_name") != null)
          uploadInfoNodeItem.setBundleName(node.get("bundle_name").asText());
        uploadInfoNodeItems.add(uploadInfoNodeItem);
      }
      hypervisorUploadInfoResponse.setUploadInfoNodeList(uploadInfoNodeItems);
    }
    return hypervisorUploadInfoResponse;
  }

}
