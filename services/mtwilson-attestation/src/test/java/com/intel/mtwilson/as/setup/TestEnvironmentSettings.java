/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.as.setup;

import com.intel.dcsg.cpg.io.ConfigurationUtil;
import java.io.IOException;
import org.apache.commons.configuration.Configuration;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class TestEnvironmentSettings {
    @Test
    public void testLoadSettings() throws IOException {
        Configuration conf = ConfigurationUtil.fromResource("/dev-0.5.4.properties");
        System.out.println(conf.getString("mtwilson.api.baseurl", "unable to load settings"));
    }
}
