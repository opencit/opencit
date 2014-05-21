/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.mtwilson.jaxrs2.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
@JacksonXmlRootElement(localName="host_aik")
public class HostAik extends Document{

    Logger log = LoggerFactory.getLogger(getClass().getName());
    
    private String hostUuid;
    private String aikSha1;
    private String aikPublicKey;

    public String getHostUuid() {
        return hostUuid;
    }

    public void setHostUuid(String hostUuid) {
        this.hostUuid = hostUuid;
    }

    public String getAikSha1() {
        return aikSha1;
    }

    public void setAikSha1(String aikSha1) {
        this.aikSha1 = aikSha1;
    }

    public String getAikPublicKey() {
        return aikPublicKey;
    }

    public void setAikPublicKey(String aikPublicKey) {
        this.aikPublicKey = aikPublicKey;
    }
    
}
