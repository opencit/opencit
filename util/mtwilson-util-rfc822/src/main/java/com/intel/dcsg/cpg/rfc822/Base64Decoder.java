/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.rfc822;

import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author jbuhacoff
 */
public class Base64Decoder implements Decoder {
    @Override
    public byte[] decode(byte[] base64) {
        return Base64.decodeBase64(base64);
    }
}
