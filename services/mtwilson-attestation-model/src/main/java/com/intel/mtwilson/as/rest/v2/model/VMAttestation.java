/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.mtwilson.jaxrs2.Document;
import com.intel.mtwilson.policy.RuleResult;
import com.intel.mtwilson.policy.VmTrustReport;

/**
 *
 * @author ssbangal
 */
@JacksonXmlRootElement(localName="vm_attestation")
public class VMAttestation extends Document {
    
    private String hostName;
    private String vmInstanceId;
    private boolean trustStatus;
    private String errorMessage;
    private String vmSaml;
    private boolean includeHostReport;
    private VmTrustReport vmTrustReport;
    HostAttestation hostAttestationReport;

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

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getVmSaml() {
        return vmSaml;
    }

    public void setVmSaml(String vmSaml) {
        this.vmSaml = vmSaml;
    }

    @JsonIgnore
    public boolean getIncludeHostReport() {
        return includeHostReport;
    }

    @JsonProperty
    public void setIncludeHostReport(boolean includeHostReport) {
        this.includeHostReport = includeHostReport;
    }
        
    public HostAttestation getHostAttestationReport() {
        return hostAttestationReport;
    }

    public void setHostAttestationReport(HostAttestation hostAttestationReport) {
        this.hostAttestationReport = hostAttestationReport;
    }

    public VmTrustReport getVmTrustReport() {
        return vmTrustReport;
    }

    public void setVmTrustReport(VmTrustReport vmTrustReport) {
        this.vmTrustReport = vmTrustReport;
    }

    
}
