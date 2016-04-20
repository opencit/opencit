/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package resource;

import java.io.*;
import java.net.URL;
import java.util.Properties;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class LoadResourceTest {
    private static Logger log = LoggerFactory.getLogger(LoadResourceTest.class);
    
    @Test
    public void testLoadFileOnWindowsWithForwardSlashes() throws IOException {
        File file = new File("/Intel/CloudSecurity/attestation-service.properties"); // this works on windows to load C:\Intel\CloudSecurity\attestation-service.properties
        System.out.println(IOUtils.toString(new FileInputStream(file)));        
    }

    @Test
    public void testLoadFileOnWindowsWithDriveLetterAndForwardSlashes() throws IOException {
        File file = new File("C:/Intel/CloudSecurity/attestation-service.properties"); // this works on windows to load C:\Intel\CloudSecurity\attestation-service.properties
        System.out.println(IOUtils.toString(new FileInputStream(file)));        
    }

}
