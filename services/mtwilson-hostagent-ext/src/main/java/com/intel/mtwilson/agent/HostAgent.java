/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.agent;

import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.model.Aik;
import com.intel.mtwilson.model.Nonce;
import com.intel.mtwilson.model.PcrIndex;
import com.intel.mtwilson.model.PcrManifest;
import com.intel.mtwilson.model.TpmQuote;
import com.intel.mtwilson.trustagent.model.VMAttestationResponse;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.mtwilson.trustagent.model.VMAttestationRequest;
import com.intel.mtwilson.trustagent.model.VMQuoteResponse;
import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Set;

/**
 * This is a draft of the interface that Linux, Citrix, and Vmware
 * agents should implement for communicating information about their hosts
 * to Mt Wilson. THis will allow Mt Wilson to treat them uniformly and move
 * all the platform-specific calls and procedures into those agents in a 
 * clean way. 
 * 
 * To obtain a HostAgent object, use the HostAgentFactory to create one for a 
 * given host. All the methods in this interface apply to the given host.
 * 
 * All the methods in this interface are intended to retrieve information
 * from the host (or its master/manager server). 
 * 
 * Note that the HostAgent is not responsible for interpreting the attestation.
 * It is only responsible for obtaining the host information, AIK, TPM Quote,
 * and Module Manifest. The Attestation Service will interpret these.
 * 
 * The methods isTpmPresent(), isTpmEnabled(), isIntelTxtSupported(), and
 *  isIntelTxtEnabled() help the
 * attestation service provide detailed trust status to clients:
 * 
 * 1. Trusted  (host complies with assigned policies in whitelist)
 * 2. Untrusted (host does not comply with assigned policies in whitelist)
 * 3. Intel TXT Not Enabled (when isIntelTxtEnabled()==false)
 * 4. TPM Not Enabled (when isTpmEnabled()==false)
 * 5. TXT and TPM Not Enabled (when both isTpmEnabled()==false and isIntelTxtEnabled()==false)
 * 6. TXT Not Supported (when isIntelTxtSupported()==false)
 * 
 * if isIntelTxtSupported() and isIntelTxtEnabled() and isTpmPresent() and isTpmEnabled() then
 *      ... evaluate policies to determine trusted or untrusted ...
 *      ... actually isIntelTxtSupported() is implied by isIntelTxtEnabled() ...
 *      ... and isTpmPresent() is implied by isTpmEnabled() ...
 * else
 *      ... host trust status is unknown ...
 *      if isIntelTxtSupported() == false then display #6
 *      else if isIntelTxtEnabled() == false && isTpmEnabled() == false then display #5
 *      else if isIntelTxtEnabled() == false then display #3
 *      else if isTpmEnabled() == false then display #4
 *      
 * 
 * 
 * @author jbuhacoff
 */
public interface HostAgent {

    /**
     * Whether the platform supports Intel TXT - is the right hardware present (not including the TPM)
     * @return 
     */
    boolean isIntelTxtSupported();
    
    /**
     * Whether Intel TXT  has been enabled on the platform (usually through the BIOS)
     * @return 
     */
    boolean isIntelTxtEnabled();
    
    
    /**
     * @return true if the host has a TPM
     */
    boolean isTpmPresent();
    
    boolean isTpmEnabled();

    
    /**
     * Linux and Citrix agents should return true, Vmware should return false.
     * @return true if we can obtain the EK for the host
     */
    boolean isEkAvailable();
    
    X509Certificate getEkCertificate();
    
    
    /**
     * Linux and Citrix agents should return true, Vmware should return false.
     * @return true if we can obtain am AIK for the host.
     */
    boolean isAikAvailable();
    
    /**
     * AIK's are RSA public keys.  The certificates only exist when a Privacy CA or
     * a Mt Wilson CA signs the public key to create a certificate.
     * @return 
     */
    PublicKey getAik();
    
    /**
     * Linux agent should return true because we use the Privacy CA.
     * Citrix agent uses DAA so it should return false.
     * Vmware agent should return false.
     * @return 
     */
    boolean isAikCaAvailable();
    
    /**
     * Draft - maybe it should return an X509Certificate object
     * @return 
     */
    X509Certificate getAikCertificate();
    
