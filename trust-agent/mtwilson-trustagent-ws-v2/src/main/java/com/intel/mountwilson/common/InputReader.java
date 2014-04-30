/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mountwilson.common;

import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author jbuhacoff
 */
public class InputReader implements Runnable {
    private final InputStream in;
    private String result = null;
    
    public InputReader(InputStream in) {
        this.in = in;
    }
    
    @Override
    public void run() {
        try {
            result = IOUtils.toString(in);
        }
        catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getResult() {
        return result;
    }

    
}
