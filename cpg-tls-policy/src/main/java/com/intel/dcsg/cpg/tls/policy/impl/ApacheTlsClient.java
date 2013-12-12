/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.tls.policy.impl;

/**
 * The Apache Http Client extends the javax.net.ssl.HostnameVerifier interface
 * with additional methods so this interface accommodates applications that
 * need to use TlsPolicy with the Apache Http Client.
 * 
 * @author jbuhacoff
 */
public interface ApacheTlsClient {

    void setApacheTlsPolicy(ApacheTlsPolicy tlsPolicy);
}
