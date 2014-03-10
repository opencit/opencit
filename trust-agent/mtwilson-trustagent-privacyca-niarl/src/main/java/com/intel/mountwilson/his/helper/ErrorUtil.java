// NEEDS TO BE INCORPORATED INTO 2.0 AS CPG PACKAGE******************************************************************
/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mountwilson.his.helper;

/**
 *
 * @author jbuhacoff
 * @since 0.1.2
 */
public class ErrorUtil {
    
    /**
     * Returns the root cause of the given Throwable. If the Throwable does not have a cause,
     * the same Throwable is returned.
     * 
     * @param e in which to find the root cause
     * @return the root cause of e or e itself
     */
    public static Throwable rootCause(Throwable e) {
        Throwable t = e;
        Throwable next;
        while(t != null ) {
            next = t.getCause();
            if( next == null ) {
                break;
            }
            t = next;
        }
        return t;
    }
    
    /**
     * Returns the first cause of the given Throwable which is an instance of the given Class. 
     * If a match is not found this method returns null.
     * 
     * @param e in which to find the given cause
     * @param clazz the class of the cause to find in e
     * @return the first Throwable in e's cause chain that is an instance of clazz, or null if there is no match
     */
    public static Throwable findCause(Throwable e, Class clazz) {
        Throwable t = e;
        while(t != null ) {
            if( clazz.isInstance(t) ) {
                break;
            }
            t = t.getCause();
        }
        return t;
    }
}