    /**
     * Draft - maybe it should return an X509Certificate object
     * @return the Privacy CA certificate that is mentioned in the AIK Certificate
     */
    X509Certificate getAikCaCertificate(); 

    
    /**
     * Linux and Vmware agent should return false.
     * Citrix agent should return true.
     * @return true if the host supports Direct Anonymous Attestation
     */
    boolean isDaaAvailable();
    
    
    
    /**
     * Draft to approximate getting the bios/os/vmm details from the host...
     * maybe split it up into those three functions? or return a host information
     * object with those details? should be similar to or the same as the host portion of the mle 
     * object ?
     * @return 
     */
    String getHostInformation();
    
    
    /**
     * Every vendor has a different API for obtaining the TPM Quote, module
     * information, etc. 
     * An administrator may want to log the "raw output" from the vendor before
     * parsing and validating. 
     * For Vmware, it's an XML document with their externally-unverifiable report
     * on the host. For Citrix and Intel, it's an XML document containing the TPM Quote and
     * other information. 
     * @return 
     */
    String getVendorHostReport()  throws IOException;
    
    /**
     * This is a draft - need to check it against linux & citrix requirements
     * to ensure it makes sense. 
     * Vmware agent must throw unsupported operation exception since it doesn't
     * provide quotes, only "pcr information" through it's API. 
     * @param aik
     * @param nonce
     * @param pcr
     * @return 
     */
    TpmQuote getTpmQuote(Aik aik, Nonce nonce, Set<PcrIndex> pcr);
    
    
    
    /**
     * 
     * Agents should return the entire set of PCRs from the host. The attestation
     * service will then choose the ones it wants to verify against the whitelist.
     * Returning all PCR's is cheap (there are only 24) and makes the API simple.
     * 
     * Agents should return the entire set of module measurements from the host.
     * The attestation service will then choose what to verify and how. 
     * 
     * Bug #607 changed return type to PcrManifest and removed post-processing argument - 
     * each host agent implementation is reponsible for completing all its processing.
     * @param host
     * @return 
     */
    PcrManifest getPcrManifest() throws IOException;

    PcrManifest getPcrManifest(Nonce challenge) throws IOException;
    
    /**
     * SAMPLE OUTPUT FROM VMWare Host:
     * BIOS - OEM:Intel Corporation
     * BIOS - Version:S5500.86B.01.00.0060.090920111354
     * OS Name:VMware ESXi
     * OS Version:5.1.0
     * VMM Name: VMware ESXi
     * VMM Version:5.1.0-613838 (Build Number)
     * 
     */
    TxtHostRecord getHostDetails() throws IOException; // original interface passed TxtHostRecord even though all the method REALLY needs is the connection string (hostname and url for vcenter,  ip adderss and port for intel but can be in the form of a connection string);  but since the hostagent interface is for a host already selected... we don't need any arguments here!!    the IOException is to wrap any client-specific error, could be changed to be soemthing more specific to trust utils library 


    /**
     * Another adapter for existing code.  Each vendor returns a string in their own format.
     * @param pcrList  may be ignored, and the full list returned
     * @return
     * @throws IOException 
     */
    String getHostAttestationReport(String pcrList) throws IOException;

    String getHostAttestationReport(String pcrList, Nonce challenge) throws IOException;
    
    /**
     * Use this to obtain host-specific information such as UUID, which may be 
     * needed for dynamic whitelist rules.  Attributes returned with this method
     * may be referenced by name from dynamic whitelist rules.
     * @return
     * @throws IOException 
     * 
     *  * Format should look something like this
     * <?xml version='1.0' encoding='UTF-8'?>
     * <Host_Attestation_Report Host_Name="10.1.70.126" vCenterVersion="5.0" HostVersion="5.0">
     *      <PCRInfo ComponentName="0" DigestValue="1d670f2ae1dde52109b33a1f14c03e079ade7fea"/>
     *      <PCRInfo ComponentName="17" DigestValue="ca21b877fa54dff86ed5170bf4dd6536cfe47e4d"/>
     *      <PCRInfo ComponentName="18" DigestValue="8cbd66606433c8b860de392efb30d76990a3b1ed"/>
     * </Host_Attestation_Report>
     */
    Map<String,String> getHostAttributes() throws IOException;
    
    
    void setAssetTag(Sha1Digest tag) throws IOException;
    
    X509Certificate getBindingKeyCertificate();    
    
    VMAttestationResponse getVMAttestationStatus(String vmInstanceId) throws IOException;
    
    VMQuoteResponse getVMAttestationReport(VMAttestationRequest obj) throws IOException;
}
