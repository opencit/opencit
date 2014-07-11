/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.cmd;

import com.intel.dcsg.cpg.crypto.RsaCredentialX509;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.mtwilson.ms.common.MSConfig;
import com.intel.dcsg.cpg.console.Command;
import com.intel.mtwilson.setup.SetupException;
import com.intel.mtwilson.setup.SetupWizard;
import com.intel.dcsg.cpg.io.Filename;
import com.intel.mtwilson.setup.SetupContext;
import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.configuration.Configuration;

/**
 *
 * @author jbuhacoff
 */
public class SearchApiClient implements Command {

    public static final Console console = System.console();

    private Configuration options = null;
    @Override
    public void setOptions(Configuration options) {
        this.options = options;
    }

    /**
     * Creates a new API Client in current directory, registers it with Mt Wilson (on localhost or as configured), and then checks the database for the expected record to validate that it's being created.
     * @param args
     * @throws Exception 
     */
    @Override
    public void execute(String[] args) throws Exception {
        Configuration serviceConf = MSConfig.getConfiguration();
        File directory = new File(".");
        String username = readInputStringWithPrompt("Username");
        String password = readInputStringWithPrompt("Password");
        File keystoreFile = new File(directory.getAbsolutePath() + File.separator + Filename.encode(username) + ".jks");
        if( !keystoreFile.exists() ) {
            System.out.println("Cannot find keystore "+keystoreFile.getAbsolutePath());
            return;
        }
        // load the existing key
        SimpleKeystore keystore = new SimpleKeystore(keystoreFile, password);
        RsaCredentialX509 rsaCredentialX509 = keystore.getRsaCredentialX509(username, password);
        // check database for record
//        ApiClientBO bo = new ApiClientBO();
//        ApiClientInfo apiClientRecord = bo.find(rsaCredentialX509.identity());
//        ApiClientInfo apiClientRecord = findApiClientRecord(serviceConf, rsaCredentialX509.identity());
//        if( apiClientRecord == null ) {
        if( findApiClientRecord(serviceConf, rsaCredentialX509.identity()) ) {
            System.out.println(String.format("Cannot find record for %s in database [fingerprint %s]", username, Hex.encodeHexString(rsaCredentialX509.identity()))); // Base64.encodeBase64String
            return;
        }
        System.out.println(String.format("Found record for %s in database [fingerprint %s]", username, Hex.encodeHexString(rsaCredentialX509.identity())));        
    }
    
    private boolean findApiClientRecord(Configuration conf, byte[] fingerprint) throws SetupException, IOException {
        boolean found = false;
        SetupWizard wizard = new SetupWizard(conf);
        try {
            try (Connection c = wizard.getMSDatabaseConnection();
                    Statement s = c.createStatement();
                    ResultSet rs = s.executeQuery("SELECT ID,name,status FROM api_client_x509 WHERE hex(fingerprint)=\"" + Hex.encodeHexString(fingerprint).toUpperCase() + "\"")) {
                if (rs.next()) {
                    found = true;
                }
            }
            return found;
        }
        catch(SQLException e) {
            throw new SetupException("Cannot find API Client record: "+e.getMessage(), e);
        }        
    }

    
    //private static String getLocalHostAddress() {
    //    try {
    //        InetAddress addr = InetAddress.getLocalHost();
    //        return addr.getHostAddress();
    //    } catch (UnknownHostException ex) {
    //        return "127.0.0.1";
    //    }
    //}
    
    private String readInputStringWithPrompt(String prompt) throws IOException {
        
        if (console == null) {
            throw new IOException("no console.");
        }
		//BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        System.out.print(String.format("%s: ", prompt));
        String input = console.readLine();
//        in.close(); // don't close System.in !!
        return input;
    }
    // comment out unused function (6/11 1.2)
    /*
    private String readInputStringWithPromptAndDefault(String prompt, String defaultValue) throws IOException {
        
        if (console == null) {
            throw new IOException("no console.");
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        System.out.print(String.format("%s [%s]: ", prompt, defaultValue));
        String input = console.readLine();
//        in.close(); // don't close System.in !!
        if( input == null || input.isEmpty() ) {
            input = defaultValue;
        }
        return input;
    }
    */
    // commenting out unused function (6/11 1.2)
    /*
    private String firstNonEmpty(String[] values) {
        for(String value : values) {
            if( value != null && !value.isEmpty() ) {
                return value;
            }
        }
        return null;
    }
    */
}
