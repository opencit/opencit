/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.client.cmd;

import com.intel.mtwilson.ApiClient;
import com.intel.mtwilson.client.AbstractCommand;
import com.intel.mtwilson.datatypes.HostTrustResponse;
import com.intel.mtwilson.model.Hostname;

/**
 *     HostTrustResponse getHostTrust(Hostname hostname) throws IOException, ApiException, SignatureException;
 * 
 * @author jbuhacoff
 */
public class GetHostTrust extends AbstractCommand {

    @Override
    public void execute(String[] args) throws Exception {
        ApiClient api = getClient();
        HostTrustResponse response = api.getHostTrust(new Hostname(args[0]));
        System.out.println(toJson(response));
    }

}
