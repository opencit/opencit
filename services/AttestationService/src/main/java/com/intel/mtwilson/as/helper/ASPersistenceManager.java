/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.as.helper;

import com.intel.mountwilson.as.common.ASConfig;
import com.intel.mtwilson.audit.helper.AuditConfig;
import com.intel.mtwilson.jpa.PersistenceManager;
import com.intel.mtwilson.ms.common.MSConfig;

/**
 *
 * @author jbuhacoff
 */
public class ASPersistenceManager extends PersistenceManager {

    @Override
    public void configure() {
        addPersistenceUnit("ASDataPU", ASConfig.getJpaProperties());
        addPersistenceUnit("MSDataPU", MSConfig.getJpaProperties());
        addPersistenceUnit("AuditDataPU", AuditConfig.getJpaProperties());
    }
    
}
