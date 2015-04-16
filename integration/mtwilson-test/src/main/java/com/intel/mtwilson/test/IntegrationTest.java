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
 * Convenience base class for junit tests that call into Mt Wilson business
 * logic which may be annotated with required permissions.
 *
 * Use this base class when you are testing against a remote database with
 * existing users.
 *
 * See also mtwilson-shiro-util test/resources for example shiro-junit.ini and
 * example test.properties.
 *
 * Example content of c:/mtwilson/configuration/test.properties:
 * <pre>
 * login.username=admin
 * login.password=password
 * </pre>
 *
 * Example unit test:
 * <pre>
 * public void testRetrieveCertificate() throws IOException {
 *   CertificateRepository certificateRepository = new CertificateRepository();
 *   CertificateFilterCriteria criteria = new CertificateFilterCriteria();
 *   criteria.subjectEqualTo = "e1ca94c1-cb01-11df-a441-001517fa99c0";
 *   CertificateCollection results = certificateRepository.search(criteria);
 *   Certificate certificate = results.getCertificates().get(0);
 *   FileUtils.writeByteArrayToFile(new File("./target/certbytes"), certificate.getCertificate());
 *   certificate.getX509Certificate();
 * }
 * </pre>
 *
 * @author jbuhacoff
 */
public class IntegrationTest {

    @BeforeClass
    public static void login() throws Exception {
        String filename = My.configuration().getDirectoryPath()+File.separator+"test.properties";//My.filesystem().getConfigurationPath() + File.separator + "test.properties";
        File file = new File(filename);
        try (FileInputStream in = new FileInputStream(file)) {
            Properties properties = new Properties();
            properties.load(in);
            String username = properties.getProperty("login.username");
            String password = properties.getProperty("login.password");
            File ini = new File(My.configuration().getDirectoryPath() + File.separator + "shiro-junit.ini");
            Login.existingUser(ini, username, password);
        }

    }
}
