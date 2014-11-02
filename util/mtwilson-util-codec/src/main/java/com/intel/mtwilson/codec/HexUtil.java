/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.codec;

import java.util.regex.Pattern;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

/**
 * @since 0.1.2
 * @author jbuhacoff
 */
public class HexUtil {
    public final static String HEX = "[0-9a-fA-F]+";
    public final static String NON_HEX = "[^0-9a-fA-F]";
    public final static String HEX_WHITESPACE = "[\\s:-]+"; // spaces, colons (like in MAC address), and dashes (like uuid)
    public final static String NON_PRINTABLE = "[^\\p{Print}]";
    private final static Pattern hexPattern = Pattern.compile("^"+HEX+"$");

    /**
     * 
     * @param text must not have any spaces or punctuation
     * @return true if the text is a valid non-empty hex string
     */
    public static boolean isHex(String text) {
        return text != null && !text.isEmpty() && hexPattern.matcher(text).matches();        
    }
    
    /**
     * Removes all non-hex characters from the input.
     * This is useful when trim isn't enough, for example
     * if there are non-printable characters mixed into
     * the input.
     * 
     * @param text
     * @return 
     */
    public static String normalize(String text) {
        return text.replaceAll(NON_HEX, "");
    }
    
    /**
     * Removes all spaces and colons. 
     */
    public static String trim(String text) {
        return text.replaceAll(HEX_WHITESPACE, "");
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
    public static byte[] toByteArray(String hex) {
        try {
            byte[] value = Hex.decodeHex(trim(hex).toCharArray());
            return value;
        }
        catch(DecoderException e) {
            throw new HexFormatException("Invalid hex input: "+hex, e);
        }
    }
}
