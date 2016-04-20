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
public class TagCertificateNotYetValid extends Fault {
    public TagCertificateNotYetValid() { } // for desearializing jackson
    
    public TagCertificateNotYetValid(Date notBefore) {
        super("Tag certificate not valid before %s", notBefore.toString());
    }
}
