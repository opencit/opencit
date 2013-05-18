/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mountwilson.common;

import com.intel.mtwilson.My;
import com.intel.mtwilson.jpa.PersistenceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class MCPersistenceManager extends PersistenceManager {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void configure() {
        try {
        addPersistenceUnit("ASDataPU", My.persistenceManager().getASDataJpaProperties(My.configuration().getConfiguration())); // ASConfig.getJpaProperties());
        addPersistenceUnit("MSDataPU", My.persistenceManager().getMSDataJpaProperties(My.configuration().getConfiguration())); // MSConfig.getJpaProperties());
        }
        catch(Exception e) {
            log.error("Cannot add persistence unit: {}", e.toString(), e);
        }
    }
    
}
