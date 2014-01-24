/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest;

import com.intel.mtwilson.ApiClient;
import com.intel.mtwilson.KeystoreUtil;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.mtwilson.datatypes.*;
import com.intel.mtwilson.model.*;
import com.intel.dcsg.cpg.crypto.RsaCredential;
import com.intel.dcsg.cpg.crypto.RsaCredentialX509;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Properties;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author dsmagadX
 */
public class AttestationFailureReportTest {

    public AttestationFailureReportTest() {
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
            KeyPair keypair = RsaUtil.generateRsaKeyPair(1024);
            X509Certificate certificate = RsaUtil.generateX509Certificate("WLMPortal"/*CN=WLMPortal, OU=10.1.71.85"*/, keypair, 365);
            RsaCredentialX509 credential = new RsaCredentialX509(keypair.getPrivate(), certificate);

            // create a new keystore and save the new key into it
            KeyStore keystore = KeyStore.getInstance("JKS");
            keystore.load(null, null);
            // Admin is the alias for the key that we are storing.
            // password is the encryption password for the key that we are storing int he key store
            keystore.setKeyEntry(aliasName, keypair.getPrivate(), keyPassword.toCharArray(), new X509Certificate[]{certificate});

            // Now we need to store the keystore in the location specified in the property file
            File ksFile = new File("C:\\mountwilson\\mw_212.jks");

            KeystoreUtil.save(keystore, keyPassword, ksFile);
            
            

            // register the new key with Mt Wilson
            Properties p = new Properties();
            p.setProperty("mtwilson.api.ssl.requireTrustedCertificate", "false");
            p.setProperty("mtwilson.api.ssl.verifyHostname", "false");

            ApiClient c = new ApiClient(new URL("https://10.1.71.212:8181"), credential, p);
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
    public  void failureReportTest() {
        String aliasName = "Admin";
        String keyPassword = "password";
        String fileName = "C:/mountwilson/mw_212.jks";

        try {

            File keystoreFile = new File(fileName);
            KeyStore keystore = KeystoreUtil.open(new FileInputStream(keystoreFile), keyPassword);
            RsaCredential credential = KeystoreUtil.loadX509(keystore, aliasName, keyPassword);
            
            Properties p = new Properties();
            p.setProperty("mtwilson.api.ssl.requireTrustedCertificate", "false");
            p.setProperty("mtwilson.api.ssl.verifyHostname", "false");
            

            ApiClient rsaApiClient = new ApiClient(new URL("https://10.1.71.212:8181"), credential, p);           
//            rsaApiClient.getHostTrust(new Hostname("10.1.71.155"));
            System.out.println("Failures only");
            printPcrAndModulesHashes(rsaApiClient.getAttestationFailureReport(new Hostname("10.1.71.154")));    
            System.out.println("All including Failures ");
            printPcrAndModulesHashes(rsaApiClient.getAttestationReport(new Hostname("10.1.71.154")));    
            
        } catch (Exception ex) {

            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }

    }

    private void printPcrAndModulesHashes(AttestationReport report) {
        for(PcrLogReport pcr : report.getPcrLogs() ){
            System.out.println( pcr.getValue() + " - " + pcr.getWhiteListValue() + " pass " + pcr.getTrustStatus());
            for(ModuleLogReport mod: pcr.getModuleLogs()){
                System.out.println( "\t\t" + mod.getValue() + " - " + mod.getWhitelistValue() + " pass " + mod.getTrustStatus());
            }
        }
    }
}
