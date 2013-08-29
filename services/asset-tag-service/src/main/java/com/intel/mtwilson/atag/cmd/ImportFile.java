/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.cmd;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.atag.AtagCommand;
import com.intel.mtwilson.atag.Derby;
import com.intel.mtwilson.atag.model.File;
import java.io.FileNotFoundException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This command exports a file from the database to the filesystem
 * @author jbuhacoff
 */
public class ImportFile extends AtagCommand {
    private static Logger log = LoggerFactory.getLogger(ImportFile.class);
    
    @Override
    public void execute(String[] args) throws Exception {
        // file name, and either outfile or stdout
        if( args.length < 1 ) { throw new IllegalArgumentException("Usage: import-file <filename> [--type=text/plain]"); }
        String filename = args[0];
        
        Derby.startDatabase();
        
        byte[] content = IOUtils.toByteArray(System.in);
        
        File file = Derby.fileDao().findByName(filename);
        if( file == null ) {
            // create new file
            Derby.fileDao().insert(new UUID(), filename, getOptions().getString("type", "text/plain") , content);
        }
        else {
            // update existing file
            Derby.fileDao().update(file.getId(), filename, getOptions().getString("type",  file.getContentType()), content);
        }
        
        Derby.stopDatabase();
    }
    
}
