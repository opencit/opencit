/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson;

import com.intel.mtwilson.api.ClientFactory;
import com.intel.dcsg.cpg.io.FileResource;
import com.intel.dcsg.cpg.tls.policy.impl.InsecureTlsPolicy;
import java.io.File;
import java.net.URL;
import com.intel.mtwilson.api.MtWilson;
import java.net.MalformedURLException;
import java.io.IOException;

/**
 *
 * @author jbuhacoff
 */
public class MyClient {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MyClient.class);

    private MtWilson v1 = null;
    
    public MtWilson v1() throws IOException, MalformedURLException {
        if( v1 == null ) {
            log.debug("Mt Wilson URL: {}", My.configuration().getMtWilsonURL().toString());
            v1 = ClientFactory.clientForUserInResource(
                new FileResource(My.configuration().getKeystoreFile()), 
                My.configuration().getKeystoreUsername(),
                My.configuration().getKeystorePassword(),
                My.configuration().getMtWilsonURL(),
                new InsecureTlsPolicy() 
                );
            
        }
        return v1;
    }
}
