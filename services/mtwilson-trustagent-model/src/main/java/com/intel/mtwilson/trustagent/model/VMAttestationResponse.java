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
@JacksonXmlRootElement(localName="vm_attestation_report")
public class VMAttestationResponse {
    
    private String hostName;
    private String vmInstanceId;
    private boolean trustStatus;

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getVmInstanceId() {
        return vmInstanceId;
    }

    public void setVmInstanceId(String vmInstanceId) {
        this.vmInstanceId = vmInstanceId;
    }

    public boolean isTrustStatus() {
        return trustStatus;
    }

    public void setTrustStatus(boolean trustStatus) {
        this.trustStatus = trustStatus;
    }
}
