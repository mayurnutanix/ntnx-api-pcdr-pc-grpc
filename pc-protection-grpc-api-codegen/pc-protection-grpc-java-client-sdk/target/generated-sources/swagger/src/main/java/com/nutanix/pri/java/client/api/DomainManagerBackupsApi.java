package com.nutanix.pri.java.client.api;

import com.nutanix.pri.java.client.ApiClient;




import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;

@lombok.extern.slf4j.Slf4j
@javax.annotation.Generated(value = "com.nutanix.swagger.codegen.generators.JavaClientSDKGenerator", date = "2025-01-06T14:24:28.197+05:30[Asia/Kolkata]")@Component("com.nutanix.pri.java.client.api.DomainManagerBackupsApi")
public class DomainManagerBackupsApi {
    private ApiClient apiClient;
    private final Set<String> headersToSkip;

    public DomainManagerBackupsApi() {
        this(new ApiClient());
    }

    @Autowired
    public DomainManagerBackupsApi(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.headersToSkip = new HashSet<>(Arrays.asList("authorization", "cookie", "host", "user-agent"));
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Creates a cluster or object store as the backup target. For a given Prism Central, there can be up to 3 clusters as backup targets  and 1 object store as backup target. If any cluster or object store is not eligible for backup or  lacks appropriate permissions, the API request will fail.  For object store backup targets, specifying backup policy is mandatory along  with the location of the object store. 
     * <p><b>202</b> - Returns the task ID corresponding to the create backup target request. 
     * <p><b>4XX</b> - Client error response
     * <p><b>5XX</b> - Server error response
     * @param domainManagerExtId A unique identifier for the domain manager.

     * @param body The body parameter
     * @param args Additional arguments
     * @return com.nutanix.dp1.pri.prism.v4.management.CreateBackupTargetApiResponse
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    
    public com.nutanix.dp1.pri.prism.v4.management.CreateBackupTargetApiResponse createBackupTarget1(String domainManagerExtId, com.nutanix.dp1.pri.prism.v4.management.BackupTarget body, Map<String, Object> ... args) throws RestClientException {
        // Check for optional argument map
        Map<String, Object> argMap = args.length > 0 ? args[0] : new HashMap<String, Object>();

        Object postBody = body;
        // verify the required parameter 'domainManagerExtId' is set
        if (domainManagerExtId == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'domainManagerExtId' when calling createBackupTarget1");
        }
        // verify the required parameter 'body' is set
        if (body == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'body' when calling createBackupTarget1");
        }
        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("domainManagerExtId", domainManagerExtId);
        String uriPath = UriComponentsBuilder.fromPath("/api/prism/v4.0/management/domain-managers/{domainManagerExtId}/backup-targets-new").buildAndExpand(uriVariables).toUriString();
        
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        argMap.forEach((key, value) -> {
            if (!this.headersToSkip.contains(key.toLowerCase())) {
                String stringValue = apiClient.parameterToString(value);
                if (stringValue != null && !stringValue.trim().isEmpty()) {
                    headerParams.add(key, apiClient.parameterToString(value));
                }
            }
        });


        final String[] accepts = { 
            "application/json"
         };
        final List<MediaType> accept = apiClient.selectHeaderAccept(accepts);
        final String[] contentTypes = { "application/json" };

        final MediaType contentType = apiClient.selectHeaderContentType(contentTypes);

        String[] authNames = new String[] { "apiKeyAuthScheme", "basicAuthScheme" };


        ParameterizedTypeReference<com.nutanix.dp1.pri.prism.v4.management.CreateBackupTargetApiResponse> returnType = new ParameterizedTypeReference<com.nutanix.dp1.pri.prism.v4.management.CreateBackupTargetApiResponse>() {};

        return apiClient.invokeAPI(uriPath, HttpMethod.POST, queryParams, postBody, headerParams, formParams, accept, contentType, authNames, returnType);
    }
    /**
     * Removes cluster/object store from the backup targets. This will stop the cluster/object store  from backing up Prism Central data. 
     * <p><b>202</b> - Returns the task ID corresponding to the delete backup target request. 
     * <p><b>4XX</b> - Client error response
     * <p><b>5XX</b> - Server error response
     * @param domainManagerExtId A unique identifier for the domain manager.

     * @param extId A globally unique identifier of an instance that is suitable for external consumption.

     * @param args Additional arguments
     * @return com.nutanix.dp1.pri.prism.v4.management.DeleteBackupTargetApiResponse
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    
    public com.nutanix.dp1.pri.prism.v4.management.DeleteBackupTargetApiResponse deleteBackupTargetById1(String domainManagerExtId, String extId, Map<String, Object> ... args) throws RestClientException {
        // Check for optional argument map
        Map<String, Object> argMap = args.length > 0 ? args[0] : new HashMap<String, Object>();

        Object postBody = null;
        // verify the required parameter 'domainManagerExtId' is set
        if (domainManagerExtId == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'domainManagerExtId' when calling deleteBackupTargetById1");
        }
        // verify the required parameter 'extId' is set
        if (extId == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'extId' when calling deleteBackupTargetById1");
        }
        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("domainManagerExtId", domainManagerExtId);
        uriVariables.put("extId", extId);
        String uriPath = UriComponentsBuilder.fromPath("/api/prism/v4.0/management/domain-managers/{domainManagerExtId}/backup-targets-new/{extId}").buildAndExpand(uriVariables).toUriString();
        
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        argMap.forEach((key, value) -> {
            if (!this.headersToSkip.contains(key.toLowerCase())) {
                String stringValue = apiClient.parameterToString(value);
                if (stringValue != null && !stringValue.trim().isEmpty()) {
                    headerParams.add(key, apiClient.parameterToString(value));
                }
            }
        });


        final String[] accepts = { 
            "application/json"
         };
        final List<MediaType> accept = apiClient.selectHeaderAccept(accepts);
        final String[] contentTypes = {  };

        final MediaType contentType = apiClient.selectHeaderContentType(contentTypes);

        String[] authNames = new String[] { "apiKeyAuthScheme", "basicAuthScheme" };


        ParameterizedTypeReference<com.nutanix.dp1.pri.prism.v4.management.DeleteBackupTargetApiResponse> returnType = new ParameterizedTypeReference<com.nutanix.dp1.pri.prism.v4.management.DeleteBackupTargetApiResponse>() {};

        return apiClient.invokeAPI(uriPath, HttpMethod.DELETE, queryParams, postBody, headerParams, formParams, accept, contentType, authNames, returnType);
    }
    /**
     * Retrieves the backup targets (cluster or object store) from a domain manager and returns the backup configuration and lastSyncTimestamp parameter to the user. 
     * <p><b>200</b> - Returns the backup target details corresponding to the cluster/object store configuration. 
     * <p><b>4XX</b> - Client error response
     * <p><b>5XX</b> - Server error response
     * @param domainManagerExtId A unique identifier for the domain manager.

     * @param extId A globally unique identifier of an instance that is suitable for external consumption.

     * @param args Additional arguments
     * @return com.nutanix.dp1.pri.prism.v4.management.GetBackupTargetApiResponse
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    
    public com.nutanix.dp1.pri.prism.v4.management.GetBackupTargetApiResponse getBackupTargetById1(String domainManagerExtId, String extId, Map<String, Object> ... args) throws RestClientException {
        // Check for optional argument map
        Map<String, Object> argMap = args.length > 0 ? args[0] : new HashMap<String, Object>();

        Object postBody = null;
        // verify the required parameter 'domainManagerExtId' is set
        if (domainManagerExtId == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'domainManagerExtId' when calling getBackupTargetById1");
        }
        // verify the required parameter 'extId' is set
        if (extId == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'extId' when calling getBackupTargetById1");
        }
        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("domainManagerExtId", domainManagerExtId);
        uriVariables.put("extId", extId);
        String uriPath = UriComponentsBuilder.fromPath("/api/prism/v4.0/management/domain-managers/{domainManagerExtId}/backup-targets-new/{extId}").buildAndExpand(uriVariables).toUriString();
        
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        argMap.forEach((key, value) -> {
            if (!this.headersToSkip.contains(key.toLowerCase())) {
                String stringValue = apiClient.parameterToString(value);
                if (stringValue != null && !stringValue.trim().isEmpty()) {
                    headerParams.add(key, apiClient.parameterToString(value));
                }
            }
        });


        final String[] accepts = { 
            "application/json"
         };
        final List<MediaType> accept = apiClient.selectHeaderAccept(accepts);
        final String[] contentTypes = {  };

        final MediaType contentType = apiClient.selectHeaderContentType(contentTypes);

        String[] authNames = new String[] { "apiKeyAuthScheme", "basicAuthScheme" };


        ParameterizedTypeReference<com.nutanix.dp1.pri.prism.v4.management.GetBackupTargetApiResponse> returnType = new ParameterizedTypeReference<com.nutanix.dp1.pri.prism.v4.management.GetBackupTargetApiResponse>() {};

        return apiClient.invokeAPI(uriPath, HttpMethod.GET, queryParams, postBody, headerParams, formParams, accept, contentType, authNames, returnType);
    }
    /**
     * Lists backup targets (cluster or object store) configured for a given domain manager. 
     * <p><b>200</b> - Returns a list of backup clusters/object store backing up the domain manager. 
     * <p><b>4XX</b> - Client error response
     * <p><b>5XX</b> - Server error response
     * @param domainManagerExtId A unique identifier for the domain manager.

     * @param args Additional arguments
     * @return com.nutanix.dp1.pri.prism.v4.management.ListBackupTargetsApiResponse
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    
    public com.nutanix.dp1.pri.prism.v4.management.ListBackupTargetsApiResponse listBackupTargets1(String domainManagerExtId, Map<String, Object> ... args) throws RestClientException {
        // Check for optional argument map
        Map<String, Object> argMap = args.length > 0 ? args[0] : new HashMap<String, Object>();

        Object postBody = null;
        // verify the required parameter 'domainManagerExtId' is set
        if (domainManagerExtId == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'domainManagerExtId' when calling listBackupTargets1");
        }
        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("domainManagerExtId", domainManagerExtId);
        String uriPath = UriComponentsBuilder.fromPath("/api/prism/v4.0/management/domain-managers/{domainManagerExtId}/backup-targets-new").buildAndExpand(uriVariables).toUriString();
        
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        argMap.forEach((key, value) -> {
            if (!this.headersToSkip.contains(key.toLowerCase())) {
                String stringValue = apiClient.parameterToString(value);
                if (stringValue != null && !stringValue.trim().isEmpty()) {
                    headerParams.add(key, apiClient.parameterToString(value));
                }
            }
        });


        final String[] accepts = { 
            "application/json"
         };
        final List<MediaType> accept = apiClient.selectHeaderAccept(accepts);
        final String[] contentTypes = {  };

        final MediaType contentType = apiClient.selectHeaderContentType(contentTypes);

        String[] authNames = new String[] { "apiKeyAuthScheme", "basicAuthScheme" };


        ParameterizedTypeReference<com.nutanix.dp1.pri.prism.v4.management.ListBackupTargetsApiResponse> returnType = new ParameterizedTypeReference<com.nutanix.dp1.pri.prism.v4.management.ListBackupTargetsApiResponse>() {};

        return apiClient.invokeAPI(uriPath, HttpMethod.GET, queryParams, postBody, headerParams, formParams, accept, contentType, authNames, returnType);
    }
    /**
     * Updates the credentials and/or RPO of the given object store. 
     * <p><b>202</b> - Returns the task ID corresponding to the update backup target request. 
     * <p><b>4XX</b> - Client error response
     * <p><b>5XX</b> - Server error response
     * @param domainManagerExtId A unique identifier for the domain manager.

     * @param extId A globally unique identifier of an instance that is suitable for external consumption.

     * @param body The body parameter
     * @param args Additional arguments
     * @return com.nutanix.dp1.pri.prism.v4.management.UpdateBackupTargetApiResponse
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    
    public com.nutanix.dp1.pri.prism.v4.management.UpdateBackupTargetApiResponse updateBackupTargetById1(String domainManagerExtId, String extId, com.nutanix.dp1.pri.prism.v4.management.BackupTarget body, Map<String, Object> ... args) throws RestClientException {
        // Check for optional argument map
        Map<String, Object> argMap = args.length > 0 ? args[0] : new HashMap<String, Object>();

        Object postBody = body;
        // verify the required parameter 'domainManagerExtId' is set
        if (domainManagerExtId == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'domainManagerExtId' when calling updateBackupTargetById1");
        }
        // verify the required parameter 'extId' is set
        if (extId == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'extId' when calling updateBackupTargetById1");
        }
        // verify the required parameter 'body' is set
        if (body == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'body' when calling updateBackupTargetById1");
        }
        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("domainManagerExtId", domainManagerExtId);
        uriVariables.put("extId", extId);
        String uriPath = UriComponentsBuilder.fromPath("/api/prism/v4.0/management/domain-managers/{domainManagerExtId}/backup-targets-new/{extId}").buildAndExpand(uriVariables).toUriString();
        
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        argMap.forEach((key, value) -> {
            if (!this.headersToSkip.contains(key.toLowerCase())) {
                String stringValue = apiClient.parameterToString(value);
                if (stringValue != null && !stringValue.trim().isEmpty()) {
                    headerParams.add(key, apiClient.parameterToString(value));
                }
            }
        });


        final String[] accepts = { 
            "application/json"
         };
        final List<MediaType> accept = apiClient.selectHeaderAccept(accepts);
        final String[] contentTypes = { "application/json" };

        final MediaType contentType = apiClient.selectHeaderContentType(contentTypes);

        String[] authNames = new String[] { "apiKeyAuthScheme", "basicAuthScheme" };


        ParameterizedTypeReference<com.nutanix.dp1.pri.prism.v4.management.UpdateBackupTargetApiResponse> returnType = new ParameterizedTypeReference<com.nutanix.dp1.pri.prism.v4.management.UpdateBackupTargetApiResponse>() {};

        return apiClient.invokeAPI(uriPath, HttpMethod.PUT, queryParams, postBody, headerParams, formParams, accept, contentType, authNames, returnType);
    }
}
