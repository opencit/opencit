/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.audit.helper;

import com.intel.mtwilson.My;
import com.intel.mtwilson.MyPersistenceManager;
import com.intel.mtwilson.jpa.PersistenceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dmagadiX
 */
public class AuditPersistenceManager extends PersistenceManager {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void configure() {
        try {
        addPersistenceUnit("AuditDataPU", MyPersistenceManager.getAuditDataJpaProperties(My.configuration().getConfiguration())); // AuditConfig.getJpaProperties());
//        addPersistenceUnit("AuditDataPU", AuditConfig.getJpaProperties());
        } catch(Exception e) {
            log.error("Cannot add persistence unit: {}", e.toString(), e);
        }
     }
    
}
