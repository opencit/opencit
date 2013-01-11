/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.ms.helper;

import com.intel.mtwilson.ms.helper.OpenSourceVMMHelper;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author dsmagadx
 */
public class OpenSourceVMMHelperTest {
    
    public OpenSourceVMMHelperTest() {
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

    /**
     * Test of getHostDetails method, of class OpenSourceVMMHelper.
     * 
     * Sample output:
     * <host_info>
     *  <timeStamp>Thu Jan 10 13:39:31 PST 2013</timeStamp>
     *  <clientIp>fe80:0:0:0:21b:21ff:fe91:3950%2</clientIp>
     *  <errorCode>0</errorCode>
     *  <errorMessage>OK</errorMessage>
     *  <osName>Ubuntu</osName>
     *  <osVersion> 11.10</osVersion>
     *  <biosOem>Intel Corp.</biosOem>
     *  <biosVersion> S5500.86B.01.00.0060.090920111354</biosVersion>
     *  <vmmName>QEMU</vmmName>
     *  <vmmVersion>0.14.1</vmmVersion>
     * </host_info>
     */
    @Test
    public void testGetHostDetails() throws Exception {
        System.out.println("getHostDetails");
        TxtHostRecord hostObj = new TxtHostRecord();
        hostObj.IPAddress = "10.1.71.145";
        hostObj.Port = 9999;
        
        /*
        OpenSourceVMMHelper instance = new OpenSourceVMMHelper();
        try{
        TxtHostRecord result = instance.getHostDetails(hostObj);
        }catch(Exception e){
            e.printStackTrace();
        }*/
//        assertNotNull(result.VMM_Name);
//        assertNotNull(result.VMM_Version);
//        assertNotNull(result.VMM_OSName);
//        assertNotNull(result.VMM_OSVersion);
//        assertNotNull(result.BIOS_Oem);
//        assertNotNull(result.BIOS_Version);
        
    }

    /**
     * Test of getHostAttestationReport method, of class OpenSourceVMMHelper.
     */
//    @Test
    public void testGetHostAttestationReport() throws Exception {
        System.out.println("getHostAttestationReport");
        TxtHostRecord hostObj = null;
        String pcrList = "";
        OpenSourceVMMHelper instance = new OpenSourceVMMHelper();
        String expResult = "";
        String result = instance.getHostAttestationReport(hostObj, pcrList);
        assertEquals(expResult, result);
        fail("The test case is a prototype.");
    }
}
