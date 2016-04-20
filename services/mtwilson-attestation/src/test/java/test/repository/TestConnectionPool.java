/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package test.repository;

import com.intel.mtwilson.as.controller.TblOemJpaController;
import com.intel.mtwilson.as.data.TblOem;
import com.intel.dcsg.cpg.jpa.PersistenceManager;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * References:
 * http://stackoverflow.com/questions/11526654/how-to-use-apache-commons-dbcp-with-eclipselink-jpa-and-tomcat-7-x
 * http://www.jarvana.com/jarvana/view/org/ow2/spec/ee/ow2-jpa-2.0-spec/1.0.3/ow2-jpa-2.0-spec-1.0.3-javadoc.jar!/javax/persistence/spi/PersistenceUnitInfo.html
 * @author jbuhacoff
 */
public class TestConnectionPool {
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private final String persistenceUnitName = "ASDataPU";
    private final String databaseConfigFile = "attestation-service.properties";
    
    private Properties getJpaProperties() {
        Properties jpaProperties = new Properties();
        jpaProperties.put("javax.persistence.jdbc.driver", "com.mysql.jdbc.Driver");
        jpaProperties.put("javax.persistence.jdbc.url", "jdbc:mysql://10.1.71.80:3306/mw_as?autoReconnect=true");
        jpaProperties.put("javax.persistence.jdbc.user" , "root");
        jpaProperties.put("javax.persistence.jdbc.password", "password");
        return jpaProperties;
    }
    
    @Test
    public void testEclipseLinkNormal() throws ClassNotFoundException, IOException {
        Properties jpaProperties = getJpaProperties();
        log.debug("Loading database driver {} for persistence unit {}",  jpaProperties.getProperty("javax.persistence.jdbc.driver"), persistenceUnitName );
        Class.forName(jpaProperties.getProperty("javax.persistence.jdbc.driver"));
        EntityManagerFactory factory = Persistence.createEntityManagerFactory(persistenceUnitName,jpaProperties);
        TblOemJpaController hostsJpa = new TblOemJpaController(factory);
        List<TblOem> oemList = hostsJpa.findTblOemEntities();
        for(TblOem oem : oemList) {
            System.out.println("OEM: "+oem.getName());
        }
    }


    @Test
    public void testEclipseLinkWithCommonsPool() throws ClassNotFoundException, IOException {
        log.info("Loading database driver with commons pool");
        Properties jpaProperties = new Properties(); //getJpaProperties();
//        log.debug("Loading database driver {} for persistence unit {}", new String[] { jpaProperties.getProperty("javax.persistence.jdbc.driver"), persistenceUnitName });
//        Class.forName(jpaProperties.getProperty("javax.persistence.jdbc.driver"));
        EntityManagerFactory factory = PersistenceManager.createEntityManagerFactory(persistenceUnitName,jpaProperties);
        TblOemJpaController hostsJpa = new TblOemJpaController(factory);
        List<TblOem> oemList = hostsJpa.findTblOemEntities();
        for(TblOem oem : oemList) {
            System.out.println("OEM: "+oem.getName());
        }
    }

    
}
