/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.datatypes;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author ssbangal
 */
public enum ValidationRegEx {

    DEFAULT_PATTERN("^[a-zA-Z0-9_-.]*$"),
    IPADDRESS_PATTERN("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$"),
    FQDN_PATTERN("^(([a-zA-Z]|[a-zA-Z][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z]|[A-Za-z][A-Za-z0-9\\-]*[A-Za-z0-9])$"),
    IPADDR_FQDN_PATTERN("(^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$)|(^(([a-zA-Z]|[a-zA-Z][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z]|[A-Za-z][A-Za-z0-9\\-]*[A-Za-z0-9])$)"),
    EMAIL_PATTERN("^([a-z0-9_\\.-]+)@([\\da-z\\.-]+)\\.([a-z\\.]{2,6})$"),
    PASSWORD_PATTERN("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$"),
    PORT("^[0-9]{1,5}"),
    ADDON_CONNECTION_STRING("ADDON_CONNECTION_STRING");
    private String regEx;

    public String getRegEx() {
        return regEx;
    }

    private ValidationRegEx(String regEx) {
        this.regEx = regEx;
    }

    private static class RegExCache {
        private static Map<String, ValidationRegEx> regExCache = new HashMap<String, ValidationRegEx>();

        static {
            for (final ValidationRegEx regex : ValidationRegEx.values()) {
                regExCache.put(regex.name(), regex);
            }
        }
    }

    public static ValidationRegEx getValidationRegEx(String regExName) {
        return ValidationRegEx.RegExCache.regExCache.get(regExName);
    }
}
