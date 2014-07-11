/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.model;

import com.intel.dcsg.cpg.validation.Model;
import com.intel.dcsg.cpg.validation.ObjectModel;

/**
 * An Internet Address can be a hostname, FQDN, IPv4, or IPv6 address. If any other
 * addressing schemes are used and can be used to route packets or translated
 * to an address that can be used to route packets, they should be added here. 
 * This class is intended
 * as a multi-use container: the operator can use whatever addressing scheme
 * is prevalent in the environment. The same input is returned by toString().
 * 
 * A note about the name:  a network address commonly refers to the first 16
 * bits of an IP address; a subnet address commonly refers to the next 8 bits,
 * and a host address commonly refers to the last 8 bits of an IP address.
 * Oftentimes the terms network address and host address are used loosely to
 * refer to the entire IP address or to a hostname or DNS name.
 * 
 * @author jbuhacoff
 */
public class InternetAddress extends ObjectModel {
    private String input = null;
    private transient Format format = null;
    private transient Model formatObject = null;
    
    public InternetAddress(String text) {
        if( text != null ) {
            input = text.trim();
        }
    }
    
    @Override
    protected void validate() {
        if( input == null ) {
            fault("Missing hostname or IP address");
            return;
        }
        
        IPv6Address ipv6 = new IPv6Address(input);
        if( ipv6.isValid() ) { 
            format = Format.IPv6; 
            formatObject = ipv6;
            return; 
        }
        
        IPv4Address ipv4 = new IPv4Address(input);
        if( ipv4.isValid() ) { 
            format = Format.IPv4;
            formatObject = ipv4;
            return; 
        }
        
        Hostname hostname = new Hostname(input); 
        if( hostname.isValid() ) { 
            format = Format.Hostname;
            formatObject = hostname;
            return; 
        }
        
        fault("Unrecognized Internet Address format: %s", input);
    }
    
    @Override
    public String toString() { return input; }
    
    public boolean isIPv6() { return isValid() && format.equals(Format.IPv6); }
    public boolean isIPv4() { return isValid() && format.equals(Format.IPv4); }
        public boolean isHostname() { return isValid() && format.equals(Format.Hostname); }

        public Model value() { return isValid() ? formatObject : null; } // returns the underlying model object for the current format (ipv6, ipv4, or hostname) so you can use it without re-validating

        public static enum Format { IPv6, IPv4, Hostname; } 
}
