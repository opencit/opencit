/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.tls.policy;

import com.intel.dcsg.cpg.x509.repository.CertificateRepository;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.X509TrustManager;

/**
 * String truststore = System.getProperty("javax.net.ssl.trustStore"); String
 * truststorePassword = System.getProperty("javax.net.ssl.trustStorePassword");
 *
 * Very basic implementation checks server cert then server's CA cert;
 * http://stackoverflow.com/questions/6629473/validate-x-509-certificate-agains-concrete-ca-java
 * Example how to use a non-default keystore with the default trust manager:
 * http://jcalcote.wordpress.com/2010/06/22/managing-a-dynamic-java-trust-store/
 *
 * @author jbuhacoff
 */
public interface TlsPolicy {
    
    boolean providesConfidentiality();
    
    boolean providesAuthentication();
    
    boolean providesIntegrity();
    

    X509TrustManager getTrustManager();

    HostnameVerifier getHostnameVerifier();

    /**
     * The trusted certificates repository, whether it is a global trusted CAs
     * or a local trusted known certificates, is an important component of the
     * TlsPolicy. If an attacker is able to add his own root CA to the certificate
     * repository he can then mount a man-in-the-middle attack on otherwise-secure
     * SSL clients.
     * 
     * @return 
     */
    CertificateRepository getCertificateRepository(); // in order to allow comparison of the trust stores of two instances of the same policy
}
