/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls;

import java.security.KeyManagementException;
import java.security.cert.X509Certificate;

/**
 *
 * @author jbuhacoff
 */
public interface MutableCertificateRepository extends CertificateRepository {
    void addCertificate(X509Certificate certificate) throws KeyManagementException;
    // from previous draft:
//    void setCertificateForAddress(InternetAddress dnsHostnameOrIpAddress, X509Certificate certificate) throws KeyManagementException;
}
