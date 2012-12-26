/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package test.cmd;

import com.intel.mtwilson.setup.TextConsole;
import com.intel.mtwilson.setup.SetupException;
import org.junit.Test;
import com.intel.mtwilson.setup.cmd.*;

/**
 *
 * @author jbuhacoff
 */
public class TestSetupCommand {
    @Test
    public void testInitializeMysqlDatabase() throws SetupException {
        InitializeMysqlDatabase cmd = new InitializeMysqlDatabase();
        cmd.execute(null);
    }


    @Test
    public void testExecuteCommand() throws SetupException {
        TextConsole.main(new String[] { "SetMtWilsonURL" });
    }

}
