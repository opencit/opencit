/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.cmd;

import com.intel.dcsg.cpg.console.input.Input;
import com.intel.dcsg.cpg.validation.Fault;
import com.intel.dcsg.cpg.console.Command;
import com.intel.mtwilson.setup.SetupTask;
import java.io.IOException;
import java.util.List;
import org.apache.commons.configuration.Configuration;
import com.intel.dcsg.cpg.util.PascalCaseNamingStrategy;

/**
 * This setup command is a bridge between mtwilson-console and the new
 * mtwilson-setup tasks
 * @deprecated use the SetupManager command in mwilson-setup instead
 * @author jbuhacoff
 */
public class V2 implements Command {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(V2.class);

    private Configuration options = null;
    @Override
    public void setOptions(Configuration options) {
        this.options = options;
    }

    @Override
    public void execute(String[] args) throws Exception {
        
//        if(! options.containsKey("env-password")) { throw new IllegalArgumentException("Usage: HashPassword --env-password=PRIVACYCA_DOWNLOAD_PASSWORD"); }          
//        String password = getExistingPassword("the PrivacyCA private key download", "env-password");
        String hyphenatedTaskName = args[0];
        
        PascalCaseNamingStrategy name = new PascalCaseNamingStrategy();
        String className = name.toPascalCase(hyphenatedTaskName);
        String[] packageNames = new String[] { "com.intel.mtwilson.setup.tasks", "com.intel.mtwilson.tag.setup", "com.intel.mtwilson.shiro.setup", "com.intel.mtwilson.privacyca.setup" };
        SetupTask setupTask = null;
        for(String packageName : packageNames) {
            try {
                Class setupTaskClass = Class.forName(packageName+"."+className);
                Object setupTaskInstance = setupTaskClass.newInstance();
                setupTask = (SetupTask)setupTaskInstance;
                break;
            }
            catch(Exception e) {
                // didn't find the class in this package or it's not the right interface
            }
        }
        if( setupTask == null ) {
            System.err.println("Setup task not found: "+className+" ("+hyphenatedTaskName+")");
            return;
        }
                
//                String[] subargs = Arrays.copyOfRange(args, 1, args.length);
        if( setupTask.isConfigured() ) {
            setupTask.run();
            if( setupTask.isValidated() ) {
                System.out.println("Completed "+hyphenatedTaskName);
            }
            else {
                System.err.println("Validation error for "+hyphenatedTaskName);
                List<Fault> validationFaults = setupTask.getValidationFaults();
                for(Fault fault : validationFaults) {
                    System.err.println(fault.toString());
                }
            }
        }
        else {
            System.err.println("Configuration error for "+hyphenatedTaskName);
            List<Fault> configurationFaults = setupTask.getConfigurationFaults();
            for(Fault fault : configurationFaults) {
                System.err.println(fault.toString());
            }
        }
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
