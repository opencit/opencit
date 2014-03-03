/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.saml;

import com.intel.mtwilson.atag.model.AttributeOidAndValue;
import com.intel.mtwilson.datatypes.TxtHost;
import java.util.ArrayList;

/**
 * TEMPORARY class to encapsulate TxtHost and asset tag attributes until
 * the APIs are updated
 * 
 * @author jbuhacoff
 */
public class TxtHostWithAssetTag {
//    private String hostId; // host id to use in the assertion - could be a UUID, or an AIK SHA1, or an IP address, or a hardware UUID
    private TxtHost host;
    private ArrayList<AttributeOidAndValue> atags;

    public TxtHostWithAssetTag() { }
    public TxtHostWithAssetTag(TxtHost host, ArrayList<AttributeOidAndValue> atags) {
        this.host = host;
        this.atags = atags;
    }
    
    public TxtHost getHost() {
        return host;
    }

    
    public void setHost(TxtHost host) {
        this.host = host;
    }

    public ArrayList<AttributeOidAndValue> getAtags() {
        return atags;
    }

    
    public void setAtags(ArrayList<AttributeOidAndValue> atags) {
        this.atags = atags;
    }
    
    
}
