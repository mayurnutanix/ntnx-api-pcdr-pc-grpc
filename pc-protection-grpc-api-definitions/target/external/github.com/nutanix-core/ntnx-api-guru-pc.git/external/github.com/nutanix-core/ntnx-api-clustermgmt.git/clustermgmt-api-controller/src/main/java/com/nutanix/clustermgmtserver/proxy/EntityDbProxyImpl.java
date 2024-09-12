/*
 * Copyright (c) 2021 Nutanix Inc. All rights reserved.
 *
 * Author: rakesh.falak@nutanix.com
 */

package com.nutanix.clustermgmtserver.proxy;

import com.nutanix.clustermgmtserver.exceptions.ClustermgmtGenericException;
import com.nutanix.clustermgmtserver.utils.ClustermgmtUtils;
import com.nutanix.insights.ifc.InsightsInterfaceProto.GetEntitiesArg;
import com.nutanix.insights.ifc.InsightsInterfaceProto.GetEntitiesWithMetricsArg;
import com.nutanix.insights.ifc.InsightsInterfaceProto.InsightsErrorProto;
import com.nutanix.insights.ifc.InsightsInterfaceProto.InsightsRpcSvc;
import com.nutanix.net.Rpc;
import com.nutanix.net.RpcClient;
import com.nutanix.net.RpcClientOnHttp;
import com.nutanix.clustermgmtserver.exceptions.ClustermgmtServiceException;
import com.nutanix.clustermgmtserver.exceptions.ClustermgmtServiceRetryException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.function.Supplier;

/**
 * Extends the {@link ClustermgmtProxy} to access/execute the EntityDB RPCs.
 */

@Slf4j
public class EntityDbProxyImpl extends ClustermgmtProxy<InsightsRpcSvc.BlockingInterface> {

  @AllArgsConstructor
  public enum EntityDbRpcName {
    GET_ENTITIES("getEntities"),
    GET_ENTITIES_WITH_METRICS("getEntitiesWithMetrics");

    @Getter
    private final String name;

    @Override
    public String toString() {
      return this.getName();
    }
  }

  public EntityDbProxyImpl(final String host, final int insightsPort) {
    log.info("Constructing proxy to Insights at {}:{}", host, insightsPort);
    RpcClient rpcClient = new RpcClientOnHttp(host, insightsPort);
    this.rpcService = InsightsRpcSvc.newBlockingStub(rpcClient);
    setUpRpcServiceMap();
  }

  private void setUpRpcServiceMap() {
    rpcServiceBiFunctionMap = new HashMap<>();
    rpcServiceBiFunctionMap.put(
      EntityDbRpcName.GET_ENTITIES.getName(),
      (rpc, message) -> rpcService.getEntities(rpc, (GetEntitiesArg) message)
    );
    rpcServiceBiFunctionMap.put(
      EntityDbRpcName.GET_ENTITIES_WITH_METRICS.getName(),
      (rpc, message) -> rpcService.getEntitiesWithMetrics(rpc, (GetEntitiesWithMetricsArg) message)
    );
  }

  @Override
  Rpc getRpc() {
    return new Rpc();
  }

  void setRpcService(InsightsRpcSvc.BlockingInterface blockingInterface) {
    this.rpcService = blockingInterface;
  }

  public <T, E> E doExecute(final EntityDbRpcName rpcName, final T rpcArg)
    throws ClustermgmtServiceException {

    CircuitBreaker circuitBreaker = ClustermgmtUtils.getCircuitBreakerInstance("Insights");
    log.debug("CB config: {}",circuitBreaker.getCircuitBreakerConfig());
    Integer failedCallsNum = circuitBreaker.getMetrics().getNumberOfFailedCalls();
    Integer successfulCallsNum = circuitBreaker.getMetrics().getNumberOfSuccessfulCalls();

    log.debug("Before calling RPC from insights service successful calls={} and failed calls={}", successfulCallsNum, failedCallsNum);
    log.debug("CB metrics: current failure rate={}", circuitBreaker.getMetrics().getFailureRate());
    log.debug("Timestamp: {} and state: {} of CB", circuitBreaker.getCurrentTimestamp(), circuitBreaker.getState());

    Supplier<RPCResponse<E>> supplier = CircuitBreaker.decorateSupplier(circuitBreaker, ()-> {
      try {
        return doExecute(rpcName.toString(), rpcArg,
          getEmptyRpcRequestContext());
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

    InsightsErrorProto.Type insightsErrorType =
      InsightsErrorProto.Type.forNumber(rpc.getResponseAppError());
    log.debug("kAppError:{} recorded in response RPC as status",
      insightsErrorType);
    switch (insightsErrorType) {
      case kRetry:
        throw new ClustermgmtServiceRetryException();
      case kNotFound:
        throw new ClustermgmtServiceException(
          rpcErrorDetail + ",Error name: " +
          insightsErrorType.name()
        );
      default:
        final String errorString = String.format(
          "Application error %s raised: %s", insightsErrorType.name(),
          rpcErrorDetail);
        log.debug(errorString);
        throw new ClustermgmtServiceException(
          errorString);
    }
  }
}
