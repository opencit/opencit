/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.security.http.apache;

import java.io.IOException;
import java.security.SignatureException;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;

/**
 * @since 0.5.2
 * @author jbuhacoff
 */
public interface ApacheHttpAuthorization {
    void addAuthorization(HttpRequest request) throws SignatureException;
    void addAuthorization(HttpEntityEnclosingRequest request) throws SignatureException, IOException;
}
