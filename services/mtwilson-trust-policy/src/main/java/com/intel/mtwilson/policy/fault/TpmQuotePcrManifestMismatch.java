/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy.fault;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.intel.mtwilson.policy.Fault;

/**
 * For vmware hosts that do not provide a TPM Quote, so we receive a PCR Manifest
 * and we have to "trust" that it's from a legitimate TPM.
 * For other hosts, we receive a TPM Quote from which we extract the PCR Manifest.
 * However, that extraction is done outside the trust policy code so for that
 * reason, if there is a TPM Quote available we must use it to verify the content
 * of the PCR Manifest.
 * 
 * This fault occurs when the contents of the PCR Manifest in the host report
 * cannot be verified against the contents of the TPM Quote in the same report.
 * 
 * @author jbuhacoff
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
public class TpmQuotePcrManifestMismatch extends Fault {
    public TpmQuotePcrManifestMismatch() {
        super("Contents of TPM Quote do not match the provided PCR Manifest");
    }
}
