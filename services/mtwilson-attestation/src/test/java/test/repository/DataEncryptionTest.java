/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.repository;

import com.intel.mtwilson.My;
import com.intel.mtwilson.MyPersistenceManager;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.jpa.PersistenceManager;
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
        Properties p = MyPersistenceManager.getASDataJpaProperties(My.configuration());
        p.store(System.out, "effective jpa config");
        log.debug("dek is {}", My.configuration().getDataEncryptionKeyBase64());
        List<TblHosts> hosts = My.jpa().mwHosts().findTblHostsEntities();
        for(TblHosts host : hosts) {
            log.debug("Host: {}  Connection: {}", host.getName(), host.getAddOnConnectionInfo());
        }
    }

    @Test
    public void testUsePersistenceUnitPostgres() throws IOException {
        Properties jpaProperties = new Properties();
        jpaProperties.setProperty("javax.persistence.jdbc.driver", "org.postgresql.Driver");
        jpaProperties.setProperty("javax.persistence.jdbc.url", "jdbc:postgresql://10.1.71.88:5432/mw_as");
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
