/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.security.core;

import org.apache.commons.net.util.SubnetUtils;

/**
 *
 * @author jbuhacoff
 */
public class IPAddressUtil {
    
    /**
     * 
     * @param ipAddress
     * @param cidrSubnet subnet in CIDR notation, like 192.168.0.0/16
     * @return true if the IP Address can be found in the Subnet
     */
    public static boolean isAddressInSubnet(String ipAddress, String cidrSubnet) {
        SubnetUtils subnet = new SubnetUtils(cidrSubnet);
        if( subnet.getInfo().isInRange(ipAddress) ) {
            return true;
        }
        return false;
    }
    
    /**
     * 
     * @param ipAddress
     * @param whitelist
     * @return the element in the whitelist which matched the IP Address, or null if there was no match
     */
    public static String matchAddressInList(String ipAddress, String[] whitelist) {
        for(String item : whitelist) {
            if( item.contains("/") && isAddressInSubnet(ipAddress, item) ) {
                return item;
            }
            else {
                // regular IP address
                if( item.equals(ipAddress) ) {
                    return item;
                }
            }
        }
        return null;        
    }
    
}
