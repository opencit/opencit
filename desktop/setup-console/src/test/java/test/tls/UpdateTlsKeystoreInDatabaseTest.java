/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package test.tls;

import com.intel.mountwilson.as.common.ASConfig;
import com.intel.mtwilson.My;
import org.junit.Test;
import com.intel.mtwilson.agent.*;
import com.intel.mtwilson.api.ApiException;
import com.intel.mtwilson.as.controller.TblHostsJpaController;
import com.intel.mtwilson.as.controller.exceptions.ASDataException;
import com.intel.mtwilson.as.controller.exceptions.IllegalOrphanException;
import com.intel.mtwilson.as.data.*;
import com.intel.mtwilson.as.ASComponentFactory;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.dcsg.cpg.io.ByteArrayResource;
import com.intel.dcsg.cpg.io.FileResource;
import com.intel.dcsg.cpg.jpa.PersistenceManager;
import com.intel.dcsg.cpg.tls.policy.TlsUtil;
import com.intel.mtwilson.model.Md5Digest;
import com.intel.mtwilson.model.Sha1Digest;
import com.intel.mtwilson.model.Sha256Digest;
import com.intel.mtwilson.ms.common.MSConfig;
import com.intel.mtwilson.ms.controller.MwPortalUserJpaController;
import com.intel.mtwilson.ms.controller.exceptions.MSDataException;
import com.intel.mtwilson.ms.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.ms.data.MwPortalUser;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
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
        MwPortalUserJpaController keystoreJpa = new MwPortalUserJpaController(My.persistenceManager().getEntityManagerFactory("MSDataPU"));
        MwPortalUser portalUser = keystoreJpa.findMwPortalUserByUserName(username);
        byte[] keystorebytes = portalUser.getKeystore();
        log.debug("keystore: {}", Md5Digest.valueOf(keystorebytes));
        ByteArrayResource resource = new ByteArrayResource(keystorebytes);
        SimpleKeystore keystore = new SimpleKeystore(resource, password);
        X509Certificate[] certificates = keystore.getTrustedCertificates("ssl");
        for(X509Certificate certificate : certificates) {
            log.debug(String.format("%s (%s)", certificate.getSubjectX500Principal().getName(), StringUtils.join(X509Util.alternativeNames(certificate),", ")));
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
        String username = "admin"; // "ManagementServiceAutomation"; 
        String password = "password";
        MwPortalUserJpaController keystoreJpa = new MwPortalUserJpaController(My.persistenceManager().getEntityManagerFactory("MSDataPU"));
        MwPortalUser portalUser = keystoreJpa.findMwPortalUserByUserName(username);
        byte[] oldkeystore = portalUser.getKeystore();
        log.debug("old keystore: {}", Md5Digest.valueOf(oldkeystore));
        ByteArrayResource resource = new ByteArrayResource(oldkeystore);
        SimpleKeystore keystore = new SimpleKeystore(resource, password);
        TlsUtil.addSslCertificatesToKeystore(keystore, My.configuration().getMtWilsonURL());
        keystore.save();
        byte[] newkeystore = resource.toByteArray();
        log.debug("new keystore: {}", Md5Digest.valueOf(newkeystore));
        portalUser.setKeystore(newkeystore);
        keystoreJpa.edit(portalUser);
    }

    @Test
    public void testAddCurrentTlsCertificateToMyUserKeystore() throws KeyManagementException, CryptographyException, IOException, KeyStoreException, NoSuchAlgorithmException, NoSuchAlgorithmException, CertificateException, NonexistentEntityException, MSDataException {
        FileResource keystoreFile = new FileResource(My.configuration().getKeystoreFile());
        SimpleKeystore keystore = new SimpleKeystore(keystoreFile, My.configuration().getKeystorePassword());
        TlsUtil.addSslCertificatesToKeystore(keystore, My.configuration().getMtWilsonURL());
        keystore.save();
    }
    
    @Test
    public void testAddCurrentSamlCertificateToExistingUserKeystore() throws KeyManagementException, CryptographyException, IOException, KeyStoreException, NoSuchAlgorithmException, NoSuchAlgorithmException, CertificateException, NonexistentEntityException, MSDataException, MalformedURLException, ApiException, SignatureException, Exception {
        String username = "admin"; // "ManagementServiceAutomation"; 
        String password = "password";
        // get the new saml certificate
        X509Certificate samlCert = My.client().getSamlCertificate();
        MwPortalUserJpaController keystoreJpa = new MwPortalUserJpaController(My.persistenceManager().getEntityManagerFactory("MSDataPU"));
        MwPortalUser portalUser = keystoreJpa.findMwPortalUserByUserName(username);
        byte[] oldkeystore = portalUser.getKeystore();
        log.debug("old keystore: {}", Md5Digest.valueOf(oldkeystore));
        ByteArrayResource resource = new ByteArrayResource(oldkeystore);
        SimpleKeystore keystore = new SimpleKeystore(resource, password);
        keystore.addTrustedSamlCertificate(samlCert, samlCert.getSubjectX500Principal().getName());
        keystore.save();
        byte[] newkeystore = resource.toByteArray();
        log.debug("new keystore: {}", Md5Digest.valueOf(newkeystore));
        portalUser.setKeystore(newkeystore);
        keystoreJpa.edit(portalUser);
    }
    
    @Test
    public void testPrintCurrentUserKeystoreContents() throws KeyManagementException, CryptographyException, IOException, KeyStoreException, NoSuchAlgorithmException, NoSuchAlgorithmException, CertificateException, NonexistentEntityException, MSDataException, UnrecoverableEntryException {
        String username = "admin";
        String password = "password";
        MwPortalUserJpaController keystoreJpa = new MwPortalUserJpaController(My.persistenceManager().getEntityManagerFactory("MSDataPU"));
        MwPortalUser portalUser = keystoreJpa.findMwPortalUserByUserName(username);
        byte[] keystorebytes = portalUser.getKeystore();
        ByteArrayResource resource = new ByteArrayResource(keystorebytes);
        SimpleKeystore keystore = new SimpleKeystore(resource, password);
        for(String alias : keystore.aliases()) {
            X509Certificate cert = keystore.getX509Certificate(alias);
            System.out.println(alias+": "+cert.getSubjectX500Principal().getName());
        }
    }
    
    @Test
    public void testUpdateAikSha256() throws KeyManagementException, CryptographyException, IOException, KeyStoreException, NoSuchAlgorithmException, NoSuchAlgorithmException, CertificateException, NonexistentEntityException, MSDataException, IllegalOrphanException, com.intel.mtwilson.as.controller.exceptions.NonexistentEntityException, com.intel.mtwilson.as.controller.exceptions.NonexistentEntityException, CertificateEncodingException, ASDataException {
        byte[] dek = Base64.decodeBase64("hPKk/2uvMFRAkpJNJgoBwA==");
        TblHostsJpaController hostsJpa = new TblHostsJpaController(My.persistenceManager().getEntityManagerFactory("ASDataPU"));
        TblHosts host = hostsJpa.findByIPAddress("10.1.71.169");
        String certificatePem = host.getAIKCertificate();
        X509Certificate certificate = X509Util.decodePemCertificate(certificatePem);
        Sha256Digest sha256 = Sha256Digest.valueOf(certificate.getEncoded());
        host.setAikSha256(sha256.toString());
        hostsJpa.edit(host);
    }
    
    
    
    
    // for mystery hill
    @Test
    public void testAddTrustedCertificateToKeystore() throws CertificateException, CryptographyException, KeyManagementException, KeyStoreException, IOException, IOException, NoSuchAlgorithmException, IllegalOrphanException, com.intel.mtwilson.as.controller.exceptions.NonexistentEntityException, ASDataException, ASDataException {
        String cert = "-----BEGIN CERTIFICATE-----\n"+
"MIIChzCCAfACAQowDQYJKoZIhvcNAQEFBQAwgYcxCzAJBgNVBAYTAlVTMQswCQYD\n"+
"VQQIDAJDQTETMBEGA1UEBwwKU2FjcmFtZW50bzEOMAwGA1UECgwFSW50ZWwxDTAL\n"+
"BgNVBAsMBElBU0kxCzAJBgNVBAMMAkNBMSowKAYJKoZIhvcNAQkBFhtqb25hdGhh\n"+
"bi5idWhhY29mZkBpbnRlbC5jb20wHhcNMTMwNDA4MDY0MjAwWhcNMTQwNDA4MDY0\n"+
"MjAwWjCBjzELMAkGA1UEBhMCVVMxEzARBgNVBAgTCkNhbGlmb3JuaWExEzARBgNV\n"+
"BAcTClNhY3JhbWVudG8xDTALBgNVBAoTBENTVVMxFDASBgNVBAMTCzEwLjEuNzEu\n"+
"MTY5MTEwLwYDVQQDEyg4NzgzNjk1ODIxMDViOGFmOTc4OTlkMWZlNGM4YTcxOWYw\n"+
"MWU1ZTEwMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDtc9wWbZnBIzGNl+j8\n"+
"8laPpKqUb3iHaxtjYl9vkXNBdVOGQt90MFGedqVqm1Gip0uGAMzoptYLZ3+cUSmu\n"+
"BJwWjfkcNQkb2kIjmJrZGeGZpSmfRTubh2DO+j1VTcaYFAMlC5SNOlHwUijMqMNg\n"+
"pbcTdO6c8FX71mDnHyPrEqaR1QIDAQABMA0GCSqGSIb3DQEBBQUAA4GBAH8e7T8O\n"+
"feENhru9lniGdlYlZsZSTxWVKGAOxR2FJaoMGO02toyqUhDMZ2BGWy7e9KoVC1Pc\n"+
"+VTUu95lCYci4JSfQILQLlIDJej35ZmnMW2ITVJKjkFRAFCCLYyu45QujHrg0TaC\n"+
"JVNhYL6Xz7PDFx0BoV3OQpHPZkcGr/xQ5UzM\n"+
"-----END CERTIFICATE-----\n";
        X509Certificate x509 = X509Util.decodePemCertificate(cert);
        String sha1 = "878369582105b8af97899d1fe4c8a719f01e5e10";
        /*
        TblHostsJpaController hostsJpa = new TblHostsJpaController(My.persistenceManager().getEntityManagerFactory("ASDataPU"), My.persistenceManager().getDek());
        */
        TblHostsJpaController hostsJpa = new TblHostsJpaController(My.persistenceManager().getEntityManagerFactory("ASDataPU"));
        TblHosts host = hostsJpa.findByIPAddress("10.1.71.169");
        SimpleKeystore keystore = new SimpleKeystore(host.getTlsKeystoreResource(),"password");
        keystore.addTrustedCertificate(x509, sha1, "dek-recipient");
        keystore.save();
        hostsJpa.edit(host);
    }
    

    @Test
    public void testShowCertificatesInExistingHostTlsKeystore() throws KeyManagementException, CryptographyException, IOException, KeyStoreException, NoSuchAlgorithmException, NoSuchAlgorithmException, CertificateException, NonexistentEntityException, MSDataException, UnrecoverableEntryException, UnrecoverableEntryException {
        /*
        TblHostsJpaController hostsJpa = new TblHostsJpaController(My.persistenceManager().getEntityManagerFactory("ASDataPU"), My.persistenceManager().getDek());
        */
        TblHostsJpaController hostsJpa = new TblHostsJpaController(My.persistenceManager().getEntityManagerFactory("ASDataPU"));
        TblHosts host = hostsJpa.findByIPAddress("10.1.70.126");
        SimpleKeystore keystore = new SimpleKeystore(host.getTlsKeystoreResource(),"password");
        ArrayList<X509Certificate> certificates = new ArrayList<X509Certificate>();
        for(String alias : keystore.aliases()) {
            log.debug("certificate alias {}", alias);
            certificates.add(keystore.getX509Certificate(alias));
        }
        for(X509Certificate certificate : certificates) {
            log.debug(String.format("Subject: %s", certificate.getSubjectX500Principal().getName()));
            log.debug(String.format("Alternative names: %s", X509Util.alternativeNames(certificate).isEmpty() ? "none" : StringUtils.join(X509Util.alternativeNames(certificate),", ")));
            log.debug(String.format("Fingerprint SHA-256: %s", Hex.encodeHexString(X509Util.sha256fingerprint(certificate))));
        }
    }
    
}

