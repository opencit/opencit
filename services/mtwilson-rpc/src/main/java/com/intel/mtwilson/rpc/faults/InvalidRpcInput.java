/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.rpc.faults;

import com.intel.mtwilson.util.validation.faults.Thrown;

/**
 *
 * @author jbuhacoff
 */
public class InvalidRpcInput extends Thrown {
    public InvalidRpcInput(Throwable cause) {
        super(cause);
    }
}
