/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.wlm.business;

import com.intel.mtwilson.wlm.business.OemBO;
import com.intel.mtwilson.wlm.business.OsBO;
import com.intel.mtwilson.wlm.business.MleBO;
import com.intel.mtwilson.datatypes.ManifestData;
import com.intel.mtwilson.datatypes.MleData;
import com.intel.mtwilson.datatypes.OemData;
import com.intel.mtwilson.datatypes.OsData;

import java.security.SecureRandom;
import java.util.List;
import java.util.jar.Manifest;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.ArrayList;

import java.util.Random;

/**
 *
 * @author dsmagadx
 */
public class MleBOTest {
    private SecureRandom rnd = new SecureRandom();
    
    public MleBOTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    
    private OsData os = null;
    private OsData getValidOs() {
        if( os == null ) {
            OsData tmp = new OsData("Test OS", "0.123", "Auto-generated OS record for JUnit testing");
            OsBO db = new OsBO();
            // first check if this OS is already in the database
            boolean found = false;
            List<OsData> list = db.getAllOs();
            for(OsData r : list) {
                if( r.getName() != null && r.getName().equals(tmp.getName()) ) {
                    found = true;
                    os = r;
                }
            }
            if( !found ) {
                os = tmp;
                db.createOs(os, null);                
            }
        }
        assert(os!=null);
        return os;
    }
    /*
    private OsData getInvalidOs() {
        return new OsData("Test OS #"+rnd.nextInt(9999), "3.14159", "Auto-generated invalid OS record for JUnit testing");
    }
    */

    
    private OemData oem = null;
    private OemData getValidOem() {
        if( oem == null ) {
            OemData tmp = new OemData("Test OEM", "Auto-generated OEM record for JUnit testing");
            OemBO db = new OemBO();
            // first check if this OS is already in the database
            boolean found = false;
            List<OemData> list = db.getAllOem();
            for(OemData r : list) {
                if( r.getName() != null && r.getName().equals(tmp.getName()) ) {
                    found = true;
                    oem = r;
                }
            }
            if( !found ) {
                oem = tmp;
                db.createOem(oem,null);                
            }
        }
        assert(oem!=null);
        return oem;
    }
    /*
    private OemData getInvalidOem() {
        return new OemData("Test OEM #"+rnd.nextInt(9999), "Auto-generated invalid OEM record for JUnit testing");
    }
    */
    
    // we need to have separate BIOS MLE and VMM MLE sample records because the MleBO will reject an MLE record that has both BIOS and VMM "extra" information
    private MleData getVmmMle() {
         ManifestData manifestData = new ManifestData("1", "abcd29384722656");
        ArrayList<ManifestData> manifestList = new ArrayList<ManifestData>();
        manifestList.add(manifestData);
        // we want this to work so we use the valid os
        OsData os = getValidOs();
       MleData mleData = new MleData(
                "TestMLE",
                "version1",
                MleData.MleType.VMM,
                MleData.AttestationType.PCR,
                manifestList,
                "Description",
                os.getName(),
                os.getVersion(),
                null);  // oem name must be null for vmm mle
        return mleData;
    }
    private MleData getBiosMle() {
        ManifestData manifestData = new ManifestData("2", "defa2342982342");
        ArrayList<ManifestData> manifestList = new ArrayList<ManifestData>();
        manifestList.add(manifestData);
        OemData oem = getValidOem();
        MleData mleData = new MleData(
                "TestMLE",
                "version1",
                MleData.MleType.BIOS,
                MleData.AttestationType.PCR,
                manifestList,
                "Description",
                null,  // os name must be null for bios mle
                null,  // os version must be null for bios mle
                oem.getName());
        return mleData;        
    }
    
    @Before
    public void setUp() {
    }

    /**
     * Test of addMLe method, of class MleBO.
     */
    @Test
    public void testAddMleVmm() {
        System.out.println("addMLeVmm");
        MleBO instance = new MleBO();
        String expResult = "true";
        String result = instance.addMLe(getVmmMle(), null);
        assertEquals(expResult, result);
    }

