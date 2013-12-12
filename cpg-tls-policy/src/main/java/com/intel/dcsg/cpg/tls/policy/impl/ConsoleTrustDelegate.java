/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.tls.policy.impl;

import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.dcsg.cpg.tls.policy.TrustDelegate;
import com.intel.dcsg.cpg.x509.repository.MutableCertificateRepository;
import java.io.Console;
import java.io.IOException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Set;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * You may initialize this trust delegate with a mutable certificate repository
 * to which new certificates can be added (with user permission), or without
 * a repository so that user permission is only a just-this-once exception and
 * not permanent. 
 * 
 * TODO also implement a GUI delegate (dialog box like browser) or maybe a PKI
 * where it checks a known and trusted key server to possible have new certificates
 * that it didnt already have locally. 
 * 
 * @author jbuhacoff
 */
public class ConsoleTrustDelegate implements TrustDelegate {
    // TODO:  add the Console object here and the yes/no input prompt methods from setup-console so i can prompt user
    private static final Console console = System.console();
    private Logger log = LoggerFactory.getLogger(getClass());
    private transient final MutableCertificateRepository repository;

    public ConsoleTrustDelegate(MutableCertificateRepository repository) {
        this.repository = repository;
    }

    /**
     *
     * @param certificate
     * @return
     */
    @Override
    public boolean acceptUnknownCertificate(X509Certificate certificate) {
        System.out.println("The server certificate could not be validated");
        System.out.println("Issuer: "+certificate.getIssuerX500Principal().getName());
        System.out.println("Subject: "+certificate.getSubjectX500Principal().getName());
        System.out.println("Type:" +certificate.getType());
        Set<String> alternativeNames = X509Util.alternativeNames(certificate);
        for(String name : alternativeNames) {
            System.out.println("Alternative Name: "+name);
        }
        System.out.println("Not valid before: "+certificate.getNotBefore().toString());
        System.out.println("Not valid after: "+certificate.getNotAfter().toString());
        System.out.println("Serial number: "+certificate.getSerialNumber().toString());
        
        try {
            System.out.println("SHA1 Fingerprint: "+Hex.encodeHexString(X509Util.sha1fingerprint(certificate)));
        }
        catch(Exception e) { 
            error("Cannot obtain SHA1 fingerprint for certificate", e); 
        }
        
        try {
            List<String> keyUsages = certificate.getExtendedKeyUsage();
            if( keyUsages != null ) {
                for(String usage : keyUsages) {
                    System.out.println("Key Usage: "+usage);
                }
            }
        }
        catch(CertificateParsingException e) { 
            error("Cannot read authorized key usages from certificate", e); 
            return false;
        }

        try {
            certificate.checkValidity();
        }
        catch(CertificateExpiredException e) {
            error("Certificate has expired", e);
            return false;
        }
        catch(CertificateNotYetValidException e) {
            error("Certificate not yet valid", e);            
            return false;
        }
        
        if( userAgreesTo("Accept this certificate?") ) {
            if( repository != null ) {
                if( userAgreesTo("Save this certificate permanently?") ) {
                    try {
                        repository.addCertificate(certificate);
                    }
                    catch(Exception e) {
                        error("Cannot save certificate", e);
                        // Note: do not return here, because this is not a fatal error... user still chose to accept certificate
                    }
                }
            }            
            return true; // user accepted certificate
        }
        return false; // user did not accept certificate
    }

    private boolean userAgreesTo(String prompt) {
        if(console==null) {
            System.out.println(prompt+" [Y/N] ");
        }
        else {
            console.printf("%s [Y/N] ", prompt);
        }
        while(true) {
            try {
                if( console == null ) {
                    int c = System.in.read();
                    if( c == 'Y' || c == 'y' ) {
                        return true;
                    }
                    if( c == 'N' || c == 'n' ) {
                        return false;
                    }
                }
                else {
                    String line = console.readLine();
                    if( line != null && line.length() > 0 ) {
                        char c = line.charAt(0);
                        if( c == 'Y' || c == 'y' ) {
                            return true;
                        }
                        if( c == 'N' || c == 'n' ) {
                            return false;
                        }                        
                    }
                }
            }
            catch(IOException e) {
                error("Cannot read keyboard", e);
            }
        }
    }
    
    private void error(String message, Throwable e) {
        log.error(message, e);
        System.out.println("*** "+message+" ***");
        System.err.println(e.toString());        
    }
}
