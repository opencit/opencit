/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.mtwilson.jaxrs2.Document;
import com.intel.mtwilson.policy.RuleResult;

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
    private String samlAssertion;
    private boolean includeHostReport;
    private RuleResult vmRuleResult;
    HostAttestation hostAttestation;

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

    public String getSamlAssertion() {
        return samlAssertion;
    }

    public void setSamlAssertion(String samlAssertion) {
        this.samlAssertion = samlAssertion;
    }

    public boolean isIncludeHostReport() {
        return includeHostReport;
    }

    public void setIncludeHostReport(boolean includeHostReport) {
        this.includeHostReport = includeHostReport;
    }
        
    public HostAttestation getHostAttestation() {
        return hostAttestation;
    }

    public void setHostAttestation(HostAttestation hostAttestation) {
        this.hostAttestation = hostAttestation;
    }

    public RuleResult getVmRuleResult() {
        return vmRuleResult;
    }

    public void setVmRuleResult(RuleResult vmRuleResult) {
        this.vmRuleResult = vmRuleResult;
    }        
    
}
