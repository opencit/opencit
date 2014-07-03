/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.mtwilson.jaxrs2.Document;
//import com.intel.dcsg.cpg.validation.Regex;
//import com.intel.dcsg.cpg.validation.RegexPatterns;

/**
 *
 * @author ssbangal
 */
@JacksonXmlRootElement(localName="tls_policy")
public class HostTlsPolicy extends Document {
    
    private String hostUuid;
    private String name;
//    private Boolean insecure = false;
//    private String[] certificates = null;;
    private byte[] keyStore;

    public String getHostUuid() {
        return hostUuid;
    }

    public void setHostUuid(String hostUuid) {
        this.hostUuid = hostUuid;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    /*public Boolean getInsecure() {
        return insecure;
    }

    public void setInsecure(Boolean insecure) {
        this.insecure = insecure;
    }
    
    @Regex(RegexPatterns.ANY_VALUE)
    public String[] getCertificates() {
        return certificates;
    }

    public void setCertificates(String[] certificates) {
        this.certificates = certificates;
    }*/

    public byte[] getKeyStore() {
        return keyStore;
    }

    public void setKeyStore(byte[] keyStore) {
        this.keyStore = keyStore;
    }
    
    
}
