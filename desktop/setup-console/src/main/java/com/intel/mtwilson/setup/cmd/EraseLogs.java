/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.cmd;

import com.intel.mountwilson.as.common.ASConfig;
import com.intel.mtwilson.MyPersistenceManager;
import com.intel.mtwilson.as.controller.MwMleSourceJpaController;
import com.intel.mtwilson.as.controller.TblHostSpecificManifestJpaController;
import com.intel.mtwilson.as.controller.TblHostsJpaController;
import com.intel.mtwilson.as.controller.TblMleJpaController;
import com.intel.mtwilson.as.controller.TblModuleManifestJpaController;
import com.intel.mtwilson.as.controller.TblModuleManifestLogJpaController;
import com.intel.mtwilson.as.controller.TblOemJpaController;
import com.intel.mtwilson.as.controller.TblOsJpaController;
import com.intel.mtwilson.as.controller.TblPcrManifestJpaController;
import com.intel.mtwilson.as.controller.TblSamlAssertionJpaController;
import com.intel.mtwilson.as.controller.TblTaLogJpaController;
import com.intel.mtwilson.as.controller.exceptions.IllegalOrphanException;
import com.intel.mtwilson.as.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.as.data.MwMleSource;
import com.intel.mtwilson.as.data.TblHostSpecificManifest;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.as.data.TblMle;
import com.intel.mtwilson.as.data.TblModuleManifest;
import com.intel.mtwilson.as.data.TblModuleManifestLog;
import com.intel.mtwilson.as.data.TblOem;
import com.intel.mtwilson.as.data.TblOs;
import com.intel.mtwilson.as.data.TblPcrManifest;
import com.intel.mtwilson.as.data.TblSamlAssertion;
import com.intel.mtwilson.as.data.TblTaLog;
import com.intel.mtwilson.as.helper.ASPersistenceManager;
import com.intel.mtwilson.audit.controller.AuditLogEntryJpaController;
import com.intel.mtwilson.audit.data.AuditLogEntry;
import com.intel.mtwilson.crypto.CryptographyException;
import com.intel.mtwilson.ms.common.MSConfig;
import com.intel.mtwilson.setup.Command;
import com.intel.mtwilson.setup.SetupContext;
import com.intel.mtwilson.setup.SetupException;
import com.intel.mtwilson.setup.SetupWizard;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManagerFactory;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.Configuration;

/**
 *
 * @author jbuhacoff
 */
public class EraseLogs implements Command {
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
        Configuration serviceConf = ASConfig.getConfiguration();
        pm = new ASPersistenceManager();
        em = pm.getEntityManagerFactory("ASDataPU");
        deleteAuditLogs();
        deleteModuleManifestLog();
        deleteTaLog();
        deleteSamlAssertion();
    }
    
    private void deleteAuditLogs() throws com.intel.mtwilson.audit.controller.exceptions.NonexistentEntityException {
        AuditLogEntryJpaController jpa = new AuditLogEntryJpaController(pm.getEntityManagerFactory("AuditDataPU"));
        List<AuditLogEntry> list = jpa.findAuditLogEntryEntities();
        for(AuditLogEntry record : list) {
            jpa.destroy(record.getId());
        }
    }
    
    private void deleteModuleManifestLog() throws NonexistentEntityException {
        TblModuleManifestLogJpaController jpa = new TblModuleManifestLogJpaController(em);
        List<TblModuleManifestLog> list = jpa.findTblModuleManifestLogEntities();
        for(TblModuleManifestLog record : list) {
            jpa.destroy(record.getId());
        }
    }
    
    private void deleteTaLog() throws NonexistentEntityException, IllegalOrphanException {
        TblTaLogJpaController jpa = new TblTaLogJpaController(em);
        List<TblTaLog> list = jpa.findTblTaLogEntities();
        for(TblTaLog record : list) {
            //record.getTblModuleManifestLogCollection(); // already deleted because we call deleteModuleManifestLog before calling this method
            jpa.destroy(record.getId());
        }
    }

    private void deleteSamlAssertion() throws NonexistentEntityException, IllegalOrphanException {
        TblSamlAssertionJpaController jpa = new TblSamlAssertionJpaController(em);
        List<TblSamlAssertion> list = jpa.findTblSamlAssertionEntities();
        for(TblSamlAssertion record : list) {
            jpa.destroy(record.getId());
        }
    }


}
