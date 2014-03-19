/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tag.setup;

import com.intel.dcsg.cpg.console.Command;
import java.util.Properties;
import org.apache.commons.configuration.MapConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class Setup extends TagCommand {
    private static Logger log = LoggerFactory.getLogger(Setup.class);
    
    @Override
    public void execute(String[] args) throws Exception {
        // tasks to complete: init-database, create-ca-key, and create-mtwilson-client
        InitDatabase initDatabase = new InitDatabase();
        CreateCaKey createCaKey = new CreateCaKey();
        CreateTlsKeystore createTlsKeystore = new CreateTlsKeystore(); // XXX TODO  we probably should keep the configuration for this in the property file then it can execute w/o arguments...
        Command[] tasks = new Command[] { initDatabase, createCaKey, createTlsKeystore };
        for(int i=0; i<tasks.length; i++) {
            tasks[i].setOptions(getOptions());
            tasks[i].execute(new String[0]);
        }
    }
    

 
    public static void main(String args[]) throws Exception {
        Setup cmd = new Setup();
        cmd.setOptions(new MapConfiguration(new Properties()));
        cmd.execute(new String[0]);
        
    }    
}
