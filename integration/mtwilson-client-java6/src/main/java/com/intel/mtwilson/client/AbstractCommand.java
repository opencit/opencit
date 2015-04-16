/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.client;

import com.intel.mtwilson.ApiClient;
import com.intel.mtwilson.api.*;
import com.intel.dcsg.cpg.configuration.CommonsConfigurationUtil;
import java.io.IOException;
import org.apache.commons.configuration.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.mtwilson.My;
//import org.codehaus.jackson.map.ObjectMapper;

/**
 *
 * @author jbuhacoff
 */
public abstract class AbstractCommand implements Command {
    private ApiClient client;
    private static final ObjectMapper mapper = new ObjectMapper();
    
    protected ApiClient getClient() throws ClientException, IOException { 
        if( client == null ) {
            client = createClient();
        }
        return client;
    }

    private ApiClient createClient() throws ClientException, IOException {
        Configuration conf = My.configuration().getConfiguration(); //CommonsConfigurationUtil.getConfiguration(); // tries jvm properties, environment variables, then mtwilson.properties;  you can set location of mtwilson.properties with -Dmtwilson.home=/path/to/dir
        return new ApiClient(conf);
    }
    
    protected String toJson(Object value) throws ClientException {
        try {
            return mapper.writeValueAsString(value);
        }
        catch(Exception e) {
            throw new ClientException("Cannot serialize object", e);
        }
    }
}
