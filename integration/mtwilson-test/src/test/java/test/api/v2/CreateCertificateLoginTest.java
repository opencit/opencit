/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.api.v2;

import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.dcsg.cpg.io.ByteArrayResource;
import com.intel.dcsg.cpg.io.Resource;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.performance.AlarmClock;
import com.intel.mtwilson.as.rest.v2.model.HostCollection;
import com.intel.mtwilson.as.rest.v2.model.HostFilterCriteria;
import com.intel.mtwilson.attestation.client.jaxrs.Hosts;
import com.intel.mtwilson.tls.policy.creator.impl.InsecureTlsPolicyCreator;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyCreator;
import com.intel.mtwilson.v2.client.MwClientUtil;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class CreateCertificateLoginTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CreateCertificateLoginTest.class);

    @BeforeClass
    public static void configureTest() {
        // init
        Extensions.register(TlsPolicyCreator.class, InsecureTlsPolicyCreator.class);
    }
    
    private Properties getProperties() throws MalformedURLException {
        // configure
        String username = "testuser4";
        String password = "password";
        URL server = new URL("https://10.1.71.56:8443/mtwilson/v2");
        Properties properties = new Properties();
        properties.setProperty("mtwilson.api.url", server.toExternalForm());
        properties.setProperty("mtwilson.api.keystore", "target"+File.separator+"test-keystore.jks");
        properties.setProperty("mtwilson.api.keystore.password", password);
        properties.setProperty("mtwilson.api.key.alias", username);
        properties.setProperty("mtwilson.api.key.password", password);
        properties.setProperty("mtwilson.api.tls.policy.insecure","true"); // corresponds to InsecureTlsPolicyCreator
        return properties;
    }
    
    @Test
    public void testCreateCertificateLogin() throws Exception {
        Properties properties = getProperties();
        // register
        ByteArrayResource resource = new ByteArrayResource();
        String comments = properties.getProperty("mtwilson.api.key.alias"); // makes it easy to search for the certificate login id later
        SimpleKeystore keystore = MwClientUtil.createUserInResourceV2(resource, 
                properties.getProperty("mtwilson.api.key.alias"),  // username
                properties.getProperty("mtwilson.api.key.password"), // password
                new URL(properties.getProperty("mtwilson.api.url")),
                properties, comments, Locale.US, "TLS");
        keystore.save(new File(properties.getProperty("mtwilson.api.keystore")), properties.getProperty("mtwilson.api.key.password"));
        log.debug("Saved keystore");
        // wait some time for manual approval, using these sql statements:
        UUID roleId = new UUID();
        log.debug("Manual approval required:");
        log.debug("Run this command on mtwilson server:  mtwilson approve-user-login-certificate-request {} --permissions hosts:search", comments);
    }
    
    @Test
    public void testCertificateLogin() throws Exception {
        Properties properties = getProperties();
        // try using it
        log.debug("Attempting to use certificate login...");
        Hosts hosts = new Hosts(properties);
        HostFilterCriteria criteria = new HostFilterCriteria();
        criteria.filter = false;
        HostCollection result = hosts.searchHosts(criteria);
        log.debug("got {} hosts from search", result.getHosts().size());
    }
}
