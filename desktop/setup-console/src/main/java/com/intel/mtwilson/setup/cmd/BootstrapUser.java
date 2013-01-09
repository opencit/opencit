/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.cmd;

import com.intel.mtwilson.ApiCommand;
import com.intel.mtwilson.crypto.RsaCredentialX509;
import com.intel.mtwilson.crypto.SimpleKeystore;
import com.intel.mtwilson.ms.common.MSConfig;
import com.intel.mtwilson.setup.Command;
import com.intel.mtwilson.setup.SetupException;
import com.intel.mtwilson.setup.SetupWizard;
import com.intel.mtwilson.io.Filename;
import com.intel.mtwilson.setup.SetupContext;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.configuration.Configuration;

/**
 *
 * @author jbuhacoff
 */
public class BootstrapUser implements Command {
    private SetupContext ctx = null;

    @Override
    public void setContext(SetupContext ctx) {
        this.ctx = ctx;
    }
    
    /**
     * Creates a new API Client in current directory, registers it with Mt Wilson (on localhost or as configured), and then checks the database for the expected record to validate that it's being created.
     * @param args
     * @throws Exception 
     */
    @Override
    public void execute(String[] args) throws Exception {
        Configuration serviceConf = MSConfig.getConfiguration();
        String defaultUrl = firstNonEmpty(new String[] { serviceConf.getString("mtwilson.api.baseurl"), System.getenv("MTWILSON_API_BASEURL"), "https://"+getLocalHostAddress()+":8181/" });
        File directory = null;
        if( args.length > 0 ) { directory = new File(args[0]); } else { directory = new File("."); }
        // ignore args[1] it's the baseurl that we already know
        String url = readInputStringWithPromptAndDefault("Mt Wilson URL", defaultUrl);
        String username = null;
        String password = null;
        if( args.length > 2 ) { username = args[2]; } else { username = readInputStringWithPrompt("Username"); }
        if( args.length > 3 ) { password = args[3]; } else { password = readInputStringWithPrompt("Password"); }
        if( password != null && password.startsWith("env:") && password.length() > 4 ) {
            password = System.getenv(password.substring(4)); 
        }
        // create user
        System.out.println(String.format("Creating keystore for %s in %s", username, directory.getAbsolutePath()));        
        com.intel.mtwilson.client.TextConsole.main(new String[] { "CreateUser", directory.getAbsolutePath(), username, password });
        File keystoreFile = new File(directory.getAbsolutePath() + File.separator + Filename.encode(username) + ".jks");
        if( !keystoreFile.exists() ) {
            System.out.println("Failed to create keystore "+keystoreFile.getAbsolutePath());
            return;
        }
        // load the new key
        SimpleKeystore keystore = new SimpleKeystore(keystoreFile, password);
        RsaCredentialX509 rsaCredentialX509 = keystore.getRsaCredentialX509(username, password);
        // register user
        System.out.println(String.format("Registering %s with service at %s", username, url));
        com.intel.mtwilson.client.TextConsole.main(new String[] { "RegisterUser", keystoreFile.getAbsolutePath(), url, "Attestation,Whitelist,Security", password });
        // check database for record
//        ApiClientBO bo = new ApiClientBO();
//        ApiClientInfo apiClientRecord = bo.find(rsaCredentialX509.identity());
//        ApiClientInfo apiClientRecord = findApiClientRecord(serviceConf, rsaCredentialX509.identity());
//        if( apiClientRecord == null ) {
        // approve user
        approveApiClientRecord(serviceConf, rsaCredentialX509.identity());
        System.out.println(String.format("Approved %s [fingerprint %s]", username, Hex.encodeHexString(rsaCredentialX509.identity())));        
    }
    
    private void approveApiClientRecord(Configuration conf, byte[] fingerprint) throws SetupException {
        SetupWizard wizard = new SetupWizard(conf);
        try {
            Connection c = wizard.getMSDatabaseConnection();        
            PreparedStatement s = c.prepareStatement("UPDATE mw_api_client_x509 SET enabled=b'1',status='Approved' WHERE hex(fingerprint)=?"); // XXX TODO should use repository code for this, not hardcoded query, because table names may change between releases or deployments
            //s.setBytes(1, fingerprint);
            s.setString(1, Hex.encodeHexString(fingerprint));
            s.executeUpdate();
            s.close();
            c.close();
        }
        catch(SQLException e) {
            throw new SetupException("Cannot find API Client record: "+e.getMessage(), e);
        }        
    }

    
    // XXX see also RemoteCommand in com.intel.mtwilson.setup (not used) and com.intel.mtwilson (in api-client)
    private static String getLocalHostAddress() {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            return addr.getHostAddress();
        } catch (UnknownHostException ex) {
            return "127.0.0.1";
        }
    }
    
    private String readInputStringWithPrompt(String prompt) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        System.out.print(String.format("%s: ", prompt));
        String input = in.readLine();
//        in.close(); // don't close System.in !!
        return input;
    }

    private String readInputStringWithPromptAndDefault(String prompt, String defaultValue) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        System.out.print(String.format("%s [%s]: ", prompt, defaultValue));
        String input = in.readLine();
//        in.close(); // don't close System.in !!
        if( input == null || input.isEmpty() ) {
            input = defaultValue;
        }
        return input;
    }
    
    private String firstNonEmpty(String[] values) {
        for(String value : values) {
            if( value != null && !value.isEmpty() ) {
                return value;
            }
        }
        return null;
    }

}
