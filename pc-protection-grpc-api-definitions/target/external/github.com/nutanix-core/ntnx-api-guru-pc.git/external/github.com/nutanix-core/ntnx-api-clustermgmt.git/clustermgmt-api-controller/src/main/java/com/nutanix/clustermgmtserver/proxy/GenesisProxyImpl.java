/*
 * Copyright (c) 2021 Nutanix Inc. All rights reserved.
 *
 * Author: rakesh.falak@nutanix.com
 */

package com.nutanix.clustermgmtserver.proxy;

import com.nutanix.clustermgmtserver.exceptions.ClustermgmtGenericException;
import com.nutanix.clustermgmtserver.exceptions.ClustermgmtServiceException;
import com.nutanix.clustermgmtserver.exceptions.ClustermgmtServiceRetryException;
import com.nutanix.clustermgmtserver.utils.ClustermgmtUtils;
import com.nutanix.infrastructure.cluster.genesis.GenesisInterfaceProto;
import com.nutanix.net.Rpc;
import com.nutanix.net.RpcClient;
import com.nutanix.net.RpcClientOnHttp;
import com.nutanix.net.RpcProto;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.function.Supplier;

/**
 * Extends the {@link ClustermgmtProxy} to access/execute the Genesis RPCs.
 */

@Slf4j
public class GenesisProxyImpl extends ClustermgmtProxy<GenesisInterfaceProto.GenesisRpcSvc.BlockingInterface> {

  @AllArgsConstructor
  public enum GenesisRpcName {
    UPDATE_CLUSTER("updateCluster"),
    ADD_SNMP_TRANSPORTS("addSnmpTransports"),
    REMOVE_SNMP_TRANSPORTS("removeSnmpTransports"),
    ADD_SNMP_USER("addSnmpUser"),
    UPDATE_SNMP_USER("updateSnmpUser"),
    DELETE_SNMP_USER("deleteSnmpUser"),
    ADD_SNMP_TRAP("addSnmpTrap"),
    UPDATE_SNMP_TRAP("updateSnmpTrap"),
    DELETE_SNMP_TRAP("deleteSnmpTrap"),
    CREATE_RSYSLOG_SERVER("createRemoteSyslogServer"),
    UPDATE_RSYSLOG_SERVER("updateRemoteSyslogServer"),
    DELETE_RSYSLOG_SERVER("deleteRemoteSyslogServer"),
    UPDATE_SNMP_STATUS("updateSnmpStatus"),
    UPDATE_CATEGORY_ASSOCIATIONS("updateCategoryAssociations"),
    GET_CONFIG_CREDENTIALS("getConfigCredentials");

    @Getter
    private final String name;

    @Override
    public String toString() {
      return this.getName();
    }
  }

  public GenesisProxyImpl(final String host, final int insightsPort) {
    log.info("Constructing proxy to Genesis at {}:{}", host, insightsPort);
    RpcClient rpcClient = new RpcClientOnHttp(host, insightsPort);
    this.rpcService = GenesisInterfaceProto.GenesisRpcSvc.newBlockingStub(rpcClient);
    setUpRpcServiceMap();
  }

