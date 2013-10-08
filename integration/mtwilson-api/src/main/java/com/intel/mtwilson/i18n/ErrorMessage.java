/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.i18n;

import com.intel.mtwilson.datatypes.ErrorCode;

/**
 *
 * @author jbuhacoff
 */
public class ErrorMessage extends Message {
    private ErrorCode errorCode;
    
    public ErrorMessage(ErrorCode errorCode, Object... args) {
        super(errorCode.name(), args);
        this.errorCode = errorCode;
    }
    
    public ErrorCode getErrorCode() { return errorCode; }
}
