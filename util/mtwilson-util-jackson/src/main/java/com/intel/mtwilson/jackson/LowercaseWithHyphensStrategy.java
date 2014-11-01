/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jackson;

/**
 *
 * @author jbuhacoff
 */
public class LowercaseWithHyphensStrategy extends LowercaseWithSeparatorStrategy {

    public LowercaseWithHyphensStrategy() {
        setSeparator('-');
    }

}
