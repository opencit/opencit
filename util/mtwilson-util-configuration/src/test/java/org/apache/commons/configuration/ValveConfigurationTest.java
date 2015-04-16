/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package org.apache.commons.configuration;

import com.intel.dcsg.cpg.configuration.CommonsKeyTransformerConfiguration;
import com.intel.dcsg.cpg.configuration.CommonsValveConfiguration;
import com.intel.mtwilson.text.transform.AllCapsNamingStrategy;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class ValveConfigurationTest {
    private Logger log = LoggerFactory.getLogger(getClass());
    
    
    @Test
    public void testCompositeConfiguration() {
        PropertiesConfiguration c1 = new PropertiesConfiguration();
        PropertiesConfiguration c2 = new PropertiesConfiguration();
        c1.setProperty("fruit", "apple");
        c2.setProperty("fruit", "banana");
        c1.setProperty("number", 1);
        c2.setProperty("boolean", false);
        CompositeConfiguration cc = new CompositeConfiguration();
        cc.addConfiguration(c1);
        cc.addConfiguration(c2);
        assertEquals("apple", cc.getString("fruit"));
        assertEquals(Integer.valueOf(1), cc.getInteger("number", null));
        assertEquals(false, cc.getBoolean("boolean"));
        assertFalse(cc.containsKey("missing"));
        // now create a ValveConfiguration with readable/writable separation
        CommonsValveConfiguration vc = new CommonsValveConfiguration();
        vc.setWriteTo(c1);
        vc.setReadFrom(cc);
        // check that all properties are coming from the composite configuration
        assertEquals("apple", vc.getString("fruit"));
        assertEquals(Integer.valueOf(1), vc.getInteger("number", null));
        assertEquals(false, vc.getBoolean("boolean"));
        assertFalse(vc.containsKey("missing"));
        // now remove a property from the valve configuration, and notice
        // that it's only removed from c1 (writable) but not from c2
        vc.clearProperty("fruit"); 
        assertNull(c1.getString("fruit"));
        assertEquals("banana",c2.getString("fruit"));
        // so because it's still in c2 you can still see it
        assertEquals("banana", vc.getString("fruit"));
    }
    
    @Test
    public void testCompositeValveConfiguration() {
        CompositeConfiguration composite = new CompositeConfiguration();
        // java system properties
        MapConfiguration systemProperties = new MapConfiguration(System.getProperties());
        composite.addConfiguration(systemProperties);
        // environment variables, automatically translating from key.name to KEY_NAME,
        EnvironmentConfiguration environment = new EnvironmentConfiguration();
        CommonsKeyTransformerConfiguration allCapsEnvironment = new CommonsKeyTransformerConfiguration(new AllCapsNamingStrategy(), environment);
        composite.addConfiguration(allCapsEnvironment);
        // default values
        PropertiesConfiguration defaults = new PropertiesConfiguration();
        defaults.setProperty("foo", "bar");
        defaults.setProperty("ku", "ku");
        composite.addConfiguration(defaults);
        // store all edits in a separate object 
        PropertiesConfiguration edits = new PropertiesConfiguration();
        // use the valve to ensure that changes only go to the writable configuration (and not to system properties or environment)
        CommonsValveConfiguration valve = new CommonsValveConfiguration();
        valve.setReadFrom(composite);
        valve.setWriteTo(edits);
        
        assertEquals("bar", valve.getString("foo"));
        valve.setProperty("newkey", "newvalue");
        assertEquals("newvalue", edits.getString("newkey"));
        assertFalse(edits.containsKey("ku"));
        assertFalse(defaults.containsKey("newkey"));
    }
}
