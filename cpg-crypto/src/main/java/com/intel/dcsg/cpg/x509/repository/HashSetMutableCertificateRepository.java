/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.x509.repository;

import java.security.KeyManagementException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A writable in-memory repository of X509Certificates. You can access the mutable hash set directly via getKeystore()
 * @author jbuhacoff
 */
public class HashSetMutableCertificateRepository implements MutableCertificateRepository {

    private Logger log = LoggerFactory.getLogger(getClass());
    private HashSet<X509Certificate> keystore = new HashSet<X509Certificate>();

    public HashSet<X509Certificate> getKeystore() {
        return keystore;
    }

    /**
     *
     * @return a list of certificates in the repository; modifying the list does not modify the repository
     */
    @Override
    public List<X509Certificate> getCertificates() {
        ArrayList<X509Certificate> allCerts = new ArrayList<X509Certificate>();
        allCerts.addAll(keystore);
        return allCerts;
    }

    /**
     * Based on the underlying HashSet hashCode
     * @return
     */
    @Override
    public int hashCode() {
        return keystore.hashCode()+1;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (other == this) {
            return true;
        }
        if (other.getClass() != this.getClass()) {
            return false;
        }
        HashSetMutableCertificateRepository rhs = (HashSetMutableCertificateRepository) other;
        return this.keystore.equals(rhs.keystore);
    }

    /**
     *
     * @param certificate
     * @throws KeyManagementException
     */
    @Override
    public void addCertificate(X509Certificate certificate) throws KeyManagementException {
        log.debug("Adding certificate to repository: {}", certificate.getSubjectX500Principal().getName());
        keystore.add(certificate);
    }
}
