/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.ui.console;

import com.intel.mtwilson.model.InternetAddress;
import com.intel.dcsg.cpg.validation.InputModel;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 *
 * @author jbuhacoff
 */
public class InternetAddressInput extends InputModel<InternetAddress> {

    @Override
    protected InternetAddress convert(String input) {
        if( input.isEmpty() ) { return null; }
        try {
            InternetAddress address = new InternetAddress(input);
            if( address.isValid() ) {
                InetAddress inet = InetAddress.getByName(address.toString());
                if( inet.isReachable(5000) ) {
                    return address;
                }
                else {
                    fault("Not reachable: %s", input);
                }
            }
            fault("Unrecognized internet address: %s", input);
        }
        catch(UnknownHostException e) {
            fault(e, "Unknown host: %s", input);
        }
        catch(IOException e) {
            fault(e, "Network error: %s", input);
        }
        return null;
    }
    

}
