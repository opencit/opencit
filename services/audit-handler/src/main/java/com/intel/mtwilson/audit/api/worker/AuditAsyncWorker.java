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
//import javax.ejb.Asynchronous;
//import javax.ejb.LocalBean;
//import javax.ejb.Stateless;
import javax.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dsmagadx
 */
//@Stateless
//@LocalBean
public class AuditAsyncWorker implements AuditWorker{
    private Logger logger = LoggerFactory.getLogger(getClass().getName());
    
    private static AuditPersistenceManager persistenceManager = new AuditPersistenceManager();

    @Override
//    @Asynchronous
    public void addLog(AuditLogEntry log) throws AuditHandlerException {
        logger.debug("Creating the log entry" );
        AuditLogEntryJpaController controller = new AuditLogEntryJpaController(null,getEntityManagerFactory() );
        controller.create(log);
    }
    private EntityManagerFactory getEntityManagerFactory() {
        return persistenceManager.getEntityManagerFactory("AuditDataPU");
    }
}
