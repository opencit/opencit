/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.api;

import com.intel.mtwilson.i18n.ErrorCode;

/**
 *
 * @since 0.5.2
 * @author jabuhacx
 */
public class ApiException extends Exception {
    private int errorCode;
    private ApiResponse response = null; // may be null; not all ApiExceptions are associated with a server response
//    private String errorMessage;
    
    public ApiException(ApiResponse response, String message) {
        super(message+" ["+ErrorCode.UNKNOWN_ERROR.toString()+"] ");
        errorCode = ErrorCode.UNKNOWN_ERROR.getErrorCode();
//        errorMessage = message;
        this.response = response;
    }
    public ApiException(ApiResponse response, String message, Exception e) {
        super(message+" ["+ErrorCode.UNKNOWN_ERROR.toString()+"] ", e);
        errorCode = ErrorCode.UNKNOWN_ERROR.getErrorCode();
//        errorMessage = message;
        this.response = response;
    }
    public ApiException(ApiResponse response, String message, int errorCode) {
        //super(message+" ["+ErrorCode.valueOf(String.valueOf(errorCode)).toString()+"] ");
        super(message+" ["+String.valueOf(errorCode)+"] ");
        this.errorCode = errorCode;
//        errorMessage = message;
        this.response = response;
    }

    public ApiException(ApiResponse response, String message, ErrorCode errorCode) {
        super(message+" ["+errorCode.toString()+"] ");
        this.errorCode = errorCode.getErrorCode();
//        errorMessage = message;
        this.response = response;
    }
    
    public ApiException(String message) {
        super(message+" ["+ErrorCode.UNKNOWN_ERROR.toString()+"] ");
        errorCode = ErrorCode.UNKNOWN_ERROR.getErrorCode();
//        errorMessage = message;
    }
    public ApiException(String message, Exception e) {
        super(message+" ["+ErrorCode.UNKNOWN_ERROR.toString()+"] ", e);
        errorCode = ErrorCode.UNKNOWN_ERROR.getErrorCode();
//        errorMessage = message;
    }
    public ApiException(String message, int errorCode) {
        //super(message+" ["+ErrorCode.valueOf(String.valueOf(errorCode)).toString()+"] ");
        super(message+" ["+String.valueOf(errorCode)+"] ");
        this.errorCode = errorCode;
//        errorMessage = message;
    }

    public ApiException(String message, ErrorCode errorCode) {
        super(message+" ["+errorCode.toString()+"] ");
        this.errorCode = errorCode.getErrorCode();
//        errorMessage = message;
    }
    
    public int getErrorCode() {
        return errorCode;
    }
    /*
    @Override
    public String getMessage() {
        return super.getMessage()+": "+getErrorContent();
    }*/
    
    public String getHttpReasonPhrase() {
        if( response != null ) {
            return response.httpReasonPhrase;
        }
        else {
            return null;
        }
    }
    
    public Integer getHttpStatusCode() {
        if( response != null ) {
            return response.httpStatusCode;
        }
        else {
            return null;
        }
    }

}
