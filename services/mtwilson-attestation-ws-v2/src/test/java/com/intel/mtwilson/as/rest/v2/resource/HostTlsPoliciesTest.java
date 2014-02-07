/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.as.rest.v2.resource;

import com.intel.mtwilson.as.rest.v2.model.*;

import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class HostTlsPoliciesTest {
    @Test
    public void testHostTlsPoliciesResource() {
        HostTlsPolicy r = new HostTlsPolicy();
        HostTlsPolicyCollection c = r.createEmptyCollection();
    }
}
