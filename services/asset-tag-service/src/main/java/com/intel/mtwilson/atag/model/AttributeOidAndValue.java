/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.model;

/**
 * XXX maybe rename to TextAttribute since we are assuming a UTF-8 string value?
 * @author jbuhacoff
 */
public class AttributeOidAndValue {
    private String oid;
    private String value;

    public AttributeOidAndValue() {
    }

    public AttributeOidAndValue(String oid, String value) {
        this.oid = oid;
        this.value = value;
    }

    public String getOid() {
        return oid;
    }

    public String getValue() {
        return value;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public void setValue(String value) {
        this.value = value;
    }
    
    
}
