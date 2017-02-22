package com.intel.mtwilson.trustagent.ws.v2;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.intel.mountwilson.common.TAException;
import com.intel.mountwilson.trustagent.data.TADataContext;
import com.intel.mtwilson.trustagent.model.HostInfo;

/**
 * 
 * @author zjj
 *
 */

public class HostTest {

	TADataContext context = new TADataContext();
	
	 @BeforeClass
	    public static void setUpClass() {
	    }
	    
	    @AfterClass
	    public static void tearDownClass() {
	    }
	    
	    @Before
	    public void setUp() {
	    	System.setProperty("mtwilson.application.id", "trustagent");
	    }
	    
	    @After
	    public void tearDown() {
	    }
	    
	    /*
	     * 
	     * Test of getHostInformation method, of class Host.
	     */	     
	     
	    @Test
	    public void getHostInformationTest(){
	    	
	    	
	    	Host host = new Host();
	    	try {
				HostInfo hostInfo = host.getHostInformation();
					            	    
				System.out.println("host timestamp: " + hostInfo.timestamp);
				System.out.println("host errorCode: " + hostInfo.errorCode);
				System.out.println("host errorMessage: " + hostInfo.errorMessage);
				System.out.println("host osName: " + hostInfo.osName);
				System.out.println("host osVersion: " + hostInfo.osVersion);
				System.out.println("host biosOem: " + hostInfo.biosOem);
				
				System.out.println("host biosVersion: " + hostInfo.biosVersion);
				System.out.println("host vmmName: " + hostInfo.vmmName);
				System.out.println("host vmmVersion: " + hostInfo.vmmVersion);
				System.out.println("host processorInfo: " + hostInfo.processorInfo);
				System.out.println("host hardwareUuid: " + hostInfo.hardwareUuid);
				System.out.println("host tpmVersion: " + hostInfo.tpmVersion);
				System.out.println("host pcrBanks: " + hostInfo.pcrBanks);
				
			} catch (TAException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	    
	    }
}