  private void setUpRpcServiceMap() {
    rpcServiceBiFunctionMap = new HashMap<>();
    rpcServiceBiFunctionMap.put(
      GenesisRpcName.UPDATE_CLUSTER.getName(),
      (rpc, message) -> rpcService.updateCluster(rpc, (GenesisInterfaceProto.UpdateClusterArg) message)
    );
    rpcServiceBiFunctionMap.put(
      GenesisRpcName.ADD_SNMP_TRANSPORTS.getName(),
      (rpc, message) -> rpcService.addSnmpTransports(rpc, (GenesisInterfaceProto.AddSnmpTransportsArg) message)
    );
    rpcServiceBiFunctionMap.put(
      GenesisRpcName.REMOVE_SNMP_TRANSPORTS.getName(),
      (rpc, message) -> rpcService.removeSnmpTransports(rpc, (GenesisInterfaceProto.RemoveSnmpTransportsArg) message)
    );
    rpcServiceBiFunctionMap.put(
      GenesisRpcName.ADD_SNMP_USER.getName(),
      (rpc, message) -> rpcService.addSnmpUser(rpc, (GenesisInterfaceProto.AddSnmpUserArg) message)
    );
    rpcServiceBiFunctionMap.put(
      GenesisRpcName.UPDATE_SNMP_USER.getName(),
      (rpc, message) -> rpcService.updateSnmpUser(rpc, (GenesisInterfaceProto.UpdateSnmpUserArg) message)
    );
    rpcServiceBiFunctionMap.put(
      GenesisRpcName.DELETE_SNMP_USER.getName(),
      (rpc, message) -> rpcService.deleteSnmpUser(rpc, (GenesisInterfaceProto.DeleteSnmpUserArg) message)
    );
    rpcServiceBiFunctionMap.put(
      GenesisRpcName.ADD_SNMP_TRAP.getName(),
      (rpc, message) -> rpcService.addSnmpTrap(rpc, (GenesisInterfaceProto.AddSnmpTrapArg) message)
    );
    rpcServiceBiFunctionMap.put(
      GenesisRpcName.UPDATE_SNMP_TRAP.getName(),
      (rpc, message) -> rpcService.updateSnmpTrap(rpc, (GenesisInterfaceProto.UpdateSnmpTrapArg) message)
    );
    rpcServiceBiFunctionMap.put(
      GenesisRpcName.DELETE_SNMP_TRAP.getName(),
      (rpc, message) -> rpcService.deleteSnmpTrap(rpc, (GenesisInterfaceProto.DeleteSnmpTrapArg) message)
    );
    rpcServiceBiFunctionMap.put(
      GenesisRpcName.CREATE_RSYSLOG_SERVER.getName(),
      (rpc, message) -> rpcService.createRemoteSyslogServer(rpc, (GenesisInterfaceProto.CreateRemoteSyslogServerArg) message)
    );
    rpcServiceBiFunctionMap.put(
      GenesisRpcName.UPDATE_RSYSLOG_SERVER.getName(),
      (rpc, message) -> rpcService.updateRemoteSyslogServer(rpc, (GenesisInterfaceProto.UpdateRemoteSyslogServerArg) message)
    );
    rpcServiceBiFunctionMap.put(
      GenesisRpcName.DELETE_RSYSLOG_SERVER.getName(),
      (rpc, message) -> rpcService.deleteRemoteSyslogServer(rpc, (GenesisInterfaceProto.DeleteRemoteSyslogServerArg) message)
    );
    rpcServiceBiFunctionMap.put(
      GenesisRpcName.UPDATE_SNMP_STATUS.getName(),
      (rpc, message) -> rpcService.updateSnmpStatus(rpc, (GenesisInterfaceProto.UpdateSnmpStatusArg) message)
    );
    rpcServiceBiFunctionMap.put(
      GenesisRpcName.UPDATE_CATEGORY_ASSOCIATIONS.getName(),
      (rpc, message) -> rpcService.updateCategoryAssociations(rpc, (GenesisInterfaceProto.UpdateCategoryAssociationsArg) message)
    );
    rpcServiceBiFunctionMap.put(
      GenesisRpcName.GET_CONFIG_CREDENTIALS.getName(),
      (rpc, message) -> rpcService.getConfigCredentials(rpc, (GenesisInterfaceProto.GetConfigCredentialsArg) message)
    );
  }

  @Override
  Rpc getRpc() {
    return new Rpc();
  }

  void setRpcService(GenesisInterfaceProto.GenesisRpcSvc.BlockingInterface blockingInterface) {
    this.rpcService = blockingInterface;
  }

  public <T, E> E doExecute(final GenesisRpcName rpcName, final T rpcArg, RpcProto.RpcRequestContext rpcRequestContext)
    throws ClustermgmtServiceException {

    CircuitBreaker circuitBreaker = ClustermgmtUtils.getCircuitBreakerInstance("Genesis");

    log.debug("CB config: {}",circuitBreaker.getCircuitBreakerConfig());
    Integer failedCallsNum = circuitBreaker.getMetrics().getNumberOfFailedCalls();
    Integer successfulCallsNum = circuitBreaker.getMetrics().getNumberOfSuccessfulCalls();

    log.debug("Before calling RPC from genesis service successful calls={} and failed calls={}", successfulCallsNum, failedCallsNum);
    log.debug("CB metrics: current failure rate={}", circuitBreaker.getMetrics().getFailureRate());
    log.debug("Timestamp: {} and state: {} of CB", circuitBreaker.getCurrentTimestamp(), circuitBreaker.getState());

    Supplier<RPCResponse<E>> supplier = CircuitBreaker.decorateSupplier(circuitBreaker, ()-> {
      try {
        return doExecute(rpcName.toString(), rpcArg,
          rpcRequestContext);
      }
      catch(ClustermgmtServiceException e){
        throw new ClustermgmtGenericException(e);
      }
    });

    RPCResponse<E> response = supplier.get();

    log.debug("Timestamp: {} and state: {} of CB", circuitBreaker.getCurrentTimestamp(), circuitBreaker.getState());

    return response.getProtobufReturn();
  }

  @Override
  void handleAppError(Rpc rpc)
    throws ClustermgmtServiceException {

    GenesisInterfaceProto.GenesisGenericError.ErrorType genesisErrorType =
      GenesisInterfaceProto.GenesisGenericError.ErrorType.forNumber(rpc.getResponseAppError());
    log.debug("kAppError:{} recorded in response RPC as status",
      genesisErrorType);
    if(genesisErrorType == null) {
      throw new ClustermgmtServiceException(
        "unknown error in genesis RPC");
    }
    switch (genesisErrorType) {
      case kRetry:
        throw new ClustermgmtServiceRetryException();
      case kNotFound:
        throw new ClustermgmtServiceException(
          rpcErrorDetail + " Error name: " +
          genesisErrorType.name()
        );
      default:
        final String errorString = String.format(
          "Application error %s raised: %s", genesisErrorType.name(),
          rpcErrorDetail);
        log.debug(errorString);
        throw new ClustermgmtServiceException(
          errorString);
    }
  }
}
