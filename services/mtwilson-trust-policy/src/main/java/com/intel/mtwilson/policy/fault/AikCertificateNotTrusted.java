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
public class AikCertificateNotTrusted extends Fault {
    public AikCertificateNotTrusted() {
        super("AIK certificate is not signed by any trusted CA");
    }
}
