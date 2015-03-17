/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.configuration;

import com.intel.mtwilson.pipe.Transformer;
import com.intel.mtwilson.text.transform.AllCapsNamingStrategy;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Transforms keys from lower.case to ALL_CAPS format.  This configuration
 * decorator can be used with an EnvironmentConfiguration in order to 
 * transform Java Properties style keys to corresponding environment
 * variables. 
 * 
 * For example:
 * <pre>
 * Configuration allcaps = new AllCapsConfiguration(new EnvironmentConfiguration());
 * String javaHome = allcaps.get("java.home");
 * </pre>
 * 
 * @author jbuhacoff
 */
public class AllCapsConfiguration extends AbstractConfiguration {
    private Pattern allcapsPattern = Pattern.compile("^([A-Z0-9_]+)$");
    private Configuration delegate;
    private Transformer<String> allcaps = new AllCapsNamingStrategy();

    public AllCapsConfiguration(Configuration delegate) {
        this.delegate = delegate;
    }
    
    
    /**
     * 
     * @return set of all-caps keys; any non-matching keys will be omitted
     */
    @Override
    public Set<String> keys() {
        HashSet<String> result = new HashSet<>();
        for(String key : delegate.keys()) {
            if( accept(key) ) {
                result.add(key);
            }
        }
        return result;
    }

    /**
     * Example: get("java.home") is transformed to get("JAVA_HOME") and only
     * returns a result if there exists a configuration key JAVA_HOME
     * 
     * @param key
     * @return 
     */
    @Override
    public String get(String key) {
        return delegate.get(allcaps.transform(key));
    }

    /**
     * Example: set("foo.bar", "xyz") is transformed to set("FOO_BAR", "xyz")
     * 
     * @param key
     * @param value 
     */
    @Override
    public void set(String key, String value) {
        delegate.set(allcaps.transform(key), value);
    }

    @Override
    public boolean isEditable() {
        return delegate.isEditable();
    }
    
    /**
     * 
     * @param key
     * @return true if the key is all-caps (digits and underscore allowed), false otherwise
     */
    private boolean accept(String key) {
        return allcapsPattern.matcher(key).matches();
    }
}
