/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.wlm.business;

import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.datatypes.OsData;
import com.intel.dcsg.cpg.crypto.RandomUtil;
import java.security.SecureRandom;
import java.util.List;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author dsmagadx
 */
public class OsBOTest {
    private SecureRandom rnd = RandomUtil.getSecureRandom();

    public OsBOTest() {
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
     * Test of createOs method, of class OsBO.
     */
    @Test
    public void testCreateOs() {
        System.out.println("createOs");
        OsData osData = getOsData();
        OsBO instance = new OsBO();

        // first check if the sample os data is already in the database
        boolean found = false;
        List<OsData> list = instance.getAllOs();
        for(OsData r : list) {
            if( r.getName() != null && r.getName().equals(osData.getName())) {
                found = true;
            }
        }
        
        // if it's already in the database, delete it first
        if( found ) {
            instance.deleteOs(osData.getName(), osData.getVersion(), null);
        }
        
        String expResult = "true";
        String result = instance.createOs(osData, null);
        assertEquals(expResult, result);

    }

    /**
     * Test of createOs method, of class OsBO.
     */
    @Test
    public void testCreateOsDuplicate() {
        System.out.println("createOsDuplicate");
        OsData osData = getOsData();
        OsBO instance = new OsBO();
        
        // first check if the sample os data is already in the database
        boolean found = false;
        List<OsData> list = instance.getAllOs();
        for(OsData r : list) {
            if( r.getName() != null && r.getName().equals(osData.getName())) {
                found = true;
            }
        }
        
        // if it's not in the database, we need to add it before testing the duplicate check              
        if( !found ) {
            instance.createOs(osData, null);
        }
        
        try {
            instance.createOs(osData, null);
            fail("Duplicate create test failed.");
        } catch (ASException e) {
            // success, trying to add a duplicate raised an exception
            System.out.println("... success: "+e.getErrorCode().getMessage()+": "+e.getErrorMessage()+": "+e.getMessage());
        }


        

    }

    /**
     * Test of getAllOs method, of class OsBO.
     */
    @Test
    public void testGetAllOs() {
        System.out.println("getAllOs");
        OsBO instance = new OsBO();
//        int expResult = 1;
        List result = instance.getAllOs();
        assertNotNull(result); // in proper operation, even if there are no records we should get an empty list
//        assertEquals(expResult, result.size()); // don't check for specific number of records since this changes

    }

    /**
     * Test of updateOs method, of class OsBO.
     */
    @Test
    public void testUpdateOs() {
        System.out.println("updateOs");
        OsData osData = getOsData();
        OsBO instance = new OsBO();
        String expResult = "true";
        // first we need to make sure the OS is already in the database
        boolean found = false;
        List<OsData> list = instance.getAllOs();
        for(OsData r: list) {
            if( r.getName() != null && r.getName().equals(osData.getName())) {
                found = true;
            }
        }
        if(!found) {
            instance.createOs(osData, null);
        }
        
        String updatedDescription = "updated "+rnd.nextInt(99);
        osData.setDescription(updatedDescription);
        String result = instance.updateOs(osData, null);
        assertEquals(expResult, result);
        // now check that the record was updated. since the OsBO API does not have a query for a specific instance, we just look through all of them
        boolean isUpdated = false;
        List<OsData> list2 = instance.getAllOs();
        for(OsData r: list2) {
            
            	if(r.getName().equals(osData.getName()) && r.getDescription().equals(updatedDescription)) {
            		isUpdated = true;
            	}

        }
        assertTrue(isUpdated);
    }

    /**
     * Test of updateOs method, of class OsBO.
     */
    @Test(expected=ASException.class)
    public void testUpdateNonExistentOs() {
        System.out.println("testUpdateNonExistentOs");
        OsData osData = getOsData();
        osData.setName("unknown");
        osData.setDescription("udpated");
        OsBO instance = new OsBO();
//        String expResult = "true";
        String result = instance.updateOs(osData, null); // trying to update a non-existent OS should cause an exception
        System.out.println("testUpdateNonExistentOs result is "+result);
    }

    
    /**
     * Test of deletOs method, of class OsBO.
     */
    @Test
    public void testDeleteOs() {
        System.out.println("deletOs");
        OsData osData = getOsData();
        OsBO instance = new OsBO();

        // first check if the sample os data is already in the database
        boolean found = false;
        List<OsData> list = instance.getAllOs();
        for(OsData r : list) {
            if( r.getName() != null && r.getName().equals(osData.getName())) {
                found = true;
            }
        }
        
        // if it's not in the database, we need to add it before testing the duplicate check              
        if( !found ) {
            instance.createOs(osData, null);
        }
        
        String expResult = "true";
        String result = instance.deleteOs(osData.getName(), osData.getVersion(), null);
        assertEquals(expResult, result);
    }

    private OsData getOsData() {
        return new OsData("testos", "version 1", "my description");
    }
}
