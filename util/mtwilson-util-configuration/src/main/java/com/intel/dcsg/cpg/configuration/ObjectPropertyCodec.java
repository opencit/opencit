/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.configuration;

import com.intel.mtwilson.codec.ObjectCodec;
import com.intel.mtwilson.codec.ByteArrayCodec;

/**
 *
 * @author jbuhacoff
 */
public class ObjectPropertyCodec {
    private ObjectCodec objectCodec;
    private ByteArrayCodec byteArrayCodec;
    private String prefix;
    private String prefixBar; // prefix + "|"
    
    public void setObjectCodec(ObjectCodec objectCodec) {
        this.objectCodec = objectCodec;
    }

    public ObjectCodec getObjectCodec() {
        return objectCodec;
    }

    public void setByteArrayCodec(ByteArrayCodec byteArrayCodec) {
        this.byteArrayCodec = byteArrayCodec;
    }

    public ByteArrayCodec getByteArrayCodec() {
        return byteArrayCodec;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
        this.prefixBar = prefix + "|";
    }

    public String getPrefix() {
        return prefix;
    }
    
    
    public String encode(Object input) {
        return String.format("%s%s", prefixBar, byteArrayCodec.encode(objectCodec.encode(input)));
    }
    
    public Object decode(String encoded) {
        if( encoded.startsWith(prefixBar) ) {
            return objectCodec.decode(byteArrayCodec.decode(encoded.substring(prefixBar.length())));
        }
        throw new IllegalArgumentException("Property does not begin with prefix "+prefix);
    }
}
