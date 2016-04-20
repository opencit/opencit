/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mountwilson.common;

import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author jbuhacoff
 */
public class CommandArg {
    
    private static final HashSet<Character> badFilenameCharset;
    private static final char[] badFilenameChars = new char[] { '%', '\'', '"', '&', '*' }; // percent doesn't cause problems but we're using it as the escape character so that's why we encode it too
    
    static {
        HashSet<Character> set = new HashSet<Character>();
        for(int i=0; i<badFilenameChars.length; i++) {
            set.add(Character.valueOf(badFilenameChars[i]));
        }
        badFilenameCharset = set;
    }
    
    public static String escapeFilename(String arg) {
        StringBuilder s = new StringBuilder(arg.length()*5/4);
        int len = arg.length();
        for(int i=0; i<len; i++) {
            char c = arg.charAt(i);
            if(badFilenameCharset.contains(Character.valueOf(c))) {
                s.append("%").append(Integer.toHexString((int)c));
            }
            else {
                s.append(c);
            }
        }
        return s.toString();
    }
    
    private static Pattern hexPattern = Pattern.compile("%([A-Za-z0-9]{2})");
    public static String unescapeFilename(String arg) {
        StringBuilder s = new StringBuilder(arg.length());
        int cursor = 0;
        Matcher m = hexPattern.matcher(arg);
        while( m.find() ) {
            if( m.start() > cursor ) {
                s.append(arg.substring(cursor, m.start())); // string BEFORE the code
            }
            String hex = m.group();
            char c = (char)Integer.parseInt(hex, 16);
            s.append(c);
            cursor = m.end();
        }
        if( cursor < arg.length() ) {
            s.append(arg.substring(cursor)); // append the remainder of the string AFTER the last code
        }
        return s.toString();
    }

}
