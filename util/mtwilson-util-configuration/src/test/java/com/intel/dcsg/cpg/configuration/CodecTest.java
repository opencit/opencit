/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.configuration;

import com.intel.mtwilson.codec.XStreamCodec;
import com.intel.mtwilson.codec.Base64Codec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class CodecTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CodecTest.class);
    
    @Test
    public void testCodec() {
        MapConfiguration map = new MapConfiguration();
        map.setString("foo", "bar");
        map.setInteger("max", Integer.MAX_VALUE);
        XStreamCodec codec = new XStreamCodec();
        byte[] encodedBytes = codec.encode(map);
        Base64Codec base64 = new Base64Codec();
        String encodedString = base64.encode(encodedBytes);
        log.debug("encoded: {}", encodedString);
        byte[] decodedBytes = base64.decode(encodedString);
        Object decodedObject = codec.decode(decodedBytes);
        MapConfiguration map2 = (MapConfiguration)decodedObject;
        log.debug("foo: {}", map2.getString("foo"));
        log.debug("max: {}", map2.getInteger("max"));
    }
    
    @Test
    public void testXStreamCodec() throws IOException {
//        XStreamPropertyCodec codec = new XStreamPropertyCodec();
        PropertiesConfiguration config = new PropertiesConfiguration();
//        config.setObjectCodec(codec);  // XStreamPropertyCodec is now the default
        config.setString("foo", "bar");
        config.setInteger("max", Integer.MAX_VALUE);
        config.setObject("apple", new Fruit("apple","red"));
        log.debug("encoded apple: {}", config.getProperties().getProperty("apple"));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        config.getProperties().storeToXML(out, null); // throws IOException
        String xml = out.toString();
        log.debug("properties: {}", xml);
        Properties restored = new Properties();
        restored.loadFromXML(new ByteArrayInputStream(xml.getBytes()));
        PropertiesConfiguration restoredConfig = new PropertiesConfiguration(restored);
//        restoredConfig.setObjectCodec(codec); // XStreamPropertyCodec is now the default
        log.debug("foo {}", restoredConfig.getString("foo"));
        log.debug("max {}", restoredConfig.getInteger("max"));
        Fruit apple = restoredConfig.getObject(Fruit.class, "apple");
        log.debug("fruit name {} color {}", apple.name, apple.color);
        
    }
    
    public static class Fruit {
        String name;
        String color;

        public Fruit(String name, String color) {
            this.name = name;
            this.color = color;
        }
        
    }
}
