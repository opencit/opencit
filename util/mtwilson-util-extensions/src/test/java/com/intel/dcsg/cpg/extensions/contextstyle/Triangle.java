/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.extensions.contextstyle;

/**
 *
 * @author jbuhacoff
 */
//@Plugin // removing annotation to show that the finder will ignore this class without the annotation
public class Triangle implements Shape {

    @Override
    public String getName() {
        return "triangle";
    }

    @Override
    public String getColor() {
        return "green";
    }
    
    public int[] getSides() {
        return new int[] { 3, 4, 5 };
    }
}
