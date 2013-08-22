/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.model;

import com.intel.dcsg.cpg.io.UUID;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * XXX TODO:  rename from "Tag" to "TagDefinition"/"TagDescription" or "TagName" (works better if it didn't have a values list)?
 * 
 * References:
 * http://www.cs.berkeley.edu/~jonah/bc/org/bouncycastle/asn1/x509/Attribute.html
 * http://www.cs.berkeley.edu/~jonah/bc/org/bouncycastle/x509/X509Attribute.html
 * http://www.cs.berkeley.edu/~jonah/bc/org/bouncycastle/asn1/ASN1EncodableVector.html
 * http://www.cs.berkeley.edu/~jonah/bc/org/bouncycastle/asn1/ASN1Encodable.html
 * http://www.cs.berkeley.edu/~jonah/bc/org/bouncycastle/asn1/x509/GeneralName.html
 * http://www.cs.berkeley.edu/~jonah/bc/org/bouncycastle/asn1/x509/sigi/NameOrPseudonym.html
 * http://www.cs.berkeley.edu/~jonah/bc/org/bouncycastle/asn1/x509/DisplayText.html
 * 
 * @author jbuhacoff
 */
public class Tag {
    private long id;
    private UUID uuid;
    private String name;
    private String oid;
    private List<String> values;

    public Tag() {
    }

    public Tag(String name, String oid) {
//        this.id = 0;
        this.uuid = new UUID();
        this.name = name;
        this.oid = oid;
        this.values = new ArrayList<String>();
    }
    
    public Tag(String name, String oid, List<String> values) {
//        this.id = 0;
//        this.uuid = uuid;
        this.name = name;
        this.oid = oid;
        this.values = values;
    }

    // convenience
    public Tag(String name, String oid, String[] values) {
//        this.id = 0;
//        this.uuid = uuid;
        this.name = name;
        this.oid = oid;
        this.values = Arrays.asList(values);
    }
    
    
    public Tag(long id, UUID uuid, String name, String oid) {
        this.id = id;
        this.uuid = uuid;
        this.name = name;
        this.oid = oid;
        this.values = new ArrayList<String>();
    }
    
    public Tag(long id, UUID uuid, String name, String oid, List<String> values) {
        this.id = id;
        this.uuid = uuid;
        this.name = name;
        this.oid = oid;
        this.values = values;
    }

    public long getId() {
        return id;
    }

    public UUID getUuid() {
        return uuid;
    }
    
    

    public String getName() {
        return name;
    }

    public String getOid() {
        return oid;
    }

    public List<String> getValues() {
        return values;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    
    public void setName(String name) {
        this.name = name;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }
    
}
