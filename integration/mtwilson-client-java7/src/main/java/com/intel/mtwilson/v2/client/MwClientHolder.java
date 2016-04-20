///*
// * Copyright (C) 2014 Intel Corporation
// * All rights reserved.
// */
//package com.intel.mtwilson.v2.client;
//
//import com.intel.dcsg.cpg.configuration.Configuration;
//import com.intel.dcsg.cpg.configuration.PropertiesConfiguration;
//import com.intel.mtwilson.configuration.Configurable;
//import java.util.HashMap;
//import java.util.Properties;
//
///**
// * Provides convenient access to existing clients for specific Mt Wilson 
// * features. It stores a client configuration and automatically configures
// * feature-specific clients before returning them.
// * 
// * Using this class is not necessary - you can instantiate a feature-specific
// * client, configure it, and use it independently of any other client.
// * 
// * Subclass to add convenience methods for obtaining client instances for 
// * specific features.
// * 
// * Feature-specific clients may look for a feature-specific base URL (for 
// * example if the feature is hosted on a separate server than other features)
// * but should use mtwilson.api.url as the default is there is no feature-
// * specific pom.xml. 
// * 
// * @author jbuhacoff
// */
//public class MwClientHolder implements Configurable {
//    /**
//     * Map string(fully-qualified-class-name) -> object(instance-of-same-class)
//     */
//    private HashMap<String,Object> map = new HashMap<>();
//    private Configuration configuration;
//    
//    public MwClientHolder(Properties properties) {
//        this.configuration = new PropertiesConfiguration(properties);
//    }
//    
//    /**
//     * 
//     * @param <T>
//     * @param clazz class or interface name
//     * @param instance of the specified class
//     * 
//     */
//    public <T> void set(Class<T> clazz, T instance) {
//        map.put(clazz.getName(), instance);
//    }
//    
//    public void set(Object instance) {
//        map.put(instance.getClass().getName(), instance);
//    }
//    
//    /**
//     * Client instances for specific features must be registered before 
//     * attempting to access them with this method. 
//     * If the client instance implements Configurable, it will be automatically
//     * configured with the current configuration.
//     * 
//     * @param <T>
//     * @param clazz class or interface name
//     * @return an instance of the specified interface, or null
//     */
//    public <T> T get(Class<T> clazz) {
//        String name = clazz.getName();
//        Object instance = map.get(name);
//        if( instance == null ) {
//            return null;
//        }
//        if( instance instanceof Configurable ) {
//            ((Configurable)instance).configure(configuration);
//        }
//        return (T)instance;
//    }
//
//    @Override
//    public void configure(Configuration configuration) {
//        this.configuration = configuration;
//    }
//    
//}
