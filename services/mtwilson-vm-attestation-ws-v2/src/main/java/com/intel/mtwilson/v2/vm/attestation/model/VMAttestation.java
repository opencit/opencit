/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.v2.vm.attestation.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.mtwilson.jaxrs2.Document;

/**
 *
 * @author ssbangal
 */
@JacksonXmlRootElement(localName="vm_attestation")
public class VMAttestation extends Document {
    
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
