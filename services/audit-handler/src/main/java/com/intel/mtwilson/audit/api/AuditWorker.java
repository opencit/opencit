/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.audit.api;

import com.intel.mtwilson.audit.data.AuditLog;
import com.intel.mtwilson.audit.data.AuditLogEntry;
import com.intel.mtwilson.audit.helper.AuditHandlerException;

/**
 *
 * @author dsmagadx
 */
public interface AuditWorker {
    public void addLog(AuditLogEntry log) throws AuditHandlerException;
}
