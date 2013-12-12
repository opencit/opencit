/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.jpa;

/**
 * XXX not being used currently; see ASPersistenceManager, MSPersistenceManager,
 * and WLMPersistenceManager in each of those web service projects for 
 * configuration of their persistence units.
 * @author jbuhacoff
 */
public enum PersistenceUnit {
    ASDATAPU("ASDataPU","attestation-service.properties"),
    MSDATAPU("MSDataPU","management-service.properties");
    
    
    private String persistenceUnitName;
    private String configurationFileName;
    
    PersistenceUnit(String persistenceUnitName, String configurationFileName) {
        this.persistenceUnitName = persistenceUnitName;
        this.configurationFileName = configurationFileName;
    }
    
    public String getPersistenceUnitName() { return persistenceUnitName; }
    public String getConfigurationFileName() { return configurationFileName; }
    
}
