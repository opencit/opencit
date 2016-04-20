/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.ui.console;

import com.intel.dcsg.cpg.validation.InputModel;

/**
 *
 * @author jbuhacoff
 */
public class StringInput extends InputModel<String> {

    @Override
    protected String convert(String input) {
        return input;
    }
    

}
