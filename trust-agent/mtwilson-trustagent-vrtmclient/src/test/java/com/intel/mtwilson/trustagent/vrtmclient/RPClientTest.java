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
        
        TCBuffer tcBuffer = Factory.newTCBuffer(100, RPCCall.IS_VM_VERIFIED);
		
        // first replace the %s of xmlRPCBlob by VMUUID, rpcore accept all method input arguments in base64 format
        String base64InputArgument = String.format(xmlRPCBlob, DatatypeConverter.printBase64Binary(("6eb62228-00e2-4da0-ac88-d4239a78aca2").getBytes()));
        System.out.println(base64InputArgument);
        tcBuffer.setRPCPayload(base64InputArgument.getBytes());
	
        System.out.println("send");

        // create instance of RPClient, One instance of RPClient for App life time is sufficient 
        // to do processing 
        TCBuffer expResult = null;
        RPClient instance = new RPClient("10.1.70.40", 16005);
        // send tcBuffer to rpcore 
        TCBuffer result = instance.send(tcBuffer);
        
        // process response
        System.out.println("rpid = " + result.getRpId());
        System.out.println("RPC Call Index =" + result.getRPCCallIndex());
        System.out.println("RPC Payload Size = " + result.getRPCPayloadSize());
        System.out.println("RPC Call Status = " + result.getRPCCallStatus());
        System.out.println("RPC Original RP ID = " + result.getOriginalRpId());
        System.out.println("RPC Payload = " + result.getRPCPayload());
		
        // close RPClient at the end of application
        instance.close();
        
        //assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
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