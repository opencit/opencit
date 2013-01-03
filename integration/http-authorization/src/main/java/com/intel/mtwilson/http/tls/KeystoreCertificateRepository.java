/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.http.tls;

import com.intel.mtwilson.crypto.SimpleKeystore;
import com.intel.mtwilson.crypto.X509Util;
import com.intel.mtwilson.datatypes.InternetAddress;
import com.intel.mtwilson.x500.DN;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class KeystoreCertificateRepository implements MutableCertificateRepository {
    private Logger log = LoggerFactory.getLogger(getClass());
    private SimpleKeystore keystore;
    
    public KeystoreCertificateRepository(SimpleKeystore simpleKeystore) {
        keystore = simpleKeystore;
    }
    
    /**
     * XXX TODO this is a draft; maybe it should return a list , since it's possible
     * for more than one certificate to match...
     * XXX TODO maybe create another method getCurrentCertificateForAddress which refines
     * the search by returning only certificates that are valid NOW (the keystore may
     * have some that are not yet valid because they have been deployed in preparation
     * for an upcoming expiration)
     * 
     * The following certificate attributes are checked in order:
     * Common name in the subject
     * Alternative name
     * Alias in the keystore
     * 
     * @param dnsHostnameOrIpAddress
     * @return the first matching certificate in the keystore; if there is more than one it is not guaranteed to always return the same one because this depends on the keystore implementation
     */
    @Override
    public X509Certificate getCertificateForAddress(InternetAddress dnsHostnameOrIpAddress) {
        try {
            String[] sslCertAliases = keystore.listTrustedSslCertificates();
            for(String alias : sslCertAliases) {
                try {
                    X509Certificate x509 = keystore.getX509Certificate(alias);
                    System.out.println("x509 subject: "+x509.getSubjectX500Principal().getName());
                    DN dn = new DN(x509.getSubjectX500Principal().getName());    
                    if( dn.getCommonName() != null && dn.getCommonName().equals(dnsHostnameOrIpAddress.toString()) ) {
                        return x509;
                    }
                    Set<String> alternativeNames = X509Util.alternativeNames(x509);
                    for(String alternativeName : alternativeNames) {
                        System.out.println("x509 alternative name: "+alternativeName);
                        if( alternativeName.equals(dnsHostnameOrIpAddress.toString()) ) {
                            return x509;
                        }
                    }
                    if( alias.equals(dnsHostnameOrIpAddress.toString()+" (ssl)") ) { // XXX TODO need to use the new Tag interface for the simple keystore
                        return x509;
                    }
                }
                catch(Exception e) {
                    log.error("Cannot load certificate alias '"+alias+"' from keystore", e);                    
                }
            }
            return null;
        }
        catch(KeyStoreException e) {
            log.error("Cannot find certificate in keystore", e);
            return null;
        }
    }
    
    @Override
    public void setCertificateForAddress(InternetAddress dnsHostnameOrIpAddress, X509Certificate certificate) throws KeyManagementException {
        keystore.addTrustedSslCertificate(certificate, dnsHostnameOrIpAddress.toString());
    }

    @Override
    public X509Certificate getCertificateForSubjectByIssuer(String subjectDN, String issuerDN) {
        try {
            String[] sslCertAliases = keystore.listTrustedSslCertificates();
            for(String alias : sslCertAliases) {
                try {
                    X509Certificate x509 = keystore.getX509Certificate(alias);
                    System.out.println("x509 subject: "+x509.getSubjectX500Principal().getName());
                    System.out.println("x509 issuer: "+x509.getIssuerX500Principal().getName());
                    if( subjectDN.equals(x509.getSubjectX500Principal().getName()) && issuerDN.equals(x509.getIssuerX500Principal().getName()) ) {
                        return x509;
                    }
                }
                catch(Exception e) {
                    log.error("Cannot load certificate alias '"+alias+"' from keystore", e);                    
                }
            }
            return null;
        }
        catch(KeyStoreException e) {
            log.error("Cannot find certificate in keystore", e);
            return null;
        }
    }

    @Override
    public X509Certificate[] getCertificateAuthorities() {
        try {
            String[] caAliases = keystore.listTrustedCaCertificates();
            X509Certificate[] caCerts = new X509Certificate[caAliases.length];
            for(int i=0; i<caAliases.length; i++) {
                caCerts[i] = keystore.getX509Certificate(caAliases[i]);
            }
            return caCerts;
        }
        catch(Exception e) {
            log.error("Cannot load certificate authorities from repository", e);
            return new X509Certificate[0];
        }
    }
    
}
