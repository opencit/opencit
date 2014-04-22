/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tag.setup.cmd;

import com.intel.mtwilson.tag.setup.TagCommand;
import com.intel.dcsg.cpg.console.Command;
import java.util.Properties;
import org.apache.commons.configuration.MapConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class TagSetup extends TagCommand {
    private static Logger log = LoggerFactory.getLogger(TagSetup.class);
    
    @Override
    public void execute(String[] args) throws Exception {
        // tasks to complete: init-database, create-ca-key, and create-mtwilson-client
        TagInitDatabase initDatabase = new TagInitDatabase();
        TagCreateCaKey createCaKey = new TagCreateCaKey();
        TagCreateTlsKeystore createTlsKeystore = new TagCreateTlsKeystore(); // XXX TODO  we probably should keep the configuration for this in the property file then it can execute w/o arguments...
        TagCreateMtWilsonClient createMtwClient = new TagCreateMtWilsonClient();
        Command[] tasks = new Command[] { initDatabase, createCaKey, createTlsKeystore, createMtwClient };
        for(int i=0; i<tasks.length; i++) {
            tasks[i].setOptions(getOptions());
            tasks[i].execute(new String[0]);
        }
    }
    

 
    public static void main(String args[]) throws Exception {
        TagSetup cmd = new TagSetup();
        cmd.setOptions(new MapConfiguration(new Properties()));
        cmd.execute(new String[0]);
        
    }    
}
