/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.i18n;

import com.intel.dcsg.cpg.i18n.Message;

/**
 * TODO: when the error code class is replaced with an exception hierarchy,
 * this class can be rewritten to wrap any exception by setting the 
 * message name to the fully qualified exception class name, and instead
 * of ordered parameters we can use javabean properties of the exceptions as
 * named parameters (requires a different syntax for localized strings) or
 * automatically generate the ordered parameters using alphabetically-ordered
 * list of javabean parameters (excluding getCause, getMessage, 
 * getLocalizedMessage, getStacktrace, and getSuppressed)
 * 
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
    
    /**
     * Moved the hard-coded bundle name from Message (now abstract) to this
     * class
     * TODO: eventually should be obtained from specific features when they
     * throw exceptions (specific to them) or from the mtwilson core api if 
     * the exception is defined there (then it WOULD be MtWilsonStrings or 
     * something similar)
     * @return 
     */
    @Override
    public String getBundleName() { return "MtWilsonStrings"; }
}
