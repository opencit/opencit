/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.controller;

import com.intel.mtwilson.as.controller.exceptions.ASDataException;
import com.intel.mountwilson.as.common.ASConfig;
import com.intel.mtwilson.as.data.TblHosts;

import javax.persistence.Persistence;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.intel.mtwilson.as.helper.BaseBO;
import com.intel.mtwilson.crypto.CryptographyException;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author dsmagadx
 */
public class TblHostsJpaControllerTest {
    
    public TblHostsJpaControllerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws ASDataException {
    }

    @AfterClass
    public static void tearDownClass() throws ASDataException {
    }
    
    @Before
    public void setUp() {
    }

    @Test
    public void testSomeMethod() throws CryptographyException {
        
        BaseBO config = new BaseBO();
        
        // because the persistence configuration is in the BaseBO instead of a separate configuration class, we need to instantiate BaseBO just to get the database connection info for javax.persistence
//        TblHosts tblHosts = new TblHostsJpaController(Persistence.createEntityManagerFactory("ASDataPU")).findByName("Hello");
//        TblHosts tblHosts = new TblHostsJpaController(config.getEntityManagerFactory(), null).findByName("Hello");
        
//        System.out.println(tblHosts);
    }
}
