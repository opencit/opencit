/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto;

import java.util.regex.Pattern;
import org.apache.commons.codec.binary.Base64;

/**
 * XXX TODO should move this into the io package
 * 
 * @since 0.1.1
 * @author jbuhacoff
 */
public class Base64Util {
    private final static Pattern base64Pattern = Pattern.compile("^[0-9a-zA-Z+/]+[=]{0,3}$"); // a rough estimate... but together with the length check in isBase64 it gives a pretty good guess

    /**
     * 
     * @param text must not have any spaces or punctuation other than base64 characters + and /
     * @return true if the text is a valid base64 string
     */
    public static boolean isBase64(String text) {
        return (text.length()%4 == 0) && base64Pattern.matcher(text).matches();        
    }
    
    /**
     * Removes all spaces, tabs, and newlines
     * 
     */
    public static String trim(String text) {
        return text.replaceAll("[ \t\r\n]", "");
    }
    
    /**
     * Wraps Apache Commons Codec Hex.decodeHex and throws an IllegalArgumentException
     * if the input is not a valid hex string. This is a convenience for methods that
     * work with hex strings and pre-validate the string before trying to convert it to
     * hex. In that context, passing something that is not a valid hex value is a programming
     * error and it's appropriate to throw an unchecked exception.
     * 
     * @param hex
     * @return 
     */
    public static byte[] toByteArray(String base64) {
        byte[] value = Base64.decodeBase64(trim(base64));
        return value;
    }
}
