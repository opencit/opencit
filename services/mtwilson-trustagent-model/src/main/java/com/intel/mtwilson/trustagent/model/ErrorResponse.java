/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.dcsg.cpg.net.InternetAddress;
import com.intel.mtwilson.i18n.ErrorCode;
import java.util.Date;

/**
 *
 * @author jbuhacoff
 */
@JacksonXmlRootElement(localName="error")
public class ErrorResponse {
    public Date timestamp;
    public InternetAddress clientIp;
    public ErrorCode errorCode;
    public String errorMessage;
/*
    public String generateErrorResponse(ErrorCode errorCode) {

        String responseXML =
                "<client_request> "
                + "<timestamp>" + new Date(System.currentTimeMillis()).toString() + "</timestamp>"
                + "<clientIp>" + CommandUtil.getHostIpAddress() + "</clientIp>"
                + "<error_code>" + errorCode.getErrorCode() + "</error_code>"
                + "<error_message>" + errorCode.getMessage() + "</error_message>"
                + "</client_request>";
        return responseXML;
    }    */
}
