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
public class TagCertificateNotYetValid extends Fault {
    public TagCertificateNotYetValid(Date notBefore) {
        super("Tag certificate not valid before %s", notBefore.toString());
    }
}
