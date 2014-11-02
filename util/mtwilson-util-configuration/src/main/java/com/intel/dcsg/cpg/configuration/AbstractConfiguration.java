/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.configuration;

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
//    private HashMap<Class<?> clazz, Converter> map;
    
    public <T> T get(Class<T> objectClass, String key) {
        return get(objectClass, key, null);
    }
    public <T> T get(Class<T> objectClass, String key, T defaultValue) {
        // look for a registered converter first
        // Converter<T> converter = clazz.find(objectClss)
        // try valueOf or string consructor 
        return defaultValue;
    }
    
    public abstract void set(String key, Object newValue);
    
}
