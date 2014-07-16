/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.i18n;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dsmagadx
 */
public enum ErrorCode {

    OK(0, "OK"), 
    SYSTEM_ERROR(1,"System error: %s. More information is available in the server log."), 
//    AUTH_FAILED(1000,"Authentication Failed"), 
//    SQL_ERROR(1001, "SQL Error"), 
    UNKNOWN_ERROR(2,"Unknown error. More information is available in the server log."),  // Used in APIClient
//    GENERAL_ERROR(1003,"Error in attestation service"), 
//    DUPLICATE_HOST_NAME(1004,"Duplicate Host Name"), 
//    TA_ERROR (1005, "TrustAgent Error"), 
//    VALIDATION_ERROR(1006,"Validation Error"),  // used to be 1005 "Invalid Parameter" in AH ErrorCode
//    WLM_SERVICE_ERROR(1007,"WLM Service Error"), 
//    TRUST_ERROR(1008,"Trust Verification Error"),
//    DATA_ERROR(1009,"Data Error"),
//    AS_ERROR(1200,"AS Error"), // this used to be 1004, same as duplicate host name error. changed to 1200
//    WML_ERROR(1201,"WLM Error"), // this one is from AH ErrorCode, is there a difference from 1007 WLM Service Error?
//    HOST_NOT_FOUND(2000,"Host not found"), 
//    NETWORK_ERROR(2001,"Network error"), 
//    VMW_TPM_NOT_SUPPORTED(3000,"VMWare - Host does not support TXT"), 
//    VMWARE_ERROR(3001, "VMWare Error"),
//    DUPLICATE_ENTITY_ERROR(4001,"Entity already configured in the system."),
    
    // HTTP error codes
    HTTP_INVALID_REQUEST(400, "HTTP 400 INVALID REQUEST"),
    HTTP_UNAUTHORIZED(401, "HTTP 401 UNAUTHORIZED"),
    HTTP_FORBIDDEN(403, "HTTP 403 FORBIDDEN"),
    HTTP_NOT_FOUND(404, "HTTP 404 NOT FOUND"),
    HTTP_INTERNAL_SERVER_ERROR(500, "HTTP 500 INTERNAL SERVER ERROR"),

