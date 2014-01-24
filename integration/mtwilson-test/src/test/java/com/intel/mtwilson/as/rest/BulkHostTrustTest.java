/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest;

import com.intel.mtwilson.ApiClient;
import com.intel.mtwilson.KeystoreUtil;
import com.intel.dcsg.cpg.crypto.RsaCredential;
import com.intel.dcsg.cpg.crypto.RsaCredentialX509;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.mtwilson.datatypes.*;
import com.intel.mtwilson.model.*;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author dsmagadX
 */
public class BulkHostTrustTest {

    public BulkHostTrustTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    //@Test
    public void apiClientRegister() {

        String aliasName = "Admin";
        String keyPassword = "password";
        try {

            // create a new private key and certificate
            KeyPair keypair = RsaUtil.generateRsaKeyPair(RsaUtil.MINIMUM_RSA_KEY_SIZE);
            X509Certificate certificate = RsaUtil.generateX509Certificate("WLMPoratl"/*CN=WLMPortal, OU=10.1.71.85"*/, keypair, 365);
            RsaCredentialX509 credential = new RsaCredentialX509(keypair.getPrivate(), certificate);

            // create a new keystore and save the new key into it
            KeyStore keystore = KeyStore.getInstance("JKS");
            keystore.load(null, null);
            // Admin is the alias for the key that we are storing.
            // password is the encryption password for the key that we are storing int he key store
            keystore.setKeyEntry(aliasName, keypair.getPrivate(), keyPassword.toCharArray(), new X509Certificate[]{certificate});

            // Now we need to store the keystore in the location specified in the property file
            File ksFile = new File("C:\\mountwilson\\mw.jks");

            KeystoreUtil.save(keystore, keyPassword, ksFile);

            // register the new key with Mt Wilson
            Properties p = new Properties();
            p.setProperty("mtwilson.api.ssl.requireTrustedCertificate", "false");
            p.setProperty("mtwilson.api.ssl.verifyHostname", "false");

            ApiClient c = new ApiClient(new URL("https://10.1.71.81:8181"), credential, p);
            ApiClientCreateRequest me = new ApiClientCreateRequest();
            me.setCertificate(credential.getCertificate().getEncoded());
            me.setRoles(new String[]{Role.Attestation.toString(), Role.Whitelist.toString(), Role.Security.toString()});
            c.register(me);

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            fail("add host failed" + ex.getMessage());
        }
    }

    @Test
    public  void bulkHostTrustTest() {
        String aliasName = "Admin";
        String keyPassword = "password";
        String fileName = "C:\\mountwilson\\mw.jks";

        try {

            File keystoreFile = new File(fileName);
            KeyStore keystore = KeystoreUtil.open(new FileInputStream(keystoreFile), keyPassword);
            RsaCredential credential = KeystoreUtil.loadX509(keystore, aliasName, keyPassword);

            Properties p = new Properties();
            p.setProperty("mtwilson.api.ssl.requireTrustedCertificate", "false");
            p.setProperty("mtwilson.api.ssl.verifyHostname", "false");

            ApiClient rsaApiClient = new ApiClient(new URL("https://10.1.71.81:8181"), credential, p);
            
            
            
            Set<Hostname> hosts = new HashSet<Hostname>();
            
            for(int i =0 ; i< 20; i++ )
                hosts.add(new Hostname("host-" + i));
            
            BulkHostTrustResponse response = rsaApiClient.getTrustForMultipleHosts(hosts, false);

            System.out.println(response);

        } catch (Exception ex) {

            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }

    }
}
