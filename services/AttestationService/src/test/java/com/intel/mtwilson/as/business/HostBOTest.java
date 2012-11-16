package com.intel.mtwilson.as.business;

import com.intel.mtwilson.as.business.HostBO;
import com.intel.mtwilson.as.business.trust.HostTrustBO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.datatypes.ErrorCode;
import com.intel.mountwilson.as.common.ValidationException;
import com.intel.mtwilson.as.controller.TblHostsJpaController;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.as.helper.ASComponentFactory;
import com.intel.mtwilson.datatypes.*;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;

/**
 *
 * @author dsmagadx
 */
public class HostBOTest {
    //private static final HostTrustBO htbo = new HostTrustBO();
    private static final String knownHost = "10.1.71.149";
    private static HostBO hostBO;
    
    @BeforeClass
    public static void createBusinessObject()  {
        hostBO = new HostBO();
    }

    @AfterClass
    public static void releaseBusinessObject()  {
        hostBO = null;
    }

    /* this works
    @Test(expected = ASException.class)
    public void testAddHostNullFails() {
        hostBO.addHost(null);
    }
    */

    /* this condition not possible anymore because HostData now validates in the constructor
    @Test(expected = ASException.class)
    public void testAddHostEmptyFails() {
        hostBO.addHost(new HostData());
    }
    * 
    */

    //@Test
    public void testQueryForHosts() {
        try {
            
            HostBO hostbo = new HostBO();
            hostbo.queryForHosts("149");
            
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            System.out.println(ex.getStackTrace());
        }
    }
    
    @Test
    public void testAddHostLocation() {
        HostTrustBO hostTBO = new ASComponentFactory().getHostTrustBO();
        boolean result = hostTBO.addHostLocation(new HostLocation("Folsom", "12345678"));
        System.out.println(result);
    }
    
    @Test
    public void testAddHostInvalidBiosFails() {
        try {
            /*
            TxtHost host = new TxtHost(
        "test-host-1", // String HostName,
        null, // String IPAddress,
        null, // Integer Port,
        "Unknown BIOS", // String BIOS_Name,
        null, // String BIOS_Version,
        "Unknown VMM", // String VMM_Name,
        null, // String VMM_Version,
        null, // String AddOn_Connection_String,
        null, // String Description,
        null // String Email
                    );
                    * *
                    */
        TxtHostRecord hostinfo = new TxtHostRecord();
        hostinfo.HostName = "test-host-1";
        hostinfo.BIOS_Name = "Unknown BIOS";
        hostinfo.VMM_Name = "Unknown VMM";
        TxtHost host = new TxtHost(hostinfo);
            hostBO.addHost(host);            
            fail("Should have thrown ASException");
        }
        catch(ValidationException e) {
            System.out.println("testAddHostInvalidBiosFails: "+e.getMessage());
        }
        catch(ASException e) {
            e.printStackTrace();
//            System.out.println("testAddHostInvalidBiosFails: "+e.getSuppressed().length);
            System.out.println("testAddHostInvalidBiosFails: "+e.getMessage());
            assertEquals(ErrorCode.AS_MISSING_INPUT, e.getErrorCode());
//            assertTrue(e.getMessage().contains("BIOS")); // ??? this fails because the ASEXception "loses" the error information because of the call to super:WebApplicationException
        }/*
        catch(Exception e) {
            e.printStackTrace();
            System.out.println("testAddHostInvalidBiosFails Exception: "+e.getSuppressed().length);
            System.out.println("testAddHostInvalidBiosFails Exception: "+e.getMessage());
            assertTrue(e.getMessage().contains("BIOS"));            
        }*/
    }

    public TxtHost createKnownHost41() {
        // bios 2, vmm 6
        /*
        TxtHost host = new TxtHost(
            "RHEL 62 KVM", // String HostName,
            "10.1.71.167", // String IPAddress,
            9999, // Integer Port,
            "EPSD", // String BIOS_Name,
            "60", // String BIOS_Version,
            "RHEL 6.2-KVM", // String VMM_Name,
            "0.12.1", // String VMM_Version,
            null, // String AddOn_Connection_String,
            "RHEL 62 KVM Integration ENV", // String Description,
            null // String Email
            );
            * 
            */
        TxtHostRecord hostinfo = new TxtHostRecord();
        hostinfo.HostName = "RHEL 62 KVM";
        hostinfo.IPAddress ="10.1.71.167";
        hostinfo.Port = 9999;
        hostinfo.BIOS_Name = "EPSD";
        hostinfo.BIOS_Version = "60";
        hostinfo.BIOS_Oem = "EPSD";
        hostinfo.VMM_Name = "KVM";
        hostinfo.VMM_Version = "0.12.1";
        hostinfo.VMM_OSName = "RHEL";
        hostinfo.VMM_OSVersion = "6.2";
        hostinfo.AddOn_Connection_String = null;
        hostinfo.Description = "RHEL 62 KVM Integration ENV";
        hostinfo.Email = null;
        TxtHost host = new TxtHost(hostinfo);
        return host;
    }
    
