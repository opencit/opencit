/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.cmd;

import com.intel.mtwilson.atag.AtagCommand;

/**
 *
 * @author jbuhacoff
 */
public class Version extends AtagCommand {

    @Override
    public void execute(String[] args) throws Exception {
        System.out.println("Provisioning Service version 0.1");
    }
    
}
