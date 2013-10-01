/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy.fault;

import com.intel.mtwilson.policy.Fault;

/**
 *
 * @author jbuhacoff
 */
public class TpmQuoteSignatureInvalid extends Fault {
    public TpmQuoteSignatureInvalid() {
        super("Signature of TPM Quote cannot be verified");
    }
}
