/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.cmd;

import com.intel.mtwilson.atag.AtagCommand;
import com.intel.mtwilson.atag.dao.Derby;
import com.intel.mtwilson.atag.model.Configuration;
import com.intel.mtwilson.atag.model.File;
import java.io.FileNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This command exports a file from the database to the filesystem
 * @author jbuhacoff
 */
public class ExportConfiguration extends AtagCommand {
    private static Logger log = LoggerFactory.getLogger(ExportConfiguration.class);
    
    @Override
    public void execute(String[] args) throws Exception {
        // file name, and either outfile or stdout
        if( args.length < 1 ) { throw new IllegalArgumentException("Usage: export-configuration <name>"); }
        String name = args[0];
        
        Derby.startDatabase();
        Configuration configuration = Derby.configurationDao().findByName(name);
        if( configuration == null ) {
            throw new FileNotFoundException();
        }
        System.out.println(configuration.getXmlContent());
        
        Derby.stopDatabase();
    }
    
}
