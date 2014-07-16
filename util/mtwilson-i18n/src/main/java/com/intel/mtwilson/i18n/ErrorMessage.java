/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.i18n;

import com.intel.dcsg.cpg.i18n.LocaleUtil;
import com.intel.dcsg.cpg.i18n.Message;
import java.text.MessageFormat;
import java.util.Locale;

/**
 * @author jbuhacoff
 */
public class ErrorMessage extends Message {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ErrorMessage.class);
    private ErrorCode errorCode;
    
    public ErrorMessage(ErrorCode errorCode, Object... args) {
        super(errorCode.name(), args);
        this.errorCode = errorCode;
    }
    
    public ErrorCode getErrorCode() { return errorCode; }
    
    @Override
    public String getBundleName() { return "MtWilsonStrings"; }

    @Override
    public String getDefaultLocalizedMessage(Locale locale) {
        String output = String.format(errorCode.getMessage(), getParameters());// super.getParameters() returns the Object... args from our constructor.
        log.debug("default localized message for {} in {} is: {}", errorCode.name(), LocaleUtil.toLanguageTag(locale), output);
        return output;
    }
    
    
}
