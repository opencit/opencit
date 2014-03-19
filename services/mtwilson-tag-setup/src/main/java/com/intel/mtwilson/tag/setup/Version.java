/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tag.setup;

/**
 *
 * @author jbuhacoff
 */
public class Version extends TagCommand {

    @Override
    public void execute(String[] args) throws Exception {
        System.out.println("Provisioning Service version 0.1");
    }
    
}
