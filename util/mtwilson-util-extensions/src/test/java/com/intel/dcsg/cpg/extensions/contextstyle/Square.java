/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.extensions.contextstyle;

import com.intel.dcsg.cpg.extensions.Plugin;

/**
 *
 * @author jbuhacoff
 */
@Plugin
public class Square implements Shape {

    @Override
    public String getName() {
        return "square";
    }

    @Override
    public String getColor() {
        return "red";
    }
    
    public int getLength() {
        return 7;
    }
    
}
