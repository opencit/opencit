package com.intel.mtwilson.as.business;

import com.intel.mountwilson.as.hostmanifestreport.data.HostManifestReportType;
import com.intel.mountwilson.as.hostmanifestreport.data.ManifestType;
import com.intel.mountwilson.as.hosttrustreport.data.HostsTrustReportType;
import com.intel.mtwilson.datatypes.AttestationReport;
import com.intel.mtwilson.model.*;
import java.util.*;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author dsmagadx
 */
public class ReportsBOTest {
    
    public ReportsBOTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }

    /*
     * Expected output: 
Trust Report. Hostname: 10.1.71.103  MLE: BIOS:EPSD-55,VMM:RHEL 6.1-Xen:4.1.1  Trust status: 0
Trust Report. Hostname: 10.1.71.103  MLE: BIOS:EPSD-55,VMM:RHEL 6.1-Xen:4.1.1  Trust status: 0
     */
    @Test
    public void testTrustReport() {
        ArrayList<Hostname> hostnames = new ArrayList<Hostname>();
        hostnames.add(new Hostname("10.1.71.103"));
        HostsTrustReportType hostTrustReportType= new ReportsBO().getTrustReport(hostnames);
        List<com.intel.mountwilson.as.hosttrustreport.data.HostType> list = hostTrustReportType.getHost();
        assertTrue( list != null );
        if( list != null ) {
	        for(com.intel.mountwilson.as.hosttrustreport.data.HostType h : list) {
	            System.out.println(String.format("Trust Report. Hostname: %s  MLE: %s  Trust status: %s", h.getHostName(), h.getMLEInfo(), h.getTrustStatus()));
	            assertEquals("10.1.71.103", h.getHostName());
	            assertEquals("BIOS:EPSD-55,VMM:Xen:4.1.1", h.getMLEInfo()); // used to be: BIOS:EPSD-55,VMM:RHEL 6.1-Xen:4.1.1
	            assertEquals(Integer.valueOf(0), h.getTrustStatus());
	        }
        }
    }
    
        /*
         * Expected output:
PCR Manifest. PCR: 0  value: e3a29bd603bf9982113b696cd37af8afc58e2877   trust: 0   verified: 2012-02-09T13:39:01.000-08:00
PCR Manifest. PCR: 19  value: cdd56ce92ce515414e72d8203a30b0107717cf27   trust: 0   verified: 2012-02-09T13:39:01.000-08:00
PCR Manifest. PCR: 17  value: 014936fb8e273d53823636235b1626ab25f1c514   trust: 0   verified: 2012-02-09T13:39:01.000-08:00
PCR Manifest. PCR: 18  value: 9c65082230f792824eba1c43e3c0fa6255186577   trust: 0   verified: 2012-02-09T13:39:01.000-08:00

         */
    @Test
    public void testManifestReport() {
        HostManifestReportType hostTrustReportType= new ReportsBO().getReportManifest(new Hostname("10.1.71.103"));
        com.intel.mountwilson.as.hostmanifestreport.data.HostType host = hostTrustReportType.getHost();
        assertTrue( host != null );
        assertEquals("10.1.71.103", host==null?"":host.getName());
        if( host != null ) {
	        List<ManifestType> manifestList = host.getManifest();
	        for(ManifestType manifest : manifestList) {
	            // each manifest represents a single PCR;  so the "Name" is the PCR Number (0..23)
	            System.out.println(String.format("PCR Manifest. PCR: %s  value: %s   trust: %s   verified: %s", manifest.getName().toString(), manifest.getValue(), manifest.getTrustStatus(), manifest.getVerifiedOn().toString()));
	        }
        }
        //assertEquals(4, manifestList.size()); // don't checka ctual value in a unit test in AS, it's out of scope.
    }

    /**
     * Test of setDataEncryptionKey method, of class ReportsBO.
     */
    @Test
    public void testSetDataEncryptionKey() {
        System.out.println("setDataEncryptionKey");
        byte[] key = null;
        ReportsBO instance = new ReportsBO();
//        instance.setDataEncryptionKey(key);
        fail("The test case is a prototype.");
    }

    /**
     * Test of getTrustReport method, of class ReportsBO.
     */
    @Test
    public void testGetTrustReport() {
        System.out.println("getTrustReport");
        Collection<Hostname> hostNames = null;
        ReportsBO instance = new ReportsBO();
        HostsTrustReportType expResult = null;
        HostsTrustReportType result = instance.getTrustReport(hostNames);
        assertEquals(expResult, result);
        fail("The test case is a prototype.");
    }

    /**
     * Test of getReportManifest method, of class ReportsBO.
     */
    @Test
    public void testGetReportManifest() {
        System.out.println("getReportManifest");
        Hostname hostName = null;
        ReportsBO instance = new ReportsBO();
        HostManifestReportType expResult = null;
        HostManifestReportType result = instance.getReportManifest(hostName);
        assertEquals(expResult, result);
        fail("The test case is a prototype.");
    }

    /**
     * Test of getHostAttestationReport method, of class ReportsBO.
     */
    @Test
    public void testGetHostAttestationReport() {
        System.out.println("getHostAttestationReport");
        Hostname hostName = null;
        ReportsBO instance = new ReportsBO();
        String expResult = "";
        String result = instance.getHostAttestationReport(hostName);
        assertEquals(expResult, result);
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAttestationFailueReport method, of class ReportsBO.
     */
    @Test
    public void testGetAttestationFailueReport() {
        System.out.println("getAttestationFailueReport");
        Hostname hostName = null;
        Boolean failureOnly = false;
        ReportsBO instance = new ReportsBO();
        AttestationReport expResult = null;
        AttestationReport result = instance.getAttestationReport(hostName, failureOnly);
        assertEquals(expResult, result);
        fail("The test case is a prototype.");
    }
    
}
