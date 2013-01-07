/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls;

import java.security.cert.X509Certificate;
import java.util.List;

/**
 * There are two models for using a certificate repository. first,
 * the model in which a large repository is used to store certificates trusted
 * for many different servers, with the methods getCertificatesForAddress() and
 * getCertificatesForSubject(). second, the model in which an address-specific
 * or subject-specific view is created on top of such a large repository such 
 * that those certificates are selected and then they appear to be the only ones
 * in the new view, with the methods getTrustedCertificates() and getTrustedAuthorities(),
 * or even a simpler getCertificates() to return both (caller can determine authorities
 * by checking for the CA flag).  in mt wilson, all tls connections have per-server
 * trusted repositories:  api clients either have all the self-signed certs of the
 * mt wilson servers or they have a CA they trust for all of them; mt wilson has a 
 * per-host repository for every monitored host or its vcenter. mt wilson can also
 * have a global trusted root ca that is automatically added to every repository
 * instance that is created.
 * This interface assumes that implementations are using a per-server storage,
 * so that getCertificates() returns only trusted certificates for the server to
 * which the connection is being made. 
 * @author jbuhacoff
 */
public interface CertificateRepository {
    List<X509Certificate> getCertificates();
    // from previous draft:
//    X509Certificate getCertificateForAddress(InternetAddress dnsHostnameOrIpAddress);
//    List<X509Certificate> getCertificateForSubject(String subjectDN); // XXX maybe ask to pass a DN object to be clear about the input format??
//    List<X509Certificate> getCertificateAuthorities(); // return all certificates with the CA flag set
}
