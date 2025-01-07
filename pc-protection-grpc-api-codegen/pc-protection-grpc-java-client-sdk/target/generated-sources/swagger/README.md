# Java Client For PC Protection PC Client SDK

The Java client for PC Protection PC Client SDK is designed for Java client application developers offering them simple and flexible access to APIs that allows the implementation of PC Protection PC Client SDK.
## Features
- Invoke Nutanix APIs with a simple interface.
- Handle Authentication seamlessly.
- Reduce boilerplate code implementation.
- Use standard methods for installation.
## Version

- API version: v4.0
- Package version: 17.0.0-SNAPSHOT

## Requirements.

- Maven 3.6
- Java 8

## Usage

### Installation

This library is distributed on [Maven Central](https://search.maven.org/). In order to add it as a dependency, please do the following:

#### Using Maven

```xml
<dependency>
  <groupId>com.nutanix.api</groupId>
  <artifactId>pc_protection_pc_client-java-client</artifactId>
  <version>17.0.0-SNAPSHOT</version>
</dependency>
```

#### Using Gradle

```groovy
dependencies {
    implementation("com.nutanix.api:pc_protection_pc_client-java-client:17.0.0-SNAPSHOT")
}
```

## Configuration

The Java client for PC Protection PC Client SDK can be configured with the following parameters

| Parameter | Description                                                                      | Required | Default Value|
|-----------|----------------------------------------------------------------------------------|----------|--------------|
| scheme    | URI scheme for connecting to the cluster (HTTP or HTTPS using SSL/TLS)           | No       | https        |
| host      | IPv4/IPv6 address or FQDN of the cluster to which the client will connect to     | Yes      | N/A          |
| port      | Port on the cluster to which the client will connect to                          | No       | 9440         |
| username  | Username to connect to a cluster                                                 | Yes      | N/A          |
| password  | Password to connect to a cluster                                                 | Yes      | N/A          |
| debugging | Runs the client in debug mode if specified                                       | No       | False        |
| verifySsl | Verify SSL certificate of cluster, the client will connect to                    | No       | True         |
| maxRetryAttempts| Maximum number of retry attempts while connecting to the cluster           | No       | 5            |
| retryInterval| Interval in milliseconds at which retry attempts are made                     | No       | 3000         |
| connectTimeout| Connection timeout in milliseconds for all operations                        | No       | 30000        |
| readTimeout| Read timeout in milliseconds for all operations                                 | No       | 30000        |
| downloadDirectory| Directory where downloaded files will be stored                           | No       | current Working Directory|


### Sample Configuration

```java
import com.nutanix.pri.java.client.ApiClient;

public class Sample {
  public void configureClient() {
    ApiClient client = new ApiClient();
    client.setHost("10.19.50.27"); // IPv4/IPv6 address or FQDN of the cluster
    client.setPort(9440); // Port to which to connect to
    client.setUsername("admin"); // UserName to connect to the cluster
    client.setPassword("password"); // Password to connect to the cluster
  }
}
```

### Authentication
Nutanix APIs currently support two type of authentication schemes:

- **HTTP Basic Authentication**
      - The Java client can be configured using the username and password parameters to send Basic headers along with every request.
- **API Key Authentication**
      - The Java client can be configured to set an API key to send "**X-ntnx-api-key**" header with every request.
  ```java
  import com.nutanix.pri.java.client.ApiClient;

  public class Sample {
    public void configureClient() {
      ApiClient client = new ApiClient();
      client.setApiKey("abcde12345");
    }
  }
  ```

### mTLS Support
The client supports auth via mTLS connections. The client can be configured to use a client certificate and key to authenticate with the server.
To enable mTLS, the codegen plugin needs to have the following additionalProperty:
```xml
<plugin>
<groupId>io.swagger.codegen.v3</groupId>
<artifactId>swagger-codegen-maven-plugin</artifactId>
<version>3.0.10</version>
<executions>
  <execution>
    ...
      <additionalProperties>
        ...
        <additionalProperty>enableMTLS=true</additionalProperty> <!-- ADD THIS PROPERTY -->
      </additionalProperties>
    </configuration>
  </execution>
</executions>
...


```
The mTLS configuration can be set using the following method:
```java
import com.nutanix.pri.java.client.ApiClient;

public class Sample {
  public void configureClient() {
    ApiClient client = new ApiClient();
    client.configureMTLS(trustStorePath, trustStorePassword, keyStorePath, keyStorePassword, privateKeyPassword, privateKeyAlias);
  }
}
```

The parameters are:
- trustStore: Path to the truststore file (String)
- trustStorePassword: Password for the truststore file (String)
- keyStorePath: Path to the keystore file (String)
- keyStorePassword: Password for the keystore file (String)
- privateKeyPassword: Password for the private key (String)
- privateKeyAlias: Alias for the private key (String)

### Retry Mechanism
The client can be configured to retry requests that fail with the following status codes. The numbers of seconds before which the next retry is attempted is determined by the retryInterval:

- [408 - Request Timeout](https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/408)
- [429 - Too Many Requests](https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/429)
- [502 - Bad Gateway](https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/502)
- [503 - Service Unavailable](https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/503)

The client will also redirect requests that fail with [302 - Found](https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/302) to the new location specified in the response header `Location`.
! Note : Within Java SDK maximum retry attempts and maximum redirect attempts are limited by the same variable maxRetryAttempts.

```java
import com.nutanix.pri.java.client.ApiClient;

public class Sample {
  public void configureClient() {
    ApiClient client = new ApiClient();
    client.setMaxRetryAttempts(5); // Max retry attempts while reconnecting on a loss of connection
    client.setRetryInterval(5000); // Interval in ms to use during retry or redirection attempts
  }
}
```

## Usage

### Invoking an operation

```java
import com.nutanix.pri.java.client.ApiClient;
import com.nutanix.pri.java.client.api.DomainManagerBackupsApi;
import com.nutanix.dp1.pri.prism.v4.management.GetBackupTargetApiResponse;

public class Sample {
  public void performOperation() {
    ApiClient client = new ApiClient();
    // Configure the client
    // ...
    DomainManagerBackupsApi domainManagerBackupsApi = new DomainManagerBackupsApi(client);
    String domainManagerExtId = "dC2cDebe-FaeF-ebdb-e303-7f44Adf3293d";
    String extId = "d2ABB8Fe-8EA9-Fad1-575d-8B31C896EF51";
      = domainManagerBackupsApi.getBackupTargetById1(domainManagerExtId, extId);
  }
}
```

### Request Options
The library provides the ability to specify additional options that can be applied directly on the 'ApiClient' object used to make network calls to the API. The library also provides a mechanism to specify operation specific headers.
#### Client headers
The 'ApiClient' can be configured to send additional headers on each request.

```java
import com.nutanix.pri.java.client.ApiClient;

public class Sample {
  public void configureClient() {
    ApiClient client = new ApiClient();
    client.addDefaultHeader("Accept-Encoding","gzip, deflate, br");
  }
}
```
You can also modify the headers sent with each individual operation:

#### Operation specific headers
Nutanix APIs require that concurrent updates are protected using [ETag](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/ETag) headers. This would mean that the [ETag](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/ETag) header received in the response of a fetch (GET) operation should be used as an [If-Match](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/If-Match) header for the modification (PUT) operation.
```java
import com.nutanix.pri.java.client.ApiClient;
import com.nutanix.dp1.pri.prism.v4.management.GetBackupTargetApiResponse;

public class Sample {
  public void performOperation() {
    ApiClient client = new ApiClient();
    // Configure the client
    // ...
    // perform GET call
    DomainManagerBackupsApi domainManagerBackupsApi = new DomainManagerBackupsApi(client);
    String domainManagerExtId = "dC2cDebe-FaeF-ebdb-e303-7f44Adf3293d";
    String extId = "d2ABB8Fe-8EA9-Fad1-575d-8B31C896EF51";
      = domainManagerBackupsApi.getBackupTargetById1(domainManagerExtId, extId);
    // Extract E-Tag Header
    final String eTagHeader = ApiClient.getEtag();
    // ...
    // Perform update call with received E-Tag reference
    BackupTarget backupTarget = (BackupTarget) .getData();
    // initialize/change parameters for update
    HashMap<String, Object> opts = new HashMap<>();
    opts.put("If-Match", eTagHeader);
    domainManagerBackupsApi.updateBackupTargetById1(backupTargetdomainManagerExtId, extId, , opts);
  }
}

```

### List Operations
List Operations for Nutanix APIs support pagination, filtering, sorting and projections. The table below details the parameters that can be used to set the options for pagination etc.

| Parameter | Description
|-----------|----------------------------------------------------------------------------------|
| $page     | specifies the page number of the result set. Must be a positive integer between 0 and the maximum number of pages that are available for that resource. Any number out of this range will lead to no results being returned.|
| $limit    | specifies the total number of records returned in the result set. Must be a positive integer between 0 and 100. Any number out of this range will lead to a validation error. If the limit is not provided a default value of 50 records will be returned in the result set|
| $filter   | allows clients to filter a collection of resources. The expression specified with $filter is evaluated for each resource in the collection, and only items where the expression evaluates to true are included in the response. Expression specified with the $filter must conform to the [OData V4.01 URL](https://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part2-url-conventions.html#sec_SystemQueryOptionfilter) conventions. |
| $orderby  | allows clients to specify the sort criteria for the returned list of objects. Resources can be sorted in ascending order using asc or descending order using desc. If asc or desc are not specified the resources will be sorted in ascending order by default. For example, 'orderby=templateName desc' would get all templates sorted by templateName in desc order. |
| $select   | allows clients to request a specific set of properties for each entity or complex type. Expression specified with the $select must conform to the OData V4.01 URL conventions. If a $select expression consists of a single select item that is an asterisk (i.e., *), then all properties on the matching resource will be returned. |
| $expand   | allows clients to request related resources when a resource that satisfies a particular request is retrieved. Each expanded item is evaluated relative to the entity containing the property being expanded. Other query options can be applied to an expanded property by appending a semicolon-separated list of query options, enclosed in parentheses, to the property name. Permissible system query options are $filter,$select and $orderby. |

List Options can be passed to list operations in order to perform pagination, filtering etc.
```java
// this sample code is not usable directly for real use-case

import com.nutanix.pri.java.client.ApiClient;
import com.nutanix.pri.java.client.api.SampleApi;

public class Sample {
  public void performOperation() {
    ApiClient client = new ApiClient();
    // Configure the client
    // ...
    SampleApi sampleApi = new SampleApi(client);
    final String extId = '66673023168b486898d76bc27e5ce9c2';
    SampleGetResponse sampleResponse = sampleApi.listSample(pageValue,    //$page
                                                            limitValue,   //$limit
                                                            filterValue,  //$filter
                                                            orderbyValue, //$orderby
                                                            selectValue,  //$select
                                                            expandValue  //$expand
                                                           );
  }
}
```

The list of filterable and sortable fields with expansion keys can be found in the documentation [here](https://developers.nutanix.com/).

## API Reference

This library has a full set of [API Reference Documentation](https://developers.nutanix.com/sdk-reference?namespace=prism&version=v4.0&language=java). This documentation is auto-generated, and the location may change.

## License
This library is licensed under Nutanix proprietary license. Full license text is available in [LICENSE](https://developers.nutanix.com/license).

## Contact us
In case of issues please reach out to us at the [mailing list](mailto:sdk@nutanix.com)