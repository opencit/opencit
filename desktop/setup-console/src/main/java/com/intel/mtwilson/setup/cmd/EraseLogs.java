/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.cmd;

import com.intel.mtwilson.as.controller.TblModuleManifestLogJpaController;
import com.intel.mtwilson.as.controller.TblSamlAssertionJpaController;
import com.intel.mtwilson.as.controller.TblTaLogJpaController;
import com.intel.mtwilson.as.controller.exceptions.IllegalOrphanException;
import com.intel.mtwilson.as.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.as.data.TblModuleManifestLog;
import com.intel.mtwilson.as.data.TblSamlAssertion;
import com.intel.mtwilson.as.data.TblTaLog;
import com.intel.mtwilson.audit.controller.AuditLogEntryJpaController;
import com.intel.mtwilson.audit.data.AuditLogEntry;
import com.intel.dcsg.cpg.console.Command;
import com.intel.mtwilson.My;
import java.io.IOException;
import java.util.List;
import javax.persistence.EntityManagerFactory;
import org.apache.commons.configuration.Configuration;

/**
 *
 * @author jbuhacoff
 */
public class EraseLogs implements Command {
    
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
        deleteAuditLogs();
        deleteModuleManifestLog();
        deleteTaLog();
        deleteSamlAssertion();
    }
    
    private void deleteAuditLogs() throws com.intel.mtwilson.audit.controller.exceptions.NonexistentEntityException, IOException {
        AuditLogEntryJpaController jpa = My.jpa().mwAuditLogEntry();
        List<AuditLogEntry> list = jpa.findAuditLogEntryEntities();
        for(AuditLogEntry record : list) {
            jpa.destroy(record.getId());
        }
    }
    
    private void deleteModuleManifestLog() throws NonexistentEntityException, IOException {
        TblModuleManifestLogJpaController jpa = My.jpa().mwModuleManifestLog();
        List<TblModuleManifestLog> list = jpa.findTblModuleManifestLogEntities();
        for(TblModuleManifestLog record : list) {
            jpa.destroy(record.getId());
        }
    }
    
    private void deleteTaLog() throws NonexistentEntityException, IllegalOrphanException, IOException {
        TblTaLogJpaController jpa = My.jpa().mwTaLog();
        List<TblTaLog> list = jpa.findTblTaLogEntities();
        for(TblTaLog record : list) {
            //record.getTblModuleManifestLogCollection(); // already deleted because we call deleteModuleManifestLog before calling this method
            jpa.destroy(record.getId());
        }
    }

    private void deleteSamlAssertion() throws NonexistentEntityException, IllegalOrphanException, IOException {
        TblSamlAssertionJpaController jpa = My.jpa().mwSamlAssertion();
        List<TblSamlAssertion> list = jpa.findTblSamlAssertionEntities();
        for(TblSamlAssertion record : list) {
            jpa.destroy(record.getId());
        }
    }


}