    AS_ASYNC_TIMEOUT(1002,"Asynchronous operation timed out after %d seconds"),
    AS_HOST_NOT_FOUND(1003,"Host '%s' not found."),
    AS_BIOS_INCORRECT(1004,"Bios '%s' or version '%s' is incorrect."),
    AS_VMM_INCORRECT(1004,"VMM '%s' or version '%s' is incorrect."),
    AS_HOST_EXISTS(1005,"Host '%s' already exists."),
    AS_IPADDRESS_EXISTS(1006,"IPAddress '%s' already used."),
    AS_MISSING_MANIFEST(1007,"MLE '%s' Version '%s' Manifest data is missing."),
    AS_MISSING_INPUT(1008,"Missing or invalid input '%s'."),
    AS_OPERATION_NOT_SUPPORTED(1009,"Operation not supported.%s" ),
    AS_QUOTE_VERIFY_COMMAND_FAILED(1010,"TPM quote verification failed.Command error code %d"),
    AS_HOST_COMMUNICATION_ERROR(1011,"Unable to communicate with host: %s"), // error message
    AS_TRUST_AGENT_ERROR(1012,"Trust Agent failed with code '%d' and message '%s'. See the Trust Agent log located at /var/log/tagent.log for more details."),
    AS_ENCRYPTION_ERROR(1013,"Encryption of data failed due to error %s"),
    AS_VMW_TPM_NOT_SUPPORTED(1014,"VMWare - Host '%s' does not support TXT"),
    AS_PCR_NOT_FOUND(1015,"PCR '%s' no found in host manifest."),
    AS_MISSING_PCR_MANIFEST(1016,"Missing Manifest data for PCR '%d'" ),
    AS_CONFIGURATION_ERROR(1017,"Configuration Error: %s "),
    AS_TRUST_AGENT_DAA_ERROR(1018,"DAA Error: %s . See the Trust Agent log located at /var/log/tagent.log for more details."),
    AS_MISSING_MLE_REQD_MANIFEST_LIST(1019,"Missing reqired manifest list for MLE '%s' Version '%s'"),
    AS_HOST_MANIFEST_MISSING_PCRS (1020,"Host Manifest is missing required PCRs."),
    AS_VMWARE_INVALID_CONNECT_STRING(1021,"Input VMWare connect string '%s' is invalid."),
    AS_HOST_NOT_FOUND_IN_VCENTER(1022,"Host '%s' is not found in VCenter."),
    AS_PCR_MANIFEST_MISSING(1023,"Pcr Manifest for PCR '%s' is missing in MLE Id '%d' associated to host '%s"),
    AS_TRUST_AGENT_CONNNECT_TIMED_OUT(1024,"Unable to connect to Trust Agent on '%s:%d'. Timed out after %d seconds. See the Trust Agent log located at /var/log/tagent.log for more details."),
    AS_TRUST_AGENT_INVALID_RESPONSE(1025, "Invalid response from host: %s. See the Trust Agent log located at /var/log/tagent.log for more details."),
    AS_INTEL_TXT_NOT_ENABLED(1026, "Host does not have Intel TXT enabled: %s"),
    AS_INVALID_AIK_CERTIFICATE(1027, "Cannot validate AIK for '%s' against known Privacy CAs"),
    AS_TPM_NOT_SUPPORTED(1028,"Host '%s' does not support TXT."),
    AS_CITRIX_ERROR(1029, "Citrix error"),
   AS_INVALID_ASSET_TAG_CERTIFICATE_HASH(1030, "Invalid asset tag certificate hash specified."),
    AS_INVALID_ASSET_TAG_CERTIFICATE(1031, "Invalid asset tag certificate specified."),
    AS_HOST_SPECIFIED_IS_CURRENTLY_NOT_MAPPED_TO_ASSET_TAG_CERTIFICATE(1032, "Host specified is currently not mapped to any asset tag certificate."),
    AS_MLE_DOES_NOT_EXIST(1033, "MLE '%s' of version '%s' is not configured in the system."),
    AS_INVALID_BIOS_MLE(1034, "BIOS MLE specified is not valid. %s"),
    AS_INVALID_VMM_MLE(1035, "VMM MLE specified is not valid. %s"),
    AS_INVALID_INPUT(1036, "Input specified is not valid."),
    AS_TLS_KEYSTORE_ERROR(1037, "Error reading TLS key from keystore."),
    AS_NOT_EDITABLE_PARAMETER(1038, "Specified parameter %s cannot be updated"),
    AS_DUPLICATE_AIK_PUBLIC_KEY(1039, "AIK public key with fingerprint '%s' already exists"),
 

