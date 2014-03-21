/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.cmd;

import com.intel.dcsg.cpg.crypto.PasswordHash;
import com.intel.dcsg.cpg.console.input.Input;
import com.intel.dcsg.cpg.crypto.file.PasswordEncryptedFile;
import com.intel.dcsg.cpg.io.FileResource;
import com.intel.dcsg.cpg.console.Command;
import com.intel.mtwilson.setup.SetupContext;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author jbuhacoff
 */
public class HashPassword implements Command {
   
    private Configuration options = null;
    @Override
    public void setOptions(Configuration options) {
        this.options = options;
    }

    @Override
    public void execute(String[] args) throws Exception {
        if(! options.containsKey("env-password")) { throw new IllegalArgumentException("Usage: HashPassword --env-password=PRIVACYCA_DOWNLOAD_PASSWORD"); }          
        String password = getExistingPassword("the PrivacyCA private key download", "env-password");
        PasswordHash pwHash = new PasswordHash(password);
        System.out.println(pwHash.toString());
    }

    
    
    /**
     * Use this method when you need the user to provide a password for an existing key. 
     * If an environment variable is provided as an option, its value is used.
     * Otherwise, the user is prompted for the password just once.
     * 
     * If an environment variable is provided but is empty, the user is prompted.
     * 
     * @param label human-readable text to incorporate into the prompt, for example "the Data Encryption Key"
     * @param optName the name of the command-line option that can be used to name an environment variable containing the password (option value never used as the password itself)
     * @return
     * @throws IOException 
     */
    public String getExistingPassword(String label, String optName) throws IOException {
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
        if( password == null ) {
            password = Input.getRequiredPasswordWithPrompt(String.format("A password is required to unlock %s.", label)); // throws IOException, or always returns value or expression
        }
        return password;        
    }    
}
