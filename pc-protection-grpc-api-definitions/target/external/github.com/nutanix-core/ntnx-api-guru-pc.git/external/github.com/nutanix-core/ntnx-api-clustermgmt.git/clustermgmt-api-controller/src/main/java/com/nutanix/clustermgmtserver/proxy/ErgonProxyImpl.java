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
import com.nutanix.ergon.ErgonErrorProto;
import com.nutanix.net.Rpc;
import com.nutanix.net.RpcClient;
import com.nutanix.net.RpcClientOnHttp;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import nutanix.ergon.ErgonInterface;

import java.util.HashMap;
import java.util.function.Supplier;

@Slf4j
public class ErgonProxyImpl extends ClustermgmtProxy<ErgonInterface.ErgonRpcSvc.BlockingInterface> {

  @AllArgsConstructor
  public enum ErgonRpcName {
    TASK_CREATE("taskCreate"),
    TASK_UPDATE("taskUpdate"),
    TASK_GET("taskGet");

    @Getter
    private final String name;

    @Override
    public String toString() {
      return this.getName();
    }
  }

  public ErgonProxyImpl(final String host, final int ergonPort) {
    log.info("Constructing proxy to Ergon at {}:{}", host, ergonPort);
    RpcClient rpcClient = new RpcClientOnHttp(host, ergonPort);
    this.rpcService = ErgonInterface.ErgonRpcSvc.newBlockingStub(rpcClient);
    setUpRpcServiceMap();
  }

  private void setUpRpcServiceMap() {
    rpcServiceBiFunctionMap = new HashMap<>();
    rpcServiceBiFunctionMap.put(
      ErgonRpcName.TASK_CREATE.getName(),
      (rpc, message) -> rpcService.taskCreate(rpc, (ErgonInterface.TaskCreateArg) message)
    );
    rpcServiceBiFunctionMap.put(
      ErgonRpcName.TASK_UPDATE.getName(),
      (rpc, message) -> rpcService.taskUpdate(rpc, (ErgonInterface.TaskUpdateArg) message)
    );
    rpcServiceBiFunctionMap.put(
      ErgonRpcName.TASK_GET.getName(),
      (rpc, message) -> rpcService.taskGet(rpc, (ErgonInterface.TaskGetArg) message)
    );
  }

  @Override
  Rpc getRpc() {
    return new Rpc();
  }

  void setRpcService(ErgonInterface.ErgonRpcSvc.BlockingInterface blockingInterface) {
    this.rpcService = blockingInterface;
  }

  public <T, E> E doExecute(final ErgonProxyImpl.ErgonRpcName rpcName, final T rpcArg)
    throws ClustermgmtServiceException {

    CircuitBreaker circuitBreaker = ClustermgmtUtils.getCircuitBreakerInstance("Ergon");
    log.debug("CB config: {}",circuitBreaker.getCircuitBreakerConfig());
    Integer failedCallsNum = circuitBreaker.getMetrics().getNumberOfFailedCalls();
    Integer successfulCallsNum = circuitBreaker.getMetrics().getNumberOfSuccessfulCalls();

    log.debug("Before calling RPC from ergon service successful calls={} and failed calls={}", successfulCallsNum, failedCallsNum);
    log.debug("CB metrics: current failure rate={}", circuitBreaker.getMetrics().getFailureRate());
    log.debug("Timestamp: {} and state: {} of CB", circuitBreaker.getCurrentTimestamp(), circuitBreaker.getState());

    Supplier<RPCResponse<E>> supplier = CircuitBreaker.decorateSupplier(circuitBreaker, ()-> {
      try {
        return doExecute(rpcName.toString(), rpcArg,
          getEmptyRequestContextForRpc());
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

    ErgonErrorProto.ErgonError.Type ergonErrorType =
      ErgonErrorProto.ErgonError.Type.forNumber(rpc.getResponseAppError());
    log.debug("kAppError:{} recorded in response RPC as status",
      ergonErrorType);
    switch (ergonErrorType) {
      case kRetry:
        throw new ClustermgmtServiceRetryException();
      case kNotFound:
        rpcErrorDetail = "Unable to process the task as it is" +
          " not synced to the PC";
        throw new ClustermgmtServiceException(rpcErrorDetail);
      default:
        final String errorString = String.format(
          "Application error %s raised: %s", ergonErrorType.name(),
          rpcErrorDetail);
        log.debug(errorString);
        throw new ClustermgmtServiceException(
          errorString);
    }
  }
}
