/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.agent;

import com.intel.mtwilson.datatypes.Aik;
import com.intel.mtwilson.datatypes.Nonce;
import com.intel.mtwilson.datatypes.Pcr;
import com.intel.mtwilson.datatypes.PcrValue;
import com.intel.mtwilson.datatypes.TpmQuote;
import java.util.List;
import java.util.Set;

/**
 * XXX TODO this is a draft of the interface that Linux, Citrix, and Vmware
 * agents should implement for communicating information about their hosts
 * to Mt Wilson. THis will allow Mt Wilson to treat them uniformly and move
 * all the platform-specific calls and procedures into those agents in a 
 * clean way. 
 * @author jbuhacoff
 */
public interface Host {
    
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
     * XXX this is a draft - need to check it against linux & citrix requirements
     * to ensure it makes sense. 
     * Vmware agent must throw unsupported operation exception since it doesn't
     * provide quotes, only "pcr information" through it's API. 
     * @param aik
     * @param nonce
     * @param pcr
     * @return 
     */
    TpmQuote getTpmQuote(Aik aik, Nonce nonce, Set<Pcr> pcr);
    
    /**
     * Agents should return the entire set of PCRs from the host. The attestation
     * service will then choose the ones it wants to verify against the whitelist.
     * Returning all PCR's is cheap (there are only 24) and makes the API simple.
     * @return 
     */
    Set<PcrValue> getPcrValues();
    
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
    Set<String> getModuleManifest();
    
    /**
     * XXX draft,  return a list of pcr values (after an assumed initial zero) that
     * when extended will yield the current pcr value. This will help us to verify
     * the pcr value for module attestation... each agent will need to figure out
     * how to obtain this information from its platform.
     * @param number
     * @return 
     */
    List<PcrValue> getPcrHistory(Pcr number);
}
