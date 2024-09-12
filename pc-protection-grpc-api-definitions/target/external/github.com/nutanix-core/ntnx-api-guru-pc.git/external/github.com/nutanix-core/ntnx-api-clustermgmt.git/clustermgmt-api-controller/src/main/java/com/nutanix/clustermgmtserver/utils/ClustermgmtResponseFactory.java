/*
 * Copyright (c) 2021 Nutanix Inc. All rights reserved.
 *
 * Author: rakesh.falak@nutanix.com
 */

package com.nutanix.clustermgmtserver.utils;

import com.nutanix.api.utils.error.AppMessageBuilder;
import com.nutanix.api.utils.stats.service.exception.StatsGatewayServiceException;
import com.nutanix.clustermgmtserver.exceptions.ClustermgmtServiceException;
import com.nutanix.clustermgmtserver.exceptions.ErrorCode;
import com.nutanix.clustermgmtserver.links.ApiLinkFactory;
import com.nutanix.util.base.ValidationException;
import dp1.clu.clustermgmt.v4.error.AppMessage;
import dp1.clu.clustermgmt.v4.error.ErrorResponse;
import dp1.clu.common.v1.config.Flag;
import dp1.clu.common.v1.config.MessageSeverity;
import dp1.clu.common.v1.response.ApiLink;
import dp1.clu.common.v1.response.ApiResponseMetadata;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Mapper
interface AppMessageMapper {

  AppMessageMapper INSTANCE = Mappers.getMapper(AppMessageMapper.class);

  default AppMessage standardAppMessageToClustermgmtAppMessage(final com.nutanix.devplatform.messages.models.AppMessage standardAppMessage) {
    final AppMessage appMessage = new AppMessage();
    appMessage.setMessage(standardAppMessage.getMessage());
    appMessage.setCode(standardAppMessage.getCode());
    appMessage.setSeverity(MessageSeverity.ERROR);
    return appMessage;
  }

}


@Slf4j
public class ClustermgmtResponseFactory {
  private static final String DEFAULT_ARTIFACT_BASE_PATH = "/home/nutanix/api_artifacts";

  /**
   * Function to return the base path for api artifacts folder.
   * Purpose of having this function is to mock the base path in UT.
   * @return The base path to artifact folder.
   */
  private static String getBasePath() {
    return DEFAULT_ARTIFACT_BASE_PATH;
  }

  public static ErrorResponse createStandardErrorResponse (final Throwable cause) {
    int errorCode = ErrorCode.CLUSTERMGMT_SERVICE_RPC_ERROR.getStandardCode();
    if (cause instanceof ClustermgmtServiceException) {
      errorCode = ((ClustermgmtServiceException) cause).getStandardCode();
    } else if (cause instanceof ValidationException) {
      errorCode = ErrorCode.CLUSTERMGMT_INVALID_INPUT.getStandardCode();
    } else if (cause instanceof StatsGatewayServiceException) {
      errorCode = ErrorCode.CLUSTERMGMT_SERVICE_RPC_ERROR.getStandardCode();
    }

    final Map<String, String> arguments = new HashMap<>();
    if (errorCode == ErrorCode.CLUSTERMGMT_SERVICE_RPC_ERROR.getStandardCode()
      || errorCode == ErrorCode.CLUSTERMGMT_INVALID_INPUT.getStandardCode()
      || errorCode == ErrorCode.CLUSTERMGMT_SERVICE_NOT_SUPPORTED_ENTITY.getStandardCode()
      || errorCode == ErrorCode.CLUSTERMGMT_SERVICE_DUPLICATE_HOST_NAME_ERROR.getStandardCode()
      || errorCode == ErrorCode.CLUSTERMGMT_ZKCONFIG_ERROR.getStandardCode()
      || errorCode == ErrorCode.CLUSTERMGMT_SERVICE_UNKNOWN_ENTITY.getStandardCode()
      || errorCode == ErrorCode.CLUSTERMGMT_ODATA_ERROR.getStandardCode()
      || errorCode == ErrorCode.CLUSTERMGMT_IDEMPOTENCY_ERROR.getStandardCode()){
      arguments.put("reason", cause.getMessage());
    }

    final com.nutanix.devplatform.messages.models.AppMessage standardAppMessage =
      AppMessageBuilder.buildAppMessage("en-US", getBasePath(), "clustermgmt", errorCode + "", arguments);

    final AppMessage appMessage = AppMessageMapper.INSTANCE.standardAppMessageToClustermgmtAppMessage(standardAppMessage);

    final ErrorResponse errorResponse = new ErrorResponse();
    errorResponse.setErrorInWrapper(Collections.singletonList(appMessage));
    return errorResponse;
  }

  /**
   * Create a new ApiResponseMetadata for List API response.
   * @param totalEntityCount Total number of entities present in the DB.
   * @param allQueryParams Map of query parameters.
   * @return a new ApiResponseMetadata with the total entity count, pagination flag and pagination links.
   */
  public static ApiResponseMetadata createListMetadata(final Integer totalEntityCount,
                                                       final Map<String, String> allQueryParams) {
    ApiResponseMetadata metadata = new ApiResponseMetadata();

    if (totalEntityCount != null) {
      // Set the Total Number of Available Entities
      metadata.setTotalAvailableResults(totalEntityCount);

      // Add all pagination links
      if(allQueryParams.containsKey("$limit") && allQueryParams.containsKey("$page")) {
        metadata.setLinks(ApiLinkFactory.paginationLinks(totalEntityCount, allQueryParams));
      }

      // Set the isPaginated Flag
      List<Flag> flags = metadata.getFlags();
      flags.forEach(flag -> {
        if ("isPaginated".equals(flag.getName())) {
          flag.setValue(true);
        }
      });
      metadata.setFlags(flags);
    }

    return metadata;
  }

  public static ApiResponseMetadata createMetadataFor(
    final List<ApiLink> apiLinks, final boolean hasError, final boolean isPaginated) {

    ApiResponseMetadata metadata = new ApiResponseMetadata();
    metadata.setLinks(apiLinks);
    List<Flag> flags = metadata.getFlags();
    flags.forEach(flag -> {
      if ("hasError".equals(flag.getName())) {
        flag.setValue(hasError);
      }
      if ("isPaginated".equals(flag.getName())) {
        flag.setValue(isPaginated);
      }
    });
    metadata.setFlags(flags);
    return metadata;
  }

}
