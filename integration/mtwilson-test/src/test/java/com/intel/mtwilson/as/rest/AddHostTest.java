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
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Properties;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author dsmagadX
 */
public class AddHostTest {

    public AddHostTest() {
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
            X509Certificate certificate = RsaUtil.generateX509Certificate("WLMPortal"/*CN=WLMPortal, OU=10.1.71.85"*/, keypair, 365);
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

            ApiClient c = new ApiClient(new URL("https://localhost:8181"), credential, p);
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

    //@Test
    public  void addHostTest() {
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

            ApiClient rsaApiClient = new ApiClient(new URL("https://localhost:8181"), credential, p);
//            List<OemData> listAllOEM = rsaApiClient.listAllOEM();
//            System.out.println();


            /*
             * MleData mleObj = new MleData(); mleObj.setName("ESXi");
             * mleObj.setVersion("5.1-12345"); mleObj.setOemName("");
             * mleObj.setOsName("VMware_ESXi"); mleObj.setOsVersion("5.1.0");
             * mleObj.setAttestationType("MODULE"); mleObj.setMleType("VMM");
             *
             * String[] biosPCRList = "17,18,19,20".split(",");
             *
             * List<ManifestData> biosMFList = new ArrayList<ManifestData>();
             * for (String biosPCR : biosPCRList){ biosMFList.add(new
             * ManifestData(biosPCR, " ")); } *
             * mleObj.setManifestList(biosMFList); boolean addMLE =
             * rsaApiClient.addMLE(mleObj);
             */
            TxtHostRecord hostObj = new TxtHostRecord();
            hostObj.HostName = "10.1.71.154";
            hostObj.Port = 0;
            hostObj.AddOn_Connection_String = "https://10.1.71.87:443/sdk;Administrator;P@ssw0rd";
            hostObj.BIOS_Name = "Intel_VMware";
            hostObj.BIOS_Oem = "Intel Corporation";
            hostObj.BIOS_Version = "s60";
            hostObj.Description = "Test";
            hostObj.Email = "";
            hostObj.IPAddress = "10.1.71.154";
            hostObj.VMM_Name = "ESXi";
            hostObj.VMM_Version = "5.1-12345";
            hostObj.VMM_OSName = "VMware_ESXi";
            hostObj.VMM_OSVersion = "5.1.0";

            TxtHost hostAddObj = new TxtHost(hostObj);
            HostResponse addHost = rsaApiClient.addHost(hostAddObj);

            System.out.println(addHost);

        } catch (Exception ex) {

            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }

    }
    
    // @Test
    public  void addOs() {
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

            ApiClient rsaApiClient = new ApiClient(new URL("https://localhost:8181"), credential, p);
            for(int i = 1; i<=1; i++ ){
                OsData osData = new OsData();

                osData.setName("Hello18");
                osData.setVersion("1.01");
                osData.setDescription("DESC Random " + System.currentTimeMillis());
                

                rsaApiClient.addOS(osData);
                }
//            rsaApiClient.getHostTrust(new Hostname("10.1.71.173"));
        } catch (Exception ex) {

            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }

    }
}
