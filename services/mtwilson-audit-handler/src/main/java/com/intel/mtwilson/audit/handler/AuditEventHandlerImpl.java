/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.audit.handler;

import com.intel.mtwilson.audit.annotations.AuditIgnore;
import com.intel.mtwilson.audit.api.AuditLogger;
//import com.intel.mtwilson.audit.helper.AuditConfig;
import com.intel.mtwilson.audit.helper.AuditEntryType;
import com.intel.mtwilson.audit.helper.AuditHandlerException;
import com.intel.mtwilson.audit.data.AuditColumnData;
import com.intel.mtwilson.audit.data.AuditLog;
import com.intel.mtwilson.audit.data.AuditTableData;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Id;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
//import org.codehaus.jackson.JsonGenerationException;
//import org.codehaus.jackson.map.JsonMappingException;
//import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.persistence.config.DescriptorCustomizer;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.descriptors.DescriptorEvent;
import org.eclipse.persistence.descriptors.DescriptorEventAdapter;
import org.eclipse.persistence.queries.WriteObjectQuery;
import org.eclipse.persistence.sessions.changesets.ChangeRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NOTE:   this class is attached to ASData/MSData objects through the DescriptorCustomizer 
 * defined in audit-api;  it loads this class at runtime to attach as a listener.
 * 
 * @author dsmagadx
 */
public class AuditEventHandlerImpl extends DescriptorEventAdapter  {
    private static Logger log = LoggerFactory.getLogger(AuditEventHandlerImpl.class);
    private static boolean isAuditEnabled = true;
    private static boolean isUnchangedColumnsRequired = true;
    
    static{
        isAuditEnabled = true; // AuditConfig.isAuditEnabled();
        isUnchangedColumnsRequired = false; // AuditConfig.isUnchangedColumnsRequired();
        
        log.debug("Audit - {}",isAuditEnabled );
        log.debug("Log Unchanged Columns  - {}",isUnchangedColumnsRequired);
    }
    /*
     * Get the security context
     */

    private static ObjectMapper mapper = new ObjectMapper();

    private AuditColumnData getAuditColumnData(Column col, Field field, Object table, HashMap<String, Object> changedColumns) throws IllegalAccessException, IllegalArgumentException, SecurityException {
        AuditColumnData auditColumnData = new AuditColumnData();
        log.trace("Column: " + col.name());
        auditColumnData.setName(col.name());
        log.trace("Field: " + field.getName());
        field.setAccessible(true);
        log.trace("Value: " + field.get(table));
        auditColumnData.setValue(field.get(table));
        field.setAccessible(false);
        auditColumnData.setIsUpdated(changedColumns.containsKey(field.getName()));
        return auditColumnData;
    }

    @Override
    public void postDelete(DescriptorEvent event) {

        handleEvent(AuditEntryType.DELETE.toString(), event);

    }

    @Override
    public void postInsert(DescriptorEvent event) {
        handleEvent(AuditEntryType.CREATE.toString(), event);
    }

    @Override
    public void postUpdate(DescriptorEvent event) {
        handleEvent(AuditEntryType.UPDATE.toString(), event);

    }

    private void handleEvent(String action,
            DescriptorEvent event) {
        log.trace("Thread: {}",Thread.currentThread().getName());
        if(isAuditEnabled){
            try {

                log.trace("Action: " + action);
                log.trace("Class: " + event.getObject());
                
                AuditLog auditLog = getAuditLog(event,action);
                
                if(auditLog != null)
                    new AuditLogger().addLog(auditLog);
                
            } catch (Exception ex) {
                log.error("Error while generating json :", new AuditHandlerException(ex));
            }
        }
    }

    private AuditTableData getTableData(DescriptorEvent event) throws IllegalAccessException, IllegalArgumentException, SecurityException {
        AuditTableData auditTableData = new AuditTableData();
        Object table = event.getObject();

        HashMap<String, Object> changedColumns = getChangedColumns(event);

        for (Field field : table.getClass().getDeclaredFields()) {
            Column col;
            log.trace("Is it required to log this column {}" , field.isAnnotationPresent(AuditIgnore.class));
            if ((col = field.getAnnotation(Column.class)) != null && !field.isAnnotationPresent(AuditIgnore.class)){
                
                if(isUnchangedColumnsRequired || event.getEventCode() != 7){ // Log all columns
                     auditTableData.getColumns().add(getAuditColumnData(col, field, table, changedColumns));
                }else if(changedColumns.containsKey(field.getName())){ // log only changed colmuns
                     auditTableData.getColumns().add(getAuditColumnData(col, field, table, changedColumns));
                }
            }
        }
        return auditTableData;
    }

    private HashMap<String, Object> getChangedColumns(DescriptorEvent event) {

        HashMap<String, Object> changedColumns = new HashMap<String, Object>();
        
        if(event.getEventCode() == 7){
            WriteObjectQuery query = (WriteObjectQuery) event.getQuery();

            List<ChangeRecord> changes = query.getObjectChangeSet().getChanges();

            for (ChangeRecord change : changes) {
                log.debug("Change: " + change.getAttribute() + " " + change.getOldValue());

                changedColumns.put(change.getAttribute(), change.getOldValue());
            }
        }
        return changedColumns;
    }

    private AuditLog getAuditLog(DescriptorEvent event, String action) throws IllegalAccessException,
		    IllegalArgumentException, 
		    SecurityException, 
		    JsonGenerationException, 
		    JsonMappingException, 
		    IOException 
		    {
        AuditLog auditLog = new AuditLog();

        auditLog.setEntityType(event.getObject().getClass().getSimpleName());
        AuditTableData auditTableData = getTableData(event);
        
        if(auditTableData.getColumns().size() > 0){
            auditLog.setData(mapper.writeValueAsString(auditTableData));
            auditLog.setEntityId(getPrimaryKey(event));
            auditLog.setAction(action);
            return auditLog;
        }else{
            log.info("No Columns changed. Returning null");
            return null;
        }
    }

    private Integer getPrimaryKey(DescriptorEvent event) throws  IllegalArgumentException, IllegalAccessException {
        Integer pk = -1;
        
        Object table = event.getObject();
        for (Field field : table.getClass().getDeclaredFields()) {
            if ((field.getAnnotation(Id.class)) != null) {
                log.trace("ID Column Field: " + field.getName());
                field.setAccessible(true);
                log.trace("ID Data Type :" + field.getType() +  "value: " + field.get(table));
                pk = (Integer) field.get(table);
                field.setAccessible(false);
            }
        }
        return pk;
    }
}
