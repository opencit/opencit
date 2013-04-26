/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.io;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Contains methods for encoding Strings into Filenames. Very conservative
 * encoding, allowing only letters, digits, and a small set of allowed 
 * non-alphanumeric characters, currently underscore and hyphen.
 * Use it when you have a string and need to use it as a filename but
 * it may contain forward slashes, dots, backslashes, and other characters
 * that may not be allowed in a filename by an operating system.
 * 
 * First version of this was naive and use % as the encoding character, which
 * causes havoc on Windows. 
 * 
 * See also: http://en.wikipedia.org/wiki/Filename
 * 
 * @author jbuhacoff
 */
public class Filename {
    
    private static final char[] allowedCharacters = new char[] { '_', '-' };
    
    /**
     * 
     * @param text to encode
     * @param escape character to prefix the hex encodings of special characters
     * @return encoded text
     */
    public static String encode(String text, char escape) {
        StringBuilder s = new StringBuilder();
        for(int i=0; i<text.length(); i++) {
            if( Character.isLetter(text.charAt(i)) || Character.isDigit(text.charAt(i)) || ArrayUtils.contains(allowedCharacters, text.charAt(i))) {
                s.append(text.charAt(i));
            }
            else {
                s.append(escape).append(String.format("%02x", text.codePointAt(i)));
            }
        }
        return s.toString();
    }
    
    /**
     * The % was chosen as the escape character for familiarity with URL's but
     * it's a bad choice for Windows platforms. On Windows, use the two-argument
     * form of this function that allows you to select a different escape character
     * such as #. 
     * 
     * @param text to encode using % as the escape character
     * @return encoded text
     */
    public static String encode(String text) {
        return encode(text, '%');
    }
    
    /**
     * 
     * @param text to decode
     * @param escape character that prefixes the hex encodings of special characters
     * @return decoded text
     */
    public static String decode(String text, char escape) {
        StringBuilder s = new StringBuilder();
        for(int i=0; i<text.length(); i++) {
            if( text.charAt(i) == escape ) {
                String hex = String.format("%c%c", text.charAt(i+1), text.charAt(i+2));
                s.append(Character.toChars(Integer.valueOf(hex, 16).intValue()));
                i += 2;
            }
            else {
                s.append(text.charAt(i));
            }
        }
        return s.toString();        
    }
    
    /**
     * The % was chosen as the escape character for familiarity with URL's but
     * it's a bad choice for Windows platforms. On Windows, use the two-argument
     * form of this function that allows you to select a different escape character
     * such as #. 
     * 
     * @param text to decode using % as the escape character
     * @return decoded text
     */
    public static String decode(String text) {
        return decode(text, '%');
    }

}
