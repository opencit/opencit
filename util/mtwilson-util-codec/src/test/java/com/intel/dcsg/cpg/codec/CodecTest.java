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
import com.intel.mtwilson.codec.XStreamCodec;
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
    public void testCodec() {
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

    public static class Fruit {
        String name;
        String color;

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
