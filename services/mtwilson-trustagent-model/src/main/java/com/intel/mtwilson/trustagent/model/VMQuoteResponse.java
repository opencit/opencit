/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.trustagent.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 *
 * @author ssbangal
 */
@JacksonXmlRootElement(localName="vm_quote_response")
public class VMQuoteResponse {
    
    private byte[] vmQuote;
    private byte[] vmTrustPolicy;
    private byte[] vmMeasurements;
    private QuoteType vmQuoteType;
    
    public enum QuoteType { X509_ATTR_CERT, XML_DSIG, SPRINT7}
    
    public byte[] getVmQuote() {
        return vmQuote;
    }

    public void setVmQuote(byte[] vmQuote) {
        this.vmQuote = vmQuote;
    }

    public byte[] getVmTrustPolicy() {
        return vmTrustPolicy;
    }

    public void setVmTrustPolicy(byte[] vmTrustPolicy) {
        this.vmTrustPolicy = vmTrustPolicy;
    }

    public byte[] getVmMeasurements() {
        return vmMeasurements;
    }

    public void setVmMeasurements(byte[] vmMeasurements) {
        this.vmMeasurements = vmMeasurements;
    }

    public QuoteType getVmQuoteType() {
        return vmQuoteType;
    }

    public void setVmQuoteType(QuoteType vmQuoteType) {
        this.vmQuoteType = vmQuoteType;
    }
        
}
