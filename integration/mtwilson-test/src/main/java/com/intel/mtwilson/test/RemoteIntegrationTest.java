/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.test;

import com.intel.dcsg.cpg.util.shiro.Login;
import com.intel.mtwilson.My;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import org.junit.BeforeClass;

/**
 * Convenience base class for junit tests that call into Mt Wilson APIs
 *
 * Use this base class when you are testing against a remote web service with
 * existing users.
 *
 * Example content of c:/mtwilson/configuration/test.properties:
 * <pre>
 * mtwilson.api.url=https://10.1.71.56:8181/mtwilson/v2
 * mtwilson.api.username=admin
 * mtwilson.api.password=password
 * </pre>
 *
 * Example unit test:
 * <pre>
 * public void testProvisionCertificate() throws Exception {
 *   TagCertificates client = new TagCertificates(testProperties);
 *   TagCertificateCollection results = client.searchTagCertificates(new
 *   TagCertificateFilterCriteria());
 *   List<TagCertificate> list = results.getTagCertificates();
 *   for(TagCertificate tagcert : list) {
 *     log.debug("certificate id {}", tagcert.getId().toString());
 *   }
 * }
 * </pre>
 *
 *
 * @author jbuhacoff
 */
public class RemoteIntegrationTest {

    protected final static Properties testProperties = new Properties();

    @BeforeClass
    public static void loadProperties() throws Exception {
        String filename = My.filesystem().getConfigurationPath() + File.separator + "test.properties";
        File file = new File(filename);
        try (FileInputStream in = new FileInputStream(file)) {
            testProperties.load(in);
        }
    }
}
