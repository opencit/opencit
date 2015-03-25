/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.configuration.cmd;

import com.intel.dcsg.cpg.console.InteractiveCommand;
import com.intel.dcsg.cpg.crypto.file.PasswordEncryptedFile;
import com.intel.dcsg.cpg.io.FileResource;
import com.intel.mtwilson.Environment;
import com.intel.mtwilson.MyConfiguration;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.commons.io.IOUtils;
/**
 * 
 * @author jbuhacoff
 */
public class ExportConfig extends InteractiveCommand {

    @Override
    public void execute(String[] args) throws Exception {
        
        
        String password;
        if( options != null && options.containsKey("env-password") ) {
            password = getExistingPassword("the Server Configuration File", "env-password");
        }
        else {
            // gets environment variable MTWILSON_PASSWORD, TRUSTAGENT_PASSWORD, KMS_PASSWORD, etc.
            password = Environment.get("PASSWORD");
            if( password == null ) {
                throw new IllegalArgumentException("Usage: ImportConfig <outfile|--out=outfile|--stdout> [--env-password=PASSWORD_VAR]");
            }
        }
        
        if( options != null && options.containsKey("out") ) {
            String filename = options.getString("out");
            try(FileOutputStream out = new FileOutputStream(new File(filename))) {
                export(out, password);
            }
        }
        else if( options != null && options.getBoolean("stdout", false) ) {
            log.debug("Output filename not provided; exporting to stdout");
            export(System.out, password);
        }
        else if( args.length == 1 ) {
            String filename = args[0];
            try(FileOutputStream out = new FileOutputStream(new File(filename))) {
                export(out, password);
            }
        }
        else {
            throw new IllegalArgumentException("Usage: ImportConfig <outfile|--out=outfile|--stdout> [--env-password=PASSWORD_VAR]");
        }
        
    }

    public void export(OutputStream out, String password) throws IOException {
        MyConfiguration config = new MyConfiguration();
        FileResource resource = new FileResource(config.getConfigurationFile());
        PasswordEncryptedFile encryptedFile = new PasswordEncryptedFile(resource, password);
        
        String content = encryptedFile.loadString();
        IOUtils.write(content, out);
    }

}
