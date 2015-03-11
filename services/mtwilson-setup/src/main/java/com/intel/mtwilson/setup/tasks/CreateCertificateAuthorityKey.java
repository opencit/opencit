/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.tasks;

import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.validation.Fault;
import com.intel.dcsg.cpg.x509.X509Builder;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.My;
import com.intel.mtwilson.setup.AbstractSetupTask;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.List;
import org.apache.commons.io.IOUtils;

/**
 * @author jbuhacoff
 */
public class CreateCertificateAuthorityKey extends AbstractSetupTask {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CreateCertificateAuthorityKey.class);

    private String caDistinguishedName = "CN=mtwilson-ca,OU=mtwilson";

    public String getCaDistinguishedName() {
        return caDistinguishedName;
    }

    public void setCaDistinguishedName(String distinguishedName) {
        this.caDistinguishedName = distinguishedName;
    }

    @Override
    protected void configure() throws Exception {
        if( caDistinguishedName == null ) {
            configuration("CA distinguished name is not configured");
        }
        getConfiguration().set("mtwilson.ca.dn", caDistinguishedName);
    }

    
    @Override
    protected void execute() throws Exception {
        createCaKey();
    }
    
    private void createCaKey() throws NoSuchAlgorithmException, CertificateEncodingException, UnsupportedEncodingException, FileNotFoundException, IOException {
        // create a new key pair
        KeyPair cakey = RsaUtil.generateRsaKeyPair(2048); // throws NoSuchAlgorithmException
        X509Builder builder = X509Builder.factory();
        X509Certificate cacert = builder.selfSigned(caDistinguishedName, cakey).build();
        if( cacert == null ) {
//            log.error("Failed to create certificate"); // no need to print this, if the build failed there are guaranteed to be faults to print...
            List<Fault> faults = builder.getFaults();
            for(Fault fault : faults) {
                log.error(String.format("%s: %s", fault.getClass().getName(), fault.toString()));
            }
            return;
            
        }
        
        String privateKeyPem = RsaUtil.encodePemPrivateKey(cakey.getPrivate());
        String cacertPem = X509Util.encodePemCertificate(cacert); // throws CertificateEncodingException
        
        String combinedPrivateKeyAndCertPem = privateKeyPem + cacertPem;
        
        byte[] combinedPrivateKeyAndCertPemBytes = combinedPrivateKeyAndCertPem.getBytes("UTF-8"); // throws UnsupportedEncodingException
        byte[] cacertPemContent = cacertPem.getBytes("UTF-8");
        
        try (FileOutputStream cakeyOut = new FileOutputStream(My.configuration().getCaKeystoreFile())) { // throws FileNotFoundException, IOException
            IOUtils.write(combinedPrivateKeyAndCertPemBytes, cakeyOut); // throws IOException
        } catch (Exception ex) {
            log.error("Error creating CA key store file", ex);
        }
        
        try (FileOutputStream cacertsOut = new FileOutputStream(My.configuration().getCaCertsFile())) {
            IOUtils.write(cacertPemContent, cacertsOut);
        } catch (Exception ex) {
            log.error("Error creating CA certificate file", ex);
        }
        
    }

    @Override
    protected void validate() throws Exception {
        // make sure that we can load the ca key and certificate and that the cert has the right flags   - 
        // remember that it may have been created elsewhere and imported so the code above to create it
        // is not necessarily what happened. 
        if(My.configuration().getCaKeystoreFile().exists()) {
            byte[] combinedPrivateKeyAndCertPemBytes;
            try (FileInputStream cakeyIn = new FileInputStream(My.configuration().getCaKeystoreFile())) {
                combinedPrivateKeyAndCertPemBytes = IOUtils.toByteArray(cakeyIn);
            }
            try {
                PrivateKey cakey = RsaUtil.decodePemPrivateKey(new String(combinedPrivateKeyAndCertPemBytes));
                log.debug("Read cakey {} from {}", cakey.getAlgorithm(), My.configuration().getCaKeystoreFile().getAbsolutePath());
            }
            catch(Exception e) {
                log.debug("Cannot read private key from {}", My.configuration().getCaKeystoreFile().getAbsolutePath(), e);
                validation("Cannot read private key from: %s", My.configuration().getCaKeystoreFile().getAbsolutePath());
            }
            try {
                X509Certificate cacert = X509Util.decodePemCertificate(new String(combinedPrivateKeyAndCertPemBytes));
                log.debug("Read cacert {} from {}", cacert.getSubjectX500Principal().getName(), My.configuration().getCaKeystoreFile().getAbsolutePath());
            }
            catch(Exception e) {
                log.debug("Cannot read certificate from {}", My.configuration().getCaKeystoreFile().getAbsolutePath(), e);
                validation("Cannot read certificate from: %s", My.configuration().getCaKeystoreFile().getAbsolutePath());
            }
        }
        else {
            validation("File not found: %s", My.configuration().getCaKeystoreFile().getAbsolutePath());
        }
        if(My.configuration().getCaCertsFile().exists()) {
            byte[] cacertPemContent;
            try (FileInputStream cacertsIn = new FileInputStream(My.configuration().getCaCertsFile())) {
                cacertPemContent = IOUtils.toByteArray(cacertsIn);
            }
            try {
                List<X509Certificate> certificates = X509Util.decodePemCertificates(new String(cacertPemContent));
                log.debug("Read {} certificates from {}", certificates.size(), My.configuration().getCaCertsFile().getAbsolutePath());
            }
            catch(Exception e) {
                log.debug("Cannot read certificates from {}", My.configuration().getCaCertsFile().getAbsolutePath(), e);
                validation("Cannot read certificates from: %s", My.configuration().getCaCertsFile().getAbsolutePath());
            }
        }        
        else {
            validation("File not found: %s", My.configuration().getCaCertsFile().getAbsolutePath());
        }
        
    }

    
}
