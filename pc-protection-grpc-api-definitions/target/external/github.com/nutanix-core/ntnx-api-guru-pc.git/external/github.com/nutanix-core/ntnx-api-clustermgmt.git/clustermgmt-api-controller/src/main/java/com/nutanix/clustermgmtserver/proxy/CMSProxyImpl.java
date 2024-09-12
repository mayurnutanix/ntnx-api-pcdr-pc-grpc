package com.nutanix.clustermgmtserver.proxy;


import com.nutanix.cluster_management.ClusterManagementErrorProto;
import com.nutanix.cluster_management.ClusterManagementInterfaceProto;
import com.nutanix.clustermgmtserver.exceptions.ClustermgmtGenericException;
import com.nutanix.clustermgmtserver.exceptions.ClustermgmtInvalidInputException;
import com.nutanix.clustermgmtserver.exceptions.ClustermgmtServiceException;
import com.nutanix.clustermgmtserver.exceptions.ClustermgmtServiceRetryException;
import com.nutanix.clustermgmtserver.utils.ClustermgmtUtils;
import com.nutanix.net.Rpc;
import com.nutanix.net.RpcClient;
import com.nutanix.net.RpcProto;
import com.nutanix.net.RpcProto.RpcRequestContext;
import com.nutanix.net.RpcClientOnHttp;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;
import java.util.HashMap;

@Slf4j
public class CMSProxyImpl extends ClustermgmtProxy<ClusterManagementInterfaceProto.ClusterManagementRpcSvc.BlockingInterface>{

  @AllArgsConstructor
  public enum CMSRpcName{

    CREATE_CLUSTER("createCluster"),
    DESTROY_CLUSTER("destroyCluster"),
    DISCOVER_HOSTS("discoverHosts");

    @Getter
    private final String name;

    @Override
    public String toString() {
      return this.getName();
    }
  }

  public CMSProxyImpl(final String host, final int servicePort) {
    log.info("Constructing proxy to Cluster Management Service at {}:{}", host, servicePort);
    RpcClient rpcClient = new RpcClientOnHttp(host, servicePort);

    this.rpcService = ClusterManagementInterfaceProto.ClusterManagementRpcSvc.newBlockingStub(rpcClient);
    setUpRpcServiceMap();
  }

  private void setUpRpcServiceMap() {
    rpcServiceBiFunctionMap = new HashMap<>();
    rpcServiceBiFunctionMap.put(
      CMSRpcName.CREATE_CLUSTER.getName(),
      (rpc, message) -> rpcService.clusterCreate(rpc, (ClusterManagementInterfaceProto.ClusterCreateArg) message)
    );
    rpcServiceBiFunctionMap.put(
      CMSRpcName.DESTROY_CLUSTER.getName(),
      (rpc, message) -> rpcService.clusterDestroy(rpc, (ClusterManagementInterfaceProto.ClusterDestroyArg) message)
    );
    rpcServiceBiFunctionMap.put(
      CMSRpcName.DISCOVER_HOSTS.getName(),
      (rpc, message) -> rpcService.discoverHosts(rpc, (ClusterManagementInterfaceProto.DiscoverHostsArg) message)
    );
  }

  @Override
  Rpc getRpc() {
    return new Rpc();
  }

  void setRpcService(ClusterManagementInterfaceProto.ClusterManagementRpcSvc.BlockingInterface blockingInterface) {
    this.rpcService = blockingInterface;
  }

  public <T, E> E doExecute(final CMSRpcName rpcName, final T rpcArg, RpcProto.RpcRequestContext rpcRequestContext)
    throws ClustermgmtServiceException {

    CircuitBreaker circuitBreaker = ClustermgmtUtils.getCircuitBreakerInstance("ClusterManagement");

    log.debug("CB config: {}",circuitBreaker.getCircuitBreakerConfig());
    Integer failedCallsNum = circuitBreaker.getMetrics().getNumberOfFailedCalls();
    Integer successfulCallsNum = circuitBreaker.getMetrics().getNumberOfSuccessfulCalls();

    log.debug("Before calling RPC from CMS service successful calls={} and failed calls={}", successfulCallsNum, failedCallsNum);
    log.debug("CB metrics: current failure rate={}", circuitBreaker.getMetrics().getFailureRate());
    log.debug("Timestamp: {} and state: {} of CB", circuitBreaker.getCurrentTimestamp(), circuitBreaker.getState());

    Supplier <RPCResponse<E>> supplier = CircuitBreaker.decorateSupplier(circuitBreaker, ()-> {
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

    ClusterManagementErrorProto.ClusterManagementError.Type cmsErrorType =
      ClusterManagementErrorProto.ClusterManagementError.Type.forNumber(rpc.getResponseAppError());

    log.debug("kAppError:{} recorded in response RPC as status",
              cmsErrorType);

    if(cmsErrorType == null){
      throw new ClustermgmtServiceException("unknown error in CMS Rpc");
    }
    // case: cmsErrorType = kNoError(comes in default)
    switch (cmsErrorType) {
      case kUncaughtException:
        log.debug(rpcErrorDetail + " Error name: " + cmsErrorType.name());
        throw new ClustermgmtServiceException(
          rpcErrorDetail + " Error name: " +
            cmsErrorType.name()
        );
      case kInvalidArgument:
        throw new ClustermgmtInvalidInputException(
          rpcErrorDetail + " Error name: " +
            cmsErrorType.name()
        );
      case kRetry:
        throw new ClustermgmtServiceRetryException();
      default:
        final String errorString = String.format(
          "Application error %s raised: %s", cmsErrorType.name(),
          rpcErrorDetail);
        log.debug(errorString);
        throw new ClustermgmtServiceException(
          errorString);
    }
  }

}
