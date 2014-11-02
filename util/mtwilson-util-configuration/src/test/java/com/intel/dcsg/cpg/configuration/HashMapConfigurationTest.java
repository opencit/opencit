/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.configuration;

import com.thoughtworks.xstream.XStream;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class HashMapConfigurationTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HashMapConfigurationTest.class);
    
    public static class Fruit { public String name; public String color; public Fruit(String name, String color) { this.name = name; this.color = color; } }
    
    private MapConfiguration createConfiguration() {
        MapConfiguration c = new MapConfiguration();
        c.setString("foo", "bar");
        c.setObject("apple", new Fruit("apple","green"));
        return c;
    }
    /**
2014-02-22 14:35:16,425 DEBUG [main] c.i.d.c.c.HashMapConfigurationTest [HashMapConfigurationTest.java:22] foo = bar
2014-02-22 14:35:16,443 DEBUG [main] c.i.d.c.c.HashMapConfigurationTest [HashMapConfigurationTest.java:24] apple = green
     * 
     */
    @Test
    public void testCreateConfiguration() {
        MapConfiguration c = createConfiguration();
        assertEquals("bar", c.getString("foo"));
        assertEquals("green", c.getObject(Fruit.class, "apple").color);
    }
    
    /**
2014-02-22 14:41:48,160 DEBUG [main] c.i.d.c.c.HashMapConfigurationTest [HashMapConfigurationTest.java:43] xml = 
<com.intel.dcsg.cpg.configuration.HashMapConfiguration>
  <map>
    <entry>
      <string>apple</string>
      <com.intel.dcsg.cpg.configuration.HashMapConfigurationTest_-Fruit>
        <name>apple</name>
        <color>green</color>
      </com.intel.dcsg.cpg.configuration.HashMapConfigurationTest_-Fruit>
    </entry>
    <entry>
      <string>foo</string>
      <string>bar</string>
    </entry>
  </map>
</com.intel.dcsg.cpg.configuration.HashMapConfiguration>
     * 
     */
    @Test
    public void testSerializeConfiguration() {
        Configuration c = createConfiguration();
        XStream xs = new XStream();
        String xml = xs.toXML(c);
        log.debug("xml = {}", xml);
        Configuration c2 = (Configuration)xs.fromXML(xml);
        assertEquals("bar", c2.getString("foo"));
        assertEquals("green", c2.getObject(Fruit.class, "apple").color);
        
    }
}
