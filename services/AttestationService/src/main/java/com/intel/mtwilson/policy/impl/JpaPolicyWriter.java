/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy.impl;

import com.intel.mtwilson.as.controller.TblLocationPcrJpaController;
import com.intel.mtwilson.as.controller.TblMleJpaController;
import com.intel.mtwilson.as.controller.TblModuleManifestJpaController;
import com.intel.mtwilson.as.controller.TblPcrManifestJpaController;
import javax.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class should be used to convert Policy or Rule objects into database records in our existing schema.
 * 
 * @author jbuhacoff
 */
public class JpaPolicyWriter {
    private Logger log = LoggerFactory.getLogger(getClass());
    
    private EntityManagerFactory entityManagerFactory;
    private TblMleJpaController mleJpaController;
    private TblPcrManifestJpaController pcrManifestJpaController;
    private TblModuleManifestJpaController moduleManifestJpaController;
    private TblLocationPcrJpaController locationPcrJpaController;

    public JpaPolicyWriter(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
        mleJpaController = new TblMleJpaController(entityManagerFactory);
        pcrManifestJpaController = new TblPcrManifestJpaController(entityManagerFactory);
        moduleManifestJpaController = new TblModuleManifestJpaController(entityManagerFactory);
        locationPcrJpaController = new TblLocationPcrJpaController(entityManagerFactory);
    }
    
}
