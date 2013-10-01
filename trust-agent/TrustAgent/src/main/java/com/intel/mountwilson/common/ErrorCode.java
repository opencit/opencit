/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mountwilson.common;

/**
 *
 * @author dsmagadX
 */
public enum ErrorCode {
    OK(0,"OK"),
    BAD_REQUEST(1000,"TrustAgent: Bad Request"), 
    CERT_MISSING(1001,"TrustAgent: AIK Certificate is missing"),
    CONFIG_MISSING(1002,"TrustAgent: Configuration file is missing"),
    BAD_PCR_VALUES(1003,"TrustAgent: Bad PCR Values"), 
    TPM_OWNERSHIP_ERROR(1005,"TrustAgent: Error with TPM ownership"), 
    FATAL_ERROR(1006,"TrustAgent: Fatal unknown error"), 
    COMMAND_ERROR(1007,"TrustAgent: "), 
    UNSUPPORTED_OPERATION(1008, "TrustAgent: Unsupported operation"),
    ERROR(1, "TrustAgent:");

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
    
    int errorCode;
    String message;
    
    
}
