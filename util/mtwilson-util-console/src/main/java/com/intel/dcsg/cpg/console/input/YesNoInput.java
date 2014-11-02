/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.console.input;

import com.intel.dcsg.cpg.console.YesNo;
import com.intel.dcsg.cpg.validation.InputModel;

/**
 *
 * @author jbuhacoff
 */
public class YesNoInput extends InputModel<YesNo> {

    @Override
    protected YesNo convert(String input) {
        try {
            YesNo selection = YesNo.valueOf(input.charAt(0));
            return selection;
        }
        catch(IllegalArgumentException e) {
            fault(e, "Allowed values: 'Yes' or 'No'");
        }
        return null;
    }
    

}
