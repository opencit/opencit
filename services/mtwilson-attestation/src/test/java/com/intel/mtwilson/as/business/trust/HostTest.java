package com.intel.mtwilson.as.business.trust;

import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.business.HostBO;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.as.ASComponentFactory;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.mtwilson.datatypes.*;
import com.intel.mtwilson.model.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
//import org.codehaus.jackson.map.ObjectMapper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author dsmagadx
 */
public class HostTest {
    //private static final HostTrustBO htbo = new HostTrustBO();
    private static final String knownHost = "10.1.71.154";
    private static HostBO hostBO;
    private static HostTrustBO hostTrustBO;
    private static ObjectMapper mapper = new ObjectMapper();
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    @BeforeClass
    public static void createBusinessObject()  {
        hostBO = new HostBO();
        hostTrustBO = ASComponentFactory.getHostTrustBO();
    }

    @AfterClass
    public static void releaseBusinessObject()  {
        hostBO = null;
        hostTrustBO = null;
    }

    @Test
    public void checkMatchingMLEExists () throws IOException {
        TxtHostRecord hostObj = new TxtHostRecord();
        hostObj.HostName = "10.1.71.167";
        hostObj.Port = 9999;
        hostObj.AddOn_Connection_String = ConnectionString.forIntel(hostObj.HostName, hostObj.Port).getConnectionStringWithPrefix();
        hostObj.BIOS_Name = "Intel_Corp.";
        hostObj.BIOS_Version = "01.00.T060";
        hostObj.VMM_Name = "Intel_Thurley_QEMU";
        hostObj.VMM_Version = "6.4-0.12.1";
//        hostObj.HostName = "10.1.71.154";
//        hostObj.AddOn_Connection_String = new ConnectionString("https://10.1.71.87:443/sdk;Administrator;P@ssw0rd").getConnectionStringWithPrefix();        
//        hostObj.BIOS_Name = "Intel_Corporation";
//        hostObj.BIOS_Version = "01.00.0060";
//        hostObj.VMM_Name = "Intel_Thurley_VMware_ESXi";
//        hostObj.VMM_Version = "5.1.0-799733";
        HostConfigData hostConfigObj = new HostConfigData();
        hostConfigObj.setTxtHostRecord(hostObj);
        hostConfigObj.setBiosPCRs("0,17");
        hostConfigObj.setVmmPCRs("18");
        String result = hostTrustBO.checkMatchingMLEExists(hostConfigObj);
        System.out.println(result);
    }

    @Test
    public void testGetTrustStatusOfHostNotInDB () throws IOException {
        TxtHostRecord hostObj = new TxtHostRecord();
        hostObj.HostName = "10.1.71.154";
        hostObj.AddOn_Connection_String = new ConnectionString("https://10.1.71.87:443/sdk;Administrator;P@ssw0rd").getConnectionStringWithPrefix();
        hostObj.BIOS_Name = "Dell_Inc.";
        hostObj.BIOS_Version = "6.3.0";
        hostObj.BIOS_Oem = "Intel Corporation";
        hostObj.VMM_Name = "Intel_Thurley_VMware_ESXi";
        hostObj.VMM_Version = "5.1.0-799733";
        hostObj.VMM_OSName = "VMware_ESXi";
        hostObj.VMM_OSVersion = "5.1.0";
        HostConfigData hostConfigObj = new HostConfigData();
        hostConfigObj.setTxtHostRecord(hostObj);
        
        HostResponse result = hostTrustBO.getTrustStatusOfHostNotInDBAndRegister(hostConfigObj);
        //HostResponse result = hostTrustBO.getTrustStatusOfHostNotInDBAndRegister(hostObj);
        System.out.println(result);
    }
    
    @Test
    public void testCreateTxtHostFromTblHostsRecord() throws CryptographyException, IOException, MalformedURLException {
        TblHosts tblHosts = My.jpa().mwHosts().findByName("10.1.71.149"); //ASComponentFactory.getHostBO().getHostByName(new Hostname("10.1.71.149"));
        log.debug("tblhosts addon connection string length: {}", tblHosts.getAddOnConnectionInfo() == null ? "NULL" : tblHosts.getAddOnConnectionInfo().length());
        TxtHostRecord txtHostRecord = hostTrustBO.createTxtHostRecord(tblHosts);
        log.debug("txthostrecord addon connection string length: {}", txtHostRecord.AddOn_Connection_String == null ? "NULL" : txtHostRecord.AddOn_Connection_String.length());
        
        // now create a TxtHost from the TxtHostRecord
        TxtHost txtHost = new TxtHost(txtHostRecord);
        log.debug("txthost addon connection string length: {}", txtHost.getAddOn_Connection_String() == null ? "NULL" : txtHost.getAddOn_Connection_String().length());        
    }

    /**
     * The known host 10.1.71.103 should have trust status BIOS:0,VMM:0
     * This is not a good unit test for AS, should be moved to integration test project
     */
    @Test
    public void testGetTrustStatusForKnownHost() throws IOException {
        HostTrustBO htbo = ASComponentFactory.getHostTrustBO();
        HostTrustStatus response = htbo.getTrustStatus(new Hostname(knownHost));
        System.out.println("testGetTrustStatusForKnownHost response bios: "+response.bios+" vmm: "+response.vmm);
//        assertTrue("BIOS:0,VMM:0".equals(response));
		// XXX MERGE WARNING
        //String saml = htbo.getTrustWithSaml(knownHost);
        //System.out.println("saml: "+saml);
        String saml = htbo.getTrustWithSaml(knownHost, false);
        System.out.println("saml: "+saml);
    }

        @Test
    public void testGetTrustStatusForKnownHostWithForceVerify() throws IOException {
        HostTrustBO htbo = ASComponentFactory.getHostTrustBO();
        String saml = "";
        try {
            saml = htbo.getTrustWithSaml(knownHost, true);
        } catch (ASException ae) {
            System.out.println(ae.getErrorMessage());
        }
        System.out.println("saml: "+saml);
    }

    
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
        hostBO.addHost(host2, null, null, null);
    }
    
    @Test
    public void testSplitCSV() {
        String hosts = ",abc,def,,xyz ,wofj, owa,,";
        // this apepars in our bulk host trust code:
                Set<String> hostSet = new HashSet<String>();
                for(String host : Arrays.asList(hosts.split(","))) {
            log.debug("Host: '{}'", host);
            if( !host.trim().isEmpty() ) {
                hostSet.add(host.trim());
            }
                    
                }
        for(String host : hostSet) {
            log.debug("Added Host: '{}'", host);
        }
    }
}
