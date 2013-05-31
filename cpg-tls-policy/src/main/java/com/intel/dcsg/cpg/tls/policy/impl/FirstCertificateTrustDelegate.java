/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.tls.policy.impl;

import com.intel.dcsg.cpg.tls.policy.TrustDelegate;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.dcsg.cpg.x509.repository.MutableCertificateRepository;
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
 * to which new certificates can be added (with user permission).  
 * 
 * This delegate may not be initialized without a repository because that would
 * mean every certificate is a "first certificate" and therefore would be trusted,
 * which is equivalent to an insecure policy that doesn't require a delegate,
 * but most importantly will not be able to implement the purpose of this delegate
 * which is to only trust a certificate if the repository is empty and then add it
 * to the repository.
 * 
 * Even though this delegate trusts the first certificate, it will still reject
 * certificates with an invalid signature, expired certificates, and not-yet-valid
 * certificates.
 * 
 * @author jbuhacoff
 */
public class FirstCertificateTrustDelegate implements TrustDelegate {
    private Logger log = LoggerFactory.getLogger(getClass());
    private transient final MutableCertificateRepository repository;

    public FirstCertificateTrustDelegate(MutableCertificateRepository repository) {
        if( repository == null ) { throw new NullPointerException("Certificate repository must be provided"); }
        this.repository = repository;
    }

    /**
     *
     * @param certificate
     * @return
     */
    @Override
    public boolean acceptUnknownCertificate(X509Certificate certificate) {
        // basic check that the certificate format is something we can work with
        String fingerprint;
        try {
            fingerprint = Hex.encodeHexString(X509Util.sha1fingerprint(certificate));
            log.debug("acceptUnknownCertificate SHA1 Fingerprint: {}", fingerprint);
        }
        catch(Exception e) { 
            log.error("Cannot obtain SHA1 fingerprint for certificate", e); 
            return false;
        }
        
        if( log.isDebugEnabled() ) {
            // if any of these throw a NullPointerException it's ok-- we wouldn't want to accept such a certificate anyway
            log.debug("acceptUnknownCertificate Issuer: {}", certificate.getIssuerX500Principal().getName());
            log.debug("acceptUnknownCertificate Subject: {}", certificate.getSubjectX500Principal().getName());
            log.debug("acceptUnknownCertificate Type: {}", certificate.getType());
            Set<String> alternativeNames = X509Util.alternativeNames(certificate);
            for(String name : alternativeNames) {
                log.debug("acceptUnknownCertificate Alternative Name: "+name);
            }
            log.debug("acceptUnknownCertificate Not valid before: "+certificate.getNotBefore().toString());
            log.debug("acceptUnknownCertificate Not valid after: "+certificate.getNotAfter().toString());
            log.debug("acceptUnknownCertificate Serial number: "+certificate.getSerialNumber().toString());
        }        

        // ensure the certificate format is valid in the key usage area, even though we don't check right now for specific usages. XXX TODO we can add an optional check for data and key encipherment which is usually enabled for TLS certificates
        try {
            List<String> keyUsages = certificate.getExtendedKeyUsage();
            if( keyUsages != null ) {
                for(String usage : keyUsages) {
                    log.debug("acceptUnknownCertificate Key Usage: "+usage);
                }
            }
        }
        catch(CertificateParsingException e) { 
            log.error("Cannot read authorized key usages from certificate", e); 
            return false;
        }

        // reject certificates outside their validity period
        try {
            certificate.checkValidity();
        }
        catch(CertificateExpiredException e) {
            log.info("Certificate has expired", e);
            return false;
        }
        catch(CertificateNotYetValidException e) {
            log.info("Certificate not yet valid", e);            
            return false;
        }
        
        // trust first certificate means only accept a new certificate automatically if the repository is empty
        List<X509Certificate> list = repository.getCertificates();
        if( list.isEmpty() ) {
            try {
                repository.addCertificate(certificate);
                return true;
            }
            catch(Exception e) {
                // Not being able to save the certificate is  fatal error for the FirstCertificateTrustDelegate because it means we will trust all certificates equivalent to InsecureTlsPolicy, which is not what the program configured. 
                log.error("Cannot save certificate", e);
                return false; 
            }
        }
        log.info("Rejecting untrusted server certificate; repository already has {} trusted certificates", list.size());
        return false;
    }

}
