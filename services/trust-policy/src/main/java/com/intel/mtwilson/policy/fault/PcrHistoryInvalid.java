/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy.fault;

import com.intel.mtwilson.model.Pcr;
import com.intel.mtwilson.policy.Fault;

/**
 *
 * @author jbuhacoff
 */
public class PcrHistoryInvalid extends Fault {
    public PcrHistoryInvalid(Pcr expected) {
        super("PCR History for PCR %d does not match its expected value %s", expected.getIndex().toInteger(), expected.getValue().toString());
    }
}
