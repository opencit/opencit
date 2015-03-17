/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.configuration;

import java.util.HashSet;
import java.util.Set;

/**
 * This class is case-sensitive. Prefix "foo." is not equivalent to "FOO."
 * 
 * Notice the dot at the end of "foo." for Java Properties or the
 * underscore at the end of "FOO_" for environment variables must
 * be included in the prefix in order to get desired results.
 * 
 * @author jbuhacoff
 */
public class PrefixConfiguration extends AbstractConfiguration {
    private Configuration delegate;
    private String prefix;

    /**
     * 
     * @param delegate configuration from which to use only prefixed keys
     * @param prefix such as "MTWILSON_" or "mtwilson." 
     */
    public PrefixConfiguration(Configuration delegate, String prefix) {
        super();
        this.delegate = delegate;
        this.prefix = prefix;
    }

    /**
     * 
     * @return the prefix such as "MTWILSON_" or "mtwilson."
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Filters returned keys to only those having the prefix; and removes
     * the prefix before returning the keys so that callers can pass the
     * returned keys to {@code get} and obtain the values.
     * 
     * @return 
     */
    @Override
    public Set<String> keys() {
        Set<String> keys = delegate.keys();
        HashSet<String> result = new HashSet<>();
        for(String key : keys) {
            if( accept(key)) {
                result.add(strip(key));
            }
        }
        return result;
    }

    /**
     * Tolerant for input, if prefix is "foo." then calling get("bar") is
     * equivalent to calling get("foo.bar")
     * @param key
     * @return 
     */
    @Override
    public String get(String key) {
        if( accept(key) ) {
            return delegate.get(key);
        }
        return delegate.get(prefix(key));
    }
    
    /**
     * Tolerant for input, if prefix is "foo." then calling set("bar", "xyz")
     * is equivalent to calling set("foo.bar", "xyz")
     * 
     * @param key
     * @param value 
     */
    @Override
    public void set(String key, String value) {
        if( accept(key) ) {
            delegate.set(key, value);
        }
        else {
            delegate.set(prefix(key), value);
        }
    }

    @Override
    public boolean isEditable() {
        return delegate.isEditable();
    }
    
    
    /**
     * Filters keys by prefix
     * 
     * @param key
     * @return true if the given key starts with the prefix, false otherwise
     */
    private boolean accept(String key) {
        return key.startsWith(prefix);
    }
    
    /**
     * Removes the prefix from the key
     * 
     * Example: strip("MTWILSON_PASSWORD") == "PASSWORD"
     * 
     * You must ensure the key is already prefixed before calling this;
     * there are no safeguards here.
     * 
     * @param key
     * @return 
     */
    private String strip(String key) {
        return key.substring(prefix.length());
    }
    
    /**
     * Prepends the prefix to the key.
     * 
     * Example:  prefix("PASSWORD") == "MTWILSON_PASSWORD"
     * 
     * You must ensure the key is not already prefixed before calling this;
     * there are no safeguards here.
     * 
     * @param key
     * @return 
     */
    private String prefix(String key) {
        return prefix + key;
    }

}
