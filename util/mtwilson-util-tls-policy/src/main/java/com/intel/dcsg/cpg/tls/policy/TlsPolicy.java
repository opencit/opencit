/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.tls.policy;

import javax.net.ssl.X509ExtendedTrustManager;
import org.apache.http.conn.ssl.X509HostnameVerifier;

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
    

    X509ExtendedTrustManager getTrustManager();

    /**
     * NOTE: when using HttpsURLConnection, the hostname verifier is only 
     * called if the server certificate does not match the address used to
     * connect. 
     * When using Apache's StrictHostnameVerifier, the failure
     * message looks like "HTTPS hostname wrong: should be <192.168.1.100>"
     * When using our DenyAllHostnameVerifier, the failure message
     * looks like "DENY-ALL"
     * 
     * 
     * @return 
     */
    X509HostnameVerifier getHostnameVerifier();
    
    /**
     * Due to the architecture of the Java SSLContext and the workaround for
     * connection pooling with TlsPolicyManager, the protocol selector must
     * be known prior to using the TlsPolicyManager. The TlsConnection 
     * class provides a convenience function for creating the SSLContext 
     * using the protocol selector.
     * be known 
     * @return 
     */
    ProtocolSelector getProtocolSelector();
}
