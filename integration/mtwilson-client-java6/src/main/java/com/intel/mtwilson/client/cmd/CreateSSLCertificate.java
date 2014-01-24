/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.client.cmd;

import com.intel.mtwilson.client.AbstractCommand;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
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
public class CreateSSLCertificate extends AbstractCommand {

    @Override
    public void execute(String[] args) throws Exception {
        if( args.length < 5 ) {
            throw new IllegalArgumentException("Usage: CreateSSLCertificate \"192.168.1.100\" \"ip:192.168.1.100\" /path/to/keystore.jks alias [env:password_var]");
        }
        String subject = args[0];
        String alternateName = args[1];
        File keystoreFile = new File(args[2]);
        String alias = args[3];
        String password = args[4];
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        if( password == null || password.isEmpty() ) {
            System.out.print("Password: ");
            password = in.readLine();
            System.out.print("Password again: ");
            String passwordAgain = in.readLine();
            if(password != null && passwordAgain != null) {
                if( !password.equals(passwordAgain) ) {
                    throw new IllegalArgumentException("The two passwords don't match");
                }
            }else{
                throw new IllegalArgumentException("Could not read password");
            }
        }
        else if( password.startsWith("env:") && password.length() > 4 ) {
            String varName = password.substring(4);
            password = System.getenv(varName);
        }

        SimpleKeystore keystore = new SimpleKeystore(keystoreFile, password);
        KeyPair keypair = RsaUtil.generateRsaKeyPair(RsaUtil.MINIMUM_RSA_KEY_SIZE);
        X509Certificate certificate = RsaUtil.generateX509Certificate(subject, alternateName, keypair, RsaUtil.DEFAULT_RSA_KEY_EXPIRES_DAYS);
        keystore.addKeyPairX509(keypair.getPrivate(), certificate, alias, password);
        keystore.save();
    }

}
