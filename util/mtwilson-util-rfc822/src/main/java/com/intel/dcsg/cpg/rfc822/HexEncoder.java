/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.rfc822;

import java.io.IOException;
import org.apache.commons.codec.binary.Hex;

/**
 *
 * @author jbuhacoff
 */
public class HexEncoder implements Encoder {

    @Override
    public byte[] encode(byte[] data) throws IOException {
        char[] encoded = Hex.encodeHex(data);
        return String.valueOf(encoded).getBytes("UTF-8");
    }
    
}
