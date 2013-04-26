/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.x509;

import java.nio.ByteBuffer;
import java.security.cert.X509Certificate;
import java.util.Comparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class supports sorting of certificates in the hashCode() method of a
 * CertificateRepository. Because there isn't really a meaning to having a
 * sorted set of certificates, the only aim of this class is to always produce
 * the same order given the same certificates. If the certificate list changes
 * then the comparison of the "new order" to the "old order" is meaningless, and
 * the only thing that can be said is they are necessarily different because the
 * list contents are different.
 *
 * @author jbuhacoff
 */
public class X509CertificateComparator implements Comparator<X509Certificate> {
    private Logger log = LoggerFactory.getLogger(getClass());

    /**
     * This method compares serial number, then subject name, then the entire
     * encoded certificate.
     *
     * @param o1
     * @param o2
     * @return
     */
    @Override
    public int compare(X509Certificate o1, X509Certificate o2) {
        int serialNumber = o1.getSerialNumber().compareTo(o2.getSerialNumber());
        if (serialNumber != 0) {
            return serialNumber;
        }
        int subjectName = o1.getSubjectX500Principal().getName().compareTo(o2.getSubjectX500Principal().getName());
        if (subjectName != 0) {
            return subjectName;
        }
        try {
            return ByteBuffer.wrap(o1.getEncoded()).compareTo(ByteBuffer.wrap(o2.getEncoded())); // CertificateEncodingException
        } catch (Exception e) {
            log.warn("Cannot compare certificates", e);
            return 0;
        }
    }
}
