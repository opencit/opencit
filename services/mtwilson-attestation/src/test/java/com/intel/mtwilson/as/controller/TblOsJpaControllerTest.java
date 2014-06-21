/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.controller;


import com.intel.mtwilson.My;
import com.intel.mtwilson.as.controller.exceptions.ASDataException;
import com.intel.mtwilson.as.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.as.data.TblOs;
import java.io.IOException;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.BeforeClass;

/**
 *
 * @author dsmagadx
 */
public class TblOsJpaControllerTest {
    
    public TblOsJpaControllerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }


    /**
     * Test of create method, of class TblOsJpaController.
     */
    @Test
    public void testCreate() throws NonexistentEntityException, ASDataException, IOException {
        System.out.println("create");
        TblOs tblOs = new TblOs();
        //tblOs.setId(19);
        tblOs.setName("Hell");
        tblOs.setVersion("2.21");
        
        TblOsJpaController instance = My.jpa().mwOs();
        instance.create(tblOs);
        
        tblOs.setVersion("2.22");
        instance.edit(tblOs);
        
        instance.destroy(tblOs.getId());
    }

    /**
     * Test of edit method, of class TblOsJpaController.
     */
    @Test
    public void testEdit() throws Exception {
//        System.out.println("edit");
//        TblOs tblOs = new TblOs();
//        tblOs.setId(19);
//        tblOs.setName("Hell");
//        tblOs.setVersion("2.20");
//        
//        TblOsJpaController instance = new TblOsJpaController(new BaseBO().getEntityManagerFactory());
//        instance.edit(tblOs);
        
        
    }

    /**
     * Test of destroy method, of class TblOsJpaController.
     */
    @Test
    public void testDestroy() throws Exception {
//        System.out.println("destroy");
//        Integer id = null;
//        TblOsJpaController instance = null;
//        instance.destroy(id);
//        fail("The test case is a prototype.");
    }

}
