/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.audit.data;

/**
 *
 * @author dsmagadx
 */
public class AuditContext {

    public AuditContext(String name, String transactionUuid, long startMilliseconds) {
        this.name = name;
        this.transactionUuid = transactionUuid;
        this.startMilliseconds = startMilliseconds;
    }
  
    private String name;
    private String transactionUuid;
    private long startMilliseconds;
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public long getStartMilliseconds() {
        return startMilliseconds;
    }

    public void setStartMilliseconds(long startMilliseconds) {
        this.startMilliseconds = startMilliseconds;
    }

    public String getTransactionUuid() {
        return transactionUuid;
    }

    public void setTransactionUuid(String transactionUuid) {
        this.transactionUuid = transactionUuid;
    }
    
}
