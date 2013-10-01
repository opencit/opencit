/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package test.database;

import com.intel.mtwilson.setup.SetupException;
import com.intel.mtwilson.setup.cmd.InitDatabase;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class DatabaseInitTest {
    @Test
    public void testFindMysqlFiles() throws Exception {
        InitDatabase cmd = new InitDatabase();
        cmd.execute(new String[] { "mysql" });
    }
    
}
