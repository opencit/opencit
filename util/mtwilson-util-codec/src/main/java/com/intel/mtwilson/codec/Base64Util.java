/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.codec;

import java.util.regex.Pattern;
import org.apache.commons.codec.binary.Base64;

/**
 * 
 * @since 0.1.2
 * @author jbuhacoff
 */
public class Base64Util {
    public final static String BASE64 = "(?:[a-zA-Z0-9+/]{4})*(?:[a-zA-Z0-9+/]{2}==|[a-zA-Z0-9+/]{3}=)?";
    public final static String NON_BASE64 = "[^a-zA-Z0-9+/=]";
    public final static String BASE64_WHITESPACE = "[\\s]+"; // spaces only
//    private final static Pattern base64Charset = Pattern.compile("[a-zA-Z0-9+/=]"); 
    private final static Pattern base64Pattern = Pattern.compile("^"+BASE64+"$");

    /**
     * 
     * @param text must not have any spaces or punctuation other than base64 characters + and /
     * @return true if the text is a valid non-empty base64 string
     */
    public static boolean isBase64(String text) {
        return text != null && !text.isEmpty() && base64Pattern.matcher(text).matches();        
    }

    /**
     * Removes all non-base64 characters from the input.
     * This is useful when trim isn't enough, for example
     * if there are non-printable characters mixed into
     * the input.
     * 
     * @param text
     * @return 
     */
    public static String normalize(String text) {
        return text.replaceAll(NON_BASE64, "");
    }
    
    /**
     * Removes all spaces, tabs, and newlines
     * 
     */
    public static String trim(String text) {
        return text.replaceAll(BASE64_WHITESPACE, "");
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
