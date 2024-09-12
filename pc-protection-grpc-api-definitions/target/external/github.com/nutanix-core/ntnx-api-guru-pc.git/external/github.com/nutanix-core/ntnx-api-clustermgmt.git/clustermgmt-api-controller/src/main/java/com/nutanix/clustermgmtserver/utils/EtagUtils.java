package com.nutanix.clustermgmtserver.utils;

import com.nutanix.clustermgmtserver.exceptions.ClustermgmtServiceException;
import org.springframework.http.HttpStatus;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.HttpHeaders.IF_NONE_MATCH;

@Slf4j
public class EtagUtils {
  private EtagUtils() { }

  /**
   * Function to return the appropriate response status
   * @param request - Request Object
   * @return Response status code
   */
  public static HttpStatus getHttpStatus(final HttpServletRequest request, final String eTag)
    throws ClustermgmtServiceException {
    final String ifNoneMatchHeaderValue = request.getHeader(IF_NONE_MATCH);
    if (eTag.equals(ifNoneMatchHeaderValue)) {
      return HttpStatus.NOT_MODIFIED;
    }
    else {
      return HttpStatus.OK;
    }
  }
}
