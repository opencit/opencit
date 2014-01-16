/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mountwilson.wlm.client;

import com.intel.mtwilson.ApiClient;
import com.intel.mtwilson.api.*;
import com.intel.mtwilson.datatypes.OsData;
import com.intel.dcsg.cpg.crypto.HmacCredential;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.util.Properties;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import security.RegisterApiClientTest;

/**
 *
 * @author dsmagadx
 */
public class GetAllOSClientTest {
    private static URL baseurl;
    private static HmacCredential credential;
    private Logger log = LoggerFactory.getLogger(getClass());
    
    public GetAllOSClientTest() {
    }

    @BeforeClass
    public static void setUpClass() throws FileNotFoundException, MalformedURLException, IOException  {
        // look for the properties file in our java classpath since this is a test class not production code
        // there should be one properties file for each environment being tested. 
        // DO NOT change the configuration of an existing properties file without coordinating with the team
        String filename = "/mtwilson-0.5.1.properties";
        InputStream in = RegisterApiClientTest.class.getResourceAsStream(filename);
        if( in == null ) {
            throw new FileNotFoundException("Cannot find properties: "+filename);
        }
        Properties config = new Properties();
        config.load(in);
        baseurl = new URL(config.getProperty("mtwilson.api.baseurl"));
        credential = new HmacCredential(config.getProperty("mtwilson.api.clientId"), config.getProperty("mtwilson.api.secretKey"));
    }

    @AfterClass
    public static void tearDownClass()  {
    }
    
    @Before
    public void setUp() {
    }

    /**
     * Test of getOs method, of class GetAllOSClient.
     */
//    @Test
    /*
    public void testGetOs() {
        System.out.println("getOs");
        GetAllOSClient instance = new GetAllOSClient();
        
        String result = instance.getOs();
        System.out.println(result);
    }
    * 
    */
    
    @Test
    public void makeOsTest() throws ClientException, IOException, ApiException, SignatureException {
        log.debug("Connecting to {}", baseurl.toExternalForm());
        Properties p = new Properties();
        p.setProperty("mtwilson.api.ssl.requireTrustedCertificate", "false");
        p.setProperty("mtwilson.api.ssl.verifyHostname", "false");
        ApiClient c = new ApiClient(baseurl, credential, p); // KeyManagementException, [MalformedURLException], [UnsupportedEncodingException]
        
        OsData os = new OsData();
        os.setName("aaaa test");
        os.setVersion("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
        boolean success = c.addOS(os);
        System.out.println(String.format("added OS Name %s Version %s, success? %s", os.getName(), os.getVersion(), success?"yes":"no"));
    }
    
}
