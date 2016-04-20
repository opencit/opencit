/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.controller;

import com.intel.mtwilson.My;
import com.intel.mtwilson.as.controller.exceptions.ASDataException;

import com.intel.mtwilson.as.data.TblMle;
import java.io.IOException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author dsmagadx
 */
public class TblMleJpaControllerTest {
    private static TblMleJpaController db;
    
    public TblMleJpaControllerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws ASDataException, IOException {
        db = My.jpa().mwMle();
    }

    @AfterClass
    public static void tearDownClass() throws ASDataException {
    }
    
    @Before
    public void setUp() {
    }

    @Test
    public void findMle41() {
        /*
        TxtHost.TxtHostRecord hostinfo = new TxtHost.TxtHostRecord();
        hostinfo.HostName = "RHEL 62 KVM";
        hostinfo.IPAddress ="10.1.71.103";
        hostinfo.Port = 9999;
        hostinfo.BIOS_Name = "EPSD";
        hostinfo.BIOS_Version = "60";
        hostinfo.VMM_Name = "Xen";
        hostinfo.VMM_Version = "4.1.1";
        hostinfo.BIOS_Oem = "HP";
        hostinfo.AddOn_Connection_String = "http://example.server.com:234/vcenter/";
        hostinfo.Description = "RHEL 62 KVM Integration ENV";
        hostinfo.Email = null;
        hostinfo.VMM_OSName = "RHEL";
        hostinfo.VMM_OSVersion = "6.1";
        TxtHost host = new TxtHost(hostinfo);
         * 
         */
        TblMle mleXenExpected = db.findMleById(5);
        if (mleXenExpected != null)
        	System.out.println(mleXenExpected.getName());
        String mleName = "Xen";
        String mleVersion = "4.1.1";
        String osName = "RHEL";
        String osVersion = "6.1";
        TblMle mleXen = db.findVmmMle(mleName, mleVersion, osName, osVersion);
        if (mleXen != null)
        	System.out.println(mleXen.getName());
        
    }
    
    /**
     * Test of getEntityManager method, of class TblMleJpaController.
     */
//    @Test
//    public void testGetEntityManager() {
//        System.out.println("getEntityManager");
//        TblMleJpaController instance = null;
//        EntityManager expResult = null;
//        EntityManager result = instance.getEntityManager();
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of create method, of class TblMleJpaController.
     */
//    @Test
//    public void testCreate() {
//        System.out.println("create");
//        TblMle tblMle = null;
//        TblMleJpaController instance = null;
//        instance.create(tblMle);
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of edit method, of class TblMleJpaController.
     */
//    @Test
//    public void testEdit() throws Exception {
//        System.out.println("edit");
//        TblMle tblMle = null;
//        TblMleJpaController instance = null;
//        instance.edit(tblMle);
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of destroy method, of class TblMleJpaController.
     */
//    @Test
//    public void testDestroy() throws Exception {
//        System.out.println("destroy");
//        Integer id = null;
//        TblMleJpaController instance = null;
//        instance.destroy(id);
//        fail("The test case is a prototype.");
//    }
//
    /**
     * Test of findTblMleEntities method, of class TblMleJpaController.
     */
//    @Test
//    public void testFindTblMleEntities_0args() {
//        System.out.println("findTblMleEntities");
//        TblMleJpaController instance = null;
//        List expResult = null;
//        List result = instance.findTblMleEntities();
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of findTblMleEntities method, of class TblMleJpaController.
//     */
//    @Test
//    public void testFindTblMleEntities_int_int() {
//        System.out.println("findTblMleEntities");
//        int maxResults = 0;
//        int firstResult = 0;
//        TblMleJpaController instance = null;
//        List expResult = null;
//        List result = instance.findTblMleEntities(maxResults, firstResult);
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of findTblMle method, of class TblMleJpaController.
//     */
//    @Test
//    public void testFindTblMle() {
//        System.out.println("findTblMle");
//        Integer id = null;
//        TblMleJpaController instance = null;
//        TblMle expResult = null;
//        TblMle result = instance.findTblMle(id);
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getTblMleCount method, of class TblMleJpaController.
//     */
//    @Test
//    public void testGetTblMleCount() {
//        System.out.println("getTblMleCount");
//        TblMleJpaController instance = null;
//        int expResult = 0;
//        int result = instance.getTblMleCount();
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of findMleByNameAndVersion method, of class TblMleJpaController.
     */
//    @Test
//    public void testFindMleByNameAndVersion() {
//        System.out.println("findMleByNameAndVersion");
//        String name = "RHEL 6.1-Xen";
//        String version = "4.1.1";
//        String mleType = "VMM";
//        TblMleJpaController instance = new TblMleJpaController(Persistence.createEntityManagerFactory("ASDataPU"));
//        
//        TblMle result = instance.findMleByNameAndVersion(name, version, mleType);
//        assertNotNull(result);
//      
//    }
    
    
     /**
     * Test of findMleByNameAndVersion method, of class TblMleJpaController.
     */
    /*
    @Test
    public void testfindMleByNameSearchCriteria() {
        System.out.println("findMleByNameSearchCriteria");
        String name = "";
        TblMleJpaController instance = new TblMleJpaController(Persistence.createEntityManagerFactory("ASDataPU"));
        
        List<TblMle> result = instance.findMleByNameSearchCriteria(name);
        for(TblMle tblMle : result){
            System.out.println("ID " + tblMle.getId() + " Name " + tblMle.getName() + " OSName " + ((tblMle.getOsId()== null)? "" : tblMle.getOsId().getName()) 
                    + "  OEMName " + ((tblMle.getOemId()== null)? "" :tblMle.getOemId().getName() ));
        }
      
    }
*/
    

}
