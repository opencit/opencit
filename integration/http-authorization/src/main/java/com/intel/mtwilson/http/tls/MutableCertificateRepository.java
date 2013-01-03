/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.http.tls;

import com.intel.mtwilson.datatypes.InternetAddress;
import java.security.KeyManagementException;
import java.security.cert.X509Certificate;

/**
 *
 * @author jbuhacoff
 */
public interface MutableCertificateRepository extends CertificateRepository {
    void setCertificateForAddress(InternetAddress dnsHostnameOrIpAddress, X509Certificate certificate) throws KeyManagementException;
}