    private boolean isRegistered(TxtHost host) {
        HostResponse response = hostBO.isHostRegistered(host.getHostName().toString());
        return response.getErrorCodeEnum().equals(ErrorCode.OK); // OK means it's registered
    }
    
    @Test
    public void testAddDuplicateHost() {
        TxtHost host = createKnownHost41();
        
        // if the host is not in the database, add it
        if( !isRegistered(host) ) {
            hostBO.addHost(host);                        
        }
        
        // now that we know this host is in the database, adding it again should throw an error
        try {
            hostBO.addHost(host);            
            fail("Should have thrown ASException");
        }
        catch(ValidationException e) {
            System.out.println("testAddDuplicateHost: "+e.getMessage());
        }
        catch(ASException e) {
            // expect a duplicate host exception, since the API does not include a "is this host registered query"
            e.printStackTrace();
//            System.out.println("testAddHostInvalidBiosFails: "+e.getSuppressed().length);
            System.out.println("testAddHostInvalidBiosFails: "+e.getMessage());
            assertEquals(ErrorCode.AS_MISSING_INPUT, e.getErrorCode());
        }
    }
        
    @Test
    public void testDeleteAndCreateKnownHost() {
        TxtHost host = createKnownHost41();
//        HostResponse registeredResponse = hostBO.isHostRegistered(host.getHostName().toString());
        if( isRegistered(host) ) {
            HostResponse deleteResponse = hostBO.deleteHost(host.getHostName());
            assertEquals(ErrorCode.OK, deleteResponse.getErrorCodeEnum());            
        }
        HostResponse addResponse = hostBO.addHost(host);        	
        assertEquals(ErrorCode.OK, addResponse.getErrorCodeEnum());
    }
    

    /**
     * The known host 10.1.71.103 should have trust status BIOS:0,VMM:0
     * This is not a good unit test for AS, should be moved to integration test project
     */
    @Test
    public void testGetTrustStatusForKnownHost() {
        HostTrustBO htbo = new ASComponentFactory().getHostTrustBO();
        HostTrustStatus response = htbo.getTrustStatus(new Hostname(knownHost));
        System.out.println("testGetTrustStatusForKnownHost response bios: "+response.bios+" vmm: "+response.vmm);
//        assertTrue("BIOS:0,VMM:0".equals(response));
        String saml = htbo.getTrustWithSaml(knownHost);
        System.out.println("saml: "+saml);
    }

    @Test
    public void testCertificateMarkers() {
        String sampleBadCert = "-----BEGIN CERTIFICATE-----AND_NO_NEWLINES_BETWEEN_MARKERS-----END CERTIFICATE-----";
        String sampleGoodCert = "-----BEGIN CERTIFICATE-----\nWITH_NEWLINES_BETWEEN_MARKERS\n-----END CERTIFICATE-----\n";
        assertTrue( sampleBadCert.indexOf("-----BEGIN CERTIFICATE-----\n") < 0 && sampleBadCert.indexOf("-----BEGIN CERTIFICATE-----") >= 0 );
        assertTrue( sampleGoodCert.indexOf("-----BEGIN CERTIFICATE-----\n") >= 0 );
        assertTrue( sampleBadCert.indexOf("\n-----END CERTIFICATE-----") < 0 && sampleBadCert.indexOf("-----END CERTIFICATE-----") >= 0 );
        assertTrue( sampleGoodCert.indexOf("\n-----END CERTIFICATE-----\n") >= 0 );
    }
    
    private static ObjectMapper mapper = new ObjectMapper();
    
    @Test
    public void testAddHost154() throws IOException {
        InputStream in = getClass().getResourceAsStream("TxtHostRecord-154.json");
        String json = IOUtils.toString(in);
        in.close();
        System.out.println(json);
        // You can either deserialize into TxtHostRecord and then create a TxtHost:
        TxtHostRecord hostRecord = mapper.readValue(json, TxtHostRecord.class);
        TxtHost host1 = new TxtHost(hostRecord);
        // Or you can deserialize a TxtHostRecord directly into TxtHost:
        TxtHost host2 = mapper.readValue(json, TxtHost.class);
        hostBO.addHost(host2);
    }
}
