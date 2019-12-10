package com.intel.mountwilson.trustagent.commands.hostinfo;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.intel.mountwilson.trustagent.data.TADataContext;


/**
 *
 * @author zjj
 */

public class HostInfoCmdTest {

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
	     * Test of execute method, of class HostInfoCmd.
	     */	    
	    @Test	    
	    public void testExecute() throws Exception {
	        System.out.println("execute");
	        
	        TADataContext context = new TADataContext();
	        HostInfoCmd cmd = new HostInfoCmd(context);
	        
	        cmd.execute();
	    }
	    
	    /**
	     * Test of getHostUUID method, of class HostInfoCmd.
	     */
	    @Test
	    public void testGetHostUUID() throws Exception {
	        System.out.println("getHostUUID");
	        
	        TADataContext context = new TADataContext();
	        HostInfoCmd cmd = new HostInfoCmd(context);
	        cmd.getHostUUID();
	    }
}
