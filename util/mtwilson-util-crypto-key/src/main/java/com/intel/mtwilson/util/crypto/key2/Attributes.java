/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.crypto.key2;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.intel.dcsg.cpg.io.Copyable;
import java.util.HashMap;
import java.util.Map;

/**
 * Extensible with new attributes via the attributes map and Jackson's
 * annotations JsonAnyGetter and JsonAnySetter. Note: if attributes was another
 * object instead of a map we could have used the JsonUnwrapped annotation
 * instead to flatten its attributes into this parent class.
 *
 * @author jbuhacoff
 */
public class Attributes implements Copyable {

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "type")
    protected Map<String, Object> attributes = new HashMap<>();

    @JsonAnyGetter
    public Map<String, Object> getAttributeMap() {
        return attributes;
    }

    @JsonIgnore
    public void setAttributeMap(Map<String, Object> map) {
        attributes = map;
    }
    
    @JsonAnySetter
    public void set(String key, Object value) {
        attributes.put(key, value);
    }

    public Object get(String key) {
        return attributes.get(key);
    }

    public void remove(String key) {
        attributes.remove(key);
    }

    @Override
    public Attributes copy() {
        Attributes newInstance = new Attributes();
        newInstance.copyFrom(this);
        return newInstance;
    }

    public void copyFrom(Attributes source) {
        for (String key : attributes.keySet()) {
            Object value = source.attributes.get(key);
            if (value instanceof Copyable) {
                Object copy = ((Copyable)value).copy();
                this.attributes.put(key, copy);
            } else {
                // since most objects don't implement Copyable there's still
                // a big chance here for havng a shallow copy of a list or
                // map. might be helpful to rely on a tool like xstream to
                // copy by serializing then deserializing into a new instance.
                this.attributes.put(key, value);
            }
        }
    }
}
