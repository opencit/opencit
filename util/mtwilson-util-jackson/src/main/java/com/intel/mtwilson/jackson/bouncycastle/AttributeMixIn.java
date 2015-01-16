/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jackson.bouncycastle;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bouncycastle.asn1.ASN1Set;

/**
 *
 * @author rksavino
 */
public abstract class AttributeMixIn {

    public AttributeMixIn() { }
    
    @JsonIgnore
    public abstract ASN1Set getAttrValues();
}
