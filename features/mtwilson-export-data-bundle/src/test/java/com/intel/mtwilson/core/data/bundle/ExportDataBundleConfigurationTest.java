/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.core.data.bundle;

import com.intel.mtwilson.util.archive.TarGzipBuilder;
import com.intel.mtwilson.core.data.bundle.TarGzipBundle;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class ExportDataBundleConfigurationTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExportDataBundleConfigurationTest.class);

    @Test
    public void testCreateBundle() throws IOException {
        // add document.pdf,  metadata.json,  integrity.json, integrity.sig
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        TarGzipBuilder tgz = new TarGzipBuilder(out);
        tgz.add("document.pdf", "this is the pdf");
        tgz.add("metadata.json", "this is the metadata");
        tgz.add("integrity.json", "this is the integrity info");
        tgz.add("integrity.sig", "this is the integrity signature");
        tgz.close();
        
        TarGzipBundle bundle = new TarGzipBundle();
        bundle.set("document.pdf", "this is the pdf");
        bundle.set("metadata.json", "this is the metadata");
        bundle.set("integrity.json", "this is the integrity info");
        bundle.set("integrity.sig", "this is the integrity signature");
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        bundle.write(buffer);
        TarGzipBundle bundle2 = new TarGzipBundle();
        bundle2.read(new ByteArrayInputStream(buffer.toByteArray()));
        for(String entry : bundle2.list()) {
            log.debug("entry: {} = {}", entry, bundle2.getString(entry));
        }
    }
}
