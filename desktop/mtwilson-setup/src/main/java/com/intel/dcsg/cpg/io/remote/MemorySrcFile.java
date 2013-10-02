/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.io.remote;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import net.schmizz.sshj.xfer.InMemorySourceFile;

/**
 *
 * @author jbuhacoff
 */
public class MemorySrcFile extends InMemorySourceFile {

    private String name;
    private byte[] array;

    public MemorySrcFile(byte[] data) {
        name = "File" + this.hashCode();
        array = data;
    }

    public MemorySrcFile(String filename, byte[] data) {
        name = filename;
        array = data;
    }

    public byte[] toByteArray() {
        return array;
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(array);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getLength() {
        return array.length;
    }
}
