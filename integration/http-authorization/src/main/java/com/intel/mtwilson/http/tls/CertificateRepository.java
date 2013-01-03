/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.http.tls;

import com.intel.mtwilson.datatypes.InternetAddress;
import java.security.cert.X509Certificate;

/**
 *
 * @author jbuhacoff
 */
public interface CertificateRepository {
    X509Certificate getCertificateForAddress(InternetAddress dnsHostnameOrIpAddress);
    X509Certificate getCertificateForSubjectByIssuer(String subjectDN, String issuerDN);
    // XXX TODO:  getCertificateForSubjectByIssuer(Subject X500 Name, Issuer X500 Name)
    X509Certificate[] getCertificateAuthorities(); // return all certificates with the CA flag set
}
