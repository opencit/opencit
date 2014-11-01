/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jaxrs2;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.security.cert.X509Certificate;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
//import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 *
 * @author jbuhacoff
 */
@JsonSerialize(include=JsonSerialize.Inclusion.NON_EMPTY) // jackson 1.9
@JsonInclude(JsonInclude.Include.NON_EMPTY) // jackson 2.0
public abstract class CertificateDocument extends Document {

    public abstract X509Certificate getX509Certificate();

    public abstract void setX509Certificate(X509Certificate certificate);

}
