/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tag.setup.cmd;

import com.intel.mtwilson.tag.setup.TagCommand;

/**
 *
 * @author jbuhacoff
 */
public class TagVersion extends TagCommand {

    @Override
    public void execute(String[] args) throws Exception {
        System.out.println("Provisioning Service version 0.1");
    }
    
}
