/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.audit.data;

import java.util.Date;

/**
 *
 * @author dsmagadx
 */
public class AuditLog {
    
    private Integer entityId;
    private String entityType;
    private Integer apiClientId;
    private String action;
    private String data;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Integer getApiClientId() {
        return apiClientId;
    }

    public void setApiClientId(Integer apiClientId) {
        this.apiClientId = apiClientId;
    }

 

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Integer getEntityId() {
        return entityId;
    }

    public void setEntityId(Integer entityId) {
        this.entityId = entityId;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }
   
}
