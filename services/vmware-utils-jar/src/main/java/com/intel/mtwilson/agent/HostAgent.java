/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.agent;

import com.intel.mountwilson.manifest.data.IManifest;
import com.intel.mountwilson.util.vmware.VCenterHost;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.datatypes.Aik;
import com.intel.mtwilson.datatypes.Nonce;
import com.intel.mtwilson.datatypes.PcrIndex;
import com.intel.mtwilson.datatypes.Pcr;
import com.intel.mtwilson.datatypes.TpmQuote;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * XXX TODO this is a draft of the interface that Linux, Citrix, and Vmware
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
 * @author jbuhacoff
 */
public interface HostAgent {
    
    /**
     * Linux, Citrix, and Vmware agents should contact the host and find out
     * if it has a TPM before determining the return value.
     * @return true if the host has a TPM
     */
    boolean isTpmAvailable();
    
    /**
     * Linux, Citrix agents should contact the host and find out
     * if its TPM is enabled (BIOS enabled and also if the agents have ownership).
     * In this case, "enabled" means it has an owner set AND that owner is
     * cooperating with Mt Wilson. 
     * Vmware agents can return true if isTpmAvailable() returns true.
     * @return 
     */
    boolean isTpmEnabled();
    
    /**
     * Linux and Citrix agents should return true, Vmware should return false.
     * @return true if we can obtain the EK for the host
     */
    boolean isEkAvailable();
    
    /**
     * Linux and Citrix agents should return true, Vmware should return false.
     * @return true if we can obtain am AIK for the host.
     */
    boolean isAikAvailable();
    
    /**
     * Linux agent should return true because we use the Privacy CA.
     * Citrix agent uses DAA so it should return false.
     * Vmware agent should return false.
     * @return 
     */
    boolean isAikCaAvailable();
    
    /**
     * Linux and Vmware agent should return false.
     * Citrix agent should return true.
     * @return true if the host supports Direct Anonymous Attestation
     */
    boolean isDaaAvailable();
    
    /**
     * XXX draft - maybe it should return an X509Certificate object
     * @return 
     */
    byte[] getAikCertificate();
    
    /**
     * XXX draft - maybe it should return an X509Certificate object
     * @return the Privacy CA certificate that is mentioned in the AIK Certificate
     */
    byte[] getAikCaCertificate(); 
    
    /**
     * XXX draft to approximate getting the bios/os/vmm details from the host...
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
    String getVendorHostReport();
    
    /**
     * XXX this is a draft - need to check it against linux & citrix requirements
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
     * Agents should return the entire set of PCRs from the host. The attestation
     * service will then choose the ones it wants to verify against the whitelist.
     * Returning all PCR's is cheap (there are only 24) and makes the API simple.
     * @return 
     */
    List<Pcr> getPcrValues();
    
    /**
     * Agents should return the entire set of module measurements from the host.
     * The attestation service will then choose what to verify and how. 
     * XXX currently written to return the mmodule manifest as a set<string> which
     * is just a draft. Need to figure out what is a convenient format and what
     * is the information we really need (just module name&value or do we also
     * need the ORDER - to verify the pcr calculation for some platforms? and
     * what about "events" and "packages" and "vendors" that are provided by
     * some platforms? needed or not?)
     * @return 
     */
    List<String> getModuleManifest();
    
    /**
     * XXX draft,  return a list of pcr values (after an assumed initial zero) that
     * when extended will yield the current pcr value. This will help us to verify
     * the pcr value for module attestation... each agent will need to figure out
     * how to obtain this information from its platform.
     * @param number
     * @return 
     */
    List<Pcr> getPcrHistory(PcrIndex number);
    
    
    /**
     * XXX TODO this method is moved here from the previous interface ManifestStrategy.
     * It's currently here to minimize code changes for the current release
     * but its functionality needs to be moved to the other HostAgent methods.
     * The VCenterHost was written with abstract methods for processDigest() and
     * processReport() and these were overridden "on the fly" with anonymous
     * subclasses in two places.  No time right now to rewrite it properly but
     * they are essentially post-processing the results we obtain from vcenter.
     * So in this adapted getManifest() method, we just provide the subclass
     * instance so it can be called for the post-processing.
     * @param host
     * @return 
     */
    HashMap<String, ? extends IManifest> getManifest(VCenterHost postProcessing);
}
