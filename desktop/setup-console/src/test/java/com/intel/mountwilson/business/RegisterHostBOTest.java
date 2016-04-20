/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mountwilson.business;
/*
import com.intel.mountwilson.ah.business.RegisterHostBO;
import com.intel.mountwilson.ah.data.RegisterHostRequest;
import com.intel.mountwilson.ah.data.RegisterHostResponse;
*/
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author dsmagadX
 */
public class RegisterHostBOTest {
    
    public RegisterHostBOTest() {
    }

    @BeforeClass
    public static void setUpClass()  {
    }

    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }

    /**
     * Test of registerHost method, of class RegisterHostBO.
     */
//    @Test
//    public void testRegisterHost() {
//        System.out.println("registerHost");
//        RegisterHostRequest host = getObject();
//        RegisterHostBO instance = new RegisterHostBO();
//        RegisterHostResponse result = instance.registerHost(host);
//        
//        assertEquals(0, result.getErrorCode());
//        
//    }
//    /**
//     * Test of updateHost method, of class RegisterHostBO.
//     */
//    @Test
//    public void testUpdateHost() {
//        System.out.println("updateHost");
//        RegisterHostRequest host = getObject();
//        host.setAddonConnectionString("Updated");
//        
//        RegisterHostBO instance = new RegisterHostBO();
//        RegisterHostResponse result = instance.updateHost(host);
//        assertEquals(0, result.getErrorCode());
//    }
   /**
     * Test of deleteHost method, of class RegisterHostBO.
     */
    /*
    @Test
    public void testDeleteHost() {
        System.out.println("deleteHost");
        RegisterHostRequest host = getObject();
        RegisterHostBO instance = new RegisterHostBO();
        try {
        RegisterHostResponse result = instance.deleteHost(host);
        assertEquals(0, result.getErrorCode());
            
        } catch (Exception e) {
            e.printStackTrace();
        }
            
    }

    private RegisterHostRequest getObject() {
        RegisterHostRequest req = new RegisterHostRequest("default", "admin", "admin", "TestServer1");
        
        req.setAddonConnectionString("addon conn string");
        req.setIpAddress("10.1.71.104");
        req.setBios("S5500");
        req.setBiosBuildNo("86B");
        req.setCacheValidityMins(30);
        req.setDescription("desc");
        req.setEmailAddress("d@g.com");
        req.setPort(9999);
        req.setVMM("Xen");
        req.setVMMBuildNo("4.1.2");
        return req;
    }
*/
}
