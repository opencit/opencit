/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.rfc822;

import java.io.IOException;

/**
 * The "identity" encoding is a no-op.
 * @author jbuhacoff
 */
public class IdentityDecoder implements Decoder {

    @Override
    public byte[] decode(byte[] encoded) throws IOException {
        return encoded;
    }
    
}
