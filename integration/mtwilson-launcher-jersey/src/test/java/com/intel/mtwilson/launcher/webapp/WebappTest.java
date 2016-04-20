/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.launcher.webapp;

import com.intel.mtwilson.jetty.JunitWebapp;
import java.io.IOException;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class WebappTest extends JunitWebapp {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WebappTest.class);
    
    @Test
    public void testWebapp() throws IOException {
        log.debug("test webapp");
        System.in.read(); // pause
    }
    
}
