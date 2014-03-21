/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tag.setup.cmd;

import com.intel.mtwilson.tag.setup.TagCommand;
import com.intel.mtwilson.tag.dao.TagJdbi;
import com.intel.mtwilson.tag.model.File;
import java.io.FileNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This command exports a file from the database to the filesystem
 * @author jbuhacoff
 */
public class TagExportFile extends TagCommand {
    private static Logger log = LoggerFactory.getLogger(TagExportFile.class);
    
    @Override
    public void execute(String[] args) throws Exception {
        // file name, and either outfile or stdout
        if( args.length < 1 ) { throw new IllegalArgumentException("Usage: export-file <filename>"); }
        String filename = args[0];
        
        File file = TagJdbi.fileDao().findByName(filename);
        if( file == null ) {
            throw new FileNotFoundException();
        }
        System.out.write(file.getContent());
        
    }
    
}
