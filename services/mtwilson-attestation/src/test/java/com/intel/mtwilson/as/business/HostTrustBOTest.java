package com.intel.mtwilson.as.business;


import com.intel.mtwilson.My;
import com.intel.mtwilson.as.business.trust.HostTrustBO;
import com.intel.mtwilson.as.ASComponentFactory;
import com.intel.mtwilson.datatypes.HostLocation;
import com.intel.mtwilson.datatypes.HostTrustStatus;
import com.intel.mtwilson.model.*;
import com.intel.mtwilson.util.ASDataCipher;
import java.io.IOException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author dsmagadx
 */
public class HostTrustBOTest {
    //private static final HostTrustBO htbo = new HostTrustBO();
    //private static final String knownHost = "Phase2Host1";
	private static final String knownHost = "10.1.71.173";
	//private static final String knownHost = "10.1.71.108";
    
    @BeforeClass
    public static void setUpClass() {

    }

    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
   //@Test
    public void testAddHostLocation() {
        HostTrustBO hostTBO = ASComponentFactory.getHostTrustBO();
        boolean result = hostTBO.addHostLocation(new HostLocation("Folsom", "12345678"));
        System.out.println(result);
        
    }
    
//    @Test
//    public void testAikverifyhomeExists() throws IOException {
//        Configuration config = ASConfig.getConfiguration();
//        String aikverifyhome = config.getString("com.intel.mountwilson.as.home", "C:/work/aikverifyhome");
//        File aikverifyhomeFolder = new File(aikverifyhome);
//        assertTrue( aikverifyhomeFolder.exists() );
//        
//        File aikverifyhomeDataFolder = new File(aikverifyhome + File.separator + "data");
//        assertTrue( aikverifyhomeDataFolder.exists() );
//        assertTrue( aikverifyhomeDataFolder.canWrite() );
//    }

    /**
     * The known host 10.1.71.103 should have trust status BIOS:0,VMM:0
     * This is not a good test for AS, should be moved to integration test project
     */
    @Test
    public void testGetTrustStatusForKnownHost() throws IOException {
        HostTrustBO htbo = new HostTrustBO();
        HostTrustStatus response = htbo.getTrustStatus(new Hostname(knownHost));
        System.out.println("testGetTrustStatusForKnownHost response = "+response.vmm);
        
        
        //assertTrue("true".equals(response));
    }
    
    /*
    @Test
    public void testCipher() {
        My.initDataEncryptionKey("NCJcq+T0FSanxY54rUhoGw==");
        String url = ASDataCipher.cipher.decryptString("4l7d1+kkFz5degCzNFQPaXjGihmKE/0PaIHMCdMCwx20gfwj/SD+wHeJRPUpkWEIEIbtVQBG6QYwmPrx3uhejtUjpAwH1qs62G9YWCOLflg=");
        System.out.print("url = " + url);
        
    }
    */

//    @Test
//    public void testCertificateMarkers() {
//        String sampleBadCert = "-----BEGIN CERTIFICATE-----AND_NO_NEWLINES_BETWEEN_MARKERS-----END CERTIFICATE-----";
//        String sampleGoodCert = "-----BEGIN CERTIFICATE-----\nWITH_NEWLINES_BETWEEN_MARKERS\n-----END CERTIFICATE-----\n";
//        assertTrue( sampleBadCert.indexOf("-----BEGIN CERTIFICATE-----\n") < 0 && sampleBadCert.indexOf("-----BEGIN CERTIFICATE-----") >= 0 );
//        assertTrue( sampleGoodCert.indexOf("-----BEGIN CERTIFICATE-----\n") >= 0 );
//        assertTrue( sampleBadCert.indexOf("\n-----END CERTIFICATE-----") < 0 && sampleBadCert.indexOf("-----END CERTIFICATE-----") >= 0 );
//        assertTrue( sampleGoodCert.indexOf("\n-----END CERTIFICATE-----\n") >= 0 );
//    }
}
