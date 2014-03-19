/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tag.setup;

import com.intel.mtwilson.tag.dao.TagJdbi;
import com.intel.mtwilson.tag.model.Configuration;
import com.intel.mtwilson.tag.model.File;
import java.io.FileNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This command exports a file from the database to the filesystem
 * @author jbuhacoff
 */
public class ExportConfiguration extends TagCommand {
    private static Logger log = LoggerFactory.getLogger(ExportConfiguration.class);
    
    @Override
    public void execute(String[] args) throws Exception {
        // file name, and either outfile or stdout
        if( args.length < 1 ) { throw new IllegalArgumentException("Usage: export-configuration <name>"); }
        String name = args[0];
        
        Configuration configuration = TagJdbi.configurationDao().findByName(name);
        if( configuration == null ) {
            throw new FileNotFoundException();
        }
        System.out.println(configuration.getXmlContent());
        
    }
    
}
