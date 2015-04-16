/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.archive;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.io.FileUtils;

/**
 * Abstracts creation of TarArchiveEntry objects to make it easier to create
 * tar files in application code.
 * 
 * @author jbuhacoff
 */
public class TarGzipBuilder {
    private GzipCompressorOutputStream gzip;
    private TarArchiveOutputStream tar;
    
    public TarGzipBuilder(OutputStream out) throws IOException {
        gzip = new GzipCompressorOutputStream(out);
        tar = new TarArchiveOutputStream(gzip);
    }
    
    public void add(File file) throws IOException {
        TarArchiveEntry tarArchiveEntry = new TarArchiveEntry(file);
        tar.putArchiveEntry(tarArchiveEntry);
        tar.write(FileUtils.readFileToByteArray(file));
        tar.closeArchiveEntry();
        tar.flush();
    }
    
    public void add(String filename, byte[] content) throws IOException {
        TarArchiveEntry tarArchiveEntry = new TarArchiveEntry(filename);
        tarArchiveEntry.setSize(content.length);
        tar.putArchiveEntry(tarArchiveEntry);
        tar.write(content);
        tar.closeArchiveEntry();
        tar.flush();
    }

    public void add(String filename, String content) throws IOException {
        add(filename, content.getBytes(Charset.forName("UTF-8")));
    }
    
    public void close() throws IOException {
        tar.close();
    }
    
}
