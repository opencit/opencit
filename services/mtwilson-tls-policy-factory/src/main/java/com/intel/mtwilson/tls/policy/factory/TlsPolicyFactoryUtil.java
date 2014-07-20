/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls.policy.factory;

import com.intel.dcsg.cpg.codec.Base64Codec;
import com.intel.dcsg.cpg.codec.Base64Util;
import com.intel.dcsg.cpg.codec.ByteArrayCodec;
import com.intel.dcsg.cpg.codec.HexCodec;
import com.intel.dcsg.cpg.codec.HexUtil;
import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author jbuhacoff
 */
public class TlsPolicyFactoryUtil {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TlsPolicyFactoryUtil.class);

    /**
     * Utility function to detect if the sample is base64-encoded or
     * hex-encoded and return a new instance of the appropriate codec.
     * If the sample
     * encoding cannot be detected, this method will return null.
     * @param sample of data either base64-encoded or hex-encoded
     * @return a new codec instance or null if the encoding is not recognized
     */
    public static ByteArrayCodec getCodecForData(String sample) {
        log.debug("getCodecForData: {}", sample);
        String printable = sample.replaceAll("[^\\p{Print}]", "");
        String hex = HexUtil.trim(printable);
        if (HexUtil.isHex(hex)) {
            log.debug("getCodecForData hex: {}", hex);
            HexCodec codec = new HexCodec();
            codec.setNormalizeInput(true);
            return codec;
        }
        String base64 = Base64Util.trim(printable);
        if (Base64Util.isBase64(base64)) {
            log.debug("getCodecForData base64: {}", base64);
            Base64Codec codec = new Base64Codec();
            codec.setNormalizeInput(true);
            return codec;
        }
        return null;
    }

    /**
     * Utility function to instantiate a codec by name
     * @param encoding "base64" or "hex"
     * @return new codec instance or null if the encoding name is not recognized
     */
    public static ByteArrayCodec getCodecByName(String encoding) {
        if (encoding.equalsIgnoreCase("base64")) {
            return new Base64Codec();
        } else if (encoding.equalsIgnoreCase("hex")) {
            return new HexCodec();
        } else {
            return null;
        }
    }

    public static String guessAlgorithmForDigest(byte[] hash) {
        if (hash.length == 16) {
            return "MD5";
        }
        if (hash.length == 20) {
            return "SHA-1";
        }
        if (hash.length == 32) {
            return "SHA-256";
        }
        if (hash.length == 48) {
            return "SHA-384";
        }
        if (hash.length == 64) {
            return "SHA-512";
        }
        return null;
    }

    /**
     * Utility function to get a sample item from a collection
     * @param collection
     * @return the first item from the collection, or null if the collection is empty
     */
    public static String getFirst(Collection<String> collection) {
        Iterator<String> it = collection.iterator();
        if (it.hasNext()) {
            return it.next();
        }
        return null;
    }
    
}
