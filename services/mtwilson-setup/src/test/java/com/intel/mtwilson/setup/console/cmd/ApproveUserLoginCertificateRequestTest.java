/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.console.cmd;

import com.intel.dcsg.cpg.console.ExtendedOptions;
import java.util.Arrays;
import org.apache.commons.configuration.Configuration;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class ApproveUserLoginCertificateRequestTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ApproveUserLoginCertificateRequestTest.class);

    
    @Test
    public void testApproveUser() throws Exception {
        String[] commandOptions = new String[] {"TestAdmin999","--roles", "whitelist,security,unknown", "--permissions", "Tdomain1:Taction1", "Tdomain2:Taction2" };
        String[] subargs = Arrays.copyOfRange(commandOptions, 0, commandOptions.length);
        ExtendedOptions getopt = new ExtendedOptions(subargs);
        Configuration options = getopt.getOptions();
        subargs = getopt.getArguments();        
        ApproveUserLoginCertificateRequest test = new ApproveUserLoginCertificateRequest();
        test.setOptions(options);
        test.execute(commandOptions);
        log.debug("Done");
    }

}
