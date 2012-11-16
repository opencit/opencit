/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.audit.api.worker;

import com.intel.mtwilson.audit.api.AuditWorker;
import com.intel.mtwilson.audit.controller.AuditLogEntryJpaController;
import com.intel.mtwilson.audit.data.AuditLogEntry;
import com.intel.mtwilson.audit.helper.AuditHandlerException;
import com.intel.mtwilson.audit.helper.AuditPersistenceManager;
import javax.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dsmagadx
 */
public class AuditSyncWorker implements AuditWorker{
    private Logger logger = LoggerFactory.getLogger(getClass().getName());
    
    private static AuditPersistenceManager persistenceManager = new AuditPersistenceManager();

    @Override
    public void addLog(AuditLogEntry log) throws AuditHandlerException {
       logger.debug("Creating the log entry.");
        AuditLogEntryJpaController controller = new AuditLogEntryJpaController(null,getEntityManagerFactory() );
        controller.create(log);
    }
    private EntityManagerFactory getEntityManagerFactory() {
        return persistenceManager.getEntityManagerFactory("AuditDataPU");
    }
}
