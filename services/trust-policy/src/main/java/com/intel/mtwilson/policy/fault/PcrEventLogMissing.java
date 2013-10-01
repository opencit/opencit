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
public class PcrEventLogMissing extends Fault {
    public PcrEventLogMissing() {
        super("Host report does not include a PCR Event Log");
    }
    public PcrEventLogMissing(PcrIndex pcrIndex) {
        super("Host report does not include a PCR Event Log for PCR %d", pcrIndex.toInteger());
    }
}
