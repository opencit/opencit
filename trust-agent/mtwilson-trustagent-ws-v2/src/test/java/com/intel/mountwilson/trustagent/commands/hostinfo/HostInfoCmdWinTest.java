/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mountwilson.trustagent.commands.hostinfo;

import com.intel.mountwilson.trustagent.data.TADataContext;
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
public class HostInfoCmdWinTest {
    
    public HostInfoCmdWinTest() {
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
     * Test of execute method, of class HostInfoCmdWin.
     */
    @Test
    public void testExecute() throws Exception {
        System.out.println("execute");
        
        TADataContext context = new TADataContext();
        HostInfoCmdWin instance = new HostInfoCmdWin(context);
        
        instance.execute();
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getHostUUID method, of class HostInfoCmdWin.
     */
    @Test
    public void testGetHostUUID() throws Exception {
        System.out.println("getHostUUID");
        
        TADataContext context = new TADataContext();
        HostInfoCmdWin instance = new HostInfoCmdWin(context);
        instance.getHostUUID();
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

}
