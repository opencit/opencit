/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.model;

import com.intel.dcsg.cpg.validation.ObjectModel;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Supports standard decimal notation for IPv4 such as 192.168.0.1
 * 
 * This class represents the address for a single host. 
 * Intentionally does not support subnet/CIDR notation. That belongs in a NetworkAddress class.
 * 
 * @author jbuhacoff
 */
public class IPv4Address extends ObjectModel {
    private static final String rDecimalByte = "(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])";
    private static final String rIPv4 = "(?:"+rDecimalByte+"\\.){3}"+rDecimalByte;
    private static final Pattern pIPv4 = Pattern.compile(rIPv4);
    
    private String input;
    
    public IPv4Address(String text) {
        input = text.trim();
    }
    
    @Override
    protected void validate() {
        Matcher mIPv4 = pIPv4.matcher(input);
        if( mIPv4.matches() ) {
            return;
        }
        fault("Unrecognized IPv4 format: %s", input);
    }
    
    @Override
    public String toString() { return input; }
    
    public byte[] toByteArray() { 
        if( !isValid() ) { return null; }
        try {
            return Inet4Address.getByName(input).getAddress();
        }
        catch(UnknownHostException e) {
            return null;
        }
    }
}
