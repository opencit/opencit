/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.extensions;

import java.util.Comparator;
import java.util.List;

/**
 *
 * @author jbuhacoff
 */
public class PreferenceComparator implements Comparator<Class<?>> {
    private List<String> preferenceOrder;
    public PreferenceComparator(List<String> preferenceOrder) {
        this.preferenceOrder = preferenceOrder;
    }
    
    // given a preference order to use for sorting,  will compare two classes
    // according to their position in the preferences. 
    // preferences are ordered highest-to-lowest.  first preference is index 0, next best preference is index 1, etc.
    // so normally when compare returns -1 it means first arg is "less than" second arg which would put it at a lower index... but we're doing a reverse sort with lower index meaning "greater than" a higher index.
    @Override
    public int compare(Class<?> o1, Class<?> o2) {
        int index1 = preferenceOrder.indexOf(o1.getName()); // -1 if it is not in the list,  0 if it's highest priority, 1 second priority, ...
        int index2 = preferenceOrder.indexOf(o2.getName()); // -1 if it is not in the list,  0 if it's highest priority, 1 second priority, ...
        if( index1 == index2 ) { return 0; } // if both are -1 (not in list) or both are same position
        if( index1 == -1 ) { return 1; } // o1 is not in list but o2 is in the list, so o1 is lower priority than o2
        if( index2 == -1 ) { return -1; } // o1 is in the list but o2 is not in the list, so o1 is higher priority than o2
        if( index1 < index2 ) { return -1; } // o1 and o2 are in the list and o1 is higher priority
        return 1; // o1 and o2 are in the list and o1 is lower priority (only option left)
    }    
}
