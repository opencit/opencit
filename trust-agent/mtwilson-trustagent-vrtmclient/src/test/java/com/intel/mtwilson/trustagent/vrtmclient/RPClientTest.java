/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.trustagent.vrtmclient;

import javax.xml.bind.DatatypeConverter;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author hxia5
 */
public class RPClientTest {
    
    public RPClientTest() {
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
    
    @After
    public void tearDown() {
    }

    /**
     * Test of send method, of class RPClient.
     */
    @Test
    public void testSend() throws Exception {
        String xmlRPCBlob=  "<?xml version='1.0'?>" 
                                        + "<methodCall>"
                                        + "<methodName>get_verification_status</methodName>"
                                        + 	"<params>"
                                        +		"<param>"
                                        +			"<value><string>%s</string></value>"
                                        +		"</param>"
                                        +	"</params>"
                                        + "</methodCall>";
        
        TCBuffer tcBuffer = Factory.newTCBuffer(RPCCall.IS_VM_VERIFIED);
		
        // first replace the %s of xmlRPCBlob by VMUUID, rpcore accept all method input arguments in base64 format
        String base64InputArgument = String.format(xmlRPCBlob, DatatypeConverter.printBase64Binary(("425d20e9-0132-48d7-a3d6-563775968efe").getBytes()));
        System.out.println(base64InputArgument);
        tcBuffer.setRPCPayload(base64InputArgument.getBytes());
	
        System.out.println("send");

        // create instance of RPClient, One instance of RPClient for App life time is sufficient 
        // to do processing 
        TCBuffer expResult = null;
        RPClient instance = new RPClient("10.1.71.68", 16005);
        // send tcBuffer to rpcore 
        TCBuffer result = instance.send(tcBuffer);
        
        // process response
        System.out.println("RPC Call Index =" + result.getRPCCallIndex());
        System.out.println("RPC Payload Size = " + result.getRPCPayloadSize());
        System.out.println("RPC Call Status = " + result.getRPCCallStatus());
        System.out.println("RPC Payload = " + result.getRPCPayload());
        
        // close RPClient at the end of application
        instance.close();
        
        //assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
    }

    @Test
    public void getVMAttestationReportPath2() throws Exception {
    
        String nonce = "2629d276-d7c7-4cd4-bc7d-c43628770020";
        String vmInstanceId = "39cd2294-f6da-4ffe-b83e-d4f975e83adf";
        
        RPClient client = new RPClient("10.1.71.68", 16005);
        String vmAttestationReportPath = client.getVMAttestationReportPath(vmInstanceId, nonce);
        System.out.println(vmAttestationReportPath);
        client.close();
        
    }
    
    
    /**
     * Test of close method, of class RPClient.
     */
    @Test
    public void testClose() {
        System.out.println("close");
        RPClient instance = null;
        instance.close();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}