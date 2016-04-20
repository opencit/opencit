package com.intel.mtwilson.datatypes;

import java.util.HashMap;
import java.util.Map;

public enum HostWhiteListTarget {
	
        BIOS_OEM("OEM"),
        BIOS_HOST("Host"),
        VMM_GLOBAL("Global"),
        VMM_OEM("OEM"),
        VMM_HOST("Host");


        private String value;

        private HostWhiteListTarget(String value){
                this.setValue(value);
        }

        public String getValue() {
                return value;
        }

        public void setValue(String value) {
                this.value = value;
        }
        
        // Used for reverse mapping and retrieving the name given the value. We have created separte mapping classes for both BIOS and VMM since the value for 2 of the items are same.
        private static class BIOSTargetWLCache {
                private static Map<String, HostWhiteListTarget> targetWLCache = new HashMap<String,HostWhiteListTarget>();

                static {
                        targetWLCache.put(HostWhiteListTarget.BIOS_OEM.getValue(), HostWhiteListTarget.BIOS_OEM);
                        targetWLCache.put(HostWhiteListTarget.BIOS_HOST.getValue(), HostWhiteListTarget.BIOS_HOST);
                }          
        }
    
        // Used for reverse mapping and retrieving the name given the value. We have created separte mapping classes for both BIOS and VMM since the value for 2 of the items are same.        
        private static class VMMTargetWLCache {
                private static Map<String, HostWhiteListTarget> targetWLCache = new HashMap<String,HostWhiteListTarget>();

                static {
                        targetWLCache.put(HostWhiteListTarget.VMM_OEM.getValue(), HostWhiteListTarget.VMM_OEM);
                        targetWLCache.put(HostWhiteListTarget.VMM_HOST.getValue(), HostWhiteListTarget.VMM_HOST);
                        targetWLCache.put(HostWhiteListTarget.VMM_GLOBAL.getValue(), HostWhiteListTarget.VMM_GLOBAL);
                }          
        }
        
        public static HostWhiteListTarget getBIOSWhiteListTarget(String wlt) {
                return BIOSTargetWLCache.targetWLCache.get(wlt);
        }
        
        public static HostWhiteListTarget getVMMWhiteListTarget(String wlt) {
                return VMMTargetWLCache.targetWLCache.get(wlt);
        }        
}
