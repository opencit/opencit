/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.tls.policy.impl;

import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import org.apache.http.conn.ssl.X509HostnameVerifier;

/**
 * Apache Http Client defines an X509HostnameVerifier interface that extends the
 * HostnameVerifier interface in the JDK.
 * 
 * This interface accommodates applications that
 * need to use TlsPolicy with the Apache Http Client.
 *
 * @author jbuhacoff
 */
public interface ApacheTlsPolicy extends TlsPolicy {

    X509HostnameVerifier getApacheHostnameVerifier();
}
