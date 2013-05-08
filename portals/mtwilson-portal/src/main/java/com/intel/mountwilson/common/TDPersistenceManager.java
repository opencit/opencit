/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mountwilson.common;

import com.intel.mountwilson.as.common.ASConfig;
import com.intel.mtwilson.jpa.PersistenceManager;
/**
 *
 * @author jbuhacoff
 */
public class TDPersistenceManager extends PersistenceManager {

    @Override
    public void configure() {
        addPersistenceUnit("MSDataPU", TDPConfig.getJpaProperties()); // for MwPortalUser
        addPersistenceUnit("ASDataPU", ASConfig.getJpaProperties());
    }
    
}
