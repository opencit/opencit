/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.file;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class ZipSignatureTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ZipSignatureTest.class);
    
    private ArrayList<String> filenames = new ArrayList<String>(); // populated by testReadZipFile()
    private ArrayList<String> signatureFilenames = new ArrayList<String>(); // populated by identifyContentAndSignature()
    
    @Test
    public void testReadZipFile() throws Exception {
        ZipInputStream in = new ZipInputStream(getClass().getResourceAsStream("/helloworld.zip"));
        ZipEntry entry = in.getNextEntry();
        while(entry != null ) {
            log.debug("entry name: {} size: {}", entry.getName(), entry.getSize());
            if( entry.getSize() > Integer.MAX_VALUE ) {
                log.debug("entry too long, skipping");
                continue;
            }
            filenames.add(entry.getName()); // to support other unit tests
            int filesize = (int)entry.getSize();
            byte[] file = new byte[filesize];
            for(int i=0; i<file.length && in.available()==1; i+=in.read(file, i, file.length-i)) {
                log.debug("read {} bytes", i);
            }
            log.debug("file conetnt: {}", new String(file, Charset.forName("UTF-8")));
            in.closeEntry();
            entry = in.getNextEntry();
        }
    }
    
    /**
     * Output:
2014-02-05 16:07:37,690 DEBUG [main] t.f.ZipSignatureTest [ZipSignatureTest.java:52] Found signature file: helloworld.txt.sig
2014-02-05 16:07:37,690 DEBUG [main] t.f.ZipSignatureTest [ZipSignatureTest.java:60] Found document file: helloworld.txt
     * 
     * @throws Exception 
     */
    @Test
    public void identifyContentAndSignature() throws Exception {
        testReadZipFile(); // to populate the "filenames" list
        // first, find all the filenames that indicate signatures
        for(String filename : filenames) {
            if( filename.endsWith(".sig") ) {
                log.debug("Found signature file: {}", filename);
                signatureFilenames.add(filename);
            }
        }
        // second, for each signature file try to find the content file
        for(String signatureFilename : signatureFilenames) {
            String documentFilename = signatureFilename.replaceAll(".sig$", "");
            if( filenames.contains(documentFilename)) {
                log.debug("Found document file: {}", documentFilename);
            }
        }
    }
    
    
}
