/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jackson.bouncycastle;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.IOException;
import org.bouncycastle.asn1.DERObject;

/**
 *
 * @author rksavino
 */
public abstract class ASN1EncodableMixIn {

    @JsonIgnore
    public abstract byte[] getEncoded() throws IOException;

    @JsonIgnore
    public abstract byte[] getDEREncoded();

    @JsonIgnore
    public abstract DERObject getDERObject();
}
