/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.model;

import com.intel.dcsg.cpg.validation.ObjectModel;
import java.net.Inet6Address;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Supports standard, compact, and mixed notations.
 * 
 * Standard notation: eight 16-bit words using hex notation, delimited by colons, leading zeros optional
 *    Example: FA47:0:0:0:0:D39:1:5CA2
 * 
 * Compact: 
 * 
 * Mixed: IPv6 and IPv4 notation together. Usually when IPv6 is an extension of an IPv4 space.
 *    Example: FA47:0:0:0:0:D39:10.0.92.21
 * 
 * @author jbuhacoff
 */
public class IPv6Address extends ObjectModel {
    private static final String rDecimalByte = "(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])";
    private static final String rIPv4 = "(?:"+rDecimalByte+"\\.){3}"+rDecimalByte;
    private static final String rStandardWord = "[A-F0-9]{1,4}";
    private static final String rStandardNotation = "^(?:"+rStandardWord+":){7}"+rStandardWord+"$";
    private static final Pattern pStandardNotation = Pattern.compile(rStandardNotation);
    private static final String rMixedNotation = "^(?:"+rStandardWord+":){6}"+rIPv4+"$";
    private static final Pattern pMixedNotation = Pattern.compile(rMixedNotation);
//    private static final String rCompactWord = "[A-F0-9]{0,4}";
//    private static final String rCompactNotation7 = "^(?:"+rCompactWord+":){0,7}"+rCompactWord+"$";
//    private static final String rCompactNotation8 = "^(?:"+rStandardWord+":){7}:|:(?::"+rStandardWord+"){7}$";
    
    private String input;
    private transient Notation notation = null;
    
    public IPv6Address(String text) {
        input = text.trim();
    }
    
    @Override
    protected void validate() {
        Matcher mStandard = pStandardNotation.matcher(input);
        if( mStandard.matches() ) {
            notation = Notation.STANDARD;
            return;
        }
        Matcher mMixed = pMixedNotation.matcher(input);
        if( mMixed.matches() ) {
            notation = Notation.MIXED;
            return;
        }
        
        fault("Unrecognized IPv6 format: %s", input);
    }
    
    @Override
    public String toString() { return input; }
    
    public Notation notation() { return isValid() ? notation : null; }

    public byte[] toByteArray() { 
        if( !isValid() ) { return null; }
        try {
            return Inet6Address.getByName(input).getAddress(); 
        }
        catch(UnknownHostException e) {
            return null;
        }
    }
    
    public static enum Notation { STANDARD, MIXED; } 
}
