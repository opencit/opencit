/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.model;

/**
 * XXX maybe rename from "AttributeOidAndValue" to "Tag" or "Attribute" since it represents one concrete (name,value) pair most
 * analogous to a physical tag ex (price,5.99) or (location,Folsom)  ?
 * 
 * XXX if we support binary tags in the future that would be a subclass of this (text) tag and would automatically 
 * decode the binary data from its text encoding
 * 
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
