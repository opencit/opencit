/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy.fault;

import com.intel.mtwilson.policy.Fault;
import java.util.Date;

/**
 *
 * @author jbuhacoff
 */
public class AikCertificateExpired extends Fault {
    public AikCertificateExpired(Date notAfter) {
        super("AIK certificate not valid after %s", notAfter.toString());
    }
}
