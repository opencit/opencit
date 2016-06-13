/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.intel.mtwilson.model.Aik;
import com.intel.mtwilson.model.Measurement;
import com.intel.mtwilson.model.PcrIndex;
import com.intel.mtwilson.model.PcrManifest;
import com.intel.mtwilson.model.TpmQuote;
import java.util.List;
import java.util.Map;
import com.intel.mtwilson.tag.model.X509AttributeCertificate;
import com.intel.mtwilson.tag.model.json.X509AttributeCertificateDeserializer;

/**
 * Note:  The trust-policy module and this HostReport object can only be used
 * AFTER a host is already registered in Mt Wilson and associated with a whitelist.
 * It should not be used during the registration process.
 * 
 * The AIK must be loaded by attestation service from the database and placed
 * into this instance.
 * 
 * The HostReport is the "actual value" used when determining a host's compliance
 * with a given TrustPolicy. 
 * The HostReport represents the response we get when we request a TPM quote and
 * other information from a host.  It's not the same as a TpmQuote because some
 * hosts (vmware) do not provide us real TPM quotes or AIKs.  So it may contain
 * (for vmware hosts) a PCR manifest without a TPM quote or AIK. 
 * @author jbuhacoff
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
public class HostReport {
    public Map<String,String> variables; // such as host uuid, which may be referenced in calculated (dynamic) policy
    public PcrManifest pcrManifest; // list of all pcr's and their values... should be a complete list, with 0's for unused pcr's
//    public Map<PcrIndex,List<Measurement>> pcrModuleManifest; // list of all modules for one pcr ... 
    public TpmQuote tpmQuote; // the original quote from the tpm which should cover the pcr manifest (except for vmware for which we don't get a real quote)
    public Aik aik; // the host's aik certificate that signed the quote (except for vmware for which we don't get an aik)
//    public Nonce nonce; // the nonce that was used to guarantee freshness (is this the challenge nonce or response nonce ??? hmm maybe not needed because it maybe part of TpmQuote)
    @JsonDeserialize(using=X509AttributeCertificateDeserializer.class)
    public X509AttributeCertificate tagCertificate;
    public byte[] assetTagReported; // This is asset tag hash reported from the host. mainly used on Windows host
}