    /**
     * Test of addMLe method, of class MleBO.
     */
    @Test
    public void testAddMleBios() {
        System.out.println("addMLeBios");
        MleBO instance = new MleBO();
        String expResult = "true";
        String result = instance.addMLe(getBiosMle(), null);
        assertEquals(expResult, result);
    }

    /**
     * Test of updateMle method, of class MleBO.
     */
    @Test
    public void testUpdateMle() {
        MleData vmm = getVmmMle();
        System.out.println("updateMle");
        MleBO instance = new MleBO();
        String expResult = "true";
        
        // first make sure the MLE is in the database
        try {
            MleData originalRecord = instance.findMle(vmm.getName(), vmm.getVersion(), vmm.getOsName(), vmm.getOsVersion(), null);
            System.out.println("MLE already in database: "+originalRecord.getName());
        }
        catch(Exception e) {
            // record was not in the database, so add it with its initial description
            instance.addMLe(vmm, null);
        }
        
        String updatedDescription = vmm.getDescription() + "updated " + rnd.nextInt(99);
        vmm.setDescription(updatedDescription); 
        String result = instance.updateMle(vmm, null);
        assertEquals(expResult, result);
        // now check that it was updated
        MleData updatedRecord = instance.findMle(vmm.getName(), vmm.getVersion(), vmm.getOsName(), vmm.getOsVersion(), null);
        assertTrue(updatedRecord.getDescription() != null && updatedRecord.getDescription().equals(updatedDescription));
        // TODO: need to check BIOS MLE
    }

    /**
     * Test of queryFOrMLE method, of class MleBO.
     */
    @Test
    public void testQueryFOrMLE() {
        System.out.println("queryFOrMLE");
        MleData mleData = getVmmMle();
        String searchCriteria = mleData.getName();
        MleBO instance = new MleBO();

        List result = instance.listMles(searchCriteria);
        assertNotNull(result);
        
        if( result.isEmpty() ) {
            testAddMleVmm();
            result = instance.listMles(searchCriteria);
            assertEquals(result.size(), 1);
        }
        // if it's not empty then we know it's fine.  don't check for size because the size may be 1 or it may be more!!
    }

    /**
     * Test of GetMLEDetails method, of class MleBO.
     */
    @Test
    public void testGetMLEDetailsVmm() {
        System.out.println("getMLEDetailsVmm");
        MleBO instance = new MleBO();
        MleData vmm = getVmmMle();
        MleData resultVmm = instance.findMle(vmm.getName(), vmm.getVersion(), 
                vmm.getOsName(), vmm.getOsVersion(), null);
        assertNotNull(resultVmm);
    }

    /**
     * Test of GetMLEDetails method, of class MleBO.
     */
    @Test
    public void testGetMLEDetailsBios() {
        System.out.println("getMLEDetailsBios");
        MleBO instance = new MleBO();
        MleData bios = getBiosMle();
        MleData resultBios = instance.findMle(bios.getName(), bios.getVersion(), 
                null, null, bios.getOemName());        
        assertNotNull(resultBios);
    }
    

    @Test
    public void testDeleteMleVmm() {
        System.out.println("deleteMleVmm");
        MleBO instance = new MleBO();
        String expResult = "true";
        MleData vmm = getVmmMle();
        String resultVmm = instance.deleteMle(vmm.getName(), vmm.getVersion(), 
                vmm.getOsName(), vmm.getOsVersion(), vmm.getOemName(), null);
        assertEquals(expResult, resultVmm);
    }

    @Test
    public void testDeleteMleBios() {
        System.out.println("deleteMleBios");
        MleBO instance = new MleBO();
        String expResult = "true";
        MleData bios = getBiosMle();
        String resultBios = instance.deleteMle(bios.getName(), bios.getVersion(), 
                bios.getOsName(), bios.getOsVersion(), bios.getOemName(), null);
        assertEquals(expResult, resultBios);
    }

}
