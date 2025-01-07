package com.nutanix.pri.java.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nutanix.json.deserializers.ObjectTypeTypedObject;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.http.RequestEntity.BodyBuilder;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.text.ParseException;
import java.time.Duration;
import java.util.*;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.lang.reflect.Method;

import com.nutanix.pri.java.client.auth.Authentication;
import com.nutanix.pri.java.client.auth.HttpBasicAuth;
import com.nutanix.pri.java.client.auth.MutualTLSConfig;
import com.nutanix.pri.java.client.auth.ApiKeyAuth;
import com.nutanix.pri.java.client.auth.OAuth;

import javax.net.ssl.SSLContext;

@Slf4j
@javax.annotation.Generated(value = "com.nutanix.swagger.codegen.generators.JavaClientSDKGenerator", date = "2025-01-06T14:24:28.197+05:30[Asia/Kolkata]")@Component("com.nutanix.pri.java.client.ApiClient")
public class ApiClient {
    public enum CollectionFormat {
        CSV(","), TSV("\t"), SSV(" "), PIPES("|"), MULTI(null);

        private final String separator;
        private CollectionFormat(String separator) {
            this.separator = separator;
        }

        private String collectionToString(Collection<? extends CharSequence> collection) {
            return collection == null ? "" : StringUtils.join(collection, separator);
        }
    }

    private final int MAX_RETRY = 5;

    private final int RETRY_DELAY = 3000;

    private final long DEFAULT_READ_TIMEOUT = 30000;

    private final long DEFAULT_CONNECT_TIMEOUT = 30000;

    private final long MAX_DEFAULT_TIMEOUT = 10800000;

    private boolean debugging = false;

    private HttpHeaders defaultHeaders = new HttpHeaders();

    private String cookie;

    private boolean refreshCookie = true;

    private String scheme = "https";

    private String host = "localhost";

    private int port = 9440;

    private RestTemplate restTemplate;

    private RetryTemplate retryTemplate;

    private int maxRetryAttempts = MAX_RETRY;

    private int retryInterval = RETRY_DELAY;

    private long readTimeout = DEFAULT_READ_TIMEOUT;

    private long connectTimeout = DEFAULT_CONNECT_TIMEOUT;

    private Map<String, Authentication> authentications;

    private HttpStatus statusCode;

    private MultiValueMap<String, String> responseHeaders;

    private DateFormat dateFormat;

    private String downloadDirectory = Paths.get("").toAbsolutePath().toString();

    private final Set<String> binaryMediaTypes = new HashSet<>(Arrays.asList
        ("application/octet-stream", "application/pdf", "application/zip"));
    private final Set<String> textMediaTypes = new HashSet<>(Arrays.asList
        ("text/event-stream", "text/html", "text/xml", "text/csv", "text/javascript", "text/markdown", "text/vcard"));

    /**
     * Flag to check if mTLS is configured
     */
    @Getter(value=AccessLevel.NONE)
    @Setter(value=AccessLevel.NONE)
    private Boolean isMTLSConfigured = false;

    @Getter(value=AccessLevel.NONE)
    @Setter(value=AccessLevel.NONE)
    private MutualTLSConfig mTLSConfig;

    private boolean verifySsl = true;
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public ApiClient() {
        this.restTemplate = buildRestTemplate();
        this.retryTemplate = buildRetryTemplate();
        init();
    }

