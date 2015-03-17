/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jackson.bouncycastle;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.x509.Attribute;

/**
 *
 * @author rksavino
 */
public class BouncyCastleModule extends Module {

    @Override
    public String getModuleName() {
        return "BouncyCastleModule";
    }

    @Override
    public Version version() {
        return new Version(1, 0, 0, "com.intel.mtwilson.util", "mtwilson-util-jackson-bouncycastle", null);
    }

    @Override
    public void setupModule(SetupContext sc) {
        sc.setMixInAnnotations(ASN1Encodable.class, ASN1EncodableMixIn.class);
        sc.setMixInAnnotations(Attribute.class, AttributeMixIn.class);
    }
}
