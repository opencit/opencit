/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.tasks;

import com.intel.mtwilson.setup.SetupTask;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.validation.Fault;
import com.intel.dcsg.cpg.validation.ObjectModel;
import com.intel.dcsg.cpg.x509.X509Builder;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.setup.AbstractSetupTask;
import java.io.UnsupportedEncodingException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.List;

/**
 * TODO instead of ObjectModel we might want to extend a similar base class
 * written specifically for setup tasks... 
 * @author jbuhacoff
 */
public class CreateCertificateAuthorityKey extends AbstractSetupTask {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CreateCertificateAuthorityKey.class);

    private String distinguishedName = "CN=mtwilson-ca,OU=mtwilson";

    public String getDistinguishedName() {
        return distinguishedName;
    }

    public void setDistinguishedName(String distinguishedName) {
        this.distinguishedName = distinguishedName;
    }

    @Override
    protected void execute() throws Exception {
        createCaKey();
    }
    
    private void createCaKey() throws NoSuchAlgorithmException, CertificateEncodingException, UnsupportedEncodingException {
        // create a new key pair
        KeyPair cakey = RsaUtil.generateRsaKeyPair(2048); // throws NoSuchAlgorithmException
        X509Builder builder = X509Builder.factory();
        X509Certificate cacert = builder.selfSigned(distinguishedName, cakey).build();
        if( cacert == null ) {
//            log.error("Failed to create certificate"); // no need to print this, if the build failed there are guaranteed to be faults to print...
            List<Fault> faults = builder.getFaults();
            for(Fault fault : faults) {
                log.error(String.format("%s%s", fault.toString(), fault.getCause() == null ? "" : ": "+fault.getCause().getMessage()));
            }
            return;
            
        }
        
        String privateKeyPem = RsaUtil.encodePemPrivateKey(cakey.getPrivate());
        String cacertPem = X509Util.encodePemCertificate(cacert); // throws CertificateEncodingException
        
        String combinedPrivateKeyAndCertPem = privateKeyPem + cacertPem;
        
        byte[] combinedPrivateKeyAndCertPemBytes = combinedPrivateKeyAndCertPem.getBytes("UTF-8"); // throws UnsupportedEncodingException
        byte[] cacertPemContent = cacertPem.getBytes("UTF-8");
        
    }

    @Override
    protected void validate() throws Exception {
        // make sure that we can load the ca key and certificate and that the cert has the right flags   - 
        // remember that it may have been created elsewhere and imported so the code above to create it
        // is not necessarily what happened. 
    }

    @Override
    protected void configure() throws Exception {
        if( distinguishedName == null ) {
            configuration("Distinguished name is not configured");
        }
    }

    
}
