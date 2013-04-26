/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.tls.policy;

import java.security.cert.X509Certificate;

/**
 * Implementations of TrustDelegate provide callback functions
 * that a TlsPolicy can invoke in order to ask the user a question,
 * such as if he or she
 * wants to accept a server certificate that is not in the keystore.
 * They can choose to save confirmed certificates into a mutable
 * certificate repository or to not save them and implement a 
 * "one-time" exception by returning true without saving.  They can
 * choose to save the certificates into any repository available to 
 * them, including the one that was passed to the TlsPolicyBuilder
 * in order for the new certificate to automatically be trusted next time.
 * 
 * @author jbuhacoff
 */
public interface TrustDelegate {
//    boolean acceptUnknownCertificateAuthority(X509Certificate certificate);
    boolean acceptUnknownCertificate(X509Certificate certificate);
}
