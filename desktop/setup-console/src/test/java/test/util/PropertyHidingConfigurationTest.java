/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.util;

import com.intel.mtwilson.setup.PropertyHidingConfiguration;
import java.util.Iterator;
import java.util.Properties;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author jbuhacoff
 */
public class PropertyHidingConfigurationTest {
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private void logConfiguration(Configuration c) {
        log.debug("Configuration {}", c.getClass().getName());
        Iterator<String> it = c.getKeys();
        while(it.hasNext()) {
            String key = it.next();
            log.debug("key: "+key+" value: "+c.getString(key)+" value with default: "+c.getString(key,"default"));
        }
    }
    
    @Test
    public void testHiddenProperty() {
        Properties p = new Properties();
        p.setProperty("key1", "value1");
        p.setProperty("key2", "value2");
        MapConfiguration c = new MapConfiguration(p);
        logConfiguration(c);
        PropertyHidingConfiguration h = new PropertyHidingConfiguration(c);
        h.hideProperty("key1");
        logConfiguration(h); // hiding key1 allows someone who is providing a default value to use the default;  so you will see key1 value = null but with default key1 value = default
    }

    @Test
    public void testNulledProperty() {
        Properties p = new Properties();
        p.setProperty("key1", "value1");
        p.setProperty("key2", "value2");
        MapConfiguration c = new MapConfiguration(p);
        logConfiguration(c);
        PropertyHidingConfiguration h = new PropertyHidingConfiguration(c);
        h.nullProperty("key1");
        logConfiguration(h); // nulling key1 will cause it to always return null even if caller provides a default value
    }

    @Test
    public void testReplacedProperty() {
        Properties p = new Properties();
        p.setProperty("key1", "value1");
        p.setProperty("key2", "value2");
        MapConfiguration c = new MapConfiguration(p);
        logConfiguration(c);
        PropertyHidingConfiguration h = new PropertyHidingConfiguration(c);
        h.replaceProperty("key1","");
        logConfiguration(h); // replacing key1 will cause it to always return the replaced value,  currentyl implemented only for getString
    }
    
}
