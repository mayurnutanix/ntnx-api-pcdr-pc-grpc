// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: proto3/prism/v4/management/management.proto

package proto3.prism.v4.management;

public interface ListBackupTargetsApiResponseOrBuilder extends
    // @@protoc_insertion_point(interface_extends:proto3.prism.v4.management.ListBackupTargetsApiResponse)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * 
   * </pre>
   *
   * <code>.proto3.prism.v4.management.BackupTargetArrayWrapper backup_target_array_data = 31007;</code>
   * @return Whether the backupTargetArrayData field is set.
   */
  boolean hasBackupTargetArrayData();
  /**
   * <pre>
   * 
   * </pre>
   *
   * <code>.proto3.prism.v4.management.BackupTargetArrayWrapper backup_target_array_data = 31007;</code>
   * @return The backupTargetArrayData.
   */
  proto3.prism.v4.management.BackupTargetArrayWrapper getBackupTargetArrayData();
  /**
   * <pre>
   * 
   * </pre>
   *
   * <code>.proto3.prism.v4.management.BackupTargetArrayWrapper backup_target_array_data = 31007;</code>
   */
  proto3.prism.v4.management.BackupTargetArrayWrapperOrBuilder getBackupTargetArrayDataOrBuilder();

  /**
   * <pre>
   * 
   * </pre>
   *
   * <code>.proto3.prism.v4.management.ErrorResponseWrapper error_response_data = 400;</code>
   * @return Whether the errorResponseData field is set.
   */
  boolean hasErrorResponseData();
  /**
   * <pre>
   * 
   * </pre>
   *
   * <code>.proto3.prism.v4.management.ErrorResponseWrapper error_response_data = 400;</code>
   * @return The errorResponseData.
   */
  proto3.prism.v4.management.ErrorResponseWrapper getErrorResponseData();
  /**
   * <pre>
   * 
   * </pre>
   *
   * <code>.proto3.prism.v4.management.ErrorResponseWrapper error_response_data = 400;</code>
   */
  proto3.prism.v4.management.ErrorResponseWrapperOrBuilder getErrorResponseDataOrBuilder();

  /**
   * <pre>
   * 
   * </pre>
   *
   * <code>.proto3.common.v1.response.ApiResponseMetadata metadata = 1001;</code>
   * @return Whether the metadata field is set.
   */
  boolean hasMetadata();
  /**
   * <pre>
   * 
   * </pre>
   *
   * <code>.proto3.common.v1.response.ApiResponseMetadata metadata = 1001;</code>
   * @return The metadata.
   */
  proto3.common.v1.response.ApiResponseMetadata getMetadata();
  /**
   * <pre>
   * 
   * </pre>
   *
   * <code>.proto3.common.v1.response.ApiResponseMetadata metadata = 1001;</code>
   */
  proto3.common.v1.response.ApiResponseMetadataOrBuilder getMetadataOrBuilder();

  public proto3.prism.v4.management.ListBackupTargetsApiResponse.DataCase getDataCase();
}
