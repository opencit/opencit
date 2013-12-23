/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.datatypes;

/**
 * XXX TODO move to validation library
 * @author ssbangal
 */
public class RegExAnnotation {
    public static final String DEFAULT = "(?:[a-zA-Z0-9(){}_\\.\\, |-]+)"; // should not include quotes, blackslashes
    public static final String IPADDRESS = "(?:(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5]))";
    public static final String FQDN = "(?:(([a-zA-Z]|[a-zA-Z][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z]|[A-Za-z][A-Za-z0-9\\-]*[A-Za-z0-9]))";
    public static final String IPADDR_FQDN = "(?:" + IPADDRESS + "|" + FQDN + ")";
    public static final String EMAIL = "(?:([a-z0-9_\\.-]+)@([\\da-z\\.-]+)\\.([a-z\\.]{2,6}))";
    public static final String PASSWORD = "(?:([a-zA-Z0-9_\\.\\, @!#$%^&+=()\\[\\]\"'*-]+))"; 
    public static final String PORT = "(?:([0-9]{1,5}))";
    // TODO: Should be replaced with a RegEx for a URL
    // CONNECTION_STRING_PREFIX = "(?:[a-zA-Z0-9]+)", 
    // ADDON_CONNECTION_STRING = "(?:" + CONNECTION_STRING_PREFIX + ":" + URL + ")|(?:" + URL +")"
    public static final String ADDON_CONNECTION_STRING = "ADDON_CONNECTION_STRING"; 
    public static final String ANY_VALUE = "(?:.*)";
}
