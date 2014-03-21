/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.security.cert.X509Certificate;

/**
 *
 * @author jbuhacoff
 */
@JacksonXmlRootElement(localName="tpmQuoteResponse")
public class TpmQuoteResponse {
    public long timestamp;
    public String clientIp;
    public String errorCode;
    public String errorMessage;
    public X509Certificate aik;
    public byte[] quote;
    public String eventLog; // base64-encoded xml .... ugh.   TODO normalize this
    
    /**
        String responseXML =
                "<client_request> "
                + "<timestamp>" + new Date(System.currentTimeMillis()).toString() + "</timestamp>"
                + "<clientIp>" + StringEscapeUtils.escapeXml(CommandUtil.getHostIpAddress()) + "</clientIp>"
                + "<error_code>" + context.getErrorCode().getErrorCode() + "</error_code>"
                + "<error_message>" + StringEscapeUtils.escapeXml(context.getErrorCode().getMessage()) + "</error_message>"
                + "<aikcert>" + StringEscapeUtils.escapeXml(context.getAIKCertificate()) + "</aikcert>"
                + "<quote>" + new String(Base64.encodeBase64(context.getTpmQuote())) + "</quote>"
                +  "<eventLog>" + context.getModules() + "</eventLog>" //To add the module information into the response.
                + "</client_request>";
     * 
     */
}
