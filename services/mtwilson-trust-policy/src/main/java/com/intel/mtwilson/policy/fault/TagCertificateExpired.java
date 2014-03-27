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
public class TagCertificateExpired extends Fault {
    public TagCertificateExpired(Date notAfter) {
        super("Tag certificate not valid after %s", notAfter.toString());
    }
}
