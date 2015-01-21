/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.configuration.cmd;

import com.intel.dcsg.cpg.console.InteractiveCommand;
import com.intel.dcsg.cpg.crypto.file.PasswordEncryptedFile;
import com.intel.dcsg.cpg.crypto.key.password.PasswordProtection;
import com.intel.dcsg.cpg.crypto.key.password.PasswordProtectionBuilder;
import com.intel.dcsg.cpg.io.FileResource;
import com.intel.mtwilson.Filesystem;
import java.io.File;
import java.io.FileInputStream;
import org.apache.commons.io.IOUtils;
/**
 * TODO:  this command has been improved in mtwilson, should be using just that one 
 * instead of a copy.
 * 
 * @author jbuhacoff
 */
public class ImportConfig extends InteractiveCommand {

    @Override
    public void execute(String[] args) throws Exception {
        
        String content;
        
        if( args.length == 1 ) {
            String filename = args[0];
            try(FileInputStream in = new FileInputStream(new File(filename))) {
                content = IOUtils.toString(in);
            }
        }
        else if( args.length < 1 && options != null && options.containsKey("stdin")) { 
            content = IOUtils.toString(System.in);
        }
        else {
            throw new IllegalArgumentException("Usage: ImportConfig <infile|--stdin> [--env-password=KMS_PASSWORD]");
        }
        
        String password = getNewPassword("the Server Configuration File", "env-password");
        
        PasswordProtection protection = PasswordProtectionBuilder.factory().aes(256).block().sha256().pbkdf2WithHmacSha1().saltBytes(8).iterations(1000).build();
        if( !protection.isAvailable() ) {
//                log.warn("Protection algorithm {} key length {} mode {} padding {} not available", protection.getAlgorithm(), protection.getKeyLengthBits(), protection.getMode(), protection.getPadding());
            protection = PasswordProtectionBuilder.factory().aes(128).block().sha256().pbkdf2WithHmacSha1().saltBytes(8).iterations(1000).build();
        }
        
        Filesystem fs = new Filesystem();
        FileResource resource = new FileResource(fs.getConfigurationFile());
        PasswordEncryptedFile encryptedFile = new PasswordEncryptedFile(resource, password, protection);

        encryptedFile.saveString(content);
    }
    

}
