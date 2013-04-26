/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.ms.business;

import com.intel.mtwilson.ms.business.HostBO;
import com.intel.mtwilson.ms.helper.MSComponentFactory;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.datatypes.HostConfigData;
import com.intel.mtwilson.datatypes.HostWhiteListTarget;
import com.intel.mtwilson.datatypes.HostVMMType;
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
   // @Test
    public void testRegisterHost() throws Exception {
        System.out.println("registerHost");
        TxtHostRecord hostObj = new TxtHostRecord();
        hostObj.HostName = "10.1.71.154";
        hostObj.AddOn_Connection_String = "https://10.1.71.87:443/sdk;Administrator;P@ssw0rd";
        HostBO instance = new MSComponentFactory().getHostBO();
        boolean expResult = true;
        boolean result = instance.registerHost(hostObj);
        assertEquals(expResult, result);
    }

    //@Test
    public void testRegisterHostWithCustomData() throws Exception {
        System.out.println("registerHost");
        TxtHostRecord hostObj = new TxtHostRecord();
        hostObj.HostName = "10.1.71.169";
        hostObj.IPAddress = "10.1.71.169";
        hostObj.AddOn_Connection_String = "intel:https://10.1.71.169:9999";
        hostObj.Port = 9999;
        //hostObj.AddOn_Connection_String = "https://10.1.71.87:154/sdk;Administrator;P@ssw0d";
        HostConfigData hostConfigObj = new HostConfigData();
        hostConfigObj.setTxtHostRecord(hostObj);
        hostConfigObj.setBiosWLTarget(HostWhiteListTarget.BIOS_OEM);
        hostConfigObj.setVmmWLTarget(HostWhiteListTarget.VMM_OEM);
        hostConfigObj.setBiosPCRs("0");
        hostConfigObj.setVmmPCRs("17,18,19");
        HostBO instance = new MSComponentFactory().getHostBO();
        boolean result = instance.registerHostFromCustomData(hostConfigObj);
        System.out.println(result);
    }

    /**
     * Test of configureWhiteListFromHost method, of class HostBO.
     */
    //@Test
    public void testConfigureWhiteListFromHost() throws Exception {
        System.out.println("configureWhiteListFromHost");
        TxtHostRecord gkvHost = new TxtHostRecord();
        gkvHost.HostName = "10.1.71.146";
        gkvHost.AddOn_Connection_String = "https://10.1.71.87:443/sdk;Administrator;P@ssw0rd";
        HostBO instance = new MSComponentFactory().getHostBO();
        boolean expResult = false;
        boolean result = instance.configureWhiteListFromHost(gkvHost);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
   // @Test
    public void testConfigureWhiteListUsingCustomData() {
        HostConfigData wlObj = new HostConfigData();
        TxtHostRecord gkvHost = new TxtHostRecord();
        gkvHost.HostName = "10.1.71.169";
        gkvHost.IPAddress = "10.1.71.169";
        gkvHost.Port = 9999;
        //gkvHost.AddOn_Connection_String = "intel:https://10.1.71.169:9999";        
       // gkvHost.AddOn_Connection_String = "https://10.1.71.87:443/sdk;Administrator;P@ssw0rd"; //intel123!";
        wlObj.setTxtHostRecord(gkvHost);
        wlObj.setBiosPCRs("0");
        wlObj.setVmmPCRs("17,18,19");
        wlObj.setBiosWhiteList(true);
        wlObj.setVmmWhiteList(true);
        wlObj.setBiosWLTarget(HostWhiteListTarget.BIOS_OEM);
        wlObj.setVmmWLTarget(HostWhiteListTarget.VMM_OEM);
        wlObj.setRegisterHost(true);
        HostBO instance = new MSComponentFactory().getHostBO();
        boolean result = instance.configureWhiteListFromCustomData(wlObj);
        System.out.println(result);
    }

}
