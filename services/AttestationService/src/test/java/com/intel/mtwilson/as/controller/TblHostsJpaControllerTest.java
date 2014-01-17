/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.controller;

import com.intel.mtwilson.as.controller.exceptions.ASDataException;
import com.intel.mountwilson.as.common.ASConfig;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.as.helper.ASPersistenceManager;

import javax.persistence.Persistence;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.intel.mtwilson.as.helper.BaseBO;
import com.intel.mtwilson.crypto.Aes128;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.jpa.PersistenceManager;
import java.io.IOException;
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
    public void testSomeMethod() throws CryptographyException, IOException {
        TblHostsJpaController jpactl = My.jpa().mwHosts();
        TblHosts host = jpactl.findByName("10.1.71.173");
        if( host == null ) {
            System.out.println("got null result");
        }
        else {
            System.out.println("got host "+host.getName());
            System.out.println("with connection "+host.getAddOnConnectionInfo());
        }
//        BaseBO config = new BaseBO();
        
        // because the persistence configuration is in the BaseBO instead of a separate configuration class, we need to instantiate BaseBO just to get the database connection info for javax.persistence
//        TblHosts tblHosts = new TblHostsJpaController(Persistence.createEntityManagerFactory("ASDataPU")).findByName("Hello");
//        TblHosts tblHosts = new TblHostsJpaController(config.getEntityManagerFactory(), null).findByName("Hello");
        
//        System.out.println(tblHosts);
    }
}