    @Autowired
    public ApiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.retryTemplate = buildRetryTemplate();
        init();
    }

    private void init() {
        // Use RFC3339 format for date and datetime.
        // See http://xml2rfc.ietf.org/public/rfc/html/rfc3339.html#anchor14
        this.dateFormat = new RFC3339DateFormat();

        // Use UTC as the default time zone.
        this.dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        // Set default User-Agent.
        addDefaultHeader("User-Agent", "Nutanix-pc_protection_pc_client-java-client/17.0.0-SNAPSHOT");

        // Setup authentications (key: authentication name, value: authentication).
        authentications = new HashMap<String, Authentication>();
        authentications.put("apiKeyAuthScheme", (Authentication) new ApiKeyAuth("header", "X-ntnx-api-key"));
        authentications.put("basicAuthScheme", (Authentication) new HttpBasicAuth());
        // Prevent the authentications from being modified.
        authentications = Collections.unmodifiableMap(authentications);
    }

    /**
     * Configure the mTLS settings for the client and build restTemplate.
     *
     * @return RestTemplate
     */
    public ApiClient configureMTLS(String trustStorePath, String trustStorePassword, String keyStorePath,
                                   String keyStorePassword, String privateKeyPassword, String privateKeyAlias) throws Exception {
        this.mTLSConfig = new MutualTLSConfig(trustStorePath, trustStorePassword,
                keyStorePath, keyStorePassword, privateKeyPassword, privateKeyAlias);
        this.isMTLSConfigured = true;
        this.restTemplate = buildRestTemplate();
        return this;
    }

    /**
     * Enable/Disable SSL Verification
     * @param verifySsl flag
     * @throws KeyStoreException if key is not found.
     * @throws NoSuchAlgorithmException if requested cryptographic algorithm is not found.
     * @return ApiClient this client
     */
    public ApiClient setVerifySsl(boolean verifySsl) throws KeyStoreException, NoSuchAlgorithmException {
        this.verifySsl = verifySsl;
        this.restTemplate = buildRestTemplate();
        return this;
    }

    /**
    * Get the current URL scheme for making a connection to the cluster
    * @return String URL scheme
    */
    public String getScheme() {
        return scheme;
    }

    /**
    * Set a URI scheme for connecting to the cluster (HTTP or HTTPS using SSL/TLS)
    * @param scheme URL scheme
    * @return ApiClient this client
    */
    public ApiClient setScheme(String scheme) {
        this.scheme = scheme;
        return this;
    }

    /**
     * Get the current hostname or base URL
     * @return String the hostname
     */
    public String getHost() {
        return host;
    }

    /**
     * Set the hostname for base URL
     * @param host the hostname
     * @return ApiClient this client
     */
    public ApiClient setHost(String host) {
        this.host = host;
        return this;
    }

    /**
     * Get the current response read timeout
     * @return long the response read timeout
     */
    public long getReadTimeout() {
        return readTimeout;
    }

    /**
     * Set the response read timeout
     * @param readTimeout the response read timeout
     * @return ApiClient this client
     */
    public ApiClient setReadTimeout(long readTimeout) {
        this.readTimeout = readTimeout;
        this.restTemplate = buildRestTemplate();
        return this;
    }

    /**
    * Get the current connection timeout
    * @return long the connection timeout
    */
    public long getConnectTimeout() {
        return connectTimeout;
    }

    /**
    * Set the connection timeout
    * @param connectTimeout the connection timeout
    * @return ApiClient this client
    */
    public ApiClient setConnectTimeout(long connectTimeout) {
        this.connectTimeout = connectTimeout;
        this.restTemplate = buildRestTemplate();
        return this;
    }

    private long getValidTimeout(long timeout, long defaultTimeout) {
        if (timeout <= 0) {
            timeout = defaultTimeout;
        } else if (timeout > MAX_DEFAULT_TIMEOUT) {
            timeout = MAX_DEFAULT_TIMEOUT;
        }

        return timeout;
    }

    /**
     * Get the current port for base URL
     * @return int the port
     */
    public int getPort() {
        return port;
    }

    /**
     * Set the port for base URL, which should exclude the semicolon (Default port: 9440)
     * @param port the port
     * @return ApiClient this client
     */
    public ApiClient setPort(int port) {
        this.port = port;
        return this;
    }

    /**
     * Get the number of max retry attempts
     * @return int number of max retry attempts
     */
    public int getMaxRetryAttempts() {
      return maxRetryAttempts;
    }

    /**
     * Set the number of max retry attempts
     * @param maxRetryAttempts number of max retry attempts
     * @return ApiClient this client
     */
    public ApiClient setMaxRetryAttempts(int maxRetryAttempts) {
      this.maxRetryAttempts = maxRetryAttempts;
      this.retryTemplate = buildRetryTemplate();
      return this;
    }

    /**
     * Get the delay between each retry attempt
     * @return int back off period in milliseconds
     */
    public int getRetryInterval() {
      return retryInterval;
    }

    /**
     * Set the delay between each retry attempt
     * @param retryInterval delay between each retry attempt in milliseconds
     * @return ApiClient this client
     */
    public ApiClient setRetryInterval(int retryInterval) {
      this.retryInterval = retryInterval;
      this.retryTemplate = buildRetryTemplate();
      return this;
    }

    /**
     * Directory path to store downloaded files
     * @param path the path of the directory
     */
    public void setDownloadDirectory(String path) {
        this.downloadDirectory = path;
    }

    /**
     * Gets the status code of the previous request
     * @return HttpStatus the status code
     */
    public HttpStatus getStatusCode() {
        return statusCode;
    }

    /**
     * Gets the response headers of the previous request
     * @return MultiValueMap a map of response headers
     */
    public MultiValueMap<String, String> getResponseHeaders() {
        return responseHeaders;
    }

    /**
     * Get authentications (key: authentication name, value: authentication).
     * @return Map the currently configured authentication types
     */
    public Map<String, Authentication> getAuthentications() {
        return authentications;
    }

    /**
     * Get authentication for the given name.
     *
     * @param authName The authentication name
     * @return The authentication, null if not found
     */
    public Authentication getAuthentication(String authName) {
        return authentications.get(authName);
    }

    /**
     * Helper method to set API key value for the first API key authentication.
     * @param apiKey the API key
     */
    public void setApiKey(String apiKey) {
        for (Authentication auth : authentications.values()) {
            if (auth instanceof ApiKeyAuth) {
                ((ApiKeyAuth) auth).setApiKey(apiKey);
                return;
            }
        }
        throw new RuntimeException("No API key authentication is configured!");
    }

    /**
     * Helper method to set API key prefix for the first API key authentication.
     * @param apiKeyPrefix the API key prefix
     */
    public void setApiKeyPrefix(String apiKeyPrefix) {
        for (Authentication auth : authentications.values()) {
            if (auth instanceof ApiKeyAuth) {
                ((ApiKeyAuth) auth).setApiKeyPrefix(apiKeyPrefix);
                return;
            }
        }
        throw new RuntimeException("No API key authentication configured!");
    }

    /**
     * Helper method to set username for the first HTTP basic authentication.
     * @param username the username
     */
    public void setUsername(String username) {
        for (Authentication auth : authentications.values()) {
            if (auth instanceof HttpBasicAuth) {
                ((HttpBasicAuth) auth).setUsername(username);
                return;
            }
        }
        throw new RuntimeException("No HTTP basic authentication configured!");
    }

    /**
     * Helper method to set password for the first HTTP basic authentication.
     * @param password the password
     */
    public void setPassword(String password) {
        for (Authentication auth : authentications.values()) {
            if (auth instanceof HttpBasicAuth) {
                ((HttpBasicAuth) auth).setPassword(password);
                return;
            }
        }
        throw new RuntimeException("No HTTP basic authentication configured!");
    }


    /**
     * Add a default header.
     *
     * @param name The header's name
     * @param value The header's value
     * @return ApiClient this client
     */
    public ApiClient addDefaultHeader(String name, String value) {
        if ("Authorization".equals(name)) {
            this.cookie = null;
        }

        defaultHeaders.add(name, value);
        if (!"Authorization".equals(name)) {
            log.info("Default header {}:{} added to the api client", name, value);
        }

        return this;
    }

    /**
    * Enable or disable debugging
    * @param debugging debug value true or false
    */
    public void setDebugging(boolean debugging) {
        List<ClientHttpRequestInterceptor> currentInterceptors = this.restTemplate.getInterceptors();
        if(debugging) {
            if (currentInterceptors == null) {
                currentInterceptors = new ArrayList<ClientHttpRequestInterceptor>();
            }
            ClientHttpRequestInterceptor interceptor = new ApiClientHttpRequestInterceptor();
            currentInterceptors.add(interceptor);
            this.restTemplate.setInterceptors(currentInterceptors);
        }
        else {
            if (currentInterceptors != null && !currentInterceptors.isEmpty()) {
                Iterator<ClientHttpRequestInterceptor> iter = currentInterceptors.iterator();
                while (iter.hasNext()) {
                    ClientHttpRequestInterceptor interceptor = iter.next();
                    if (interceptor instanceof ApiClientHttpRequestInterceptor) {
                        iter.remove();
                    }
                }
                this.restTemplate.setInterceptors(currentInterceptors);
            }
        }
        this.debugging = debugging;
    }

    /**
     * Check that whether debugging is enabled for this API client.
     * @return boolean true if this client is enabled for debugging, false otherwise
     */
    public boolean isDebugging() {
        return debugging;
    }

    /**
     * Get the date format used to parse/format date parameters.
     * @return DateFormat format
     */
    public DateFormat getDateFormat() {
        return dateFormat;
    }

    /**
     * Set the date format used to parse/format date parameters.
     * @param dateFormat Date format
     * @return ApiClient
     */
    public ApiClient setDateFormat(DateFormat dateFormat) {
        this.dateFormat = dateFormat;
        return this;
    }

    /**
     * Parse the given string into Date object.
     * @param str date to parse
     * @return Date parsed date
     */
    public Date parseDate(String str) {
        try {
            return dateFormat.parse(str);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Format the given Date object into string.
     * @param date date to format
     * @return String formatted date
     */
    public String formatDate(Date date) {
        return dateFormat.format(date);
    }

    /**
    * Format the given OffsetDateTime object into string.
    * @param OffsetDateTime dateTime to format
    * @return String formatted dateTime
    */
    public String formatOffsetDateTime(OffsetDateTime dateTime) {
        DateTimeFormatter ISO_8601_FORMATTER = DateTimeFormatter
        .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        .withZone(ZoneId.of("UTC"));
        if(dateTime.getOffset().equals(ZoneOffset.UTC)) {
            return ISO_8601_FORMATTER.format(dateTime);
        }
        return dateTime.toString();
    }

        /**
     * Format the given parameter object into string.
     * @param param the object to convert
     * @return String the parameter represented as a String
     */
    public String parameterToString(Object param) {
        if (param == null) {
            return "";
        }
        else if (param instanceof OffsetDateTime) {
            return formatOffsetDateTime( (OffsetDateTime) param);
        }
        else if (param instanceof Date) {
            return formatDate( (Date) param);
        }
        else if (param instanceof Collection) {
            StringBuilder b = new StringBuilder();
            for(Object o : (Collection<?>) param) {
                if(b.length() > 0) {
                    b.append(",");
                }
                b.append(String.valueOf(o));
            }
            return b.toString();
        }
        else {
            return String.valueOf(param);
        }
    }

    /**
     * Converts a parameter to a {@link MultiValueMap} for use in REST requests
     * @param collectionFormat The format to convert to
     * @param name The name of the parameter
     * @param value The parameter's value
     * @return a Map containing the String value(s) of the input parameter
     */
    public MultiValueMap<String, String> parameterToMultiValueMap(CollectionFormat collectionFormat, String name, Object value) {
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();

        if (name == null || name.isEmpty() || value == null) {
            return params;
        }

        if(collectionFormat == null) {
            collectionFormat = CollectionFormat.CSV;
        }

        Collection<?> valueCollection = null;
        if (value instanceof Collection) {
            valueCollection = (Collection<?>) value;
        }
        else {
            params.add(name, parameterToString(value));
            return params;
        }

        if (valueCollection.isEmpty()){
            return params;
        }

        if (collectionFormat.equals(CollectionFormat.MULTI)) {
            for (Object item : valueCollection) {
                params.add(name, parameterToString(item));
            }
            return params;
        }

        List<String> values = new ArrayList<String>();
        for(Object o : valueCollection) {
            values.add(parameterToString(o));
        }
        params.add(name, collectionFormat.collectionToString(values));

        return params;
    }

    /*
    * Check if the given {@code String} is a JSON MIME.
    * @param mediaType the input MediaType
    * @return boolean true if the MediaType represents JSON, false otherwise
    */
    public boolean isJsonMime(String mediaType) {
        // "* / *" is default to JSON
        if ("*/*".equals(mediaType)) {
            return true;
        }

        try {
            return isJsonMime(MediaType.parseMediaType(mediaType));
        } catch (InvalidMediaTypeException e) {
        }
        return false;
    }

    /**
     * Check if the given MIME is a JSON MIME.
     * JSON MIME examples:
     *     application/json
     *     application/json; charset=UTF8
     *     APPLICATION/JSON
     * @param mediaType the input MediaType
     * @return boolean true if the MediaType represents JSON, false otherwise
     */
    public boolean isJsonMime(MediaType mediaType) {
        return mediaType != null && (MediaType.APPLICATION_JSON.isCompatibleWith(mediaType) || mediaType.getSubtype().matches("^.*\\+json[;]?\\s*$"));
    }

    /**
     * Select the Accept header's value from the given accepts array:
     *     if JSON exists in the given array, use it;
     *     otherwise use all of them (joining into a string)
     *
     * @param accepts The accepts array to select from
     * @return List The list of MediaTypes to use for the Accept header
     */
    public List<MediaType> selectHeaderAccept(String[] accepts) {
        if (accepts.length == 0) {
            return null;
        }
        return MediaType.parseMediaTypes(StringUtils.join(accepts, ","));
    }

    /**
     * Select the Content-Type header's value from the given array:
     *     if JSON exists in the given array, use it;
     *     otherwise use the first one of the array.
     *
     * @param contentTypes The Content-Type array to select from
     * @return MediaType The Content-Type header to use. If the given array is empty, JSON will be used.
     */
    public MediaType selectHeaderContentType(String[] contentTypes) {
        if (contentTypes.length == 0) {
            return MediaType.APPLICATION_JSON;
        }
        for (String contentType : contentTypes) {
            MediaType mediaType = MediaType.parseMediaType(contentType);
            if (isJsonMime(mediaType)) {
                return mediaType;
            }
        }
        return MediaType.parseMediaType(contentTypes[0]);
    }

    /**
     * Select the body to use for the request
     * @param obj the body object
     * @param formParams the form parameters
     * @param contentType the content type of the request
     * @return Object the selected body
     */
    protected Object selectBody(Object obj, MultiValueMap<String, Object> formParams, MediaType contentType) {
        boolean isForm = MediaType.MULTIPART_FORM_DATA.isCompatibleWith(contentType) || MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(contentType);
        return isForm ? formParams : obj;
    }

    /**
     * Invoke API by sending HTTP request with the given options.
     *
     * @param <T> the return type to use
     * @param path The sub-path of the HTTP URL
     * @param method The request method
     * @param queryParams The query parameters
     * @param body The request body object
     * @param headerParams The header parameters
     * @param formParams The form parameters
     * @param accept The request's Accept header
     * @param contentType The request's Content-Type header
     * @param authNames The authentications to apply
     * @param returnType The return type into which to deserialize the response
     * @return The response body in chosen type
     * @throws org.springframework.web.client.RestClientException
     */
    public <T> T invokeAPI(String path, HttpMethod method, MultiValueMap<String, String> queryParams, Object body, HttpHeaders headerParams, MultiValueMap<String, Object> formParams, List<MediaType> accept, MediaType contentType, String[] authNames, ParameterizedTypeReference<T> returnType) throws RestClientException {
        try {
            return invokeAPIInternal(path, method, queryParams, body, headerParams, formParams, accept, contentType, authNames, returnType);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().equals(HttpStatus.UNAUTHORIZED)) {
                log.error("Server responded with 401 status due to {message}.", e.getMessage());
                this.cookie = null;
                refreshCookie = true;
                log.debug("Retrying request with basic header as server responded with 401 status due to cookie expiration.");
                return invokeAPIInternal(path, method, queryParams, body, headerParams, formParams, accept, contentType, authNames, returnType);
            } else {
                log.error("Unable to make a successful request", e);
                throw e;
            }
        } catch (Exception e) {
            log.error("Unable to make a successful request", e);
            throw e;
        }
    }


    private <T> T invokeAPIInternal(String path, HttpMethod method, MultiValueMap<String, String> queryParams, Object body, HttpHeaders headerParams, MultiValueMap<String, Object> formParams, List<MediaType> accept, MediaType contentType, String[] authNames, ParameterizedTypeReference<T> returnType) throws RestClientException {

        final UriComponentsBuilder builder = UriComponentsBuilder.newInstance().scheme(scheme).host(host).port(port).path(path);
        if (queryParams != null) {
            builder.queryParams(queryParams);
        }

        final BodyBuilder requestBuilder = RequestEntity.method(method, builder.build().toUri());
        if(accept != null && !headerParams.containsKey(HttpHeaders.ACCEPT)) {
            requestBuilder.accept(accept.toArray(new MediaType[accept.size()]));
        }
        if(contentType != null && !headerParams.containsKey(HttpHeaders.CONTENT_TYPE)) {
            requestBuilder.contentType(contentType);
        }

        if(defaultHeaders.getOrDefault("NTNX-Request-Id", null) == null
            && headerParams.getOrDefault("NTNX-Request-Id", null) == null) {
            UUID generatedID = UUID.randomUUID();
            headerParams.add("NTNX-Request-Id", String.valueOf(generatedID));
        }
        if(!headerParams.containsKey(HttpHeaders.IF_MATCH)) {
          addEtagReferenceToHeader(body, requestBuilder);
        }
        headerParams.putAll(defaultHeaders);

        if(cookie != null) {
            requestBuilder.header(HttpHeaders.COOKIE, cookie);
            if (headerParams.containsKey(HttpHeaders.AUTHORIZATION)) {
                headerParams.remove(HttpHeaders.AUTHORIZATION);
            }
        }
        else {
            updateParamsForAuth(authNames, queryParams, headerParams);
        }
        addHeadersToRequest(headerParams, requestBuilder);

        RequestEntity<Object> requestEntity = requestBuilder.body(selectBody(body, formParams, contentType));

        return sendRequest(requestEntity, returnType);
    }

    @SneakyThrows
    private <T> T sendRequest(RequestEntity<Object> requestEntity, ParameterizedTypeReference<T> returnType) {
        List<HttpStatus> retryStatusList = Arrays.asList(HttpStatus.SERVICE_UNAVAILABLE, HttpStatus.GATEWAY_TIMEOUT, HttpStatus.REQUEST_TIMEOUT, HttpStatus.TOO_MANY_REQUESTS);
        AtomicReference<T> body = new AtomicReference<>();
        retryTemplate.execute(context -> {
            try {
                restTemplate.getMessageConverters().removeIf(element -> element instanceof CustomTextHttpMessageConverter<?>);
                restTemplate.getMessageConverters().add(new CustomTextHttpMessageConverter<T>(returnType, textMediaTypes));
                ResponseEntity<T> responseEntity = restTemplate.exchange(requestEntity, returnType);
                statusCode = responseEntity.getStatusCode();
                responseEntity = handleRedirection(responseEntity, requestEntity, returnType, body);
                if (responseEntity.getBody() instanceof OneOfBinaryResponseWrapper) {
                    OneOfBinaryResponseWrapper responseWrapper = (OneOfBinaryResponseWrapper) responseEntity.getBody();
                    responseWrapper.setResponseEntity(responseEntity);
                    body.set((T) responseWrapper);
                } else {
                    body.set(getResponseBody(responseEntity, returnType));
                }
            } catch (HttpStatusCodeException e) {
                if(!retryStatusList.contains(e.getStatusCode())) {
                    context.setExhaustedOnly();
                }
                log.error("Unable to make a successful request", e);
                throw e;
            }

            return null;
        });

        return body.get();
    }

    private <T> ResponseEntity<T> handleRedirection(ResponseEntity<T> responseEntity, RequestEntity<Object> originalRequestEntity, ParameterizedTypeReference<T> returnType, AtomicReference<T> body) {
        if (statusCode.is3xxRedirection() && responseEntity.getHeaders() != null && responseEntity.getHeaders().getLocation() != null) {
            String location = responseEntity.getHeaders().getLocation().toString();
            log.info("Redirecting to {}", location);
            UriComponentsBuilder builder = UriComponentsBuilder.newInstance().fromPath(location);
            builder.query(originalRequestEntity.getUrl().getQuery());
            BodyBuilder requestBuilder = RequestEntity.method(originalRequestEntity.getMethod(), location);
            requestBuilder.body(originalRequestEntity.getBody());
            if (responseEntity.getHeaders().containsKey("X-Redirect-Token")) {
                log.info("Populating X-Redirect-Token header to Cookie");
                String cookie = responseEntity.getHeaders().get("X-Redirect-Token").toString();
                requestBuilder.header(HttpHeaders.COOKIE, cookie);
            }
            requestBuilder.headers(originalRequestEntity.getHeaders());
            responseEntity = restTemplate.exchange(requestBuilder.build(), returnType);
            statusCode = responseEntity.getStatusCode();
            log.debug("Redirected response status code: {}", statusCode);
        }
        return responseEntity;
    }

    public Map<String, Object> getFileDownloadResponse(Resource resource, HttpHeaders headers) throws IOException {
        String filename = String.format("downloaded_file_%s", java.lang.System.currentTimeMillis());
        if (headers.containsKey(HttpHeaders.CONTENT_DISPOSITION)) {
            String contentDisposition = headers.get(HttpHeaders.CONTENT_DISPOSITION).get(0);
            filename = contentDisposition.replaceFirst(".*filename=\\\"?([^\\\"]+)\\\"?.*", "$1");
            int index = filename.lastIndexOf('.');
            filename = String.format("%s_%s%s", filename.substring(0, index), java.lang.System.currentTimeMillis(), filename.substring(index));
        } else {
            log.warn("{} header is missing, downloading file to {}", HttpHeaders.CONTENT_DISPOSITION, filename);
        }

        Path path = Paths.get(this.downloadDirectory, filename);
        StreamUtils.copy(resource.getInputStream(), new FileOutputStream(path.toFile()));
        java.util.Map<String, Object> retVal = new HashMap<>();
        retVal.put("path", path);
        retVal.put("$objectType", path.getClass());
        return retVal;
    }

    /**
     * Get the response body based on satus code and updates the cookies to api client
     *
     * @param responseEntity Response Entity of rest template
     * @param returnType Type reference for the dto type
     * @return
     * <ul>
     *  <li>204 - returns null</li>
     *  <li>Other 2xx - Adds ETag to $reserved if possible and returns the body</li>
     *  <li>For all others - throws RestClientException</li>
     * </ul>
     * @param <T>
     */
    public <T> T getResponseBody(ResponseEntity<T> responseEntity, ParameterizedTypeReference<T> returnType) {
        // Set Cookie information to reuse in subsequent requests for a valid response
        List<String> responseCookies = responseEntity.getHeaders().get(HttpHeaders.SET_COOKIE);
        if (refreshCookie && responseCookies != null && cookie == null) {
            StringBuilder sb = new StringBuilder();
            for (String responseCookie : responseCookies) {
                sb.append(responseCookie.split(";")[0]).append(";");
            }
            sb.deleteCharAt(sb.length() - 1);
            cookie = sb.toString();
            refreshCookie = false;
        }

        if (responseEntity.getStatusCode() == HttpStatus.NO_CONTENT) {
            return null;
        } else if (responseEntity.getStatusCode().is2xxSuccessful()) {
            if (returnType == null) {
                return null;
            }

            if (responseEntity.getHeaders().getOrDefault(HttpHeaders.ETAG, null) != null && responseEntity.getBody() instanceof ObjectTypeTypedObject) {
                ((ObjectTypeTypedObject) responseEntity.getBody()).get$reserved().put(HttpHeaders.ETAG, responseEntity.getHeaders().get(HttpHeaders.ETAG).get(0));
            }

            return responseEntity.getBody();
        } else {
            // The error handler built into the RestTemplate should handle 400 and 500 series errors.
            cookie = null;
            String message = "API returned " + statusCode + " and it wasn't handled by the RestTemplate error handler";
            log.error(message);
            throw new RestClientException(message);
        }
    }

    private void addEtagReferenceToHeader(Object body, BodyBuilder requestBuilder) {
        if (body instanceof ObjectTypeTypedObject) {
            ObjectTypeTypedObject bodyInstance = (ObjectTypeTypedObject) body;
            Map<String, Object> reservedFields = bodyInstance.get$reserved();
            if (MapUtils.isNotEmpty(reservedFields) && reservedFields.containsKey(HttpHeaders.ETAG)) {
                String etagReference = String.valueOf(reservedFields.get(HttpHeaders.ETAG));
                requestBuilder.header(HttpHeaders.IF_MATCH, etagReference);
            }
        }
    }

    /**
    * Get ETag from an object if exists.
    *
    * The ETag is usually provided in the response of the GET API calls,
    * which can further be used in other HTTP operations.
    *
    * @param object Object from which ETag needs to be retrieved
    * @return String ETag header in the object if it's an API response object, otherwise null
    */
    public static String getEtag(Object object) {
        String etag = null;
        if (object != null && object instanceof ObjectTypeTypedObject) {
            ObjectTypeTypedObject typedObject = (ObjectTypeTypedObject) object;
            if (MapUtils.isNotEmpty(typedObject.get$reserved())) {
                Optional<Entry<String, Object>> etagEntry = typedObject.get$reserved().entrySet().stream().filter(x -> {
                                                                return HttpHeaders.ETAG.toLowerCase()
                                                                            .equals(x.getKey().toLowerCase());
                                                            }).findFirst();
                etag = etagEntry.isPresent() ? (String) etagEntry.get().getValue() : null;
            }
        }

        return etag;
    }

    /**
     * Add headers to the request that is being built
     * @param headers The headers to add
     * @param requestBuilder The current request
     */
    private void addHeadersToRequest(HttpHeaders headers, BodyBuilder requestBuilder) {
        for (Entry<String, List<String>> entry : headers.entrySet()) {
            List<String> values = entry.getValue();
            for(String value : values) {
                if (value != null) {
                    requestBuilder.header(entry.getKey(), value);
                }
            }
        }
    }

    /**
     * Build the RestTemplate used to make HTTP requests.
     * @return RestTemplate
     */
    private RestTemplate buildRestTemplate() {
        RestTemplateBuilder restTemplateBuilder= new RestTemplateBuilder();
        if (isMTLSConfigured) {
            try {
                SSLConnectionSocketFactory sslConnectionSocketFactory = mTLSConfig.getSSLConnectionSocket();
                CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory).disableCookieManagement().disableRedirectHandling().build();
                restTemplateBuilder = restTemplateBuilder.requestFactory(() -> new HttpComponentsClientHttpRequestFactory(httpClient));
            } catch (Exception e) {
                log.error("Failed to configure mTLS settings", e);
                throw new RestClientException("Failed to configure mTLS settings", e);
            }
        } else if (this.verifySsl == false) {
            try {
                TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
                SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
                                                                   .loadTrustMaterial(null, acceptingTrustStrategy)
                                                                   .build();
                SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext,
                                                                                         new NoopHostnameVerifier());
                CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslSocketFactory).disableCookieManagement().disableRedirectHandling().build();
                restTemplateBuilder = restTemplateBuilder.requestFactory(() -> new HttpComponentsClientHttpRequestFactory(httpClient));
            } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
                String message = "Cannot disable SSL Verification, perform setVerifySsl(true) or retry";
                log.error(message, e);
                throw new RestClientException(message, e);
            }
        } else {
            restTemplateBuilder = restTemplateBuilder.requestFactory(restTemplateBuilder::buildRequestFactory);
                    }
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        OBJECT_MAPPER.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true);
        OBJECT_MAPPER.setDefaultPropertyInclusion(JsonInclude.Include.NON_EMPTY);
        OBJECT_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        MappingJackson2HttpMessageConverter jacksonConverter = new MappingJackson2HttpMessageConverter(OBJECT_MAPPER);
        ResourceHttpMessageConverter resourceConverter = new ResourceHttpMessageConverter();
        CustomBinaryResponseMessageConverter  customBinaryResponseConverter =
            new CustomBinaryResponseMessageConverter(jacksonConverter, resourceConverter, binaryMediaTypes);
        restTemplateBuilder = restTemplateBuilder.additionalMessageConverters(jacksonConverter)
                                                .additionalMessageConverters(resourceConverter);
        Duration readTimeoutDuration = Duration.ofMillis(getValidTimeout(this.readTimeout, DEFAULT_READ_TIMEOUT));
        Duration connectTimeoutDuration = Duration.ofMillis(getValidTimeout(this.connectTimeout, DEFAULT_CONNECT_TIMEOUT));
        restTemplateBuilder = restTemplateBuilder.setConnectTimeout(connectTimeoutDuration).setReadTimeout(readTimeoutDuration);
        RestTemplate restTemplate = restTemplateBuilder.build();
        restTemplate.getMessageConverters().add(0, customBinaryResponseConverter);
        return restTemplate;
    }

    /**
     * Build the RetryTemplate used to retry failed HTTP requests.
     * @return RetryTemplate
     */
    private RetryTemplate buildRetryTemplate(){
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(maxRetryAttempts);
        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(retryInterval);
        RetryTemplate template = new RetryTemplate();
        template.setRetryPolicy(retryPolicy);
        template.setBackOffPolicy(backOffPolicy);
        return template;
    }

    /**
     * Update query and header parameters based on authentication settings.
     *
     * @param authNames The authentications to apply
     * @param queryParams The query parameters
     * @param headerParams The header parameters
     */
    private void updateParamsForAuth(String[] authNames, MultiValueMap<String, String> queryParams, HttpHeaders headerParams) {
        for (String authName : authNames) {
            Authentication auth = authentications.get(authName);
            if (auth == null) {
                log.error("Authentication undefined: {}", authName);
                throw new RestClientException("Authentication undefined: " + authName);
            }

            auth.applyToParams(queryParams, headerParams);
        }
    }

        @Slf4j
    private static class ApiClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
            logRequest(request, body);
            ClientHttpResponse response = execution.execute(request, body);
            response = new BufferedClientHttpResponse(response);
            logResponse(response);
            return response;
        }

        private void logRequest(HttpRequest request, byte[] body) throws UnsupportedEncodingException {
            log.info("{} {}" , request.getMethod(), request.getURI());
            log.debug("Request Headers:\n{}" , headersToString(request.getHeaders()));
            if (isOctetStream(request.getHeaders())) {
                log.debug("Request Body is an octet stream");
            } else {
                log.debug("Request Body:\n{}",new String(body, StandardCharsets.UTF_8));
            }
        }

        private void logResponse(ClientHttpResponse response) throws IOException {
            log.info("Reply: {} {}" , response.getRawStatusCode(), response.getStatusText());
            log.debug("Response Headers:\n{}" , headersToString(response.getHeaders()));
            if (isOctetStream(response.getHeaders())) {
                log.debug("Response Body is an octet stream");
            } else {
                log.debug("Response Body:\n{}", bodyToString(response.getBody()));
            }
        }

        private boolean isOctetStream(HttpHeaders headers) {
            return headers != null && headers.containsKey(HttpHeaders.CONTENT_TYPE)
                   && headers.get(HttpHeaders.CONTENT_TYPE).stream().anyMatch(content -> content.toString().equals(MediaType.APPLICATION_OCTET_STREAM_VALUE));
        }

        private String headersToString(HttpHeaders headers) {
            StringBuilder builder = new StringBuilder();
            for(Entry<String, List<String>> entry : headers.entrySet()) {
                builder.append(entry.getKey()).append("=[");
                for(String value : entry.getValue()) {
                    builder.append(value).append(",");
                }
                builder.setLength(builder.length() - 1); // Get rid of trailing comma
                builder.append("]\n");
            }
            builder.setLength(builder.length() - 1); // Get rid of trailing comma
            return builder.toString();
        }
        
        private String bodyToString(InputStream body) throws IOException {
            StringBuilder builder = new StringBuilder();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(body, StandardCharsets.UTF_8));
            String line = bufferedReader.readLine();
            while (line != null) {
                builder.append(line).append(System.lineSeparator());
                line = bufferedReader.readLine();
            }
            bufferedReader.close();
            return builder.toString();
        }


        private class BufferedClientHttpResponse implements ClientHttpResponse {

            private final ClientHttpResponse response;
            private byte[] body;

            public BufferedClientHttpResponse(ClientHttpResponse response) {
                this.response = response;
            }

            @Override
            public HttpStatus getStatusCode() throws IOException {
                return response.getStatusCode();
            }

            @Override
            public int getRawStatusCode() throws IOException {
                return response.getRawStatusCode();
            }

            @Override
            public String getStatusText() throws IOException {
                return response.getStatusText();
            }

            @Override
            public void close() {
                response.close();
            }

            @Override
            public HttpHeaders getHeaders() {
                return response.getHeaders();
            }

            @Override
            public InputStream getBody() throws IOException {
                if (body == null) {
                    body = StreamUtils.copyToByteArray(response.getBody());
                }

                return new ByteArrayInputStream(body);
            }
        }
    }

    @Slf4j
    public static class CustomBinaryResponseMessageConverter extends AbstractHttpMessageConverter<OneOfBinaryResponseWrapper> {

        private final MappingJackson2HttpMessageConverter jackson2HttpMessageConverter;
        private final ResourceHttpMessageConverter resourceHttpMessageConverter;
        private final Set<String> supportedBinaryMediaTypes;
    
        public CustomBinaryResponseMessageConverter(MappingJackson2HttpMessageConverter jackson2HttpMessageConverter,
                                                   ResourceHttpMessageConverter resourceHttpMessageConverter,
                                                   Set<String> supportedBinaryMediaTypes) {
            this.jackson2HttpMessageConverter = jackson2HttpMessageConverter;
            this.resourceHttpMessageConverter = resourceHttpMessageConverter;
            this.supportedBinaryMediaTypes = supportedBinaryMediaTypes;

            // Set supprted media types
            List<MediaType> supportedMediaTypes = supportedBinaryMediaTypes.stream()
                    .map(type -> MediaType.parseMediaType(type)).collect(Collectors.toList());
            supportedMediaTypes.add(MediaType.APPLICATION_JSON);
            setSupportedMediaTypes(supportedMediaTypes);
        }

        @Override
        protected boolean supports(Class<?> clazz) {
            return ApiClient.OneOfBinaryResponseWrapper.class.equals(clazz);
        }

        @Override
        protected OneOfBinaryResponseWrapper readInternal(Class<? extends OneOfBinaryResponseWrapper> clazz,
                HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
            String contentType = CollectionUtils.isNotEmpty(inputMessage.getHeaders().get(HttpHeaders.CONTENT_TYPE)) ?
                    inputMessage.getHeaders().get(HttpHeaders.CONTENT_TYPE).get(0) : null;

            if (supportedBinaryMediaTypes.contains(contentType)) {
                Resource resource = resourceHttpMessageConverter.read(Resource.class, inputMessage);
                return new OneOfBinaryResponseWrapper(null, resource, true, null);
            } else if (MediaType.APPLICATION_JSON_VALUE.equals(contentType)) {
                String jsonObject = StreamUtils.copyToString(inputMessage.getBody(), StandardCharsets.UTF_8);
                return new OneOfBinaryResponseWrapper(jsonObject, null, false, null);
            }

            return null;
        }

        @Override
        protected void writeInternal(OneOfBinaryResponseWrapper oneOfBinaryResponseWrapper,
                HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
            // This is without any implementation as this converter is only for GET operations.
        }
    }

    public static class CustomTextHttpMessageConverter<T> extends AbstractHttpMessageConverter<T> {
        private final Class<T> clazz;

        public CustomTextHttpMessageConverter(ParameterizedTypeReference<T> typeRef, Set<String> supportedTextMediaTypes) {
            // This converter is only for text responses
            List<MediaType> supportedMediaTypes = supportedTextMediaTypes.stream()
                .map(type -> MediaType.parseMediaType(type)).collect(Collectors.toList());
            setSupportedMediaTypes(supportedMediaTypes);
            this.clazz = (Class<T>) typeRef.getType();
        }

        @Override
        protected boolean supports(Class<?> clazz) {
            return clazz.isAssignableFrom(this.clazz);
        }

        @Override
        protected T readInternal(Class<? extends T> clazz, HttpInputMessage inputMessage)
        throws IOException {
            try {
                // Capture the text content from the input stream as byte[]
                byte[] textData = readBytesFromBody(inputMessage.getBody());
                T instance = clazz.getDeclaredConstructor().newInstance();

                // Set the "data" field with the raw byte[] content
                Method setDataMethod = instance.getClass().getMethod("setDataInWrapper", Object.class);
                setDataMethod.invoke(instance, textData);

                return instance;
            } catch (Exception e) {
                throw new IOException("Error reading text data", e);
            }
        }

        @Override
        protected void writeInternal(T t, HttpOutputMessage outputMessage) throws IOException {
            // Implement for use cases involving writing text data
        }

        private byte[] readBytesFromBody(InputStream body) throws IOException {
            try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(); InputStream inputStream = body) {
                byte[] buffer = new byte[1024];
                int bytesRead;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                }
                return byteArrayOutputStream.toByteArray();
            }
        }
    }

    @Data
    @AllArgsConstructor
    public static class OneOfBinaryResponseWrapper {
        private String jsonObject;
        private Resource resourceObject;
        private boolean isBinary;
        private ResponseEntity responseEntity;
    }
}
