/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.setup.helper;

import com.intel.mountwilson.as.common.ASConfig;
import com.intel.mtwilson.audit.helper.AuditConfig;
import com.intel.mtwilson.jpa.PersistenceManager;
import com.intel.mtwilson.ms.common.MSConfig;

/**
 *
 * @author stdalex
 */
public class SCPersistenceManager extends PersistenceManager {
    @Override
    public void configure() {
        
        addPersistenceUnit("ASDataPU", ASConfig.getJpaProperties());
        //addPersistenceUnit("MSDataPU", MSConfig.getJpaProperties());
        //addPersistenceUnit("AuditDataPU", AuditConfig.getJpaProperties());
    }
}
