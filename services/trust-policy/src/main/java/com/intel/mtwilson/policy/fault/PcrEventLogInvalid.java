/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy.fault;

import com.intel.mtwilson.model.PcrIndex;
import com.intel.mtwilson.policy.Fault;

/**
 *
 * @author jbuhacoff
 */
public class PcrEventLogInvalid extends Fault {
    public PcrEventLogInvalid() {
        super("Host report does not include a PCR Module Manifest");
    }
    public PcrEventLogInvalid(PcrIndex pcrIndex) {
        super("Host report does not include a PCR Module Manifest for PCR %d", pcrIndex.toInteger());
    }
}
