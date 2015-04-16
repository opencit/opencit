/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.configuration.cmd;

import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.console.InteractiveCommand;
import com.intel.mtwilson.configuration.ConfigurationFactory;
import com.intel.mtwilson.configuration.ConfigurationProvider;
/**
 * Display or set a specific configuration key 
 * 
 * @author jbuhacoff
 */
public class Config extends InteractiveCommand {

    @Override
    public void execute(String[] args) throws Exception {
        
        boolean updated = false;
        String key;
        String newValue;
        
        if( args.length == 1 ) {
            key = args[0];
            newValue = null;
        }
        else if( args.length == 2 ) {
            key = args[0];
            newValue = args[1];
        }
        else {
            throw new IllegalArgumentException("Usage: Config <key> [--delete|newValue]");
        }            
        
            ConfigurationProvider provider = ConfigurationFactory.getConfigurationProvider();
            Configuration configuration = provider.load();

            String existingValue = configuration.get(key);

            if( newValue == null && existingValue != null ) {
                System.out.println(existingValue);
            }
            
            if( getOptions().getBoolean("delete", false) ) {
                configuration.set(key, null);
                updated = true;
            }
            
            if( newValue != null ) {
                configuration.set(key, newValue);
                System.out.println(newValue);
                updated = true;
            }
            
            if( updated ) {
                provider.save(configuration);
            }
            
    }
    

}
