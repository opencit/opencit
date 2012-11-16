/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.backport.java.lang;

/**
 *
 * @author jbuhacoff
 */
public class String {
    public static boolean isEmpty(java.lang.String instance) {
        return instance.length() == 0;
    }
}
