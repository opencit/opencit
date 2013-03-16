/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy.fault;

import com.intel.mtwilson.model.PcrIndex;
import com.intel.mtwilson.validation.Fault;

/**
 *
 * @author jbuhacoff
 */
public class PcrModuleManifestMissing extends Fault {
    public PcrModuleManifestMissing() {
        super("Host report does not include a PCR Module Manifest");
    }
    public PcrModuleManifestMissing(PcrIndex pcrIndex) {
        super("Host report does not include a PCR Module Manifest for PCR %d", pcrIndex.toInteger());
    }
}
