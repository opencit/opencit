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
    private String aikSha256;
    private String aikPublicKeySha256;
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

    public String getAikSha256() {
        return aikSha256;
    }

    public void setAikSha256(String aikSha256) {
        this.aikSha256 = aikSha256;
    }

    public String getAikPublicKeySha256() {
        return aikPublicKeySha256;
    }

    public void setAikPublicKeySha256(String aikPublicKeySha256) {
        this.aikPublicKeySha256 = aikPublicKeySha256;
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
