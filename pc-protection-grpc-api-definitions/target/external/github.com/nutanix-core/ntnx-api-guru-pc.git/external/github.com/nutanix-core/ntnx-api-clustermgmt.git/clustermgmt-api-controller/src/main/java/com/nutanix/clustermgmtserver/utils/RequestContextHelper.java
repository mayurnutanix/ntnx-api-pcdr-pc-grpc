package com.nutanix.clustermgmtserver.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import com.nutanix.api.utils.links.ApiLinkUtils;
import com.nutanix.clustermgmtserver.exceptions.ClustermgmtServiceException;
import com.nutanix.net.RpcProto;
import com.nutanix.api.utils.auth.IAMTokenClaims;
import com.nutanix.util.base.UuidUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Objects;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestContextHelper {

  private static String FULL_VERSION_HEADER_NAME = "Full-Version";
  private static String FORWARDED_HOST_HEADER_NAME = "X-FORWARDED-HOST";
  private static String errorAfterRequestIdCreation = "error_after_request_id_creation";
  private static String errorInjectionHeader = "NTNX-Clustermgmt-Error-Injection";

  private static ServletRequestAttributes getServletRequestAttributes() {
    return (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
  }

  public static HttpServletRequest getRequest() {
    final ServletRequestAttributes attrs = getServletRequestAttributes();
    return attrs.getRequest();
  }

  public static String getVersionHeader() {
    final HttpServletRequest request = getRequest();
    return request.getHeader(FULL_VERSION_HEADER_NAME);
  }

  /**
   * Returns the IAMTokenClaims for the current request.
   * @return IAMTokenClaims or null if not available.
   */
  public static IAMTokenClaims getIAMTokenClaims () {
    final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null) {
      log.warn("Null authentication from SecurityContextHolder");
      return null;
    }

    final Object principal = authentication.getPrincipal();
    if (!(principal instanceof IAMTokenClaims)) {
      log.warn("Authentication principal is not an IAMTokenClaims instance: {}", principal);
      return null;
    }

    return (IAMTokenClaims) principal;
  }

  /**
   * Returns the user UUID for the current request.
   * @return The user UUID or null in case of error.
   */
  public static String getUserUUID() {
    final IAMTokenClaims iamTokenClaims = getIAMTokenClaims();
    if (iamTokenClaims == null) {
      return null;
    }
    return iamTokenClaims.getUserUUID();
  }

  public static RpcProto.RpcRequestContext getRpcRequestContext() {
    try {
      IAMTokenClaims iamTokenClaims = getIAMTokenClaims();
      if(!Objects.isNull(iamTokenClaims)) {
        String userName = iamTokenClaims.getSubject();
        ByteString userUUID = UuidUtils.getByteStringFromUUID(iamTokenClaims.getUserUUID());

        ServletRequestAttributes attributes =
          (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest httpRequest = attributes.getRequest();
        String userIp = ApiLinkUtils.getOriginHostPort(httpRequest).split(":")[0];

        log.debug("RpcRequestContext: user name {}, UUID {}, IP {}", userName,
          userUUID, userIp);
        return RpcProto.RpcRequestContext.newBuilder().
          setUserName(userName).setUserUuid(userUUID).setUserIp(userIp).
          setShouldAuthorize(true).build();
      }
    } catch (Exception ex) {
      log.error("Failed to form RPC request context, error: ", ex);
    }
    return RpcProto.RpcRequestContext.newBuilder().build();
  }

  public static String getHostname() {
    final HttpServletRequest request = getRequest();
    if (StringUtils.isNotBlank(request.getHeader(FORWARDED_HOST_HEADER_NAME))) {
      return request.getHeader(FORWARDED_HOST_HEADER_NAME).split(",")[0];
    }
    return "localhost:9440";
  }

  public static void ParseErrorInjectionHeader() throws ClustermgmtServiceException {
    final HttpServletRequest request = getRequest();
    try {
      String jsonString = request.getHeader(errorInjectionHeader);

      ErrorInjection.setErrorAfterRequestIdCreation(false);

      if (jsonString == null) {
        return;
      }
      ObjectMapper mapper = new ObjectMapper();

      Map<String, Boolean> jsonObj = mapper.readValue(jsonString, Map.class);

      if (jsonObj.containsKey(errorAfterRequestIdCreation)) {
        ErrorInjection.setErrorAfterRequestIdCreation(jsonObj.get(errorAfterRequestIdCreation));
      }
    }
    catch (JsonProcessingException e) {
      // JSON parsing exception.
      throw new ClustermgmtServiceException(e.getMessage());
    }
  }

}
