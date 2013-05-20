/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.as.helper;

import com.intel.mtwilson.jpa.PersistenceManager;
import com.intel.mtwilson.My;
import com.intel.mtwilson.MyPersistenceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author jbuhacoff
 */
public class ASPersistenceManager extends PersistenceManager {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void configure() {
        try {
        addPersistenceUnit("ASDataPU", MyPersistenceManager.getASDataJpaProperties(My.configuration().getConfiguration())); // ASConfig.getJpaProperties());
        addPersistenceUnit("MSDataPU", MyPersistenceManager.getMSDataJpaProperties(My.configuration().getConfiguration())); // MSConfig.getJpaProperties());
        addPersistenceUnit("AuditDataPU", MyPersistenceManager.getAuditDataJpaProperties(My.configuration().getConfiguration())); // AuditConfig.getJpaProperties());
        }
        catch(Exception e) {
            log.error("Cannot add persistence unit: {}", e.toString(), e);
        }
    }
    
}
