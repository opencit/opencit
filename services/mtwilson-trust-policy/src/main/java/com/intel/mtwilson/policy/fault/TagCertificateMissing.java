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
public class TagCertificateMissing extends Fault {
    public TagCertificateMissing() {
        super("Host trust policy requires tag validation but the tag certificate was not found");
    }
}
