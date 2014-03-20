/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package test.database;

import com.intel.mtwilson.setup.SetupException;
import com.intel.mtwilson.tag.setup.cmd.TagInitDatabase;
import java.util.Properties;
import org.apache.commons.configuration.MapConfiguration;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class DatabaseInitTest {
    @Test
    public void testFindMysqlFiles() throws Exception {
        Properties p = new Properties();
        p.setProperty("check", "true");
        p.setProperty("verbose", "true");
        TagInitDatabase cmd = new TagInitDatabase();
        cmd.setOptions(new MapConfiguration(p));
        cmd.execute(new String[] { "mysql" });
    }
    
}
