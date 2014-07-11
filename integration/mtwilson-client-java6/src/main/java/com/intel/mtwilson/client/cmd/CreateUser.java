/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.client.cmd;

import com.intel.mtwilson.client.AbstractCommand;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.io.Filename;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.security.KeyPair;
import java.security.cert.X509Certificate;

/**
 *     HostTrustResponse getHostTrust(Hostname hostname) throws IOException, ApiException, SignatureException;
 * 
 * @author jbuhacoff
 */
public class CreateUser extends AbstractCommand {

    @Override
    public void execute(String[] args) throws Exception {
            // args[1] should be path to folder
            File directory = new File(args[0]);
            String username = null, password = null;
            // args[2] is optional username (if not provided we will prompt)
            if( args.length > 1 ) { username = args[1]; }
            // args[3] is optional password plaintext (not recommended) or environment variable name (recommended) (if not provided we will prompt)
            if( args.length > 2 ) { password = args[2]; }
            
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            if( username == null || username.isEmpty() ) {
                System.out.print("Username: ");
                username = in.readLine();
            }
            if( password == null || password.isEmpty() ) {
                System.out.print("Password: ");
                password = in.readLine();
                System.out.print("Password again: ");
                String passwordAgain = in.readLine();
                if(password != null && passwordAgain != null) {
                    if( !password.equals(passwordAgain) ) {
                        System.err.println("The two passwords don't match");
                        System.exit(1);
                    }
                }else{
                     System.err.println("Unable to read password.  Please run command again");
                     System.exit(1);
                }
                    
            }
            else if( password.startsWith("env:") && password.length() > 4 ) {
                String varName = password.substring(4);
                password = System.getenv(varName);
            }

            if( password == null || password.isEmpty() || password.length() < 6 ) {
                System.err.println("The password must be at least six characters");
                System.exit(1);
            }
            //CN=username, OU=IASI, O=Intel, L=Folsom, ST=CA, C=US
            /*
            System.out.print("Common Name (optional): "); 
            String cn = in.readLine();
            System.out.print("Organizational Unit (optional): ");
            String ou = in.readLine();
            System.out.print("Organization (optional): ");
            String o = in.readLine();
            System.out.print("City/locality (optional): ");
            String l = in.readLine();
            System.out.print("State (optional): ");
            String st = in.readLine();
            System.out.print("Country (optional): ");
            String c = in.readLine();
            if( cn.isEmpty() ) { cn = username; }
            String[] parts = new String[] { 
                String.format("CN=%s",cn), 
                ou.isEmpty() ? "" : String.format("OU=%s", ou),
                o.isEmpty() ? "" : String.format("O=%s", o), 
                l.isEmpty() ? "" : String.format(""), st, c };
            String subject = StringUtils.join(parts);
            */
            String subject = username; //String.format("CN=%s", username);
            
            File keystoreFile = new File(directory.getAbsoluteFile() + File.separator + Filename.encode(username) + ".jks");
            SimpleKeystore keystore = new SimpleKeystore(keystoreFile, password);
            KeyPair keypair = RsaUtil.generateRsaKeyPair(RsaUtil.MINIMUM_RSA_KEY_SIZE);
            X509Certificate certificate = RsaUtil.generateX509Certificate(subject, keypair, RsaUtil.DEFAULT_RSA_KEY_EXPIRES_DAYS);
            keystore.addKeyPairX509(keypair.getPrivate(), certificate, username, password);
            keystore.save();
            System.out.println("Created keystore: "+keystoreFile.getName());
    }

}
