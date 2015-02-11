/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.codec;

import com.intel.mtwilson.codec.Base64Util;
import com.intel.mtwilson.codec.Base64Codec;
import com.intel.mtwilson.codec.ByteArrayCodec;
import com.intel.mtwilson.codec.HexCodec;
import com.intel.mtwilson.codec.HexUtil;
import com.intel.mtwilson.codec.JacksonCodec;
import com.intel.mtwilson.codec.XStreamCodec;
import java.nio.charset.Charset;
import java.util.HashMap;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class CodecTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CodecTest.class);
    
    @Test
    public void testXstreamCodec() {
        HashMap<String,Object> map = new HashMap<>();
        map.put("foo", "bar");
        map.put("max", Integer.MAX_VALUE);
        XStreamCodec codec = new XStreamCodec();
        byte[] encodedBytes = codec.encode(map);
        Base64Codec base64 = new Base64Codec();
        String encodedString = base64.encode(encodedBytes);
        log.debug("encoded: {}", encodedString);
        byte[] decodedBytes = base64.decode(encodedString);
        Object decodedObject = codec.decode(decodedBytes);
        HashMap<String,Object> map2 = (HashMap<String,Object>)decodedObject;
        log.debug("foo: {}", map2.get("foo"));
        log.debug("max: {}", map2.get("max"));
    }

    @Test
    public void testJacksonCodec() {
        HashMap<String,Object> map = new HashMap<>();
        map.put("foo", "bar");
        map.put("max", Integer.MAX_VALUE);
        JacksonCodec codec = new JacksonCodec();
        byte[] encodedBytes = codec.encode(map);
        log.debug("encoded object: {}", new String(encodedBytes));
        Base64Codec base64 = new Base64Codec();
        String encodedString = base64.encode(encodedBytes);
        log.debug("encoded: {}", encodedString);
        byte[] decodedBytes = base64.decode(encodedString);
        Object decodedObject = codec.decode(decodedBytes);
        HashMap<String,Object> map2 = (HashMap<String,Object>)decodedObject;
        log.debug("foo: {}", map2.get("foo"));
        log.debug("max: {}", map2.get("max"));
    }
    
    @Test
    public void testJacksonCodecUnknownProperties() {
        Animal dog = new Animal("sparky", "dog", "alice");
        Fruit apple = new Fruit("apple", "red");
        JacksonCodec codec = new JacksonCodec();
        log.debug("animal: {}", new String(codec.encode(dog), Charset.forName("UTF-8"))); // animal: {"@class":"com.intel.dcsg.cpg.codec.CodecTest$Animal","name":"sparky","species":"dog","ownerName":"alice"}
        log.debug("fruit: {}", new String(codec.encode(apple), Charset.forName("UTF-8"))); // fruit: {"@class":"com.intel.dcsg.cpg.codec.CodecTest$Fruit","name":"apple","color":"red"}
        
        MarketFruit soldApple = new MarketFruit("corner market", "apple", "red");
        
        Fruit soldApple2 = (Fruit)codec.decode(codec.encode(soldApple)); // what it actually returns is a MarketFruit class, we just cast it to Fruit which is legal
        log.debug("soldApple2: {}", new String(codec.encode(soldApple2), Charset.forName("UTF-8")));// soldApple2: {"@class":"com.intel.dcsg.cpg.codec.CodecTest$MarketFruit","name":"apple","color":"red","marketName":"corner market"}
//        MarketFruit apple2 = (MarketFruit)codec.decode(codec.encode(apple)); // other way wouldn't work,  you can't cast Fruit to MarketFruit
//        log.debug("apple2: {}", new String(codec.encode(apple2), Charset.forName("UTF-8")));
        
    }
    
    public static class Animal {
        public String name;
        public String species;
        public String ownerName;

        public Animal() {
        }

        
        public Animal(String name, String species, String ownerName) {
            this.name = name;
            this.species = species;
            this.ownerName = ownerName;
        }
        
        
    }
    public static class MarketFruit extends Fruit {
        public String marketName;

        public MarketFruit() {
        }

        public MarketFruit(String marketName, String name, String color) {
            super(name, color);
            this.marketName = marketName;
        }
        
        
    }
    
    public static class Fruit {
        public String name;
        public String color;

        public Fruit() {
        }

        public Fruit(String name, String color) {
            this.name = name;
            this.color = color;
        }
        
    }
    public static ByteArrayCodec getCodecForData(String sample) {
        log.debug("getCodecForData: {}", sample);
        String hex = HexUtil.trim(sample); // important to remove only whitespace here and not ALL non-hex characters (because then isHex will almost always return true)
        if( HexUtil.isHex(hex)) {
            log.debug("getCodecForData hex: {}", hex);
            HexCodec codec = new HexCodec(); // encoding = "hex";
            codec.setNormalizeInput(true); // automatically remove non-hex characters before decoding
            return codec;
        }
        String base64 = Base64Util.trim(sample); // important to remove only whitespace here and not ALL non-hex characters (because then isBase64 will almost always return true)
        if( Base64Util.isBase64(base64) ) {
            log.debug("getCodecForData base64: {}", base64);
            Base64Codec codec = new Base64Codec(); // encoding = "base64";
            codec.setNormalizeInput(true); // automatically remove non-base64 characters before decoding
            return codec;
        }
        return null;
    }
    
    @Test
    public void testGetCodecForData() {
        ByteArrayCodec codec;
        codec = getCodecForData("aaaa");
        assertNotNull(codec);
        log.debug("codec class {}", codec.getClass().getName());
        assertTrue(codec instanceof HexCodec );
        codec = getCodecForData("aaaaaa=="); // would fail for aaaa==  because it's not valid base64 string even though it has only base64 characters.
        assertNotNull(codec);
        log.debug("codec class {}", codec.getClass().getName());
        assertTrue(codec instanceof Base64Codec );
    }
    
    @Test
    public void testHexCodec() {
        HexCodec codec = new HexCodec();
        codec.setNormalizeInput(true);
        byte[] data = codec.decode("\u200eB0:91:3C:79:2D:67:19:84:64:EA:C9:2A:D9:03:44:3F:4B:8C:BE:0F");
        log.debug("worked  {}", data.length);
    }
}
