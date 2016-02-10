/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.as;

import com.intel.dcsg.cpg.jpa.PersistenceManager;
import com.intel.mtwilson.My;
import com.intel.mtwilson.MyPersistenceManager;
/**
 *
 * @author jbuhacoff
 */
public class ASPersistenceManager extends PersistenceManager {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ASPersistenceManager.class);
    

    @Override
    public void configure() {
        try {
        addPersistenceUnit("ASDataPU", MyPersistenceManager.getASDataJpaProperties(My.configuration())); // ASConfig.getJpaProperties());
        addPersistenceUnit("MSDataPU", MyPersistenceManager.getMSDataJpaProperties(My.configuration())); // MSConfig.getJpaProperties());
        addPersistenceUnit("AuditDataPU", MyPersistenceManager.getAuditDataJpaProperties(My.configuration())); // AuditConfig.getJpaProperties());
        }
        catch(Exception e) {
            log.error("Cannot add persistence unit: {}", e.toString(), e);
        }
    }
    
}
