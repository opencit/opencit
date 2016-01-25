/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.mtwilson.datatypes.HostTrustResponse;
import com.intel.mtwilson.jaxrs2.Document;
import com.intel.mtwilson.policy.HostReport;
import com.intel.mtwilson.policy.TrustReport;

/**
 *
 * @author ssbangal
 */
@JacksonXmlRootElement(localName="host_attestation")
public class HostAttestation extends Document {
    
    private String hostUuid;
    private String hostName;
    private String aikSha1;
    private String aikPublicKeySha1;
    private String challengeHex;
    private TrustReport trustReport;
    private HostTrustResponse hostTrustResponse;
    private String saml;

    public String getHostUuid() {
        return hostUuid;
    }

    public void setHostUuid(String hostUuid) {
        this.hostUuid = hostUuid;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getAikSha1() {
        return aikSha1;
    }

    public void setAikSha1(String aikSha1) {
        this.aikSha1 = aikSha1;
    }

    public String getAikPublicKeySha1() {
        return aikPublicKeySha1;
    }

    public void setAikPublicKeySha1(String aikPublicKeySha1) {
        this.aikPublicKeySha1 = aikPublicKeySha1;
    }
    
    public String getChallenge() {
        return challengeHex;
    }

    public void setChallenge(String challenge) {
        this.challengeHex = challenge;
    }

    public TrustReport getTrustReport() {
        return trustReport;
    }

    public void setTrustReport(TrustReport trustReport) {
        this.trustReport = trustReport;
    }

    public HostTrustResponse getHostTrustResponse() {
        return hostTrustResponse;
    }

    public void setHostTrustResponse(HostTrustResponse hostTrustResponse) {
        this.hostTrustResponse = hostTrustResponse;
    }
    
    public String getSaml() {
        return saml;
    }
    
    public void setSaml(String saml) {
        this.saml = saml;
    }
}
