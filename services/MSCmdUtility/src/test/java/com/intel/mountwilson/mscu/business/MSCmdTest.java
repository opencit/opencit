package com.intel.mountwilson.mscu.business;

import com.intel.mountwilson.mscu.common.MSCUConfig;
import org.apache.commons.configuration.Configuration;
import org.junit.Test;

public class MSCmdTest
{
    @Test
    public void testApp() {
        Configuration config = MSCUConfig.getConfiguration();       
        System.out.println("mtwilson.api.baseurl :" + config.getString("mtwilson.api.baseurl"));
    }
}
