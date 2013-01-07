/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.X509TrustManager;

/**
                String truststore = System.getProperty("javax.net.ssl.trustStore");
                String truststorePassword = System.getProperty("javax.net.ssl.trustStorePassword");
     * XXX TODO any the implementation of this interface method needs careful review.
     * We are checking the server's SSL certificate chain against our trusted
     * certificates in the keystore.
     * 
     * Very basic implementation checks server cert then server's CA cert; http://stackoverflow.com/questions/6629473/validate-x-509-certificate-agains-concrete-ca-java
     * Example how to use a non-default keystore with the default trust manager: http://jcalcote.wordpress.com/2010/06/22/managing-a-dynamic-java-trust-store/
 *
 * @author jbuhacoff
 */
public interface TlsPolicy {
    X509TrustManager getTrustManager();
    HostnameVerifier getHostnameVerifier();
    CertificateRepository getCertificateRepository(); // in order to allow comparison of the trust stores of two instances of the same policy
}
