/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.controller;

import com.intel.mtwilson.as.controller.exceptions.ASDataException;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.data.TblHosts;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.intel.dcsg.cpg.crypto.CryptographyException;
//import static com.intel.mtwilson.as.controller.TblHostsJpaController.findByName;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dsmagadx
 */
public class TblHostsJpaControllerTest {
    private final Logger log = LoggerFactory.getLogger(TblHostsJpaControllerTest.class);
    
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
    
    @Test
    public void testMultiThreadedHostsTableAccess() throws IOException, CryptographyException {
        ExecutorService scheduler = Executors.newFixedThreadPool(100);
        TblHostsJpaController jpactl = My.jpa().mwHosts();
        try {
            Set<String> hosts = new HashSet<>(Arrays.asList(
                    "mh-kvm-42", "mh-kvm-45", "mh-kvm-61", "mh-kvm-66",
                    "mh-kvm-68", "mh-kvm-72", "mh-kvm-73", "mh-kvm-74",
                    "mh-kvm-76", "mh-kvm-77", "mh-kvm-78", "mh-kvm-79"));
            Set<GetHostFromDB> tasks = new HashSet<>();
            ArrayList<Future<?>> taskStatus = new ArrayList<>();
            for(String host : hosts) {
                GetHostFromDB task = new GetHostFromDB(host, jpactl);
                tasks.add(task);
                Future<?> status = scheduler.submit(task);
                taskStatus.add(status);
            }
            
            for (Future<?> status : taskStatus) {
                try {
                    status.get(10, TimeUnit.SECONDS);
                } catch (Exception e) {
                    log.error("Exception while retrieving the status of the tasks", e);
                }
            }
            
            for(GetHostFromDB task : tasks) {
                if( task.getResult() != null ) {
                    log.info("RESULT: {} {}", task.getResult().getName(), task.getResult().getHardwareUuid());
                }
            }
        } catch (Exception e) {
            log.debug("exception", e);
        }

     }
    
    public class GetHostFromDB implements Runnable {
        private final Logger log = LoggerFactory.getLogger(GetHostFromDB.class);
        private final String host;
        private final TblHostsJpaController tblHostsJpaController;
        private TblHosts tblHosts;

        GetHostFromDB(String host, TblHostsJpaController tblHostsJpaController) {
            this.host = host;
            this.tblHostsJpaController = tblHostsJpaController;
        }

        @Override
        public void run() {
            log.debug("Starting to retrieve host {} from database...", host);
            tblHosts = tblHostsJpaController.findByName(host);
            log.debug("Retrieved host {} from database.", host);
        }
        
        public TblHosts getResult() { return tblHosts; }
    }
}
