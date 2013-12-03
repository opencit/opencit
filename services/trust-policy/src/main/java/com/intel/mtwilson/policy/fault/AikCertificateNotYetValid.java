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
public class AikCertificateNotYetValid extends Fault {
    public AikCertificateNotYetValid(Date notBefore) {
        super("AIK certificate not valid before %s", notBefore.toString());
    }
}
