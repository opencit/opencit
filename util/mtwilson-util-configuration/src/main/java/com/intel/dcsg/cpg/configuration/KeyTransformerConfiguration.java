/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.configuration;

import com.intel.mtwilson.pipe.Transformer;
import java.util.Set;

/**
 * This class is a decorator that can wrap any existing Configuration 
 * instance to transform names of keys before they are passed to that
 * instance. For example if the application uses key names like 
 * java.style.properties it can automatically transform them to 
 * SHELL_STYLE_VARIABLES by using the KeyTransformerConfiguration with
 * the AllCapsNamingStrategy. Another example of a transformation might
 * be to prepend the application name to every key, so that "fruit.color"
 * becomes "myapp.fruit.color"
 * 
 * @author jbuhacoff
 */
public class KeyTransformerConfiguration extends AbstractConfiguration implements Configuration {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(KeyTransformerConfiguration.class);
    protected Configuration configuration;
    protected Transformer<String> transformer;
    
    public KeyTransformerConfiguration(Transformer<String> transformer, Configuration configuration) {
        super();
        this.transformer = transformer;
        this.configuration = configuration;
    }

    /**
     * Note that returned keys are not transformed, they are in original form.
     * 
     * @return 
     */
    @Override
    public Set<String> keys() {
        return configuration.keys();
    }
    
    @Override
    public String get(String key) {
        String key2 = transformer.transform(key);
        String value = configuration.get(key2);
        //log.debug("get key {} transformed {} -> value {}", key, key2, value);
        return value;
    }
    
    @Override
    public void set(String key, String value) {
        configuration.set(transformer.transform(key), value);
    }

    @Override
    public boolean isEditable() {
        return configuration.isEditable();
    }

    
}
