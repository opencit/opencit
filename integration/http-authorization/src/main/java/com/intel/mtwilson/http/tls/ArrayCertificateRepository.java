/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.http.tls;

import com.intel.mtwilson.crypto.X509Util;
import com.intel.mtwilson.datatypes.InternetAddress;
import com.intel.mtwilson.x500.DN;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class ArrayCertificateRepository implements CertificateRepository {
    private Logger log = LoggerFactory.getLogger(getClass());
    private X509Certificate[] keystore;
    
    public ArrayCertificateRepository(X509Certificate[] certificates) {
        keystore = certificates;
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
     * 
     * @param dnsHostnameOrIpAddress
     * @return the first matching certificate in the list
     */
    @Override
    public X509Certificate getCertificateForAddress(InternetAddress dnsHostnameOrIpAddress) {
        for(X509Certificate x509 : keystore) {
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
        }
        return null;
    }
    
    @Override
    public X509Certificate getCertificateForSubjectByIssuer(String subjectDN, String issuerDN) {
        for(X509Certificate x509 : keystore) {
            System.out.println("x509 subject: "+x509.getSubjectX500Principal().getName());
            System.out.println("x509 issuer: "+x509.getIssuerX500Principal().getName());
            if( subjectDN.equals(x509.getSubjectX500Principal().getName()) && issuerDN.equals(x509.getIssuerX500Principal().getName()) ) {
                return x509;
            }
        }
        return null;
    }

    @Override
    public X509Certificate[] getCertificateAuthorities() {
        ArrayList<X509Certificate> caCerts = new ArrayList<X509Certificate>(keystore.length);
        for(X509Certificate cert : keystore) {
            if( X509Util.isCA(cert) ) {
                caCerts.add(cert);
            }
        }
        return caCerts.toArray(new X509Certificate[caCerts.size()]);
    }
    
}
