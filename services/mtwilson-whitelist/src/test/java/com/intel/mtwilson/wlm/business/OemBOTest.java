/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.wlm.business;

import com.intel.mtwilson.wlm.business.OemBO;
import com.intel.mtwilson.datatypes.OemData;

import java.security.SecureRandom;
import java.util.List;
import java.util.Random;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author dsmagadx
 */
public class OemBOTest {
    private SecureRandom rnd = new SecureRandom();
    
    public OemBOTest() {
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

    /**
     * Test of createOs method, of class OemBO.
     */
    @Test
    public void testCreateOem() {
        System.out.println("createOem");
        OemData oemData = getOemData();
        OemBO instance = new OemBO();

        // first check if the sample oem data is already in the database
        boolean found = false;
        List<OemData> list = instance.getAllOem();
        for(OemData r : list) {
            if( r.getName() != null && r.getName().equals(oemData.getName())) {
                found = true;
            }
        }
        
        // if it's already in the database, delete it first
        if( found ) {
            instance.deleteOem(oemData.getName(),null);
        }
        
        
        String expResult = "true";
        String result = instance.createOem(oemData,null);
        assertEquals(expResult, result);
    }
    /**
     * Test of getAllOs method, of class OemBO.
     */
    @Test
    public void testGetAllOem() {
        System.out.println("getAllOem");
        OemBO instance = new OemBO();
//        int expResult = 1;
        List result = instance.getAllOem();
        assertNotNull(result); // don't check for a specific record count because this can change during testing/development
        
    }

    /**
     * Test of updateOs method, of class OemBO.
     */
    @Test
    public void testUpdateOem() {
        System.out.println("updateOem");
        OemData oemData = getOemData();
        OemBO instance = new OemBO();
        String expResult = "true";
        
        // before updating we need to make sure it is already in the database
        boolean found = false;
        List<OemData> list = instance.getAllOem();
        for(OemData r : list) {
            if( r.getName() != null && r.getName().equals(oemData.getName()) ) {
                found = true;
            }
        }        
        if( !found ) {
            instance.createOem(oemData,null);
        }
        
        String updatedDescription = "updated "+rnd.nextInt(99);
        oemData.setDescription(updatedDescription);
        String result = instance.updateOem(oemData,null);
        assertEquals(expResult, result);
        // now make sure it was updated
        boolean isUpdated = false;
        List<OemData> list2 = instance.getAllOem();
        for(OemData r : list2) {
            	if(r.getName().equals(oemData.getName()) && r.getDescription().equals(updatedDescription)) {
            		isUpdated = true;
            	}
        }        
        assertTrue(isUpdated);
    }


    /**
     * Test of deletOs method, of class OemBO.
     */
    @Test
    public void testDeleteOs() {
        System.out.println("deletOem");
        OemData oemData = getOemData();
        OemBO instance = new OemBO();
        String expResult = "true";
        
        
        // first check if the sample os data is already in the database
        boolean found = false;
        List<OemData> list = instance.getAllOem();
        for(OemData r : list) {
            if( r.getName() != null && r.getName().equals(oemData.getName())) {
                found = true;
            }
        }
        
        // if it's not in the database, we need to add it before testing the delete action
        if( !found ) {
            instance.createOem(oemData,null);
        }
        
        
        String result = instance.deleteOem(oemData.getName(),null);
        assertEquals(expResult, result);
    }
    
    public OemData getOemData(){
        return new OemData("HP", "HP");
    }
}
