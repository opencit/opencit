/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.audit.helper;

import com.intel.mtwilson.jpa.PersistenceManager;

/**
 *
 * @author dmagadiX
 */
public class AuditPersistenceManager extends PersistenceManager {

    @Override
    public void configure() {
        addPersistenceUnit("AuditDataPU", AuditConfig.getJpaProperties());
    }
    
}
