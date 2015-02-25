/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.configuration;

import com.intel.mtwilson.text.transform.AllCapsNamingStrategy;
import java.util.Iterator;
import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.EnvironmentConfiguration;

/**
 * Automatically tries an ALL_CAPS version of a preference name.
 * For example, the preferenec mtwilson.ssl.required may be set as a JVM
 * property or in a configuration file, but as an environment variable it 
 * is most likely to be called MTWILSON_SSL_REQUIRED. 
 * 
 * This Configuration implementation automatically converts dot.names to ALL_CAPS
 * to find them in the environment. It wraps the Apache Commons EnvironmentConfiguration.
 * 
 * Because the EnvironmentConfiguration does not support modifying any of the
 * environment variables, this class does not support that either.
 * 
 * Recommended usage:  Use a CompositeConfiguration, add EnvironmentConfiguration to it first and then add AllCapsEnvironmentConfiguration.
 * That way, if you access environment variables that do exist you will get them without a performance penalty. But, 
 * if you use dot.notation and they exist as ALL_CAPS instead of the dot.notation this class will find them for you.
 * 
 * @author jbuhacoff
 */
public class CommonsAllCapsEnvironmentConfiguration extends AbstractConfiguration {
    private EnvironmentConfiguration env;
    private AllCapsNamingStrategy capitalizer = new AllCapsNamingStrategy();
    
    public CommonsAllCapsEnvironmentConfiguration() {
        env = new EnvironmentConfiguration();
    }
    
    /**
     * The transformation of dot.camelCase to ALL_CAPS:
     * All letters are uppercased.
     * Dots are converted to underscores.
     * CamelCase is converted to SEPARATE_WORDS.
     * @param propertyName
     * @return all-uppercase version of property name, dots converted to underscores, and camelCase words separated by underscore
     */
    public String toAllCaps(String propertyName) {
        return capitalizer.toAllCaps(propertyName);
    }
    
    @Override
    protected void addPropertyDirect(String string, Object o) {
        env.addProperty(string, o);
    }

    @Override
    public boolean isEmpty() {
        return env.isEmpty();
    }

    @Override
    public boolean containsKey(String string) {
        return env.containsKey(toAllCaps(string));
    }

    @Override
    public Object getProperty(String string) {
        return env.getProperty(toAllCaps(string));
    }

    /**
     * 
     * @return the real names of environment variables; does not modify output
     */
    @Override
    public Iterator<String> getKeys() {
        return env.getKeys();
    }
    
}
