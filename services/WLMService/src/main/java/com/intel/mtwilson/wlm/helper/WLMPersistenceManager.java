/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.wlm.helper;

import com.intel.mountwilson.as.common.ASConfig;
import com.intel.mtwilson.My;
import com.intel.mtwilson.MyPersistenceManager;

import com.intel.mtwilson.audit.helper.AuditConfig;
import com.intel.mtwilson.ms.common.MSConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * To use this class, add it to web.xml as a listener:
    <listener>
        <listener-class>com.intel.mountwilson.wlm.helper.WLMPersistenceManager</listener-class>
    </listener>
 * Sequence of actions:
 * Servlet container loads and initializes the WLMPersistenceManager listener (empty constructor)
 * Servlet container loads and initializes the REST classes
 * The REST classes create the "business objects" that provide JPA access
 * The business objects instantiate their own copy of WLMPersistenceManager
 * The business objects call getEntityManagerFactory(persistenceUnit) on their instance
 * That method is inherited from PersistenceManager and it checks if any persistence units are configured
 * On the first call they are not configured so it calls configure() in the WLMPersistenceManager instance
 * The WLMPersistenceManager instance adds the persistence units needed for this application
 * Subsequent calls to getEntityManagerFactory return the configured persistence units
 * The base class PersistenceManager keeps the factories in a concurrent hash map and they are
 * shared among all threads.
 * @author jbuhacoff
 */
public class WLMPersistenceManager extends com.intel.mtwilson.jpa.PersistenceManager {
    private static final Logger log = LoggerFactory.getLogger(WLMPersistenceManager.class);

    @Override
    public void configure() {
        log.info("WLMPersistenceManager configure() adding persistence units ASDataPU, MSDataPU and AuditDataPU");
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
