/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package test.config;

import com.intel.mtwilson.ms.common.MSConfig;
import org.apache.commons.configuration.Configuration;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class MSConfigTest {
    @Test
    public void testLoadConfig() {
        Configuration serviceConf = MSConfig.getConfiguration();        
        assertEquals("https://10.1.71.80:8181", serviceConf.getString("mtwilson.api.baseurl"));
    }
}
