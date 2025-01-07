/*
 * Copyright (c) 2021 Nutanix Inc. All rights reserved.
 *
 * Author: rakesh.falak@nutanix.com
 */

package com.nutanix.clustermgmtserver.proxy;

import com.nutanix.api.utils.rpc.BaseProxy;
import com.google.common.base.Preconditions;
import com.google.protobuf.ServiceException;
import com.nutanix.clustermgmtserver.exceptions.ClustermgmtServiceUnavailableException;
import com.nutanix.net.Rpc;
import com.nutanix.net.RpcProto;
import com.nutanix.net.RpcProto.RpcResponseHeader.RpcStatus;
import com.nutanix.clustermgmtserver.exceptions.ClustermgmtServiceException;
import com.nutanix.clustermgmtserver.exceptions.ClustermgmtServiceRetryException;
import com.nutanix.clustermgmtserver.function.ExceptionBiFunction;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static com.nutanix.net.RpcProto.RpcRequestContext;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Generic class for all local rpc calls to backend components.
 * Based on the supplied rpc name it invokes the correct rpc method using
 * generic BiFunction.
 */

@Slf4j
abstract class ClustermgmtProxy<X> extends BaseProxy {

  private static final int DEFAULT_NUM_RETRIES = 2;
  private static final int DEFAULT_RETRY_DELAY_MILLISECONDS = 500;

  @RequiredArgsConstructor
  public static class RPCResponse<T> {
    @Getter
    private final T protobufReturn;
    private final byte[] responsePayload;
  }

  // The backend RPC service's interface.
  X rpcService;

  // The status of the RPC invoked.
  RpcStatus rpcStatus = RpcStatus.kNoError;

  // The detailed message corresponding to the RPC error.
  String rpcErrorDetail;

  // The map that returns BiFunction corresponding to a specific RPC service.
  Map<String, ExceptionBiFunction<Rpc, Object, Object, ServiceException>> rpcServiceBiFunctionMap;

  RpcRequestContext getEmptyRequestContextForRpc() {
    return RpcRequestContext.newBuilder().build();
  }

  /**
   * Returns the Rpc controller instance for the backend component.
   *
   * @return rpc controller instance.
   */
  abstract Rpc getRpc();

  /**
   * Generic RPC call method which is used for retry logic.
   *
   * @param rpcName           Name of the Rpc to be invoked.
   * @param rpcArg            Rpc argument provided.
   * @param rpcRequestContext Rpc request context information.
   * @param <T>               Generic type of the rpc argument.
   * @param <E>               Generic type of the rpc response.
   * @return Returns the response of the rpc call.
   * @throws ClustermgmtServiceException on retry exception.
   */
  <T, E> RPCResponse<E> doExecute(String rpcName,
                                  T rpcArg,
                                  RpcProto.RpcRequestContext rpcRequestContext)
    throws ClustermgmtServiceException {

    int retry = 0;
    while (true) {
      try {
        return executeRpcCall(rpcName, rpcArg, rpcRequestContext);
      }
      catch (final ClustermgmtServiceRetryException re) {
        if (retry < DEFAULT_NUM_RETRIES) {
          retry++;
          try {
            MILLISECONDS.sleep(DEFAULT_RETRY_DELAY_MILLISECONDS);
          }
          catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
          }
        }
        else {
          final String message = String.format("Request failed when executing remote operation %s: %s",
            rpcName, rpcErrorDetail);
          throw new ClustermgmtServiceUnavailableException(message);
        }
      }
    }
  }

  /**
   * Generic method that uses generic BiFunction to invoke the RPCs.
   *
   * @param rpcName           Name of the Rpc to be invoked.
   * @param rpcArg            Rpc argument provided.
   * @param rpcRequestContext Rpc request context information.
   * @param <T>               Generic type of the rpc argument.
   * @param <E>               Generic type of the rpc response.
   * @return Returns the response of the rpc call.
   * @throws ClustermgmtServiceRetryException on retry exception.
   * @throws ClustermgmtServiceException   on rpc service exception.
   */
  private <T, E> RPCResponse<E> executeRpcCall(
    String rpcName, T rpcArg, RpcProto.RpcRequestContext rpcRequestContext)
    throws ClustermgmtServiceRetryException, ClustermgmtServiceException {

    Preconditions.checkNotNull(rpcArg, "Rpc arguments cannot be null");
    final Rpc rpc = getRpc();
    rpc.getRequestHeader().setRequestContext(rpcRequestContext);
    E rpcRet;
    rpcStatus = RpcProto.RpcResponseHeader.RpcStatus.kNoError;
    rpcErrorDetail = "";
    log.debug("Get {} request to execute with argument {} ", rpcName, rpcArg);
    try {
      ExceptionBiFunction<Rpc, Object, Object, ServiceException> rpcBiFunction =
        rpcServiceBiFunctionMap.get(rpcName);
      if (rpcBiFunction == null) {
        throw new ClustermgmtServiceException(
          String.format("Rpc method %s not found", rpcName)
        );
      }
      rpcRet = (E) rpcBiFunction.apply(rpc, rpcArg);
      rpcStatus = rpc.getResponseRpcStatus();
      rpcErrorDetail = rpc.getResponseErrorDetail();
      throwIfError(rpc);
      log.debug("Received response {} ", rpcRet);
    }
    catch (ServiceException e) {
      throw new ClustermgmtServiceException(e.getMessage());
    }
    return new RPCResponse<>(rpcRet, rpc.getResponsePayload());
  }

  /**
   * Method to raise retry or service error exception.
   *
   * @param rpc The rpc controller used for the rpc call.
   * @throws ClustermgmtServiceRetryException on retry exception.
   * @throws ClustermgmtServiceException   on rpc service exception.
   */
  void throwIfError(final Rpc rpc) throws ClustermgmtServiceRetryException,
    ClustermgmtServiceException {

    if (rpcStatus != RpcStatus.kNoError) {
      log.debug("{} recorded in response RPC as status", rpcStatus);
    }
    switch (rpcStatus) {
      case kNoError:
        return;
      case kAppError:
        handleAppError(rpc);
        return;
      case kTransportError:
      case kTimeout:
        throw new ClustermgmtServiceRetryException();
      default:
        final String errorString = String.format(
          "RPC error %s raised: %s", rpc.getResponseRpcStatus().name(),
          rpcErrorDetail);
        log.debug(errorString);
        throw new ClustermgmtServiceException(
          errorString);
    }
  }

  /**
   * An abstract method to handle kAppError scenarios. This method must be
   * overridden by the service related proxy classes which inherit this class.
   *
   * @param rpc The rpc controller used for the rpc call.
   * @throws ClustermgmtServiceRetryException on retry exception.
   * @throws ClustermgmtServiceException   on rpc service exception.
   */
  abstract void handleAppError(final Rpc rpc)
    throws ClustermgmtServiceException;
}
