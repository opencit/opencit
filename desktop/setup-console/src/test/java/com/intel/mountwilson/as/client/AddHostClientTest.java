/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mountwilson.as.client;

import com.intel.mtwilson.ApiClient;
import com.intel.dcsg.cpg.crypto.HmacCredential;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

/*

import com.intel.mountwilson.as.client.registerhost.HostInput;
import com.intel.mountwilson.as.client.registerhost.AddHostClient;
import com.intel.mountwilson.common.AHException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
*/
/**
 *
 * @author dsmagadX
 */
public class AddHostClientTest {
    /*
    public AddHostClientTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass()  {
    }
    
    @Before
    public void setUp() {
    }
*/
    /**
     * Test of addHost method, of class AddHostClient.
     */
    
//    @Test
    public void testAddHost() throws Exception {
        System.out.println("addHost");
        
//        AddHostClient instance = new AddHostClient();
        /*
                String hostName,
                String ipAddress, 
                String port, 
                String bios, 
                String biosVersion, 
                String biosOem,
                String vmmName, 
                String vmmVersion,
                String vmmOsName,
                String vmmOsVersion,
                String email,
                String addonConnectionString,
                String description         * 
         */
        /*
        HostInput addHostJSON = new HostInput("XenSvr1", "10.1.71.104", "9999",
                "EPSD",//"S5500", 
                "60", //86B",
                "EPSD", //", 
                "Xen", "4.1.2",
                "RHEL","6.1", 
                "host@host.com","addon conn string" , "my desc");
        
        try {
            instance.addHost(addHostJSON);
            
        }
        catch(AHException e) {
            // You will get an exception if the host is already in the database! 
            System.out.println("Cannot add host: "+e.getErrorMessage()+" "+e.getMessage());
        }
        */
        
//        ApiClient c = new ApiClient(new URL("http://localhost:8080"), new HmacCredential("username", "password"));
        
    }
}
