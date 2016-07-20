/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.mtwilson.jackson.X509CertificateDeserializer;
import java.security.cert.X509Certificate;
import com.intel.mtwilson.jackson.X509CertificateSerializer;

/**
 *
 * @author jbuhacoff
 */
@JacksonXmlRootElement(localName="tpm_quote_response")
public class TpmQuoteResponse {
    public long timestamp;
    public String clientIp;
    public String errorCode;
    public String errorMessage;
    @JsonSerialize(using=X509CertificateSerializer.class)
    @JsonDeserialize(using=X509CertificateDeserializer.class)
    public X509Certificate aik;
    public byte[] quote;
    public String eventLog; // base64-encoded xml ....
    public String tcbMeasurement;
    public String selectedPcrBanks;
    
    // added for assetTag attestation based on sha1(nonce | assetTag
    public boolean isTagProvisioned;
    public byte[] assetTag;
    
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
