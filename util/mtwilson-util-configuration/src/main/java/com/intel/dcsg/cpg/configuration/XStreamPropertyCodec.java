/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.configuration;

import com.intel.mtwilson.codec.Base64Codec;
import com.intel.mtwilson.codec.XStreamCodec;

/**
 * TODO optional support for cpg-extensions by creating a factory class or
 * filter that can find this codec for properties having the "xstream" prefix
 * 
 * @author jbuhacoff
 */
public class XStreamPropertyCodec extends ObjectPropertyCodec {
    public XStreamPropertyCodec() {
        super();
        setObjectCodec(new XStreamCodec());
        setByteArrayCodec(new Base64Codec());
        setPrefix("xstream");
    }
}
