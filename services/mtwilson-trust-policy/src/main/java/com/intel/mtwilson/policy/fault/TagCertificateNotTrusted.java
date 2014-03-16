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
public class TagCertificateNotTrusted extends Fault {
    public TagCertificateNotTrusted() {
        super("Tag certificate is not signed by any trusted CA");
    }
}
