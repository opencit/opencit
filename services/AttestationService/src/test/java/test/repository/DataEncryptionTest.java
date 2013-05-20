/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.repository;

import com.intel.mtwilson.My;
import com.intel.mtwilson.MyPersistenceManager;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.crypto.CryptographyException;
import com.intel.mtwilson.jpa.PersistenceManager;
import com.intel.mtwilson.model.Hostname;
import java.io.IOException;
import org.junit.Test;
import java.util.HashSet;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Properties;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;


/**
 *
 * @author jbuhacoff
 */
public class DataEncryptionTest {
    private Logger log = LoggerFactory.getLogger(getClass());
    //mtwilson.as.dek=95nBEEFshB6xrjpi5wp8Og\=\=
    @Test
    public void testDecryptionWithProbablyIncorrectKey() throws IOException, CryptographyException {
        HashSet<Hostname> hostnames = new HashSet<Hostname>();
        hostnames.add(new Hostname("10.1.71.173"));
        hostnames.add(new Hostname("10.1.71.170"));
        hostnames.add(new Hostname("10.1.71.201"));
        hostnames.add(new Hostname("10.1.71.174"));
        hostnames.add(new Hostname("10.1.71.175"));
        hostnames.add(new Hostname("10.1.71.169"));
        hostnames.add(new Hostname("10.1.71.126"));
        hostnames.add(new Hostname("RHEL8"));
        hostnames.add(new Hostname("10.1.71.172"));
        hostnames.add(new Hostname("RHEL168"));
        Properties p = MyPersistenceManager.getASDataJpaProperties(My.configuration().getConfiguration());
        p.store(System.out, "effective jpa config");
        for(Hostname hostname : hostnames) {
            TblHosts host = My.jpa().mwHosts().findByName(hostname.toString());
            if( host == null ) {
                log.debug("Host: {} is not found", hostname.toString());
            }
            else {
                log.debug("Host: {}  Connection: {}", hostname.toString(), host.getAddOnConnectionInfo());
            }
        }
    }

    @Test
    public void testUsePersistenceUnitPostgres() throws IOException {
        Properties jpaProperties = new Properties();
        jpaProperties.setProperty("javax.persistence.jdbc.driver", "org.postgresql.Driver");
        jpaProperties.setProperty("javax.persistence.jdbc.url", "jdbc:postgresql://10.1.71.227:5432/mw_as");
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
