/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.run;

import com.intel.dcsg.cpg.console.Command;
import com.intel.mtwilson.atag.cmd.StartHttpServer;
import java.util.Properties;
import org.apache.commons.configuration.MapConfiguration;

/**
 *
 * @author jbuhacoff
 */
public class StartHttpServerTest {
    
    
    public static void main(String args[]) throws Exception {
        Command cmd = new StartHttpServer();
        cmd.setOptions(new MapConfiguration(new Properties()));
        cmd.execute(new String[0]);
        
    }
    
}