    // Below error codes are for general white list service errors
    AS_REGISTER_HOST_ERROR(1200, "Error during host registration: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    AS_UPDATE_HOST_ERROR(1201, "Error during host update: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    AS_DELETE_HOST_ERROR(1202, "Error during host deletion: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    AS_VERIFY_HOST_ERROR(1203, "Error during verification of registered host: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    AS_QUERY_HOST_ERROR(1204, "Error during querying for registered hosts: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    AS_BULK_REGISTER_HOST_ERROR(1205, "Error during bulk host registration: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    AS_HOST_REPORT_ERROR(1206, "Error during retrieval of host trust report: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    AS_HOST_ATTESTATION_REPORT_ERROR(1207, "Error during retrieval of host attestation report: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    AS_BULK_HOST_TRUST_ERROR(1208, "Error during bulk host trust retrieval: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    AS_HOST_TRUST_ERROR(1209, "Error during retrieval of host trust status: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    AS_HOST_LOCATION_ERROR(1210, "Error during retrieval of host location information: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    AS_HOST_LOCATION_CONFIG_ERROR(1211, "Error during configuration of host location: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    AS_HOST_TRUST_CERT_ERROR(1212, "Error during retrieval of host trust certificate: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    AS_INPUT_VALIDATION_ERROR(1213, "Input %s for %s is not valid."),
    AS_ASSET_TAG_CERT_RETRIEVE_ERROR(1214, "Error during retrieval of asset tag certificate: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    AS_ASSET_TAG_CERT_CREATE_ERROR(1215, "Error during creation of asset tag certificate: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    AS_ASSET_TAG_CERT_UPDATE_ERROR(1216, "Error during update of asset tag certificate: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    AS_ASSET_TAG_CERT_DELETE_ERROR(1217, "Error during deletion of asset tag certificate: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    AS_AIK_CREATE_ERROR(1218, "Error during addition of AIK for the host: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    AS_NO_HOSTS_CONFIGURED(1219, "Currently there are no hosts configured in the system."),

    // Error codes for White List Service
    WS_OEM_DOES_NOT_EXIST(2001, "OEM '%s' is not configured in the system."),
    WS_OEM_ALREADY_EXISTS(2002, "OEM '%s' is already configured in the system."),
    WS_OEM_ASSOCIATION_EXISTS(2003, "OEM '%s' cannot be deleted as it is associated with %d MLEs."),
    WS_OS_DOES_NOT_EXIST(2004, "OS '%s' of version '%s' is not configured in the system."),
    WS_OS_ALREADY_EXISTS(2005, "OS '%s' of version '%s' is already configured in the system."),
    WS_OS_ASSOCIATION_EXISTS(2006, "OS '%s' of version '%s' cannot be deleted as it is associated with %d MLEs."),
    WS_MLE_ALREADY_EXISTS(2007, "MLE '%s' is already configured in the system."),
    WS_ESX_MLE_NOT_SUPPORTED(2008, "Specified ESXi version is not supported. The system only supports ESXi 5.0 & 5.1."),
    WS_MLE_DOES_NOT_EXIST(2009, "MLE '%s' of version '%s' is not configured in the system."),
    WS_MLE_ASSOCIATION_EXISTS(2010, "MLE '%s' of version '%s' cannot be deleted as it is associated with '%d' hosts."),
    WS_MLE_DATA_MISSING(2011, "Required input parameter '%s' is either null or empty. "),
    WS_OEM_OS_DATA_CANNOT_COEXIST(2012, "Both OEM and OS information cannot be provided together."),
    WS_PCR_WHITELIST_ALREADY_EXISTS(2013, "White list for the PCR '%s' is already configured in the system."),
    WS_PCR_WHITELIST_DOES_NOT_EXIST(2014, "White list for the PCR '%s' is not configured in the system."),
    WS_EVENT_TYPE_DOES_NOT_EXIST(2015, "Event type '%s' is not valid."),
    WS_MODULE_WHITELIST_ALREADY_EXISTS(2016, "White list for the module '%s' is already configured in the system."),
    WS_MODULE_WHITELIST_DOES_NOT_EXIST(2017, "White list for the module '%s' is not configured in the system"),
    WS_NAME_SPACE_DOES_NOT_EXIST(2018, "Name space table is not configured in the system. Please contact administrator."),
    WS_MLE_SOURCE_MAPPING_ALREADY_EXISTS(2019, "White list host mapping already exists for the MLE '%s'."),
    WS_MLE_SOURCE_MAPPING_DOES_NOT_EXIST(2020, "White list host mapping does not exist for the MLE '%s'."),
    WS_INVALID_WHITE_LIST_VALUE(2021, "White list value '%s' specified for '%s' is invalid. Only hexadecimal SHA1 values are allowed."),
    
    // Below error codes are for general white list service errors
    WS_OEM_RETRIEVAL_ERROR(2201, "Error during retrieval of OEM information: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    WS_OEM_UPDATE_ERROR(2202, "Error during OEM update: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    WS_OEM_CREATE_ERROR(2203, "Error during OEM creation: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    WS_OEM_DELETE_ERROR(2204, "Error during OEM deletion: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    WS_OS_RETRIEVAL_ERROR(2205, "Error during retrieval of OS information: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    WS_OS_UPDATE_ERROR(2206, "Error during OS update: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    WS_OS_CREATE_ERROR(2207, "Error during OS creation: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    WS_OS_DELETE_ERROR(2208, "Error during OS deletion: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    WS_MLE_CREATE_ERROR(2209, "Error during MLE creation: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    WS_MLE_UPDATE_ERROR(2210, "Error during MLE update: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    WS_MLE_DELETE_ERROR(2211, "Error during MLE deletion: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    WS_MLE_RETRIEVAL_ERROR(2212, "Error during retrieval of MLE information: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    WS_PCR_WHITELIST_CREATE_ERROR(2213, "Error during PCR whitelist creation: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    WS_PCR_WHITELIST_UPDATE_ERROR(2214, "Error during PCR whitelist update: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    WS_PCR_WHITELIST_DELETE_ERROR(2215, "Error during PCR whitelist deletion: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    WS_PCR_WHITELIST_RETRIEVAL_ERROR(2216, "Error during retrieval of PCR whitelists for MLE: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    WS_MODULE_WHITELIST_CREATE_ERROR(2217, "Error during Module whitelist creation: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    WS_MODULE_WHITELIST_UPDATE_ERROR(2218, "Error during Module whitelist update: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    WS_MODULE_WHITELIST_DELETE_ERROR(2219, "Error during Module whitelist deletion: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    WS_MODULE_WHITELIST_RETRIEVAL_ERROR(2220, "Error during retrieval of Module whitelists for MLE: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    WS_MLE_HOST_MAP_CREATE_ERROR(2221, "Error during configuration of host used for creating white lists: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    WS_MLE_HOST_MAP_UPDATE_ERROR(2222, "Error during update of the configuration of host used for creating white lists: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    WS_MLE_HOST_MAP_DELETE_ERROR(2223, "Error during deletion of the configuration of host used for creating white lists: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    WS_MLE_HOST_MAP_RETRIEVAL_ERROR(2224, "Error during retrieval of host information used for creating white lists: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    
    // Below are error codes for the Management service
    // 3000 to 3100 General Management Service errors
    MS_MLE_ERROR(3001, "Error during retrieval of host MLE information: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    MS_API_CLIENT_ERROR(3002, "Error while creating the Api Client object: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    MS_PLATFORM_RETRIEVAL_ERROR(3003, "Error during retrieval of platform name details: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    MS_BULK_REGISTRATION_ERROR(3004, "Error during bulk host registration: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    MS_HOST_REGISTRATION_ERROR(3005, "Error during host registration: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    MS_HOST_UPDATE_ERROR(3006, "Error during host update: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    MS_WHITELIST_CONFIG_ERROR(3007, "Error during white list configuration: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    MS_MLE_VERIFICATION_ERROR(3008, "Errror during MLE verification for host: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    MS_BIOS_MLE_ERROR(3009, "Error during OEM - BIOS MLE configuration: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    MS_VMM_MLE_ERROR(3010, "Error during OS - VMM MLE configuration: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    MS_MLE_WHITELIST_HOST_MAPPING_ERROR(3011, "Error during MLE white list host mapping: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    MS_MLE_DELETION_ERROR(3012, "Error during MLE deletion: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    MS_WHITELIST_UPLOAD_ERROR(3013, "Error during white list upload to database: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    MS_BIOS_MLE_NAME_ERROR(3014, "Error during BIOS MLE name generation: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    MS_VMM_MLE_NAME_ERROR(3015, "Error during VMM MLE name generation: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    MS_API_USER_REGISTRATION_ERROR(3016, "Error during API Client registration: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    MS_API_USER_UPDATE_ERROR(3017, "Error during API user update: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    MS_API_USER_SEARCH_ERROR(3018, "Error during search for API user: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    MS_CA_ENABLE_ERROR(3019, "Error enabling CA: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    MS_CA_DISABLE_ERROR(3020, "Error disabling CA: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    MS_ROOT_CA_CERT_NOT_FOUND_ERROR(3021, "Mt Wilson Root CA certificate file is not found: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    MS_ROOT_CA_CERT_READ_ERROR(3022, "Failed to read Mt Wilson Root CA certificate file: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    MS_ROOT_CA_CERT_ERROR(3023, "Error during retrieval of root certificate CA chain: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    MS_SAML_CERT_NOT_FOUND_ERROR(3024, "SAML certificate file is not found: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    MS_SAML_CERT_READ_ERROR(3025, "Failed to read SAML certificate file: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    MS_SAML_CERT_ERROR(3026, "Error during retrieval of SAML certificate chain: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    MS_PRIVACYCA_CERT_NOT_FOUND_ERROR(3027, "Privacy CA certificate file is not found: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    MS_PRIVACYCA_CERT_READ_ERROR(3028, "Failed to read Privacy CA certificate file: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    MS_PRIVACYCA_CERT_ERROR(3029, "Error during retrieval of Privacy CA certificate chain: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    MS_SSL_CERT_NOT_FOUND_ERROR(3030, "Server SSL certificate file is not found: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    MS_SSL_CERT_READ_ERROR(3031, "Failed to read server SSL certificate file: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    MS_SSL_CERT_ERROR(3032, "Error during retrieval of SSL CA chain: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()    
    MS_API_USER_DELETION_ERROR(3033, "Error during API Client deletion: %s. More information is available in the server log"),  // argument should be  e.getClass().getSimpleName()
    
    // 3100 to 3200 APIClient
    MS_EXPIRED_CERTIFICATE(3101, "Client certificate has already expired. %s"),
    MS_CERTIFICATE_NOT_YET_VALID(3102, "Client certificate is not yet valid. Validity date is in the future. %s"),
    MS_DUPLICATE_CERTIFICATE(3103, "Certificate already registered in the system."),
    MS_CERTIFICATE_ENCODING_ERROR(3104, "Error in certificate encoding. %s"), 
    MS_BAD_CERTIFICATE_FILE(3105,"Certificate file is not valid."),
    MS_MISSING_CERTIFICATE_FILE(3106, "Certificate file is missing."),
    MS_UN_SUPPORTED_HASH_ALGORITHM(3107, "Error generating fingerprint. Hash algorithm not supported. %s"),
    MS_API_CLIENT_INVALID_ROLE(3108, "Requested role '%s' is not a valid. Valid roles include Attestation, Audit, Cache, Report, Security & Whitelist."),
    MS_API_CLIENT_ROLE_ALEADY_EXISTS(3109, "Requested role already configured for the Api Client."),
    MS_API_CLIENT_CREATE_ERROR(3110, "Error during the creation of Api Client request in the system."),
    MS_API_CLIENT_UPDATE_ERROR(3111, "Error during the updation of Api Client request in the system."),
    MS_API_CLIENT_FIND_ERROR(3112, "Error during the search for Api Client in the system."),
    MS_INVALID_CERTIFICATE_DATA(3113, "Certificate data is not valid. %s"),
    MS_ERROR_PARSING_INPUT(3114,"Error parsing input: %s"),
    MS_USER_ALREADY_EXISTS(3115, "User %s already exists in the system."),
    MS_USER_DOES_NOT_EXISTS(3116, "User %s is not configured in the system"),

    // 3201 to 3300 Automation
    MS_HOST_COMMUNICATION_ERROR(3201, "Error during communication with the host. Please verify the host parameters. '%s'."),
    MS_INVALID_WHITELIST_TARGET(3202, "WhiteList target specified for the host is invalid: %s."),
    MS_INVALID_PCRS(3203, "PCR list provided is either null or not valid. Provide a comma separated list of PCRs."),
    MS_OEM_NOT_FOUND(3204, "OEM '%s' is not configured in the system."),
    MS_OS_NOT_FOUND(3205, "OS '%s' is not configured in the system."),
    MS_BIOS_MLE_NOT_FOUND(3206, "BIOS MLE '%s' is not configured in the system. Please verify if the white list is properly configured."),
    MS_VMM_MLE_NOT_FOUND(3207, "VMM MLE %s' is not configured in the system. Please verify if the white list is properly configured."),
    MS_API_EXCEPTION(3208, "API Call failed with error '%s'."),
    MS_INVALID_ATTESTATION_REPORT(3209, "Unable to retrieve the TPM values from the host. Verify the host TXT configuration."),
    MS_HOST_CONFIGURATION_ERROR(3210, "Unable to retrieve the host configuration details. Please verify the host information."),
    MS_MLE_CONFIGURATION_NOT_FOUND(3211, "BIOS or Hypervisor MLE is not configured correctly."),
    MS_INVALID_AIK_CERTIFICATE(3212, "Cannot validate AIK for '%s' against known Privacy CAs"),
    MS_DUPLICATE_AIK_PUBLIC_KEY(3213, "AIK public key with fingerprint '%s' already exists"),
    
    TLS_COMMMUNICATION_ERROR(4001, "Cannot establish secure connection to '%s': %s");
   
    
    

    
    public int getErrorCode() {
        
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private ErrorCode(int errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }
    
    private static class ErrorCodeCache {
        private static Map<Integer,ErrorCode> ecCache = new HashMap<Integer,ErrorCode>();

        static {
            for (final ErrorCode ec : ErrorCode.values()) {
                ecCache.put(ec.getErrorCode(), ec);
            }
        }
    }
    
    public static ErrorCode getErrorCode(int ec) {
        return ErrorCodeCache.ecCache.get(ec);
    }
    
    int errorCode;
    String message;
}
