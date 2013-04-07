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
public class PcrManifestMissing extends Fault {
    public PcrManifestMissing() {
        super("Host report does not include a PCR Manifest");
    }
}
