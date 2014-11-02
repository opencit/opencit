/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.rfc822;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

/**
 *
 * @author jbuhacoff
 */
public class GzipEncoder implements Encoder {
    @Override
    public byte[] encode(byte[] input) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        GZIPOutputStream out = new GZIPOutputStream(buffer); // throws IOException
        out.write(input);
        out.close();
        return buffer.toByteArray();
    }
}
