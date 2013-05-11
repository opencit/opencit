/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.agent.intel;

import com.intel.mountwilson.ta.data.hostinfo.HostInfo;
import com.intel.mtwilson.agent.HostAgent;
import com.intel.mtwilson.crypto.X509Util;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.model.Aik;
import com.intel.mtwilson.model.InternetAddress;
import com.intel.mtwilson.model.Nonce;
import com.intel.mtwilson.model.PcrIndex;
import com.intel.mtwilson.model.PcrManifest;
import com.intel.mtwilson.model.TpmQuote;
import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Instances of VmwareAgent should be created by the VmwareAgentFactory
 * @author jbuhacoff
 */
public class IntelHostAgent implements HostAgent {
    private transient Logger log = LoggerFactory.getLogger(getClass());
    private transient final TrustAgentSecureClient trustAgentClient;
//    private final String hostname;
    private InternetAddress hostAddress;
    private Boolean isTpmAvailable = null;
    private String vendorHostReport = null;
    private String vmmName = null;
    private PcrManifest pcrManifest = null;
    
    public IntelHostAgent(TrustAgentSecureClient client, InternetAddress hostAddress) throws Exception {
        trustAgentClient = client;
        this.hostAddress = hostAddress;
//        this.hostname = hostname;
    }
    
    
    
    @Override
    public boolean isTpmPresent() {
//        throw new UnsupportedOperationException("Not supported yet.");
        // bug #538  for now assuming all trust-agent hosts have tpm since we don't have a separate capabilities call
        return true; //  XXX TODO need to have a separate call to trust agent to get host capabilities  ... see bug #540
    }

    @Override
    public boolean isTpmEnabled() {
        return true; // XXX TODO we need a capability to get this from the host!!  throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isEkAvailable() {
        return false; // vmware does not make the EK available through its API
    }

    @Override
    public boolean isAikAvailable() { // XXX TODO need to distinguish between "the host system could have an AIK" (maybe isAikSupported) and "the host system actually does have an AIK" (isAikAvailable)
        return true;  // assume we can always get an AIK from a trust agent,  for now
    }

    @Override
    public boolean isAikCaAvailable() { // XXX TODO probably needs to be separated like isAik*  into isAikCaSupported and isAikCaAvailable .    AikCa is synonym for PrivacyCa.
        return true; // assume hosts running trust agent always use a privacy ca,  for now
    }

    @Override
    public boolean isDaaAvailable() {
        return false; // intel trust agent currently does not support DAA
    }

    @Override
    public X509Certificate getAikCertificate() {
        String pem = trustAgentClient.getAIKCertificate();
        try {
            X509Certificate aikCert = X509Util.decodePemCertificate(pem);
            isTpmAvailable = true;
            return aikCert;
        }
        catch(Exception e) {
            log.error("Cannot decode AIK certificate: {}", e.toString());
            log.debug(pem);
            return null;
        }
    }

    @Override
    public X509Certificate getAikCaCertificate() {
        throw new UnsupportedOperationException("Not supported yet.");  // XXX TODO we need a new API for trust agent to return the privacy ca public key ... corresponding to its aik cert (it should get it from privacy ca when the aik cert is signed ) ... that will allow AS to check that AIK CERT was signed by this AIK CA CERT (PRIVACY CA) and then it can check that the PRIVACY CA cert is in the list of trusted certs. this way if the privacy ca is NOT in the list,  we have some info to display to the administrator.  the other way to do it is to try and look up the privacy ca cert based on the issuer name in the AIK CERT,  but then if it's not in the list the only thing we have to display to the administrator is the AIK CERT info, which only mentinos teh issuer's name.   in the case of a mt wilson privacy ca,  the issuer name is useless because it doesn't say which server its on, etc. 
//        return null; // XXX TODO throw exception or return null? call should first check isAikCaAvailable  // vmware does not make the Privacy CA Certificate available through its API, if it even uses a Privacy CA
    }

    @Override
    public String getHostInformation() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getVendorHostReport()  throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public TpmQuote getTpmQuote(Aik aik, Nonce nonce, Set<PcrIndex> pcr) {
        throw new UnsupportedOperationException("Not supported  yet."); // XXX TODO throw exception or return null?
    }


    @Override
    public PcrManifest getPcrManifest() throws IOException {
        if( pcrManifest == null ) {
            try {
                TAHelper helper = new TAHelper();
                pcrManifest = helper.getQuoteInformationForHost(hostAddress.toString(), trustAgentClient); // XXX TODO we should save the entire quote structure so we can return the quote itself with another method to allow the caller to later verify the manifest they just got.
            }
            catch(Exception e) {
                throw new IOException("Cannot retrieve PCR Manifest from "+hostAddress.toString(), e);
            }
        }
        return pcrManifest;
    }

    @Override
    public TxtHostRecord getHostDetails() throws IOException {
        HostInfo hostInfo = trustAgentClient.getHostInfo();
        TxtHostRecord host = new TxtHostRecord();
        host.BIOS_Name = hostInfo.getBiosOem().trim(); // XXX TODO the HostInfo class doesn't have a getBiosName() function!!
        host.BIOS_Oem = hostInfo.getBiosOem().trim();
        host.BIOS_Version = hostInfo.getBiosVersion().trim();
        host.VMM_Name = hostInfo.getVmmName().trim();
        host.VMM_Version = hostInfo.getVmmVersion().trim();
        host.VMM_OSName = hostInfo.getOsName().trim();
        host.VMM_OSVersion = hostInfo.getOsVersion().trim();
        // now set some state we need for getHostAttestationReport
        vmmName = host.VMM_Name; // XXX maybe we should maintain the entire TxtHostRecord or something similar
        return host;
    }

    @Override
    public String getHostAttestationReport(String pcrList) throws IOException {
        if( vendorHostReport != null ) { return vendorHostReport; }
        if( vmmName == null ) { getHostDetails(); }
//        throw new UnsupportedOperationException("Not supported yet.");
        // XXX TODO huge kludge, we are relying on the OpenSourceVMMHelper for this, which uses API Client to call Attestation Service, which then creates a TrustAgentSecureClient to get some information from the host but also grabs some from the database and then generates the XML format we need
//        OpenSourceVMMHelper helper = new OpenSourceVMMHelper();
//        return help.getHostAttestationReport(hostAddress);
        try {
            TAHelper helper = new TAHelper();
            // XXX the PCR information returned here is NOT verified using the host's trusted AIk certificate from our database... must call helper.setTrustedAik(...) before calling helper.getQuoteInformationForHost(...) in order to verify the quote
            // currently the getHostAttestationReport function is ONLY called from Management Service HostBO.configureWhiteListFromCustomData(...)  so there wouldn't be any saved trusted AIK in the database anyway
            pcrManifest = helper.getQuoteInformationForHost(hostAddress.toString(), trustAgentClient);
            vendorHostReport = helper.getHostAttestationReport(hostAddress.toString(), pcrManifest, vmmName);
            log.debug("Host attestation report for {}", hostAddress);
            log.debug(vendorHostReport);
            return vendorHostReport;
        }
        catch(Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public boolean isIntelTxtSupported() {
        return true; // XXX TODO need to implement detection
    }

    @Override
    public boolean isIntelTxtEnabled() {
        return true; // XXX TODO need to implement detection
    }

    @Override
    public PublicKey getAik() {
        X509Certificate aikcert = getAikCertificate();
        return aikcert.getPublicKey();
    }

    @Override
    public X509Certificate getEkCertificate() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Map<String, String> getHostAttributes() throws IOException {
        return new HashMap<String,String>();
    }
    
}
