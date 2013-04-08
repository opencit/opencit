/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package test.tls;

import com.intel.mountwilson.as.common.ASConfig;
import org.junit.Test;
import com.intel.mtwilson.agent.*;
import com.intel.mtwilson.tls.*;
import com.intel.mtwilson.as.data.*;
import com.intel.mtwilson.crypto.CryptographyException;
import com.intel.mtwilson.crypto.SimpleKeystore;
import com.intel.mtwilson.crypto.SslUtil;
import com.intel.mtwilson.crypto.X509Util;
import com.intel.mtwilson.io.ByteArrayResource;
import com.intel.mtwilson.jpa.PersistenceManager;
import com.intel.mtwilson.model.Md5Digest;
import com.intel.mtwilson.ms.common.MSConfig;
import com.intel.mtwilson.ms.controller.MwPortalUserJpaController;
import com.intel.mtwilson.ms.controller.exceptions.MSDataException;
import com.intel.mtwilson.ms.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.ms.data.MwPortalUser;
import java.io.IOException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author jbuhacoff
 */
public class UpdateTlsKeystoreInDatabaseTest {
    private transient Logger log = LoggerFactory.getLogger(getClass());
        
    @Test
    public void testAddEsxHostAndSaveTlsKeystore() throws IOException {
        TblHosts host = new TblHosts();
        host.setName("10.1.71.176");
        host.setAddOnConnectionInfo("vmware:https://10.1.71.162:443/sdk;administrator;intel123!");
        log.debug("before connecting, tls policy = {} and keystore length = {}", host.getTlsPolicyName(), host.getTlsKeystore() == null ? "null" : host.getTlsKeystore().length);
        HostAgentFactory factory = new HostAgentFactory();
        HostAgent agent = factory.getHostAgent(host);
        String vendorHostReport = agent.getVendorHostReport();
        System.out.println(vendorHostReport);
        log.debug("after connecting, tls policy = {} and keystore length = {}", host.getTlsPolicyName(), host.getTlsKeystore() == null ? "null" : host.getTlsKeystore().length);
    }
    
    /**
2013-04-07 19:19:10,476 DEBUG [main] t.t.UpdateTlsKeystoreInDatabaseTest [UpdateTlsKeystoreInDatabaseTest.java:67] keystore: c9067fae3940819e5294e7d86b250e8a
2013-04-07 19:19:10,500 INFO [main] t.t.UpdateTlsKeystoreInDatabaseTest [UpdateTlsKeystoreInDatabaseTest.java:72] CN=10.1.71.88,OU=Mt Wilson,O=Trusted Data Center,C=US (10.1.71.88)
     * 
     * @throws KeyManagementException
     * @throws CryptographyException
     * @throws IOException
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     * @throws NonexistentEntityException
     * @throws MSDataException
     * @throws UnrecoverableEntryException
     * @throws UnrecoverableEntryException 
     */
    @Test
    public void testShowTlsCertificatesInExistingUserKeystore() throws KeyManagementException, CryptographyException, IOException, KeyStoreException, NoSuchAlgorithmException, NoSuchAlgorithmException, CertificateException, NonexistentEntityException, MSDataException, UnrecoverableEntryException, UnrecoverableEntryException {
        String username = "ManagementServiceAutomation";
        String password = "password";
        CustomMSPersistenceManager pm = new CustomMSPersistenceManager();
        MwPortalUserJpaController keystoreJpa = new MwPortalUserJpaController(pm.getEntityManagerFactory("MSDataPU"));
        MwPortalUser portalUser = keystoreJpa.findMwPortalUserByUserName(username);
        byte[] keystorebytes = portalUser.getKeystore();
        log.debug("keystore: {}", Md5Digest.valueOf(keystorebytes));
        ByteArrayResource resource = new ByteArrayResource(keystorebytes);
        SimpleKeystore keystore = new SimpleKeystore(resource, password);
        X509Certificate[] certificates = keystore.getTrustedCertificates("ssl");
        for(X509Certificate certificate : certificates) {
            log.info(String.format("%s (%s)", certificate.getSubjectX500Principal().getName(), StringUtils.join(X509Util.alternativeNames(certificate),", ")));
        }
    }
    
