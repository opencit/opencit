/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.portal;

import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.mtwilson.My;
import com.intel.mtwilson.TrustAssertion;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.datatypes.xml.HostTrustXmlResponse;
import com.intel.mtwilson.model.Hostname;
import com.intel.mtwilson.ms.data.MwPortalUser;
import com.intel.mtwilson.saml.TrustAssertion.HostTrustAssertion;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.codec.binary.BinaryCodec;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class DashboardTest {
    private Logger log = LoggerFactory.getLogger(getClass());
    
    @Test
    public void testConfigurationRead() throws IOException {
        for (String localeName : My.configuration().getAvailableLocales()) {
                System.out.println(localeName);
            }
    }
    
    //@Test
    public void testGetStatusForVmware173() throws Exception {
        // use the portal user's keystore to validate the saml assertion, since we are getting "invalid saml signature"
//        List<MwPortalUser> admins = My.jpa().mwPortalUser().findMwPortalUserByUsernameEnabled("admin");
//        MwPortalUser admin = admins.get(0);
        MwPortalUser admin = My.jpa().mwPortalUser().findMwPortalUserByUserName("admin");
        SimpleKeystore keystore = new SimpleKeystore(admin.getKeystoreResource(), My.configuration().getKeystorePassword());
        for(String alias : keystore.listTrustedSamlCertificates()) {
            X509Certificate cert = keystore.getX509Certificate(alias);
            log.debug("trusted cert: {}", Sha1Digest.valueOf(X509Util.sha1fingerprint(cert)).toHexString());
        }
        X509Certificate currentSamlCert = My.client().getSamlCertificate();
        log.debug("current cert: {}", Sha1Digest.digestOf(currentSamlCert.getEncoded()).toHexString());
        
        String saml = My.client().getSamlForHost(new Hostname("10.1.71.173"));
        TrustAssertion trustAssertion = new TrustAssertion(keystore.getTrustedCertificates(SimpleKeystore.SAML), saml);
        log.debug("Assertion is valid? {}", trustAssertion.isValid());
//        log.debug("Assertion attributes: {}", StringUtils.join(trustAssertion.getAttributeNames(), ", "));
        Set<String> hostnames = trustAssertion.getHosts();
        for(String hostname : hostnames) {
            log.debug("Assertion for host {}", hostname);
            HostTrustAssertion hostTrustAssertion = trustAssertion.getTrustAssertion(hostname);
        log.debug("Assertion attributes: {}", StringUtils.join(hostTrustAssertion.getAttributeNames(), ", "));
            
        }
    }
    
    //@Test
    public void getSamlForMultipleHosts() throws Exception {
        HashSet<Hostname> hostnames = new HashSet<Hostname>();
        hostnames.add(new Hostname("10.1.71.173"));
        hostnames.add(new Hostname("10.1.71.170"));
        hostnames.add(new Hostname("10.1.71.201"));
        hostnames.add(new Hostname("10.1.71.174"));
        hostnames.add(new Hostname("10.1.71.175"));
        hostnames.add(new Hostname("10.1.71.169"));
        hostnames.add(new Hostname("10.1.71.126"));
        hostnames.add(new Hostname("RHEL8"));
        hostnames.add(new Hostname("10.1.71.172"));
        hostnames.add(new Hostname("RHEL168"));
        List<HostTrustXmlResponse> statuslist = My.client().getSamlForMultipleHosts(hostnames, false);
    }
    
    //@Test
    public void getKeystoreAndTest() throws Exception {
        MwPortalUser admin = My.jpa().mwPortalUser().findMwPortalUserByUserName("admin");
        byte[] bKeystore = admin.getKeystore();
        
        SimpleKeystore keystore = new SimpleKeystore(admin.getKeystoreResource(), My.configuration().getKeystorePassword());
        System.err.println("keystore pass: " + My.configuration().getKeystorePassword() + "\nkeystore size: " + bKeystore.length + "\nkeystore ascii: " + BinaryCodec.toAsciiString(bKeystore) + "\nkeystore contents: " + new String(bKeystore));
    }
}
