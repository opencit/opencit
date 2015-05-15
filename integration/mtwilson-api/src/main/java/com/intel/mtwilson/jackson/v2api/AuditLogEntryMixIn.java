/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jackson.v2api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;

/**
 *
 * @author jbuhacoff
 */
public abstract class AuditLogEntryMixIn {

    @JsonProperty("id")
    public abstract Integer getRecordId();

    public abstract void setRecordId(Integer recordId);

    @JsonProperty("transaction_id")
    public abstract String getTransactionId();

    public abstract void setTransactionId(String transactionId);

    @JsonProperty("entity_id")
    public abstract Integer getEntityId();

    public abstract void setEntityId(Integer entityId);

    @JsonProperty("entity_type")
    public abstract String getEntityType();

    public abstract void setEntityType(String entityType);

    @JsonProperty("finger_print")
    public abstract String getFingerprint();

    public abstract void setFingerprint(String fingerprint);

    @JsonProperty("create_dt")
    public abstract Date getCreatedOn();

    public abstract void setCreatedOn(Date createdOn);
    
    @JsonProperty("action")
    public abstract String getAction();

    public abstract void setAction(String action);

    @JsonProperty("data")
    public abstract String getData();

    public abstract void setData(String data);
    
}