    /**
     * Sample output:
2013-04-07 19:18:18,607 DEBUG [main] t.t.UpdateTlsKeystoreInDatabaseTest [UpdateTlsKeystoreInDatabaseTest.java:87] old keystore: 773f4e70212139dc883da3da696d421e
2013-04-07 19:18:18,710 DEBUG [main] c.i.m.c.SslUtil [SslUtil.java:192] Saving certificates from server URL: https://10.1.71.88:8181
2013-04-07 19:18:23,701 DEBUG [main] c.i.m.c.SslUtil [SslUtil.java:195] Server status line: HTTP OK (200)
2013-04-07 19:18:23,706 INFO [main] c.i.m.c.SslUtil [SslUtil.java:107] Added SSL certificate with alias 10.1.71.88-1, subject CN=10.1.71.88,OU=Mt Wilson,O=Trusted Data Center,C=US, fingerprint c067da6346d6698d6ed1ef2e5b7a9d0a097304cb, from server 10.1.71.88
2013-04-07 19:18:23,710 DEBUG [main] t.t.UpdateTlsKeystoreInDatabaseTest [UpdateTlsKeystoreInDatabaseTest.java:93] new keystore: c9067fae3940819e5294e7d86b250e8a
     * 
     * @throws KeyManagementException
     * @throws CryptographyException
     * @throws IOException
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     * @throws NonexistentEntityException
     * @throws MSDataException Samp
     */
    @Test
    public void testAddCurrentTlsCertificateToExistingUserKeystore() throws KeyManagementException, CryptographyException, IOException, KeyStoreException, NoSuchAlgorithmException, NoSuchAlgorithmException, CertificateException, NonexistentEntityException, MSDataException {
        String username = "ManagementServiceAutomation";
        String password = "password";
        String url = "https://10.1.71.88:8181";
        //MSConfig.getJpaProperties(new MSConfig(p).getConfigurationInstance());
        CustomMSPersistenceManager pm = new CustomMSPersistenceManager();
        MwPortalUserJpaController keystoreJpa = new MwPortalUserJpaController(pm.getEntityManagerFactory("MSDataPU"));
        MwPortalUser portalUser = keystoreJpa.findMwPortalUserByUserName(username);
        byte[] oldkeystore = portalUser.getKeystore();
        log.debug("old keystore: {}", Md5Digest.valueOf(oldkeystore));
        ByteArrayResource resource = new ByteArrayResource(oldkeystore);
        SimpleKeystore keystore = new SimpleKeystore(resource, password);
        SslUtil.addSslCertificatesToKeystore(keystore, new URL(url));
        keystore.save();
        byte[] newkeystore = resource.toByteArray();
        log.debug("new keystore: {}", Md5Digest.valueOf(newkeystore));
        portalUser.setKeystore(newkeystore);
        keystoreJpa.edit(portalUser);
    }
    
    public static class CustomMSPersistenceManager extends PersistenceManager {

        @Override
        public void configure() {
            Properties p = new Properties();
            p.setProperty("mtwilson.db.host", "10.1.71.88");
            p.setProperty("mtwilson.db.schema", "mw_as");
            p.setProperty("mtwilson.db.user", "root");
            p.setProperty("mtwilson.db.password", "password");
            p.setProperty("mtwilson.db.port", "3306");
            MapConfiguration c = new MapConfiguration(p);
            addPersistenceUnit("ASDataPU", ASConfig.getJpaProperties(c));
            addPersistenceUnit("MSDataPU", MSConfig.getJpaProperties(c));
        }

    }
    
}
