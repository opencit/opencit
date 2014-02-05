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
    public PcrEventLogInvalid(PcrIndex pcrIndex) {
        super("PCR %d Event Log is invalid", pcrIndex.toInteger());
    }
}
