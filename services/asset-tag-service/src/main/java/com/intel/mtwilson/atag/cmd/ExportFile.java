/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.cmd;

import com.intel.mtwilson.atag.AtagCommand;
import com.intel.mtwilson.atag.dao.Derby;
import com.intel.mtwilson.atag.model.File;
import java.io.FileNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This command exports a file from the database to the filesystem
 * @author jbuhacoff
 */
public class ExportFile extends AtagCommand {
    private static Logger log = LoggerFactory.getLogger(ExportFile.class);
    
    @Override
    public void execute(String[] args) throws Exception {
        // file name, and either outfile or stdout
        if( args.length < 1 ) { throw new IllegalArgumentException("Usage: export-file <filename>"); }
        String filename = args[0];
        
        Derby.startDatabase();
        File file = Derby.fileDao().findByName(filename);
        if( file == null ) {
            throw new FileNotFoundException();
        }
        System.out.write(file.getContent());
        
        Derby.stopDatabase();
    }
    
}
