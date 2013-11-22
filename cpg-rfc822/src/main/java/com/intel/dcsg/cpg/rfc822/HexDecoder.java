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
public class HexDecoder implements Decoder {

    @Override
    public byte[] decode(byte[] encoded) throws IOException {
        try {
            return Hex.decodeHex(new String(encoded, "UTF-8").toCharArray()); // throws DecoderException
        }
        catch(Exception e) {
            throw new IOException("Cannot hex-decode data", e);
        }
    }
    
}
