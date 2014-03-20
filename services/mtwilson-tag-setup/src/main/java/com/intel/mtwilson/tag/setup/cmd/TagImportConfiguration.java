/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tag.setup.cmd;

import com.intel.mtwilson.tag.setup.TagCommand;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.tag.dao.TagJdbi;
import com.intel.mtwilson.tag.model.Configuration;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This command exports a file from the database to the filesystem
 * @author jbuhacoff
 */
public class TagImportConfiguration extends TagCommand {
    private static Logger log = LoggerFactory.getLogger(TagImportConfiguration.class);
    
    @Override
    public void execute(String[] args) throws Exception {
        // file name, and either outfile or stdout
        if( args.length < 1 ) { throw new IllegalArgumentException("Usage: import-configuration <name>"); }
        String name = args[0];
                
        byte[] content = IOUtils.toByteArray(System.in);
        
        Configuration configuration = TagJdbi.configurationDao().findByName(name);
        if( configuration == null ) {
            // create new file
            TagJdbi.configurationDao().insert(new UUID(), name, new String(content));
        }
        else {
            // update existing file
            TagJdbi.configurationDao().update(configuration.getId(), name, new String(content));
        }
        
    }
    
}
