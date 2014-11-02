/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.extensions.contextstyle;

/**
 *
 * @author jbuhacoff
 */
public class Circle implements Shape {

    @Override
    public String getName() {
        return "circle";
    }

    @Override
    public String getColor() {
        return "black";
    }
    
    public int getRadius() {
        return 5;
    }
}
