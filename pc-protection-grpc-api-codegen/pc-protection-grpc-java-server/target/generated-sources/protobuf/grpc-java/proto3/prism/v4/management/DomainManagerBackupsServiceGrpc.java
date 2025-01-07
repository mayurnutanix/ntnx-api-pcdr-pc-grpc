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
  private static volatile io.grpc.MethodDescriptor<proto3.prism.v4.management.CreateBackupTarget1Arg,
      proto3.prism.v4.management.CreateBackupTarget1Ret> getCreateBackupTarget1Method;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "createBackupTarget1",
      requestType = proto3.prism.v4.management.CreateBackupTarget1Arg.class,
      responseType = proto3.prism.v4.management.CreateBackupTarget1Ret.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<proto3.prism.v4.management.CreateBackupTarget1Arg,
      proto3.prism.v4.management.CreateBackupTarget1Ret> getCreateBackupTarget1Method() {
    io.grpc.MethodDescriptor<proto3.prism.v4.management.CreateBackupTarget1Arg, proto3.prism.v4.management.CreateBackupTarget1Ret> getCreateBackupTarget1Method;
    if ((getCreateBackupTarget1Method = DomainManagerBackupsServiceGrpc.getCreateBackupTarget1Method) == null) {
      synchronized (DomainManagerBackupsServiceGrpc.class) {
        if ((getCreateBackupTarget1Method = DomainManagerBackupsServiceGrpc.getCreateBackupTarget1Method) == null) {
          DomainManagerBackupsServiceGrpc.getCreateBackupTarget1Method = getCreateBackupTarget1Method =
              io.grpc.MethodDescriptor.<proto3.prism.v4.management.CreateBackupTarget1Arg, proto3.prism.v4.management.CreateBackupTarget1Ret>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "createBackupTarget1"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto3.prism.v4.management.CreateBackupTarget1Arg.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto3.prism.v4.management.CreateBackupTarget1Ret.getDefaultInstance()))
              .setSchemaDescriptor(new DomainManagerBackupsServiceMethodDescriptorSupplier("createBackupTarget1"))
              .build();
        }
      }
    }
    return getCreateBackupTarget1Method;
  }

  private static volatile io.grpc.MethodDescriptor<proto3.prism.v4.management.DeleteBackupTargetById1Arg,
      proto3.prism.v4.management.DeleteBackupTargetById1Ret> getDeleteBackupTargetById1Method;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "deleteBackupTargetById1",
      requestType = proto3.prism.v4.management.DeleteBackupTargetById1Arg.class,
      responseType = proto3.prism.v4.management.DeleteBackupTargetById1Ret.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<proto3.prism.v4.management.DeleteBackupTargetById1Arg,
      proto3.prism.v4.management.DeleteBackupTargetById1Ret> getDeleteBackupTargetById1Method() {
    io.grpc.MethodDescriptor<proto3.prism.v4.management.DeleteBackupTargetById1Arg, proto3.prism.v4.management.DeleteBackupTargetById1Ret> getDeleteBackupTargetById1Method;
    if ((getDeleteBackupTargetById1Method = DomainManagerBackupsServiceGrpc.getDeleteBackupTargetById1Method) == null) {
      synchronized (DomainManagerBackupsServiceGrpc.class) {
        if ((getDeleteBackupTargetById1Method = DomainManagerBackupsServiceGrpc.getDeleteBackupTargetById1Method) == null) {
          DomainManagerBackupsServiceGrpc.getDeleteBackupTargetById1Method = getDeleteBackupTargetById1Method =
              io.grpc.MethodDescriptor.<proto3.prism.v4.management.DeleteBackupTargetById1Arg, proto3.prism.v4.management.DeleteBackupTargetById1Ret>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "deleteBackupTargetById1"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto3.prism.v4.management.DeleteBackupTargetById1Arg.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto3.prism.v4.management.DeleteBackupTargetById1Ret.getDefaultInstance()))
              .setSchemaDescriptor(new DomainManagerBackupsServiceMethodDescriptorSupplier("deleteBackupTargetById1"))
              .build();
        }
      }
    }
    return getDeleteBackupTargetById1Method;
  }

  private static volatile io.grpc.MethodDescriptor<proto3.prism.v4.management.GetBackupTargetById1Arg,
      proto3.prism.v4.management.GetBackupTargetById1Ret> getGetBackupTargetById1Method;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "getBackupTargetById1",
      requestType = proto3.prism.v4.management.GetBackupTargetById1Arg.class,
      responseType = proto3.prism.v4.management.GetBackupTargetById1Ret.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<proto3.prism.v4.management.GetBackupTargetById1Arg,
      proto3.prism.v4.management.GetBackupTargetById1Ret> getGetBackupTargetById1Method() {
    io.grpc.MethodDescriptor<proto3.prism.v4.management.GetBackupTargetById1Arg, proto3.prism.v4.management.GetBackupTargetById1Ret> getGetBackupTargetById1Method;
    if ((getGetBackupTargetById1Method = DomainManagerBackupsServiceGrpc.getGetBackupTargetById1Method) == null) {
      synchronized (DomainManagerBackupsServiceGrpc.class) {
        if ((getGetBackupTargetById1Method = DomainManagerBackupsServiceGrpc.getGetBackupTargetById1Method) == null) {
          DomainManagerBackupsServiceGrpc.getGetBackupTargetById1Method = getGetBackupTargetById1Method =
              io.grpc.MethodDescriptor.<proto3.prism.v4.management.GetBackupTargetById1Arg, proto3.prism.v4.management.GetBackupTargetById1Ret>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "getBackupTargetById1"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto3.prism.v4.management.GetBackupTargetById1Arg.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto3.prism.v4.management.GetBackupTargetById1Ret.getDefaultInstance()))
              .setSchemaDescriptor(new DomainManagerBackupsServiceMethodDescriptorSupplier("getBackupTargetById1"))
              .build();
        }
      }
    }
    return getGetBackupTargetById1Method;
  }

  private static volatile io.grpc.MethodDescriptor<proto3.prism.v4.management.ListBackupTargets1Arg,
      proto3.prism.v4.management.ListBackupTargets1Ret> getListBackupTargets1Method;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "listBackupTargets1",
      requestType = proto3.prism.v4.management.ListBackupTargets1Arg.class,
      responseType = proto3.prism.v4.management.ListBackupTargets1Ret.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<proto3.prism.v4.management.ListBackupTargets1Arg,
      proto3.prism.v4.management.ListBackupTargets1Ret> getListBackupTargets1Method() {
    io.grpc.MethodDescriptor<proto3.prism.v4.management.ListBackupTargets1Arg, proto3.prism.v4.management.ListBackupTargets1Ret> getListBackupTargets1Method;
    if ((getListBackupTargets1Method = DomainManagerBackupsServiceGrpc.getListBackupTargets1Method) == null) {
      synchronized (DomainManagerBackupsServiceGrpc.class) {
        if ((getListBackupTargets1Method = DomainManagerBackupsServiceGrpc.getListBackupTargets1Method) == null) {
          DomainManagerBackupsServiceGrpc.getListBackupTargets1Method = getListBackupTargets1Method =
              io.grpc.MethodDescriptor.<proto3.prism.v4.management.ListBackupTargets1Arg, proto3.prism.v4.management.ListBackupTargets1Ret>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "listBackupTargets1"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto3.prism.v4.management.ListBackupTargets1Arg.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto3.prism.v4.management.ListBackupTargets1Ret.getDefaultInstance()))
              .setSchemaDescriptor(new DomainManagerBackupsServiceMethodDescriptorSupplier("listBackupTargets1"))
              .build();
        }
      }
    }
    return getListBackupTargets1Method;
  }

  private static volatile io.grpc.MethodDescriptor<proto3.prism.v4.management.UpdateBackupTargetById1Arg,
      proto3.prism.v4.management.UpdateBackupTargetById1Ret> getUpdateBackupTargetById1Method;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "updateBackupTargetById1",
      requestType = proto3.prism.v4.management.UpdateBackupTargetById1Arg.class,
      responseType = proto3.prism.v4.management.UpdateBackupTargetById1Ret.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<proto3.prism.v4.management.UpdateBackupTargetById1Arg,
      proto3.prism.v4.management.UpdateBackupTargetById1Ret> getUpdateBackupTargetById1Method() {
    io.grpc.MethodDescriptor<proto3.prism.v4.management.UpdateBackupTargetById1Arg, proto3.prism.v4.management.UpdateBackupTargetById1Ret> getUpdateBackupTargetById1Method;
    if ((getUpdateBackupTargetById1Method = DomainManagerBackupsServiceGrpc.getUpdateBackupTargetById1Method) == null) {
      synchronized (DomainManagerBackupsServiceGrpc.class) {
        if ((getUpdateBackupTargetById1Method = DomainManagerBackupsServiceGrpc.getUpdateBackupTargetById1Method) == null) {
          DomainManagerBackupsServiceGrpc.getUpdateBackupTargetById1Method = getUpdateBackupTargetById1Method =
              io.grpc.MethodDescriptor.<proto3.prism.v4.management.UpdateBackupTargetById1Arg, proto3.prism.v4.management.UpdateBackupTargetById1Ret>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "updateBackupTargetById1"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto3.prism.v4.management.UpdateBackupTargetById1Arg.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto3.prism.v4.management.UpdateBackupTargetById1Ret.getDefaultInstance()))
              .setSchemaDescriptor(new DomainManagerBackupsServiceMethodDescriptorSupplier("updateBackupTargetById1"))
              .build();
        }
      }
    }
    return getUpdateBackupTargetById1Method;
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
     * uri: /prism/v4/management/domain-managers/{domainManagerExtId}/backup-targets-new
     * http method: POST
     * Create backup target 
     * Creates a cluster or object store as the backup target. For a given Prism Central,
     *there can be up to 3 clusters as backup targets 
     *and 1 object store as backup target. If any cluster or object store is not eligible for backup or 
     *lacks appropriate permissions, the API request will fail. 
     *For object store backup targets, specifying backup policy is mandatory along 
     *with the location of the object store.
     * </pre>
     */
    public void createBackupTarget1(proto3.prism.v4.management.CreateBackupTarget1Arg request,
        io.grpc.stub.StreamObserver<proto3.prism.v4.management.CreateBackupTarget1Ret> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getCreateBackupTarget1Method(), responseObserver);
    }

    /**
     * <pre>
     * uri: /prism/v4/management/domain-managers/{domainManagerExtId}/backup-targets-new/{extId}
     * http method: DELETE
     * Delete backup target 
     * Removes cluster/object store from the backup targets. This will stop the cluster/object store 
     *from backing up Prism Central data.
     * </pre>
     */
    public void deleteBackupTargetById1(proto3.prism.v4.management.DeleteBackupTargetById1Arg request,
        io.grpc.stub.StreamObserver<proto3.prism.v4.management.DeleteBackupTargetById1Ret> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getDeleteBackupTargetById1Method(), responseObserver);
    }

    /**
     * <pre>
     * uri: /prism/v4/management/domain-managers/{domainManagerExtId}/backup-targets-new/{extId}
     * http method: GET
     * Fetch backup target 
     * Retrieves the backup targets (cluster or object store) from a domain manager and returns the
     *backup configuration and lastSyncTimestamp parameter to the user.
     * </pre>
     */
    public void getBackupTargetById1(proto3.prism.v4.management.GetBackupTargetById1Arg request,
        io.grpc.stub.StreamObserver<proto3.prism.v4.management.GetBackupTargetById1Ret> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetBackupTargetById1Method(), responseObserver);
    }

    /**
     * <pre>
     * uri: /prism/v4/management/domain-managers/{domainManagerExtId}/backup-targets-new
     * http method: GET
     * List backup targets 
     * Lists backup targets (cluster or object store) configured for a given domain manager.
     * </pre>
     */
    public void listBackupTargets1(proto3.prism.v4.management.ListBackupTargets1Arg request,
        io.grpc.stub.StreamObserver<proto3.prism.v4.management.ListBackupTargets1Ret> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getListBackupTargets1Method(), responseObserver);
    }

    /**
     * <pre>
     * uri: /prism/v4/management/domain-managers/{domainManagerExtId}/backup-targets-new/{extId}
     * http method: PUT
     * Update backup target 
     * Updates the credentials and/or RPO of the given object store.
     * </pre>
     */
    public void updateBackupTargetById1(proto3.prism.v4.management.UpdateBackupTargetById1Arg request,
        io.grpc.stub.StreamObserver<proto3.prism.v4.management.UpdateBackupTargetById1Ret> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getUpdateBackupTargetById1Method(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getCreateBackupTarget1Method(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                proto3.prism.v4.management.CreateBackupTarget1Arg,
                proto3.prism.v4.management.CreateBackupTarget1Ret>(
                  this, METHODID_CREATE_BACKUP_TARGET1)))
          .addMethod(
            getDeleteBackupTargetById1Method(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                proto3.prism.v4.management.DeleteBackupTargetById1Arg,
                proto3.prism.v4.management.DeleteBackupTargetById1Ret>(
                  this, METHODID_DELETE_BACKUP_TARGET_BY_ID1)))
          .addMethod(
            getGetBackupTargetById1Method(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                proto3.prism.v4.management.GetBackupTargetById1Arg,
                proto3.prism.v4.management.GetBackupTargetById1Ret>(
                  this, METHODID_GET_BACKUP_TARGET_BY_ID1)))
          .addMethod(
            getListBackupTargets1Method(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                proto3.prism.v4.management.ListBackupTargets1Arg,
                proto3.prism.v4.management.ListBackupTargets1Ret>(
                  this, METHODID_LIST_BACKUP_TARGETS1)))
          .addMethod(
            getUpdateBackupTargetById1Method(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                proto3.prism.v4.management.UpdateBackupTargetById1Arg,
                proto3.prism.v4.management.UpdateBackupTargetById1Ret>(
                  this, METHODID_UPDATE_BACKUP_TARGET_BY_ID1)))
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
     * uri: /prism/v4/management/domain-managers/{domainManagerExtId}/backup-targets-new
     * http method: POST
     * Create backup target 
     * Creates a cluster or object store as the backup target. For a given Prism Central,
     *there can be up to 3 clusters as backup targets 
     *and 1 object store as backup target. If any cluster or object store is not eligible for backup or 
     *lacks appropriate permissions, the API request will fail. 
     *For object store backup targets, specifying backup policy is mandatory along 
     *with the location of the object store.
     * </pre>
     */
    public void createBackupTarget1(proto3.prism.v4.management.CreateBackupTarget1Arg request,
        io.grpc.stub.StreamObserver<proto3.prism.v4.management.CreateBackupTarget1Ret> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getCreateBackupTarget1Method(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * uri: /prism/v4/management/domain-managers/{domainManagerExtId}/backup-targets-new/{extId}
     * http method: DELETE
     * Delete backup target 
     * Removes cluster/object store from the backup targets. This will stop the cluster/object store 
     *from backing up Prism Central data.
     * </pre>
     */
    public void deleteBackupTargetById1(proto3.prism.v4.management.DeleteBackupTargetById1Arg request,
        io.grpc.stub.StreamObserver<proto3.prism.v4.management.DeleteBackupTargetById1Ret> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getDeleteBackupTargetById1Method(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * uri: /prism/v4/management/domain-managers/{domainManagerExtId}/backup-targets-new/{extId}
     * http method: GET
     * Fetch backup target 
     * Retrieves the backup targets (cluster or object store) from a domain manager and returns the
     *backup configuration and lastSyncTimestamp parameter to the user.
     * </pre>
     */
    public void getBackupTargetById1(proto3.prism.v4.management.GetBackupTargetById1Arg request,
        io.grpc.stub.StreamObserver<proto3.prism.v4.management.GetBackupTargetById1Ret> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetBackupTargetById1Method(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * uri: /prism/v4/management/domain-managers/{domainManagerExtId}/backup-targets-new
     * http method: GET
     * List backup targets 
     * Lists backup targets (cluster or object store) configured for a given domain manager.
     * </pre>
     */
    public void listBackupTargets1(proto3.prism.v4.management.ListBackupTargets1Arg request,
        io.grpc.stub.StreamObserver<proto3.prism.v4.management.ListBackupTargets1Ret> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getListBackupTargets1Method(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * uri: /prism/v4/management/domain-managers/{domainManagerExtId}/backup-targets-new/{extId}
     * http method: PUT
     * Update backup target 
     * Updates the credentials and/or RPO of the given object store.
     * </pre>
     */
    public void updateBackupTargetById1(proto3.prism.v4.management.UpdateBackupTargetById1Arg request,
        io.grpc.stub.StreamObserver<proto3.prism.v4.management.UpdateBackupTargetById1Ret> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getUpdateBackupTargetById1Method(), getCallOptions()), request, responseObserver);
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
     * uri: /prism/v4/management/domain-managers/{domainManagerExtId}/backup-targets-new
     * http method: POST
     * Create backup target 
     * Creates a cluster or object store as the backup target. For a given Prism Central,
     *there can be up to 3 clusters as backup targets 
     *and 1 object store as backup target. If any cluster or object store is not eligible for backup or 
     *lacks appropriate permissions, the API request will fail. 
     *For object store backup targets, specifying backup policy is mandatory along 
     *with the location of the object store.
     * </pre>
     */
    public proto3.prism.v4.management.CreateBackupTarget1Ret createBackupTarget1(proto3.prism.v4.management.CreateBackupTarget1Arg request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getCreateBackupTarget1Method(), getCallOptions(), request);
    }

    /**
     * <pre>
     * uri: /prism/v4/management/domain-managers/{domainManagerExtId}/backup-targets-new/{extId}
     * http method: DELETE
     * Delete backup target 
     * Removes cluster/object store from the backup targets. This will stop the cluster/object store 
     *from backing up Prism Central data.
     * </pre>
     */
    public proto3.prism.v4.management.DeleteBackupTargetById1Ret deleteBackupTargetById1(proto3.prism.v4.management.DeleteBackupTargetById1Arg request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getDeleteBackupTargetById1Method(), getCallOptions(), request);
    }

    /**
     * <pre>
     * uri: /prism/v4/management/domain-managers/{domainManagerExtId}/backup-targets-new/{extId}
     * http method: GET
     * Fetch backup target 
     * Retrieves the backup targets (cluster or object store) from a domain manager and returns the
     *backup configuration and lastSyncTimestamp parameter to the user.
     * </pre>
     */
    public proto3.prism.v4.management.GetBackupTargetById1Ret getBackupTargetById1(proto3.prism.v4.management.GetBackupTargetById1Arg request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetBackupTargetById1Method(), getCallOptions(), request);
    }

    /**
     * <pre>
     * uri: /prism/v4/management/domain-managers/{domainManagerExtId}/backup-targets-new
     * http method: GET
     * List backup targets 
     * Lists backup targets (cluster or object store) configured for a given domain manager.
     * </pre>
     */
    public proto3.prism.v4.management.ListBackupTargets1Ret listBackupTargets1(proto3.prism.v4.management.ListBackupTargets1Arg request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getListBackupTargets1Method(), getCallOptions(), request);
    }

    /**
     * <pre>
     * uri: /prism/v4/management/domain-managers/{domainManagerExtId}/backup-targets-new/{extId}
     * http method: PUT
     * Update backup target 
     * Updates the credentials and/or RPO of the given object store.
     * </pre>
     */
    public proto3.prism.v4.management.UpdateBackupTargetById1Ret updateBackupTargetById1(proto3.prism.v4.management.UpdateBackupTargetById1Arg request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getUpdateBackupTargetById1Method(), getCallOptions(), request);
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
     * uri: /prism/v4/management/domain-managers/{domainManagerExtId}/backup-targets-new
     * http method: POST
     * Create backup target 
     * Creates a cluster or object store as the backup target. For a given Prism Central,
     *there can be up to 3 clusters as backup targets 
     *and 1 object store as backup target. If any cluster or object store is not eligible for backup or 
     *lacks appropriate permissions, the API request will fail. 
     *For object store backup targets, specifying backup policy is mandatory along 
     *with the location of the object store.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<proto3.prism.v4.management.CreateBackupTarget1Ret> createBackupTarget1(
        proto3.prism.v4.management.CreateBackupTarget1Arg request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getCreateBackupTarget1Method(), getCallOptions()), request);
    }

    /**
     * <pre>
     * uri: /prism/v4/management/domain-managers/{domainManagerExtId}/backup-targets-new/{extId}
     * http method: DELETE
     * Delete backup target 
     * Removes cluster/object store from the backup targets. This will stop the cluster/object store 
     *from backing up Prism Central data.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<proto3.prism.v4.management.DeleteBackupTargetById1Ret> deleteBackupTargetById1(
        proto3.prism.v4.management.DeleteBackupTargetById1Arg request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getDeleteBackupTargetById1Method(), getCallOptions()), request);
    }

    /**
     * <pre>
     * uri: /prism/v4/management/domain-managers/{domainManagerExtId}/backup-targets-new/{extId}
     * http method: GET
     * Fetch backup target 
     * Retrieves the backup targets (cluster or object store) from a domain manager and returns the
     *backup configuration and lastSyncTimestamp parameter to the user.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<proto3.prism.v4.management.GetBackupTargetById1Ret> getBackupTargetById1(
        proto3.prism.v4.management.GetBackupTargetById1Arg request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetBackupTargetById1Method(), getCallOptions()), request);
    }

    /**
     * <pre>
     * uri: /prism/v4/management/domain-managers/{domainManagerExtId}/backup-targets-new
     * http method: GET
     * List backup targets 
     * Lists backup targets (cluster or object store) configured for a given domain manager.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<proto3.prism.v4.management.ListBackupTargets1Ret> listBackupTargets1(
        proto3.prism.v4.management.ListBackupTargets1Arg request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getListBackupTargets1Method(), getCallOptions()), request);
    }

    /**
     * <pre>
     * uri: /prism/v4/management/domain-managers/{domainManagerExtId}/backup-targets-new/{extId}
     * http method: PUT
     * Update backup target 
     * Updates the credentials and/or RPO of the given object store.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<proto3.prism.v4.management.UpdateBackupTargetById1Ret> updateBackupTargetById1(
        proto3.prism.v4.management.UpdateBackupTargetById1Arg request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getUpdateBackupTargetById1Method(), getCallOptions()), request);
    }
  }

  private static final int METHODID_CREATE_BACKUP_TARGET1 = 0;
  private static final int METHODID_DELETE_BACKUP_TARGET_BY_ID1 = 1;
  private static final int METHODID_GET_BACKUP_TARGET_BY_ID1 = 2;
  private static final int METHODID_LIST_BACKUP_TARGETS1 = 3;
  private static final int METHODID_UPDATE_BACKUP_TARGET_BY_ID1 = 4;

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
        case METHODID_CREATE_BACKUP_TARGET1:
          serviceImpl.createBackupTarget1((proto3.prism.v4.management.CreateBackupTarget1Arg) request,
              (io.grpc.stub.StreamObserver<proto3.prism.v4.management.CreateBackupTarget1Ret>) responseObserver);
          break;
        case METHODID_DELETE_BACKUP_TARGET_BY_ID1:
          serviceImpl.deleteBackupTargetById1((proto3.prism.v4.management.DeleteBackupTargetById1Arg) request,
              (io.grpc.stub.StreamObserver<proto3.prism.v4.management.DeleteBackupTargetById1Ret>) responseObserver);
          break;
        case METHODID_GET_BACKUP_TARGET_BY_ID1:
          serviceImpl.getBackupTargetById1((proto3.prism.v4.management.GetBackupTargetById1Arg) request,
              (io.grpc.stub.StreamObserver<proto3.prism.v4.management.GetBackupTargetById1Ret>) responseObserver);
          break;
        case METHODID_LIST_BACKUP_TARGETS1:
          serviceImpl.listBackupTargets1((proto3.prism.v4.management.ListBackupTargets1Arg) request,
              (io.grpc.stub.StreamObserver<proto3.prism.v4.management.ListBackupTargets1Ret>) responseObserver);
          break;
        case METHODID_UPDATE_BACKUP_TARGET_BY_ID1:
          serviceImpl.updateBackupTargetById1((proto3.prism.v4.management.UpdateBackupTargetById1Arg) request,
              (io.grpc.stub.StreamObserver<proto3.prism.v4.management.UpdateBackupTargetById1Ret>) responseObserver);
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
              .addMethod(getCreateBackupTarget1Method())
              .addMethod(getDeleteBackupTargetById1Method())
              .addMethod(getGetBackupTargetById1Method())
              .addMethod(getListBackupTargets1Method())
              .addMethod(getUpdateBackupTargetById1Method())
              .build();
        }
      }
    }
    return result;
  }
}
