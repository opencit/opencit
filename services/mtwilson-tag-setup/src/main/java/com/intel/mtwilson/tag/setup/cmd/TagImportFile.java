/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tag.setup.cmd;

import com.intel.mtwilson.tag.setup.TagCommand;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.tag.dao.TagJdbi;
import com.intel.mtwilson.tag.model.File;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This command exports a file from the database to the filesystem
 * @author jbuhacoff
 */
public class TagImportFile extends TagCommand {
    private static Logger log = LoggerFactory.getLogger(TagImportFile.class);
    
    @Override
    public void execute(String[] args) throws Exception {
        // file name, and either outfile or stdout
        if( args.length < 1 ) { throw new IllegalArgumentException("Usage: import-file <filename> [--type=text/plain]"); }
        String filename = args[0];
                
        byte[] content = IOUtils.toByteArray(System.in);
        
        File file = TagJdbi.fileDao().findByName(filename);
        if( file == null ) {
            // create new file
            TagJdbi.fileDao().insert(new UUID(), filename, getOptions().getString("type", "text/plain") , content);
        }
        else {
            // update existing file
            TagJdbi.fileDao().update(file.getId(), filename, getOptions().getString("type",  file.getContentType()), content);
        }
        
    }
    
}
