/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package test.cmd;

import com.intel.mtwilson.setup.SetupException;
import com.intel.mtwilson.setup.TextConsole;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class CheckConfigTest {
    @Test
    public void testCheckConfig() throws Exception {
//        TextConsole.main(new String[] { "CheckConfig" });
        TextConsole.main(new String[] { "CheckConfig", "--jpa" });
    }


    @Test
    public void testExecuteCommand() throws SetupException {
//        TextConsole.main(new String[] { "SetMtWilsonURL" });
    }

}
