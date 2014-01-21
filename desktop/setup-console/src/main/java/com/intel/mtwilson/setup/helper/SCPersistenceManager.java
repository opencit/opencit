/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.setup.helper;

import com.intel.mountwilson.as.common.ASConfig;
import com.intel.mtwilson.My;
import com.intel.mtwilson.MyPersistenceManager;
import com.intel.mtwilson.audit.helper.AuditConfig;
import com.intel.dcsg.cpg.jpa.PersistenceManager;
import com.intel.mtwilson.ms.common.MSConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author stdalex
 */
public class SCPersistenceManager extends PersistenceManager {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void configure() {
        try {
        addPersistenceUnit("ASDataPU", MyPersistenceManager.getASDataJpaProperties(My.configuration())); // ASConfig.getJpaProperties());
        addPersistenceUnit("MSDataPU", MyPersistenceManager.getMSDataJpaProperties(My.configuration())); // MSConfig.getJpaProperties());
//        addPersistenceUnit("AuditDataPU", My.persistenceManager().getAuditDataJpaProperties(My.configuration().getConfiguration())); // AuditConfig.getJpaProperties());
        }
        catch(Exception e) {
            log.error("Cannot add persistence unit: {}", e.toString(), e);
        }
        
    }
}
