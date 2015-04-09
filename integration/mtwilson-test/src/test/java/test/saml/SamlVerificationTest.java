/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.saml;

import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.io.FileResource;
import com.intel.dcsg.cpg.x509.X509Builder;
import com.intel.mtwilson.My;
import com.intel.mtwilson.TrustAssertion;
import com.intel.mtwilson.saml.SamlAssertion;
import com.intel.mtwilson.saml.SamlGenerator;
import com.intel.mtwilson.datatypes.HostTrustStatus;
import com.intel.mtwilson.datatypes.TxtHost;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import java.io.File;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Properties;
import org.apache.commons.configuration.MapConfiguration;
import org.junit.Test;

/**
 * http://stackoverflow.com/questions/17331187/xml-dig-sig-error-after-upgrade-to-java7u25
 * 
 * @author jbuhacoff
 */
public class SamlVerificationTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SamlVerificationTest.class);

    @Test
    public void testCreateAndVerifySamlAssertion() throws Exception {
        // generate SAML signing credential and save it in a keystore
        KeyPair keypair = RsaUtil.generateRsaKeyPair(1024);
        X509Certificate certificate = X509Builder.factory().selfSigned("CN=saml", keypair).build();
        File keystoreFile = My.repository().getDirectory("saml"); //new File(My.filesystem().getBootstrapFilesystem().getVarPath() + File.separator + "test-saml.jks");
        if( keystoreFile.exists() ) { keystoreFile.delete(); }
        String password = RandomUtil.randomHexString(8);
        String alias = "samlkey1";
        Properties properties = new Properties();
        properties.setProperty("saml.keystore.file", keystoreFile.getAbsolutePath());
        properties.setProperty("saml.keystore.password", password);
        properties.setProperty("saml.key.alias", alias);
        properties.setProperty("saml.key.password", password);
        properties.setProperty("saml.issuer", "https://127.0.0.1");
        MapConfiguration configuration = new MapConfiguration(properties);
        FileResource resource = new FileResource(keystoreFile);
        SimpleKeystore keystore = new SimpleKeystore(resource, password);
        keystore.addKeyPairX509(keypair.getPrivate(), certificate, alias, password);
        keystore.save();
        // generate fake data for the assertion
        TxtHostRecord txtHostRecord = new TxtHostRecord();
        txtHostRecord.IPAddress = "127.0.0.1";
        txtHostRecord.HostName = "localhost";
        txtHostRecord.BIOS_Name = "generic bios";
        txtHostRecord.BIOS_Version = "1.0";
        HostTrustStatus hostTrustStatus = new HostTrustStatus();
        hostTrustStatus.asset_tag = false;
        hostTrustStatus.bios = false;
        hostTrustStatus.location = false;
        hostTrustStatus.vmm = false;
        TxtHost host = new TxtHost(txtHostRecord, hostTrustStatus);
        // generate SAML assertion
        SamlGenerator generator = new SamlGenerator(resource, configuration);
        SamlAssertion assertion = generator.generateHostAssertion(host, null, null);
        log.debug("assertion: {}", assertion.assertion);
        // verify SAML assertion
        TrustAssertion verifier = new TrustAssertion(new X509Certificate[] { certificate }, assertion.assertion);
        if( verifier.isValid() ) {
            log.debug("valid assertion");
//            log.debug("subject {}", verifier.getSubject());
        }
        else {
            log.debug("invalid assertion", verifier.error());
        }
    }
}
