/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.cmd;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.atag.AtagCommand;
import com.intel.mtwilson.atag.dao.Derby;
import com.intel.mtwilson.atag.model.Configuration;
import com.intel.mtwilson.atag.model.File;
import java.io.FileNotFoundException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This command exports a file from the database to the filesystem
 * @author jbuhacoff
 */
public class ImportConfiguration extends AtagCommand {
    private static Logger log = LoggerFactory.getLogger(ImportConfiguration.class);
    
    @Override
    public void execute(String[] args) throws Exception {
        // file name, and either outfile or stdout
        if( args.length < 1 ) { throw new IllegalArgumentException("Usage: import-configuration <name>"); }
        String name = args[0];
        
        Derby.startDatabase();
        
        byte[] content = IOUtils.toByteArray(System.in);
        
        Configuration configuration = Derby.configurationDao().findByName(name);
        if( configuration == null ) {
            // create new file
            Derby.configurationDao().insert(new UUID(), name, new String(content));
        }
        else {
            // update existing file
            Derby.configurationDao().update(configuration.getId(), name, new String(content));
        }
        
        Derby.stopDatabase();
    }
    
}
