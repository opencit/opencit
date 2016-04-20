/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.ms.business;

import com.intel.mtwilson.as.rest.v2.model.WhitelistConfigurationData;
import com.intel.mtwilson.ms.business.HostBO;
import com.intel.mtwilson.ms.MSComponentFactory;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.datatypes.HostConfigData;
import com.intel.mtwilson.datatypes.HostConfigData;
import com.intel.mtwilson.datatypes.HostConfigResponse;
import com.intel.mtwilson.datatypes.HostConfigResponseList;
import com.intel.mtwilson.datatypes.HostWhiteListTarget;
import com.intel.mtwilson.datatypes.HostVMMType;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.datatypes.TxtHostRecordList;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author ssbangal
 */
public class HostBOTest {
    
    public HostBOTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of registerHost method, of class HostBO.
     */
   @Test
    public void testRegisterHost() throws Exception {
        System.out.println("registerHost");
        TxtHostRecord hostObj = new TxtHostRecord();
        hostObj.HostName = "10.1.71.169";
        hostObj.IPAddress = "10.1.71.169";
        hostObj.Port = 9999;        
//        hostObj.HostName = "10.1.71.155";
//        hostObj.AddOn_Connection_String = "https://10.1.71.87:443/sdk;Administrator;P@ssw0rd";
        HostBO instance = MSComponentFactory.getHostBO();
        boolean expResult = true;
        boolean result = instance.registerHost(hostObj);
        assertEquals(expResult, result);
    }

    //@Test
    public void testRegisterHostWithCustomData() throws Exception {
        System.out.println("registerHost");
        TxtHostRecord hostObj = new TxtHostRecord();
        hostObj.HostName = "10.1.71.155";
        hostObj.AddOn_Connection_String = "https://10.1.71.87:443/sdk;Administrator;P@ssw0d";
        HostConfigData hostConfigObj = new HostConfigData();
        hostConfigObj.setTxtHostRecord(hostObj);
        hostConfigObj.setBiosWLTarget(HostWhiteListTarget.BIOS_HOST);
        hostConfigObj.setVmmWLTarget(HostWhiteListTarget.VMM_GLOBAL);
        HostBO instance = MSComponentFactory.getHostBO();
        boolean result = instance.registerHostFromCustomData(hostConfigObj);
        System.out.println(result);
    }

    /**
     * Test of configureWhiteListFromHost method, of class HostBO.
     */
    @Test
    public void testConfigureWhiteListFromHost() throws Exception {
        System.out.println("configureWhiteListFromHost");
        TxtHostRecord gkvHost = new TxtHostRecord();
        gkvHost.HostName = "10.1.71.155";
        gkvHost.AddOn_Connection_String = "vmware:https://10.1.71.87:443/sdk;Administrator;P@ssw0rd";
        HostBO instance = MSComponentFactory.getHostBO();
        boolean expResult = false;
        boolean result = instance.configureWhiteListFromHost(gkvHost);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
   @Test
    public void testConfigureWhiteListUsingCustomData() {       
        HostConfigData wlObj = new HostConfigData();
        TxtHostRecord gkvHost = new TxtHostRecord();
        gkvHost.HostName = "10.1.71.154";
        //gkvHost.IPAddress = "10.1.71.169";
        //gkvHost.Port = 9999;
        gkvHost.AddOn_Connection_String = "vmware:https://10.1.71.87:443/sdk;Administrator;P@ssw0rd"; //intel123!";
        wlObj.setTxtHostRecord(gkvHost);
        wlObj.setBiosPCRs("0,17");
        wlObj.setVmmPCRs("18,19,20");
        wlObj.setBiosWhiteList(true);
        wlObj.setVmmWhiteList(true);
        wlObj.setBiosWLTarget(HostWhiteListTarget.BIOS_OEM);
        wlObj.setVmmWLTarget(HostWhiteListTarget.VMM_OEM);
        wlObj.setRegisterHost(false);
        HostBO instance = MSComponentFactory.getHostBO();
        WhitelistConfigurationData newCustomObj = new WhitelistConfigurationData(wlObj);
        newCustomObj.setBiosMleName("");
        newCustomObj.setVmmMleName("");
        HostConfigData newHCData = new HostConfigData(newCustomObj);
        System.out.println(newHCData.toString());
        HostConfigData newHCData2 = newCustomObj;
        newHCData2.setBiosPCRs("0,1,2,17");
        System.out.println(newHCData2.toString());
        newCustomObj = (WhitelistConfigurationData) newHCData2;
        boolean result = instance.configureWhiteListFromCustomData(newCustomObj);
        System.out.println(result);
    }

       // @Test
       //public void testBulkHostRegistration() throws Exception {
       //         System.out.println("Bulk host registrations");
       //         TxtHostRecordList hostList = new TxtHostRecordList();
       //         TxtHostRecord hostObj = new TxtHostRecord();
       //         hostObj.HostName = "10.1.71.154";
       //         hostObj.AddOn_Connection_String = "https://10.1.71.87:443/sdk;Administrator;P@ssw0rd";
       //         hostList.getHostRecords().add(hostObj);
       //
       //         HostConfigResponseList result = MSComponentFactory.getHostBO().registerHosts(hostList);
       //         for (HostConfigResponse hostRes : result.getHostRecords()) {
       //                 System.out.println(hostRes.getHostName() + ":" + hostRes.getStatus() + ":" + hostRes.getErrorMessage());
       //         }
       //}
    
}
