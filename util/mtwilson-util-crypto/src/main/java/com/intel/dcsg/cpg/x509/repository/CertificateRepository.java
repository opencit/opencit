/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.x509.repository;

import java.security.cert.X509Certificate;
import java.util.List;

/**
 * A CertificateRepository represents a source of trusted certificates for
 * the verification being attempted. This means if there is a global trusted root
 * CA list and a per-server list they should be combined by the implementation
 * to provide one coherent view to the TlsPolicy.
 * 
 * This interface returns the entire list of certificates to the caller, so
 * the list must be "static". 
 * 
 * XXX TODO maybe change this interface to return an Iterator<X509Certificate> so that
 * it's easier to return a dynamic object that fetches & buffers from a database, for
 * example, so that if there are a lot of certificates they do not all need to be in 
 * memory at the same time. 
 * 
 * See also SearchableCertificateRepository for an interface that allows more control
 * over which certificates are returned, like searching by issuer, subject, validity dates,
 * fingerprints, etc. 
 *
 * @since 0.1
 * @author jbuhacoff
 */
public interface CertificateRepository {

    /**
     * 
     * @return an immutable list of certificates (possibly empty);  must not return null
     */
    List<X509Certificate> getCertificates();
}
