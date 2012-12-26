/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.ui.console;

import com.intel.mtwilson.datatypes.Hostname;
import com.intel.mtwilson.validation.InputModel;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

/**
 *
 * @author jbuhacoff
 */
public class URLInput extends InputModel<URL> {

    @Override
    protected URL convert(String input) {
        try {
            URL url = new URL(input);
            return url;
        }
        catch(MalformedURLException e) {
            fault(e, "Invalid URL: %s", input);
        }
        return null;
    }
    

}
