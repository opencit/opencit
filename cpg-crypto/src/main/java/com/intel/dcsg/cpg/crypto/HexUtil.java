/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto;

import java.util.regex.Pattern;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

/**
 *  * XXX TODO should move this into the io package

 * @since 0.1
 * @author jbuhacoff
 */
public class HexUtil {
    private final static Pattern hexPattern = Pattern.compile("^[0-9a-fA-F]+$");

    /**
     * 
     * @param text must not have any spaces or punctuation
     * @return true if the text is a valid hex string
     */
    public static boolean isHex(String text) {
        return hexPattern.matcher(text).matches();        
    }
    
    /**
     * Removes all spaces and colons. 
     */
    public static String trim(String text) {
        return text.replaceAll("[ :]", "");
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
