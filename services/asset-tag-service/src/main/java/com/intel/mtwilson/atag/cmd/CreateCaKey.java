/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.cmd;

import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.io.ByteArray;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.validation.Fault;
import com.intel.dcsg.cpg.x509.X509Builder;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.atag.AtagCommand;
import com.intel.mtwilson.atag.Derby;
import com.intel.mtwilson.atag.model.File;
import java.io.FileNotFoundException;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Properties;
import org.apache.commons.configuration.MapConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This command exports a file from the database to the filesystem
 * @author jbuhacoff
 */
public class CreateCaKey extends AtagCommand {
    private static Logger log = LoggerFactory.getLogger(CreateCaKey.class);
    public static final String PRIVATEKEY_FILE = "cakey";
    public static final String CACERTS_FILE = "cacerts";
    
    @Override
    public void execute(String[] args) throws Exception {
        // file name, and either outfile or stdout
        if( args.length < 1 ) { throw new IllegalArgumentException("Usage: create-ca-key \"CN=mykey,O=myorg,C=US\""); } // on Windows especially do not use single quotes around the argument because it will be a part of it
        String dn = args[0];
        
        // create a new key pair
        KeyPair cakey = RsaUtil.generateRsaKeyPair(2048);
        X509Builder builder = X509Builder.factory();
        X509Certificate cacert = builder.selfSigned(dn, cakey).build();
        if( cacert == null ) {
//            log.error("Failed to create certificate"); // no need to print this, if the build failed there are guaranteed to be faults to print...
            List<Fault> faults = builder.getFaults();
            for(Fault fault : faults) {
                log.error(String.format("%s%s", fault.toString(), fault.getCause() == null ? "" : ": "+fault.getCause().getMessage()));
            }
            return;
            
        }
        
        String privateKeyPem = RsaUtil.encodePemPrivateKey(cakey.getPrivate());
        String cacertPem = X509Util.encodePemCertificate(cacert);
        
        String combinedPrivateKeyAndCertPem = privateKeyPem + cacertPem;
        
        byte[] combinedPrivateKeyAndCertPemBytes = combinedPrivateKeyAndCertPem.getBytes("UTF-8");
        byte[] cacertPemContent = cacertPem.getBytes("UTF-8");

        Derby.startDatabase();        
        
        // for now... there can only be ONE CA private key in the database  (but we support storing multiple certs)
        File cakeyFile = Derby.fileDao().findByName(PRIVATEKEY_FILE);
        if( cakeyFile == null ) {
            // create new private key file
            Derby.fileDao().insert(new UUID(), PRIVATEKEY_FILE, "text/plain", combinedPrivateKeyAndCertPemBytes);
        }
        else {
            // replace existing private key... 
            // XXX IMPORTANT TODO   before we replace it we need to revoke it so that nobody else with a copy can use it to sign any more certs
            Derby.fileDao().update(cakeyFile.getId(), PRIVATEKEY_FILE, "text/plain", combinedPrivateKeyAndCertPemBytes);
        }
        
        // add the ca cert to the list of approved certs
        File cacertsFile = Derby.fileDao().findByName(CACERTS_FILE);
        if( cacertsFile == null ) {
            // create new cacerts file
            Derby.fileDao().insert(new UUID(), CACERTS_FILE, "text/plain", cacertPemContent);
        }
        else {
            // append new cacert to existing file
            Derby.fileDao().update(cacertsFile.getId(), CACERTS_FILE, "text/plain", ByteArray.concat(cacertsFile.getContent(), cacertPemContent));
        }
        
        Derby.stopDatabase();
    }
    
    
    public static void main(String args[]) throws Exception {
        CreateCaKey cmd = new CreateCaKey();
        cmd.setOptions(new MapConfiguration(new Properties()));
        cmd.execute(new String[] { "CN=Asset CA,OU=Datacenter,C=US" });
        
    }    
    
}
