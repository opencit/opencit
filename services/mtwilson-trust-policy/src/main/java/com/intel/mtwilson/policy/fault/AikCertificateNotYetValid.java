/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy.fault;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.intel.mtwilson.policy.Fault;
import java.util.Date;

/**
 *
 * @author jbuhacoff
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
public class AikCertificateNotYetValid extends Fault {
    public AikCertificateNotYetValid() { } // for desearializing jackson
    
    public AikCertificateNotYetValid(Date notBefore) {
        super("AIK certificate not valid before %s", notBefore.toString());
    }
}
