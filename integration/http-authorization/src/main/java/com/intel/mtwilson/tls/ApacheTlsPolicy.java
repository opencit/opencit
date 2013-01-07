/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls;

import org.apache.http.conn.ssl.X509HostnameVerifier;

/**
 * Apache Http Client defines an X509HostnameVerifier interface that extends
 * the HostnameVerifier interface in the JDK.
 * 
 * @author jbuhacoff
 */
public interface ApacheTlsPolicy extends TlsPolicy {
    X509HostnameVerifier getApacheHostnameVerifier();
}
