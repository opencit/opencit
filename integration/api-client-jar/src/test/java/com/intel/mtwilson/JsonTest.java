/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson;

import com.intel.mtwilson.datatypes.Hostname;
import java.io.IOException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class JsonTest {
    private static Logger log = LoggerFactory.getLogger(JsonTest.class);
    protected static final ObjectMapper mapper = new ObjectMapper();

    // copy of ApiClient.toJSON
    private String toJSON(Object value) throws IOException {
        return mapper.writeValueAsString(value);        
    }

    // copy of ApiClient.fromJSON
    private <T> T fromJSON(String document, Class<T> valueType) throws IOException, ApiException {
        try {
            return mapper.readValue(document, valueType);
        }
        catch(org.codehaus.jackson.JsonParseException e) {
            log.error("Cannot parse response: "+document);
            throw new ApiException("Cannot parse response", e);
        }
    }
    
    @Test
    public void testSerializeHostnameToJson() throws IOException {
        Hostname a = new Hostname("myHostname");
        String json = toJSON(a);
        System.out.println(json);
    }
    
    @Test
    public void testDeserializeJsonToHostname() throws IOException, ApiException {
        String json = "\"myHostname\"";
        Hostname a = fromJSON(json, Hostname.class);
        System.out.println(a.toString());
    }
    
}
