/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.http.tls;

import javax.net.ssl.X509TrustManager;
import javax.net.ssl.HostnameVerifier;

/**
 *
 * @author jbuhacoff
 */
public interface TlsPolicy {
    X509TrustManager getTrustManager();
    HostnameVerifier getHostnameVerifier();
}
