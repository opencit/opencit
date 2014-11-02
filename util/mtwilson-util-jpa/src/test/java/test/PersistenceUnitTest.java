/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package test;

import com.intel.dcsg.cpg.jpa.PersistenceManager;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.spi.PersistenceUnitInfo;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class PersistenceUnitTest {
    private final Logger log = LoggerFactory.getLogger(getClass());
    
//    @Test
    public void testReadPersistenceXml() throws IOException {
        Properties jpaProperties = new Properties();
        jpaProperties.setProperty("javax.persistence.jdbc.driver", "com.mysql.jdbc.Driver");
        jpaProperties.setProperty("javax.persistence.jdbc.url", "jdbc:mysql://127.0.0.1:3306/mw_as");
        jpaProperties.setProperty("javax.persistence.jdbc.user", "root");
        jpaProperties.setProperty("javax.persistence.jdbc.password", "password");
        PersistenceUnitInfo info = PersistenceManager.getPersistenceUnitInfo("ASDataPU", jpaProperties); // from persistence.xml in our test resources folder in this project 
        log.debug("Persistence Unit Name: "+info.getPersistenceUnitName());
        log.debug("Transaction Type: "+info.getTransactionType().name());
        log.debug("Provider: "+info.getPersistenceProviderClassName());
        log.debug("Persistence Unit Name: "+StringUtils.join(info.getManagedClassNames(), ", "));
    }
    
//    @Test
    public void testUsePersistenceUnit() throws IOException {
        Properties jpaProperties = new Properties();
        jpaProperties.setProperty("javax.persistence.jdbc.driver", "com.mysql.jdbc.Driver");
        jpaProperties.setProperty("javax.persistence.jdbc.url", "jdbc:mysql://10.1.71.80:3306/mw_as");
        jpaProperties.setProperty("javax.persistence.jdbc.user", "root");
        jpaProperties.setProperty("javax.persistence.jdbc.password", "password");
        EntityManagerFactory emf = PersistenceManager.createEntityManagerFactory("ASDataPU", jpaProperties);
        EntityManager em = emf.createEntityManager();
        Query q = em.createNativeQuery("SELECT * FROM mw_oem");
        List results = q.getResultList();
        System.out.println("Got "+results.size()+" records");
        em.close();
    }
}
