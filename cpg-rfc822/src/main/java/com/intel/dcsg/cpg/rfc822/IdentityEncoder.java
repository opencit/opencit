/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.rfc822;

import java.io.IOException;

/**
 * The "identity" encoding is a no-op.
 * 
 * @author jbuhacoff
 */
public class IdentityEncoder implements Encoder {

    @Override
    public byte[] encode(byte[] data) throws IOException {
        return data;
    }
    
}
