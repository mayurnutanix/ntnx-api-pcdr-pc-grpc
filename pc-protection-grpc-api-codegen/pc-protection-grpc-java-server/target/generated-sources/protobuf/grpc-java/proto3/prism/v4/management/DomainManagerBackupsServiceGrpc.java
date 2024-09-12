package proto3.prism.v4.management;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.37.0)",
    comments = "Source: proto3/prism/v4/management/DomainManagerBackups_service.proto")
public final class DomainManagerBackupsServiceGrpc {

  private DomainManagerBackupsServiceGrpc() {}

  public static final String SERVICE_NAME = "proto3.prism.v4.management.DomainManagerBackupsService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<proto3.prism.v4.management.CreateBackupTargetArg,
      proto3.prism.v4.management.CreateBackupTargetRet> getCreateBackupTargetMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "createBackupTarget",
      requestType = proto3.prism.v4.management.CreateBackupTargetArg.class,
      responseType = proto3.prism.v4.management.CreateBackupTargetRet.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<proto3.prism.v4.management.CreateBackupTargetArg,
      proto3.prism.v4.management.CreateBackupTargetRet> getCreateBackupTargetMethod() {
    io.grpc.MethodDescriptor<proto3.prism.v4.management.CreateBackupTargetArg, proto3.prism.v4.management.CreateBackupTargetRet> getCreateBackupTargetMethod;
    if ((getCreateBackupTargetMethod = DomainManagerBackupsServiceGrpc.getCreateBackupTargetMethod) == null) {
      synchronized (DomainManagerBackupsServiceGrpc.class) {
        if ((getCreateBackupTargetMethod = DomainManagerBackupsServiceGrpc.getCreateBackupTargetMethod) == null) {
          DomainManagerBackupsServiceGrpc.getCreateBackupTargetMethod = getCreateBackupTargetMethod =
              io.grpc.MethodDescriptor.<proto3.prism.v4.management.CreateBackupTargetArg, proto3.prism.v4.management.CreateBackupTargetRet>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "createBackupTarget"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto3.prism.v4.management.CreateBackupTargetArg.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto3.prism.v4.management.CreateBackupTargetRet.getDefaultInstance()))
              .setSchemaDescriptor(new DomainManagerBackupsServiceMethodDescriptorSupplier("createBackupTarget"))
              .build();
        }
      }
    }
    return getCreateBackupTargetMethod;
  }

  private static volatile io.grpc.MethodDescriptor<proto3.prism.v4.management.DeleteBackupTargetByIdArg,
      proto3.prism.v4.management.DeleteBackupTargetByIdRet> getDeleteBackupTargetByIdMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "deleteBackupTargetById",
      requestType = proto3.prism.v4.management.DeleteBackupTargetByIdArg.class,
      responseType = proto3.prism.v4.management.DeleteBackupTargetByIdRet.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<proto3.prism.v4.management.DeleteBackupTargetByIdArg,
      proto3.prism.v4.management.DeleteBackupTargetByIdRet> getDeleteBackupTargetByIdMethod() {
    io.grpc.MethodDescriptor<proto3.prism.v4.management.DeleteBackupTargetByIdArg, proto3.prism.v4.management.DeleteBackupTargetByIdRet> getDeleteBackupTargetByIdMethod;
    if ((getDeleteBackupTargetByIdMethod = DomainManagerBackupsServiceGrpc.getDeleteBackupTargetByIdMethod) == null) {
      synchronized (DomainManagerBackupsServiceGrpc.class) {
        if ((getDeleteBackupTargetByIdMethod = DomainManagerBackupsServiceGrpc.getDeleteBackupTargetByIdMethod) == null) {
          DomainManagerBackupsServiceGrpc.getDeleteBackupTargetByIdMethod = getDeleteBackupTargetByIdMethod =
              io.grpc.MethodDescriptor.<proto3.prism.v4.management.DeleteBackupTargetByIdArg, proto3.prism.v4.management.DeleteBackupTargetByIdRet>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "deleteBackupTargetById"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto3.prism.v4.management.DeleteBackupTargetByIdArg.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto3.prism.v4.management.DeleteBackupTargetByIdRet.getDefaultInstance()))
              .setSchemaDescriptor(new DomainManagerBackupsServiceMethodDescriptorSupplier("deleteBackupTargetById"))
              .build();
        }
      }
    }
    return getDeleteBackupTargetByIdMethod;
  }

  private static volatile io.grpc.MethodDescriptor<proto3.prism.v4.management.GetBackupTargetByIdArg,
      proto3.prism.v4.management.GetBackupTargetByIdRet> getGetBackupTargetByIdMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "getBackupTargetById",
      requestType = proto3.prism.v4.management.GetBackupTargetByIdArg.class,
      responseType = proto3.prism.v4.management.GetBackupTargetByIdRet.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<proto3.prism.v4.management.GetBackupTargetByIdArg,
      proto3.prism.v4.management.GetBackupTargetByIdRet> getGetBackupTargetByIdMethod() {
    io.grpc.MethodDescriptor<proto3.prism.v4.management.GetBackupTargetByIdArg, proto3.prism.v4.management.GetBackupTargetByIdRet> getGetBackupTargetByIdMethod;
    if ((getGetBackupTargetByIdMethod = DomainManagerBackupsServiceGrpc.getGetBackupTargetByIdMethod) == null) {
      synchronized (DomainManagerBackupsServiceGrpc.class) {
        if ((getGetBackupTargetByIdMethod = DomainManagerBackupsServiceGrpc.getGetBackupTargetByIdMethod) == null) {
          DomainManagerBackupsServiceGrpc.getGetBackupTargetByIdMethod = getGetBackupTargetByIdMethod =
              io.grpc.MethodDescriptor.<proto3.prism.v4.management.GetBackupTargetByIdArg, proto3.prism.v4.management.GetBackupTargetByIdRet>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "getBackupTargetById"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto3.prism.v4.management.GetBackupTargetByIdArg.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto3.prism.v4.management.GetBackupTargetByIdRet.getDefaultInstance()))
              .setSchemaDescriptor(new DomainManagerBackupsServiceMethodDescriptorSupplier("getBackupTargetById"))
              .build();
        }
      }
    }
    return getGetBackupTargetByIdMethod;
  }

  private static volatile io.grpc.MethodDescriptor<proto3.prism.v4.management.ListBackupTargetsArg,
      proto3.prism.v4.management.ListBackupTargetsRet> getListBackupTargetsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "listBackupTargets",
      requestType = proto3.prism.v4.management.ListBackupTargetsArg.class,
      responseType = proto3.prism.v4.management.ListBackupTargetsRet.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<proto3.prism.v4.management.ListBackupTargetsArg,
      proto3.prism.v4.management.ListBackupTargetsRet> getListBackupTargetsMethod() {
    io.grpc.MethodDescriptor<proto3.prism.v4.management.ListBackupTargetsArg, proto3.prism.v4.management.ListBackupTargetsRet> getListBackupTargetsMethod;
    if ((getListBackupTargetsMethod = DomainManagerBackupsServiceGrpc.getListBackupTargetsMethod) == null) {
      synchronized (DomainManagerBackupsServiceGrpc.class) {
        if ((getListBackupTargetsMethod = DomainManagerBackupsServiceGrpc.getListBackupTargetsMethod) == null) {
          DomainManagerBackupsServiceGrpc.getListBackupTargetsMethod = getListBackupTargetsMethod =
              io.grpc.MethodDescriptor.<proto3.prism.v4.management.ListBackupTargetsArg, proto3.prism.v4.management.ListBackupTargetsRet>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "listBackupTargets"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto3.prism.v4.management.ListBackupTargetsArg.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto3.prism.v4.management.ListBackupTargetsRet.getDefaultInstance()))
              .setSchemaDescriptor(new DomainManagerBackupsServiceMethodDescriptorSupplier("listBackupTargets"))
              .build();
        }
      }
    }
    return getListBackupTargetsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<proto3.prism.v4.management.UpdateBackupTargetByIdArg,
      proto3.prism.v4.management.UpdateBackupTargetByIdRet> getUpdateBackupTargetByIdMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "updateBackupTargetById",
      requestType = proto3.prism.v4.management.UpdateBackupTargetByIdArg.class,
      responseType = proto3.prism.v4.management.UpdateBackupTargetByIdRet.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<proto3.prism.v4.management.UpdateBackupTargetByIdArg,
      proto3.prism.v4.management.UpdateBackupTargetByIdRet> getUpdateBackupTargetByIdMethod() {
    io.grpc.MethodDescriptor<proto3.prism.v4.management.UpdateBackupTargetByIdArg, proto3.prism.v4.management.UpdateBackupTargetByIdRet> getUpdateBackupTargetByIdMethod;
    if ((getUpdateBackupTargetByIdMethod = DomainManagerBackupsServiceGrpc.getUpdateBackupTargetByIdMethod) == null) {
      synchronized (DomainManagerBackupsServiceGrpc.class) {
        if ((getUpdateBackupTargetByIdMethod = DomainManagerBackupsServiceGrpc.getUpdateBackupTargetByIdMethod) == null) {
          DomainManagerBackupsServiceGrpc.getUpdateBackupTargetByIdMethod = getUpdateBackupTargetByIdMethod =
              io.grpc.MethodDescriptor.<proto3.prism.v4.management.UpdateBackupTargetByIdArg, proto3.prism.v4.management.UpdateBackupTargetByIdRet>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "updateBackupTargetById"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto3.prism.v4.management.UpdateBackupTargetByIdArg.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto3.prism.v4.management.UpdateBackupTargetByIdRet.getDefaultInstance()))
              .setSchemaDescriptor(new DomainManagerBackupsServiceMethodDescriptorSupplier("updateBackupTargetById"))
              .build();
        }
      }
    }
    return getUpdateBackupTargetByIdMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static DomainManagerBackupsServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<DomainManagerBackupsServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<DomainManagerBackupsServiceStub>() {
        @java.lang.Override
        public DomainManagerBackupsServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new DomainManagerBackupsServiceStub(channel, callOptions);
        }
      };
    return DomainManagerBackupsServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static DomainManagerBackupsServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<DomainManagerBackupsServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<DomainManagerBackupsServiceBlockingStub>() {
        @java.lang.Override
        public DomainManagerBackupsServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new DomainManagerBackupsServiceBlockingStub(channel, callOptions);
        }
      };
    return DomainManagerBackupsServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static DomainManagerBackupsServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<DomainManagerBackupsServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<DomainManagerBackupsServiceFutureStub>() {
        @java.lang.Override
        public DomainManagerBackupsServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new DomainManagerBackupsServiceFutureStub(channel, callOptions);
        }
      };
    return DomainManagerBackupsServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public static abstract class DomainManagerBackupsServiceImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * uri: /prism/v4/management/domain-managers/{domainManagerExtId}/backup-targets
     * http method: POST
     * Create backup target 
     * Creates a cluster or object store as backup target on which the backup is required to be
     *stored. The maximum allowed backup targets at a given moment  for cluster is 3 and for object store is 1.
     *If any one of the cluster/object store does not qualify for backup, then the API will fail.
     * </pre>
     */
    public void createBackupTarget(proto3.prism.v4.management.CreateBackupTargetArg request,
        io.grpc.stub.StreamObserver<proto3.prism.v4.management.CreateBackupTargetRet> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getCreateBackupTargetMethod(), responseObserver);
    }

    /**
     * <pre>
     * uri: /prism/v4/management/domain-managers/{domainManagerExtId}/backup-targets/{extId}
     * http method: DELETE
     * Delete backup target 
     * Removes cluster/object store from backup targets. This will stop backup from being
     *taken on cluster/object store.
     * </pre>
     */
    public void deleteBackupTargetById(proto3.prism.v4.management.DeleteBackupTargetByIdArg request,
        io.grpc.stub.StreamObserver<proto3.prism.v4.management.DeleteBackupTargetByIdRet> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getDeleteBackupTargetByIdMethod(), responseObserver);
    }

    /**
     * <pre>
     * uri: /prism/v4/management/domain-managers/{domainManagerExtId}/backup-targets/{extId}
     * http method: GET
     * Fetch backup target 
     * Retrieves the backup targets (cluster or object store) from domain manager. Returns the
     *backup config and lastSyncTimestamp to the user
     * </pre>
     */
    public void getBackupTargetById(proto3.prism.v4.management.GetBackupTargetByIdArg request,
        io.grpc.stub.StreamObserver<proto3.prism.v4.management.GetBackupTargetByIdRet> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetBackupTargetByIdMethod(), responseObserver);
    }

    /**
     * <pre>
     * uri: /prism/v4/management/domain-managers/{domainManagerExtId}/backup-targets
     * http method: GET
     * List Backup Targets 
     * Lists backup targets (cluster or object store) configured for a given domain manager.
     * </pre>
     */
    public void listBackupTargets(proto3.prism.v4.management.ListBackupTargetsArg request,
        io.grpc.stub.StreamObserver<proto3.prism.v4.management.ListBackupTargetsRet> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getListBackupTargetsMethod(), responseObserver);
    }

    /**
     * <pre>
     * uri: /prism/v4/management/domain-managers/{domainManagerExtId}/backup-targets/{extId}
     * http method: PUT
     * Update backup target 
     * Updates the credentials and/or rpo of the given objectstore
     * </pre>
     */
    public void updateBackupTargetById(proto3.prism.v4.management.UpdateBackupTargetByIdArg request,
        io.grpc.stub.StreamObserver<proto3.prism.v4.management.UpdateBackupTargetByIdRet> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getUpdateBackupTargetByIdMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getCreateBackupTargetMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                proto3.prism.v4.management.CreateBackupTargetArg,
                proto3.prism.v4.management.CreateBackupTargetRet>(
                  this, METHODID_CREATE_BACKUP_TARGET)))
          .addMethod(
            getDeleteBackupTargetByIdMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                proto3.prism.v4.management.DeleteBackupTargetByIdArg,
                proto3.prism.v4.management.DeleteBackupTargetByIdRet>(
                  this, METHODID_DELETE_BACKUP_TARGET_BY_ID)))
          .addMethod(
            getGetBackupTargetByIdMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                proto3.prism.v4.management.GetBackupTargetByIdArg,
                proto3.prism.v4.management.GetBackupTargetByIdRet>(
                  this, METHODID_GET_BACKUP_TARGET_BY_ID)))
          .addMethod(
            getListBackupTargetsMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                proto3.prism.v4.management.ListBackupTargetsArg,
                proto3.prism.v4.management.ListBackupTargetsRet>(
                  this, METHODID_LIST_BACKUP_TARGETS)))
          .addMethod(
            getUpdateBackupTargetByIdMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                proto3.prism.v4.management.UpdateBackupTargetByIdArg,
                proto3.prism.v4.management.UpdateBackupTargetByIdRet>(
                  this, METHODID_UPDATE_BACKUP_TARGET_BY_ID)))
          .build();
    }
  }

  /**
   */
  public static final class DomainManagerBackupsServiceStub extends io.grpc.stub.AbstractAsyncStub<DomainManagerBackupsServiceStub> {
    private DomainManagerBackupsServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected DomainManagerBackupsServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new DomainManagerBackupsServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * uri: /prism/v4/management/domain-managers/{domainManagerExtId}/backup-targets
     * http method: POST
     * Create backup target 
     * Creates a cluster or object store as backup target on which the backup is required to be
     *stored. The maximum allowed backup targets at a given moment  for cluster is 3 and for object store is 1.
     *If any one of the cluster/object store does not qualify for backup, then the API will fail.
     * </pre>
     */
    public void createBackupTarget(proto3.prism.v4.management.CreateBackupTargetArg request,
        io.grpc.stub.StreamObserver<proto3.prism.v4.management.CreateBackupTargetRet> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getCreateBackupTargetMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * uri: /prism/v4/management/domain-managers/{domainManagerExtId}/backup-targets/{extId}
     * http method: DELETE
     * Delete backup target 
     * Removes cluster/object store from backup targets. This will stop backup from being
     *taken on cluster/object store.
     * </pre>
     */
    public void deleteBackupTargetById(proto3.prism.v4.management.DeleteBackupTargetByIdArg request,
        io.grpc.stub.StreamObserver<proto3.prism.v4.management.DeleteBackupTargetByIdRet> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getDeleteBackupTargetByIdMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * uri: /prism/v4/management/domain-managers/{domainManagerExtId}/backup-targets/{extId}
     * http method: GET
     * Fetch backup target 
     * Retrieves the backup targets (cluster or object store) from domain manager. Returns the
     *backup config and lastSyncTimestamp to the user
     * </pre>
     */
    public void getBackupTargetById(proto3.prism.v4.management.GetBackupTargetByIdArg request,
        io.grpc.stub.StreamObserver<proto3.prism.v4.management.GetBackupTargetByIdRet> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetBackupTargetByIdMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * uri: /prism/v4/management/domain-managers/{domainManagerExtId}/backup-targets
     * http method: GET
     * List Backup Targets 
     * Lists backup targets (cluster or object store) configured for a given domain manager.
     * </pre>
     */
    public void listBackupTargets(proto3.prism.v4.management.ListBackupTargetsArg request,
        io.grpc.stub.StreamObserver<proto3.prism.v4.management.ListBackupTargetsRet> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getListBackupTargetsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * uri: /prism/v4/management/domain-managers/{domainManagerExtId}/backup-targets/{extId}
     * http method: PUT
     * Update backup target 
     * Updates the credentials and/or rpo of the given objectstore
     * </pre>
     */
    public void updateBackupTargetById(proto3.prism.v4.management.UpdateBackupTargetByIdArg request,
        io.grpc.stub.StreamObserver<proto3.prism.v4.management.UpdateBackupTargetByIdRet> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getUpdateBackupTargetByIdMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class DomainManagerBackupsServiceBlockingStub extends io.grpc.stub.AbstractBlockingStub<DomainManagerBackupsServiceBlockingStub> {
    private DomainManagerBackupsServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected DomainManagerBackupsServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new DomainManagerBackupsServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * uri: /prism/v4/management/domain-managers/{domainManagerExtId}/backup-targets
     * http method: POST
     * Create backup target 
     * Creates a cluster or object store as backup target on which the backup is required to be
     *stored. The maximum allowed backup targets at a given moment  for cluster is 3 and for object store is 1.
     *If any one of the cluster/object store does not qualify for backup, then the API will fail.
     * </pre>
     */
    public proto3.prism.v4.management.CreateBackupTargetRet createBackupTarget(proto3.prism.v4.management.CreateBackupTargetArg request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getCreateBackupTargetMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * uri: /prism/v4/management/domain-managers/{domainManagerExtId}/backup-targets/{extId}
     * http method: DELETE
     * Delete backup target 
     * Removes cluster/object store from backup targets. This will stop backup from being
     *taken on cluster/object store.
     * </pre>
     */
    public proto3.prism.v4.management.DeleteBackupTargetByIdRet deleteBackupTargetById(proto3.prism.v4.management.DeleteBackupTargetByIdArg request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getDeleteBackupTargetByIdMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * uri: /prism/v4/management/domain-managers/{domainManagerExtId}/backup-targets/{extId}
     * http method: GET
     * Fetch backup target 
     * Retrieves the backup targets (cluster or object store) from domain manager. Returns the
     *backup config and lastSyncTimestamp to the user
     * </pre>
     */
    public proto3.prism.v4.management.GetBackupTargetByIdRet getBackupTargetById(proto3.prism.v4.management.GetBackupTargetByIdArg request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetBackupTargetByIdMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * uri: /prism/v4/management/domain-managers/{domainManagerExtId}/backup-targets
     * http method: GET
     * List Backup Targets 
     * Lists backup targets (cluster or object store) configured for a given domain manager.
     * </pre>
     */
    public proto3.prism.v4.management.ListBackupTargetsRet listBackupTargets(proto3.prism.v4.management.ListBackupTargetsArg request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getListBackupTargetsMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * uri: /prism/v4/management/domain-managers/{domainManagerExtId}/backup-targets/{extId}
     * http method: PUT
     * Update backup target 
     * Updates the credentials and/or rpo of the given objectstore
     * </pre>
     */
    public proto3.prism.v4.management.UpdateBackupTargetByIdRet updateBackupTargetById(proto3.prism.v4.management.UpdateBackupTargetByIdArg request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getUpdateBackupTargetByIdMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class DomainManagerBackupsServiceFutureStub extends io.grpc.stub.AbstractFutureStub<DomainManagerBackupsServiceFutureStub> {
    private DomainManagerBackupsServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected DomainManagerBackupsServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new DomainManagerBackupsServiceFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * uri: /prism/v4/management/domain-managers/{domainManagerExtId}/backup-targets
     * http method: POST
     * Create backup target 
     * Creates a cluster or object store as backup target on which the backup is required to be
     *stored. The maximum allowed backup targets at a given moment  for cluster is 3 and for object store is 1.
     *If any one of the cluster/object store does not qualify for backup, then the API will fail.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<proto3.prism.v4.management.CreateBackupTargetRet> createBackupTarget(
        proto3.prism.v4.management.CreateBackupTargetArg request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getCreateBackupTargetMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * uri: /prism/v4/management/domain-managers/{domainManagerExtId}/backup-targets/{extId}
     * http method: DELETE
     * Delete backup target 
     * Removes cluster/object store from backup targets. This will stop backup from being
     *taken on cluster/object store.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<proto3.prism.v4.management.DeleteBackupTargetByIdRet> deleteBackupTargetById(
        proto3.prism.v4.management.DeleteBackupTargetByIdArg request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getDeleteBackupTargetByIdMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * uri: /prism/v4/management/domain-managers/{domainManagerExtId}/backup-targets/{extId}
     * http method: GET
     * Fetch backup target 
     * Retrieves the backup targets (cluster or object store) from domain manager. Returns the
     *backup config and lastSyncTimestamp to the user
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<proto3.prism.v4.management.GetBackupTargetByIdRet> getBackupTargetById(
        proto3.prism.v4.management.GetBackupTargetByIdArg request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetBackupTargetByIdMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * uri: /prism/v4/management/domain-managers/{domainManagerExtId}/backup-targets
     * http method: GET
     * List Backup Targets 
     * Lists backup targets (cluster or object store) configured for a given domain manager.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<proto3.prism.v4.management.ListBackupTargetsRet> listBackupTargets(
        proto3.prism.v4.management.ListBackupTargetsArg request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getListBackupTargetsMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * uri: /prism/v4/management/domain-managers/{domainManagerExtId}/backup-targets/{extId}
     * http method: PUT
     * Update backup target 
     * Updates the credentials and/or rpo of the given objectstore
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<proto3.prism.v4.management.UpdateBackupTargetByIdRet> updateBackupTargetById(
        proto3.prism.v4.management.UpdateBackupTargetByIdArg request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getUpdateBackupTargetByIdMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_CREATE_BACKUP_TARGET = 0;
  private static final int METHODID_DELETE_BACKUP_TARGET_BY_ID = 1;
  private static final int METHODID_GET_BACKUP_TARGET_BY_ID = 2;
  private static final int METHODID_LIST_BACKUP_TARGETS = 3;
  private static final int METHODID_UPDATE_BACKUP_TARGET_BY_ID = 4;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final DomainManagerBackupsServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(DomainManagerBackupsServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_CREATE_BACKUP_TARGET:
          serviceImpl.createBackupTarget((proto3.prism.v4.management.CreateBackupTargetArg) request,
              (io.grpc.stub.StreamObserver<proto3.prism.v4.management.CreateBackupTargetRet>) responseObserver);
          break;
        case METHODID_DELETE_BACKUP_TARGET_BY_ID:
          serviceImpl.deleteBackupTargetById((proto3.prism.v4.management.DeleteBackupTargetByIdArg) request,
              (io.grpc.stub.StreamObserver<proto3.prism.v4.management.DeleteBackupTargetByIdRet>) responseObserver);
          break;
        case METHODID_GET_BACKUP_TARGET_BY_ID:
          serviceImpl.getBackupTargetById((proto3.prism.v4.management.GetBackupTargetByIdArg) request,
              (io.grpc.stub.StreamObserver<proto3.prism.v4.management.GetBackupTargetByIdRet>) responseObserver);
          break;
        case METHODID_LIST_BACKUP_TARGETS:
          serviceImpl.listBackupTargets((proto3.prism.v4.management.ListBackupTargetsArg) request,
              (io.grpc.stub.StreamObserver<proto3.prism.v4.management.ListBackupTargetsRet>) responseObserver);
          break;
        case METHODID_UPDATE_BACKUP_TARGET_BY_ID:
          serviceImpl.updateBackupTargetById((proto3.prism.v4.management.UpdateBackupTargetByIdArg) request,
              (io.grpc.stub.StreamObserver<proto3.prism.v4.management.UpdateBackupTargetByIdRet>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class DomainManagerBackupsServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    DomainManagerBackupsServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return proto3.prism.v4.management.DomainManagerBackupsServiceOuterClass.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("DomainManagerBackupsService");
    }
  }

  private static final class DomainManagerBackupsServiceFileDescriptorSupplier
      extends DomainManagerBackupsServiceBaseDescriptorSupplier {
    DomainManagerBackupsServiceFileDescriptorSupplier() {}
  }

  private static final class DomainManagerBackupsServiceMethodDescriptorSupplier
      extends DomainManagerBackupsServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    DomainManagerBackupsServiceMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (DomainManagerBackupsServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new DomainManagerBackupsServiceFileDescriptorSupplier())
              .addMethod(getCreateBackupTargetMethod())
              .addMethod(getDeleteBackupTargetByIdMethod())
              .addMethod(getGetBackupTargetByIdMethod())
              .addMethod(getListBackupTargetsMethod())
              .addMethod(getUpdateBackupTargetByIdMethod())
              .build();
        }
      }
    }
    return result;
  }
}
