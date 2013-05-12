/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.datatypes;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author dsmagadx
 */
public enum ErrorCode {

    OK(0, "OK"), 
    SYSTEM_ERROR(1,"System error: %s"), 
//    AUTH_FAILED(1000,"Authentication Failed"), 
//    SQL_ERROR(1001, "SQL Error"), 
    UNKNOWN_ERROR(2,"Error"),  // Used in APIClient
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
    AS_TPM_NOT_SUPPORTED(1014,"Host '%s' does not support TXT."),


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
    
    
    // Below are error codes for the Management service
    // 3000 to 3100 General Management Service errors
    // 3100 to 3200 APIClient
    MS_EXPIRED_CERTIFICATE(3101, "Client certificate has already expired. %s"),
    MS_CERTIFICATE_NOT_YET_VALID(3102, "Client certificate is not yet valid. Validity date is in the future. %s"),
    MS_DUPLICATE_CERTIFICATE(3103, "Certificate already registered in the system."),
    MS_CERTIFICATE_ENCODING_ERROR(3104, "Error in certificate encoding. Cannot generate fingerprint. %s"),
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
