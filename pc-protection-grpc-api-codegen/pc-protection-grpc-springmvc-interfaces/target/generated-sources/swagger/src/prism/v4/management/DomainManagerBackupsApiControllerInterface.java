/*
 * Generated file ..
 *
 * Product version: 17.0.0-SNAPSHOT
 *
 * Part of the PC Protection PC Client SDK
 *
 * (c) 2025 Nutanix Inc.  All rights reserved
 *
 */

package prism.v4.management;

import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;
import java.util.concurrent.CompletableFuture;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.validation.Valid;
import javax.validation.constraints.*;
import com.nutanix.devplatform.annotation.GetValidator;
import com.nutanix.devplatform.annotation.ArgName;

import java.util.List;
    @javax.annotation.Generated(value = "com.nutanix.swagger.codegen.generators.SpringMvcApiInterfaceGenerator", date = "2025-01-06T14:24:19.110+05:30[Asia/Kolkata]")
@Validated
public interface DomainManagerBackupsApiControllerInterface {
        /**
         * This operation supports the following parameters:
         * 
         *  Query Params
         *   
         *  Path Params
         *   

         *  Header Params
         *   

         *  Body Params
         *      @RequestBody dp1.pri.prism.v4.management.BackupTarget body,

         *  Form Params
         *   

         * 
         *  Query Params
         *   
         *  Path Params
         *   @PathVariable("domainManagerExtId") String domainManagerExtId,

         *  Header Params
         *   

         *  Body Params
         *   

         *  Form Params
         *   

         * 
         */
        public static final String CREATE_BACKUP_TARGET1_URI = "/prism/v4/management/domain-managers/{domainManagerExtId}/backup-targets-new";
        @RequestMapping(value = CREATE_BACKUP_TARGET1_URI, 
            produces = { "application/json" }, 
            consumes = { "application/json" },
        method = RequestMethod.POST)
        @com.nutanix.prism.rbac.RbacPermissions(
            operationsSupported = { "Create_Domain_Manager_Backup_Target" },
            entityType = "prism_central", 
            basedInIDF = false,
            serviceType = "Prism",
            metadataProvider = com.nutanix.prism.rbac.metadata.provider.AllowAllMetadataProvider.class,
            clusterSpecificRequestBuilder = com.nutanix.prism.rbac.request.builder.DefaultRequestBuilder.class,
            operationHandler = com.nutanix.prism.rbac.SingleOperationHandler.class,
            operationResponseEvaluator = com.nutanix.prism.rbac.SingleOperationResponseEvaluator.class,
            legacyRolesSupported = { "Internal Super Admin", "Super Admin", "Prism Admin", "Domain Manager Admin" },
            isListCall = false
        )
        CompletableFuture<ResponseEntity<MappingJacksonValue>> createBackupTarget1(

   @RequestBody dp1.pri.prism.v4.management.BackupTarget body,

@PathVariable("domainManagerExtId") String domainManagerExtId,



 @RequestParam Map<String, String> allQueryParams, HttpServletRequest request, HttpServletResponse response);
        /**
         * This operation supports the following parameters:
         * 
         *  Query Params
         *   
         *  Path Params
         *   @PathVariable("domainManagerExtId") String domainManagerExtId,

         *  Header Params
         *   

         *  Body Params
         *   

         *  Form Params
         *   

         * 
         *  Query Params
         *   
         *  Path Params
         *   @PathVariable("extId") String extId,

         *  Header Params
         *   

         *  Body Params
         *   

         *  Form Params
         *   

         * 
         */
        public static final String DELETE_BACKUP_TARGET_BY_ID1_URI = "/prism/v4/management/domain-managers/{domainManagerExtId}/backup-targets-new/{extId}";
        @RequestMapping(value = DELETE_BACKUP_TARGET_BY_ID1_URI, 
            produces = { "application/json" }, 
        method = RequestMethod.DELETE)
        @com.nutanix.prism.rbac.RbacPermissions(
            operationsSupported = { "Delete_Domain_Manager_Backup_Target" },
            entityType = "prism_central", 
            basedInIDF = false,
            serviceType = "Prism",
            metadataProvider = com.nutanix.prism.rbac.metadata.provider.AllowAllMetadataProvider.class,
            clusterSpecificRequestBuilder = com.nutanix.prism.rbac.request.builder.DefaultRequestBuilder.class,
            operationHandler = com.nutanix.prism.rbac.SingleOperationHandler.class,
            operationResponseEvaluator = com.nutanix.prism.rbac.SingleOperationResponseEvaluator.class,
            legacyRolesSupported = { "Internal Super Admin", "Super Admin", "Prism Admin", "Domain Manager Admin" },
            isListCall = false
        )
        CompletableFuture<ResponseEntity<MappingJacksonValue>> deleteBackupTargetById1(@PathVariable("domainManagerExtId") String domainManagerExtId,



@PathVariable("extId") String extId,



 @RequestParam Map<String, String> allQueryParams, HttpServletRequest request, HttpServletResponse response);
        /**
         * This operation supports the following parameters:
         * 
         *  Query Params
         *   
         *  Path Params
         *   @PathVariable("domainManagerExtId") String domainManagerExtId,

         *  Header Params
         *   

         *  Body Params
         *   

         *  Form Params
         *   

         * 
         *  Query Params
         *   
         *  Path Params
         *   @PathVariable("extId") String extId,

         *  Header Params
         *   

         *  Body Params
         *   

         *  Form Params
         *   

         * 
         */
        public static final String GET_BACKUP_TARGET_BY_ID1_URI = "/prism/v4/management/domain-managers/{domainManagerExtId}/backup-targets-new/{extId}";
        @RequestMapping(value = GET_BACKUP_TARGET_BY_ID1_URI, 
            produces = { "application/json" }, 
        method = RequestMethod.GET)
        @com.nutanix.prism.rbac.RbacPermissions(
            operationsSupported = { "View_Domain_Manager_Backup_Target" },
            entityType = "prism_central", 
            basedInIDF = false,
            serviceType = "Prism",
            metadataProvider = com.nutanix.prism.rbac.metadata.provider.AllowAllMetadataProvider.class,
            clusterSpecificRequestBuilder = com.nutanix.prism.rbac.request.builder.DefaultRequestBuilder.class,
            operationHandler = com.nutanix.prism.rbac.SingleOperationHandler.class,
            operationResponseEvaluator = com.nutanix.prism.rbac.SingleOperationResponseEvaluator.class,
            legacyRolesSupported = { "Internal Super Admin", "Super Admin", "Prism Admin", "Prism Viewer", "Domain Manager Admin", "Domain Manager Viewer" },
            isListCall = false
        )
        @GetValidator
        CompletableFuture<ResponseEntity<MappingJacksonValue>> getBackupTargetById1(@PathVariable("domainManagerExtId") @Pattern(regexp = "^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$")   @ArgName("domainManagerExtId-path") String domainManagerExtId,



@PathVariable("extId") @Pattern(regexp = "^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$")   @ArgName("extId-path") String extId,



 @RequestParam Map<String, String> allQueryParams, HttpServletRequest request, HttpServletResponse response);
        /**
         * This operation supports the following parameters:
         * 
         *  Query Params
         *   
         *  Path Params
         *   @PathVariable("domainManagerExtId") String domainManagerExtId,

         *  Header Params
         *   

         *  Body Params
         *   

         *  Form Params
         *   

         * 
         */
        public static final String LIST_BACKUP_TARGETS1_URI = "/prism/v4/management/domain-managers/{domainManagerExtId}/backup-targets-new";
        @RequestMapping(value = LIST_BACKUP_TARGETS1_URI, 
            produces = { "application/json" }, 
        method = RequestMethod.GET)
        @com.nutanix.prism.rbac.RbacPermissions(
            operationsSupported = { "View_Domain_Manager_Backup_Target" },
            entityType = "prism_central", 
            basedInIDF = false,
            serviceType = "Prism",
            metadataProvider = com.nutanix.prism.rbac.metadata.provider.AllowAllMetadataProvider.class,
            clusterSpecificRequestBuilder = com.nutanix.prism.rbac.request.builder.DefaultRequestBuilder.class,
            operationHandler = com.nutanix.prism.rbac.SingleOperationHandler.class,
            operationResponseEvaluator = com.nutanix.prism.rbac.SingleOperationResponseEvaluator.class,
            legacyRolesSupported = { "Internal Super Admin", "Super Admin", "Prism Admin", "Prism Viewer", "Domain Manager Admin", "Domain Manager Viewer" },
            isListCall = true
        )
        @GetValidator
        CompletableFuture<ResponseEntity<MappingJacksonValue>> listBackupTargets1(@PathVariable("domainManagerExtId") @Pattern(regexp = "^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$")   @ArgName("domainManagerExtId-path") String domainManagerExtId,



 @RequestParam Map<String, String> allQueryParams, HttpServletRequest request, HttpServletResponse response);
        /**
         * This operation supports the following parameters:
         * 
         *  Query Params
         *   
         *  Path Params
         *   

         *  Header Params
         *   

         *  Body Params
         *      @RequestBody dp1.pri.prism.v4.management.BackupTarget body,

         *  Form Params
         *   

         * 
         *  Query Params
         *   
         *  Path Params
         *   @PathVariable("domainManagerExtId") String domainManagerExtId,

         *  Header Params
         *   

         *  Body Params
         *   

         *  Form Params
         *   

         * 
         *  Query Params
         *   
         *  Path Params
         *   @PathVariable("extId") String extId,

         *  Header Params
         *   

         *  Body Params
         *   

         *  Form Params
         *   

         * 
         */
        public static final String UPDATE_BACKUP_TARGET_BY_ID1_URI = "/prism/v4/management/domain-managers/{domainManagerExtId}/backup-targets-new/{extId}";
        @RequestMapping(value = UPDATE_BACKUP_TARGET_BY_ID1_URI, 
            produces = { "application/json" }, 
            consumes = { "application/json" },
        method = RequestMethod.PUT)
        @com.nutanix.prism.rbac.RbacPermissions(
            operationsSupported = { "Update_Domain_Manager_Backup_Target" },
            entityType = "prism_central", 
            basedInIDF = false,
            serviceType = "Prism",
            metadataProvider = com.nutanix.prism.rbac.metadata.provider.AllowAllMetadataProvider.class,
            clusterSpecificRequestBuilder = com.nutanix.prism.rbac.request.builder.DefaultRequestBuilder.class,
            operationHandler = com.nutanix.prism.rbac.SingleOperationHandler.class,
            operationResponseEvaluator = com.nutanix.prism.rbac.SingleOperationResponseEvaluator.class,
            legacyRolesSupported = { "Internal Super Admin", "Super Admin", "Prism Admin", "Domain Manager Admin" },
            isListCall = false
        )
        CompletableFuture<ResponseEntity<MappingJacksonValue>> updateBackupTargetById1(

   @RequestBody dp1.pri.prism.v4.management.BackupTarget body,

@PathVariable("domainManagerExtId") String domainManagerExtId,



@PathVariable("extId") String extId,



 @RequestParam Map<String, String> allQueryParams, HttpServletRequest request, HttpServletResponse response);
}

