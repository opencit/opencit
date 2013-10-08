/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.io.remote;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import net.schmizz.sshj.xfer.InMemoryDestFile;

/**
 *
 * @author jbuhacoff
 */
public class MemoryDstFile extends InMemoryDestFile {

    private ByteArrayOutputStream out = new ByteArrayOutputStream();

    @Override
    public OutputStream getOutputStream() throws IOException {
        return out;
    }

    public byte[] toByteArray() {
        return out.toByteArray();
    }

    public InputStream getInputStream() {
        return new ByteArrayInputStream(toByteArray());
    }
}
