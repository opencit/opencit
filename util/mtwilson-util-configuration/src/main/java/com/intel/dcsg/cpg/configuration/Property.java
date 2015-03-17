/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.configuration;

/**
 * Encapsulates a configuration property name and its default value. 
 * This is useful to avoid littering the code with default values everywhere
 * a property is obtained from the configuration. It can be used as an alternative
 * to declaring separate constants for property name and for property default value. 
 * 
 * <pre>
 * public class Example {
 *   public static final Property TIMEOUT_SECONDS = new Property("timeout", "60");
 *   public void run() {
 *     Configuration conf = ConfigurationFactory.getConfiguration(getClass());
 *     System.out.println(String.format("Timeout is %s", conf.getString(TIMEOUT_SECONDS)));
 *   }
 * }
 * </pre>
 * 
 * @author jbuhacoff
 */
public class Property {
    private String name;
    private String defaultValue;

    public Property(String name, String defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return name;
    }

    public String getDefaultValue() {
        return defaultValue;
    }
    
}
