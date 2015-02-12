/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.configuration.cmd;

import com.intel.dcsg.cpg.console.InteractiveCommand;
import com.intel.dcsg.cpg.crypto.file.PasswordEncryptedFile;
import com.intel.dcsg.cpg.io.FileResource;
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
        
        
        if( args.length == 1 ) {
            String password = getExistingPassword("the Server Configuration File", "env-password");
            String filename = args[0];
            try(FileOutputStream out = new FileOutputStream(new File(filename))) {
                export(out, password);
            }
        }
        else if( args.length < 1 && options != null && options.containsKey("stdout")) { 
            log.debug("Output filename not provided; exporting to stdout");
            // usage: ExportConfig <outfile> [--env-password CONFIG_PASSWD]
            String password = getExistingPassword("the Server Configuration File", "env-password");
            export(System.out, password);
        }
        else {
            throw new IllegalArgumentException("Usage: ExportConfig <outfile|--stdout> [--env-password=KMS_PASSWORD]");
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
