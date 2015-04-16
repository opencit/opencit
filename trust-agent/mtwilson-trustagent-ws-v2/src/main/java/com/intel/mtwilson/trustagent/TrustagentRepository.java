/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent;

import com.intel.dcsg.cpg.x509.X509Util;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author jbuhacoff
 */
public class TrustagentRepository {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrustagentRepository.class);
    private TrustagentConfiguration configuration;
    public TrustagentRepository(TrustagentConfiguration configuration) { 
        this.configuration = configuration;
    }
    
    public X509Certificate getExistingAikCertificate() throws IOException, CertificateException {
        File aikCertificateFile = configuration.getAikCertificateFile();
        if( !aikCertificateFile.exists() ) {
            throw new FileNotFoundException(aikCertificateFile.getAbsolutePath());
        }
        String aikPem = FileUtils.readFileToString(aikCertificateFile);
        X509Certificate aikCertificate = X509Util.decodePemCertificate(aikPem);
        return aikCertificate;
    }
    
    public X509Certificate getAikCertificate() throws IOException, CertificateException {
        try {
            X509Certificate aik = getExistingAikCertificate();
            return aik;
        }
        catch(IOException | CertificateException e) {
            log.debug("Cannot load AIK certificate", e);
            return null;
        }
    }
    
    public X509Certificate getBindingKeyCertificate() throws IOException, CertificateException {
        try {
            File bkCertificateFile = configuration.getBindingKeyX509CertificateFile();
            if (!bkCertificateFile.exists()) {
                throw new FileNotFoundException(bkCertificateFile.getAbsolutePath());
            }
            String bkPem = FileUtils.readFileToString(bkCertificateFile);
            X509Certificate bkCertificate = X509Util.decodePemCertificate(bkPem);
            return bkCertificate;
        } catch (IOException | CertificateException e) {
            log.debug("Cannot load Binding Key certificate", e);
            return null;
        }
    }
    
}
