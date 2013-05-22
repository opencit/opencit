/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.x509.repository;

import java.security.KeyManagementException;
import java.security.cert.X509Certificate;

/**
 * 
 * @since 0.1
 * @author jbuhacoff
 */
public interface MutableCertificateRepository extends CertificateRepository {

    /**
     * The contract of addCertificate is that once added, the certificate will be made available the next
     * time someone calls "getCertificates" (in CertificateRepository interface). 
     * The implementation is responsible for saving to disk, keeping track in memory, etc. either immediately
     * or buffered but must behave as described above.
     * 
     * @param certificate
     * @throws KeyManagementException 
     */
    void addCertificate(X509Certificate certificate) throws KeyManagementException;
}    