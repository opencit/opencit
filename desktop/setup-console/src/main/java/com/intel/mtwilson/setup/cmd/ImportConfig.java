/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.cmd;

import com.intel.dcsg.cpg.console.input.Input;
import com.intel.dcsg.cpg.crypto.file.PasswordEncryptedFile;
import com.intel.dcsg.cpg.crypto.key.password.PasswordProtection;
import com.intel.dcsg.cpg.crypto.key.password.PasswordProtectionBuilder;
import com.intel.dcsg.cpg.io.FileResource;
import com.intel.dcsg.cpg.io.Platform;
import com.intel.mountwilson.as.common.ASConfig;
import com.intel.dcsg.cpg.console.Command;
import com.intel.mtwilson.setup.SetupContext;
import com.intel.mtwilson.setup.model.SetupTarget;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author jbuhacoff
 */
public class ImportConfig implements Command {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ImportConfig.class);

    private Configuration options = null;
    @Override
    public void setOptions(Configuration options) {
        this.options = options;
    }

    @Override
    public void execute(String[] args) throws Exception {

        if( args.length < 1) { throw new IllegalArgumentException("Usage: ImportConfig <encrypted-file> [--in=<file>|--stdin] [--env-password=MTWILSON_PASSWORD]"); }   
        String password = getNewPassword("the Mt Wilson Encrypted Configuration File", "env-password");
        for(int i=0;i<args.length;i++) {
            String filename = args[i];
        
        
            PasswordProtection protection = PasswordProtectionBuilder.factory().aes(256).block().sha256().pbkdf2WithHmacSha1().saltBytes(8).iterations(1000).build();
            if( !protection.isAvailable() ) {
//                log.warn("Protection algorithm {} key length {} mode {} padding {} not available", protection.getAlgorithm(), protection.getKeyLengthBits(), protection.getMode(), protection.getPadding());
                protection = PasswordProtectionBuilder.factory().aes(128).block().sha256().pbkdf2WithHmacSha1().saltBytes(8).iterations(1000).build();
            }
            FileResource resource = new FileResource(new File(filename));
            PasswordEncryptedFile encryptedFile = new PasswordEncryptedFile(resource, password, protection);
        
            String content ;
            if( options.containsKey("in") ) {
                try (FileInputStream in = new FileInputStream(new File(options.getString("in")))) {
                    content = IOUtils.toString(in);
                }
            }
            else if( options.getBoolean("stdin", false) ) {
                content = IOUtils.toString(System.in);
            }else {
                try (FileInputStream in = new FileInputStream(new File(filename))) {
                    content = IOUtils.toString(in);
                }
            }
            encryptedFile.saveString(content);
        }
    }

    
    /**
     * Use this method when you need the user to set a password for a new key. 
     * If an environment variable is provided as an option, its value is used.
     * Otherwise, the user is prompted for the password twice (to confirm).
     * 
     * If an environment variable is provided but is empty, the user is prompted.
     * 
     * @param label human-readable text to incorporate into the prompt, for example "the Data Encryption Key"
     * @param optName the name of the command-line option that can be used to name an environment variable containing the password (option value never used as the password itself)
     * @throws IOException 
     */
    public String getNewPassword(String label, String optName) throws IOException {
        String password;
        if( options != null && options.containsKey(optName) ) {
            String passwordVar = options.getString(optName);
            password = System.getenv(passwordVar);
            if( password == null ) {
                System.err.println(String.format("Cannot get password from environment variable '%s' specified by option '%s'", passwordVar, optName));
            }
        }
        else {
            password = System.getenv("MTWILSON_PASSWORD");
        }
        if( password == null || password.isEmpty() ) {
            password = Input.getConfirmedPasswordWithPrompt(String.format("You must protect %s with a password.", label)); // throws IOException, or always returns value or expression
        }
        return password;
    }
    

}
