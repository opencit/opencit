/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tag.setup.cmd;

import com.intel.mtwilson.tag.setup.TagCommand;
import com.intel.mtwilson.tag.model.File;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.io.ByteArray;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.validation.Fault;
import com.intel.dcsg.cpg.x509.X509Builder;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.My;
import com.intel.mtwilson.tag.dao.TagJdbi;
import java.io.FileOutputStream;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This command exports a file from the database to the filesystem
 * 
 * Usage: create-ca-key "CN=mykey,O=myorg,C=US"
 * 
 * Use double-quotes; on Windows especially do not use single quotes around the argument because it will be a part of it
 * 
 * If a distinguished name is not provided, a default name will be used
 * 
 * @author jbuhacoff
 */
public class TagCreateCaKey extends TagCommand {
    private static Logger log = LoggerFactory.getLogger(TagCreateCaKey.class);
    public static final String PRIVATEKEY_FILE = "cakey";
    public static final String CACERTS_FILE = "cacerts";
    
    @Override
    public void execute(String[] args) throws Exception {
        // file name, and either outfile or stdout
        String dn;
        if( args.length > 0 ) { 
            dn = args[0];
        } 
        else {
            dn = "CN=asset-tag-service,OU=mtwilson"; 
        }
        
        // create a new key pair
        KeyPair cakey = RsaUtil.generateRsaKeyPair(2048);
        X509Builder builder = X509Builder.factory();
        X509Certificate cacert = builder.selfSigned(dn, cakey).expires(3650, TimeUnit.DAYS).build();
        if( cacert == null ) {
//            log.error("Failed to create certificate"); // no need to print this, if the build failed there are guaranteed to be faults to print...
            List<Fault> faults = builder.getFaults();
            for(Fault fault : faults) {
                log.error(String.format("%s: %s", fault.getClass().getName(), fault.toString()));
//                log.error(String.format("%s%s", fault.toString(), fault.getCause() == null ? "" : ": "+fault.getCause().getMessage()));
            }
            return;
            
        }
        
        String privateKeyPem = RsaUtil.encodePemPrivateKey(cakey.getPrivate());
        String cacertPem = X509Util.encodePemCertificate(cacert);
        
        String combinedPrivateKeyAndCertPem = privateKeyPem + cacertPem;
        
        byte[] combinedPrivateKeyAndCertPemBytes = combinedPrivateKeyAndCertPem.getBytes("UTF-8");
        byte[] cacertPemContent = cacertPem.getBytes("UTF-8");
        
        // for now... there can only be ONE CA private key in the database  (but we support storing multiple certs)
        File cakeyFile = TagJdbi.fileDao().findByName(PRIVATEKEY_FILE);
        if( cakeyFile == null ) {
            // create new private key file
            TagJdbi.fileDao().insert(new UUID(), PRIVATEKEY_FILE, "text/plain", combinedPrivateKeyAndCertPemBytes);
        }
        else {
            // replace existing private key... 
            TagJdbi.fileDao().update(cakeyFile.getId(), PRIVATEKEY_FILE, "text/plain", combinedPrivateKeyAndCertPemBytes);
        }
        
        // add the ca cert to the list of approved certs
        File cacertsFile = TagJdbi.fileDao().findByName(CACERTS_FILE);
        if( cacertsFile == null ) {
            // create new cacerts file
            TagJdbi.fileDao().insert(new UUID(), CACERTS_FILE, "text/plain", cacertPemContent);
        }
        else {
            // append new cacert to existing file in database
            byte[] content = ByteArray.concat(cacertsFile.getContent(), cacertPemContent);
            TagJdbi.fileDao().update(cacertsFile.getId(), CACERTS_FILE, "text/plain", content);
            // and write to disk also for easy sharing with mtwilson: tag-cacerts.pem
            try(FileOutputStream out = new FileOutputStream(My.configuration().getAssetTagCaCertificateFile())) {
                IOUtils.write(content, out);
            }
        }
        
    }
    
    
    public static void main(String args[]) throws Exception {
        TagCreateCaKey cmd = new TagCreateCaKey();
        cmd.setOptions(new MapConfiguration(new Properties()));
        cmd.execute(new String[] { "CN=Asset CA,OU=Datacenter,C=US" });
        
    }    
    
}
