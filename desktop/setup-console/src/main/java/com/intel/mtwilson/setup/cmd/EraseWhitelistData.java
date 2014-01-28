/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.cmd;

import com.intel.mountwilson.as.common.ASConfig;
import com.intel.mtwilson.as.controller.MwMleSourceJpaController;
import com.intel.mtwilson.as.controller.TblHostSpecificManifestJpaController;
import com.intel.mtwilson.as.controller.TblMleJpaController;
import com.intel.mtwilson.as.controller.TblModuleManifestJpaController;
import com.intel.mtwilson.as.controller.TblOemJpaController;
import com.intel.mtwilson.as.controller.TblOsJpaController;
import com.intel.mtwilson.as.controller.TblPcrManifestJpaController;
import com.intel.mtwilson.as.controller.exceptions.IllegalOrphanException;
import com.intel.mtwilson.as.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.as.data.MwMleSource;
import com.intel.mtwilson.as.data.TblHostSpecificManifest;
import com.intel.mtwilson.as.data.TblMle;
import com.intel.mtwilson.as.data.TblModuleManifest;
import com.intel.mtwilson.as.data.TblOem;
import com.intel.mtwilson.as.data.TblOs;
import com.intel.mtwilson.as.data.TblPcrManifest;
import com.intel.mtwilson.as.ASPersistenceManager;
import com.intel.mtwilson.setup.Command;
import com.intel.mtwilson.setup.SetupContext;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManagerFactory;
import org.apache.commons.configuration.Configuration;

/**
 *
 * @author jbuhacoff
 */
public class EraseWhitelistData implements Command {
    private SetupContext ctx = null;
    private ASPersistenceManager pm;
    private EntityManagerFactory em;
    
    @Override
    public void setContext(SetupContext ctx) {
        this.ctx = ctx;
    }

    private Configuration options = null;
    @Override
    public void setOptions(Configuration options) {
        this.options = options;
    }

    /**
     * Creates a new API Client in current directory, registers it with Mt Wilson (on localhost or as configured), and then checks the database for the expected record to validate that it's being created.
     * @param args
     * @throws Exception 
     */
    @Override
    public void execute(String[] args) throws Exception {
        //Configuration serviceConf = ASConfig.getConfiguration();
        pm = new ASPersistenceManager();
        em = pm.getEntityManagerFactory("ASDataPU");
        deleteMleSource();
        deleteModuleManifest();
        deletePcrManifest();
        deleteMle();
        deleteOs();
        deleteOem();
    }
    
    private void deleteMleSource() throws NonexistentEntityException {
        MwMleSourceJpaController jpa = new MwMleSourceJpaController(em);
        List<MwMleSource> list = jpa.findMwMleSourceEntities();
        for(MwMleSource record : list) {
            jpa.destroy(record.getId());
        }
    }
    
    private void deleteModuleManifest() throws NonexistentEntityException, IllegalOrphanException {
        TblModuleManifestJpaController jpa = new TblModuleManifestJpaController(em);
        TblHostSpecificManifestJpaController hsmjpa = new TblHostSpecificManifestJpaController(em);
        List<TblModuleManifest> list = jpa.findTblModuleManifestEntities();
        for(TblModuleManifest record : list) {
            Collection<TblHostSpecificManifest> hsmlist = record.getTblHostSpecificManifestCollection();
            for(TblHostSpecificManifest hsmrecord : hsmlist) {
                hsmjpa.destroy(hsmrecord.getId());
            }
            jpa.destroy(record.getId());
        }
    }
    
    private void deletePcrManifest() throws NonexistentEntityException {
        TblPcrManifestJpaController jpa = new TblPcrManifestJpaController(em);
        List<TblPcrManifest> list = jpa.findTblPcrManifestEntities();
        for(TblPcrManifest record : list) {
            jpa.destroy(record.getId());
        }
    }

    private void deleteMle() throws NonexistentEntityException, IllegalOrphanException {
        TblMleJpaController jpa = new TblMleJpaController(em);
        List<TblMle> list = jpa.findTblMleEntities();
        for(TblMle record : list) {
            jpa.destroy(record.getId());
        }
    }

    private void deleteOs() throws NonexistentEntityException, IllegalOrphanException {
        TblOsJpaController jpa = new TblOsJpaController(em);
        List<TblOs> list = jpa.findTblOsEntities();
        for(TblOs record : list) {
            jpa.destroy(record.getId());
        }
    }

    private void deleteOem() throws NonexistentEntityException, IllegalOrphanException {
        TblOemJpaController jpa = new TblOemJpaController(em);
        List<TblOem> list = jpa.findTblOemEntities();
        for(TblOem record : list) {
            jpa.destroy(record.getId());
        }
    }
}
