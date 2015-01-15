/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.extensions.plugins;

/**
 *
 * @author jbuhacoff
 */
public class WidgetBar implements Widget {

    @Override
    public String getType() {
        return "bar";
    }

    @Override
    public void run() {
        System.out.println("WidgetBar");
    }
    
}
