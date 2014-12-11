/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * TODO:  both CachingCompositeConfiguration and KeyTransformerConfiguration
 * in addition to the other configuration implementations make it obvious that
 * the interface does not scale well - adding a new configuration method, for
 * example byte[] requires adding a corresponding method to every implementation
 * even though the behavior is generally the same.  The configuration 
 * interface should be simply one get and one set method with signatures
 * like getObject and setObject and there should
 * be a registered converter for every data type including primitive types
 * so that even these can be customized and applications can write convenience
 * wrappers around configuration for any data type they need to store; they
 * can already do this with xstream or with a subclass but they also have to
 * handle all the primitive types and this is needlessly repetitive. 
 * Registration should be per instance of the configuration class and 
 * configuration factories can encapsulate common configurations.
 *
 * For built-in types we can either create ObjectPropertyCodec instances
 * that do valueOf and toString on most of them and have one codec per type
 * so it can be replaced; or we can have one converter that handles many
 * built-in types, or we can use introspection to look for valueOf and String constructor,
 * for deserialization and always use toString for serialization which would
 * automatically handle many types in the same way jax-ws-rs does it but 
 * probably it will be somewhat slower.  or maybe a combination of these,
 * using registered converters first, including xstream if something was
 * explicitly serialized with it, then an automatic introspection conversion.
 *
 * @author jbuhacoff
 */
public abstract class AbstractConfiguration implements Configuration {

    /*
    @Override
    public String get(Property property) {
        return get(property.getName(), property.getDefaultValue());
    }
    
    @Override
    public void set(Property property, String value) {
        set(property.getName(), value);
    }
    */
    
    @Override
    public String get(String key) {
        return get(key, null);
    }
    
    /**
     * Any changes to the returned Map will NOT affect the configuration
     * instance.
     * @return a Map instance with a copy of the configuration settings
     */
    public Map<String,String> toMap() {
        HashMap<String,String> map = new HashMap<>();
        for(String key : keys()) {
            String value = get(key, null);
            if( value != null ) {
                map.put(key, value);
            }  
        }
        return map;
    }
    
    /**
     * Any changes to the returned Properties will NOT affect the configuration
     * instance.
     * @return a Properties instance with a copy of the configuration settings
     */
    public Properties toProperties() {
        Properties properties = new Properties();
        for(String key : keys()) {
            String value = get(key, null);
            if( value != null ) {
                properties.setProperty(key, value);
            }  
        }
        return properties;
    }
    
}
