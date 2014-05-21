/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package test.io;

import com.intel.mtwilson.api.*;
import com.intel.mtwilson.datatypes.OpenStackHostTrustLevelQuery;
import com.intel.mtwilson.model.Hostname;
import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.mtwilson.datatypes.ErrorResponse;
//import org.codehaus.jackson.map.DeserializationConfig;
//import org.codehaus.jackson.map.ObjectMapper;
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
        catch(com.fasterxml.jackson.core.JsonParseException e) {
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

    @Test
    public void testMultipleDeserializeJsonToHostnameSinglePOJO() throws IOException, ApiException {
        String jsonSingle = "{\"hosts\":\"myHostname\"}";
        String jsonArray = "{\"hosts\":[\"myHostname\"]}";
        //mapper.configure(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        OpenStackHostTrustLevelQuery single = fromJSON(jsonSingle, OpenStackHostTrustLevelQuery.class);
        System.out.println("(single) count: "+single.hosts.length+", first: "+single.hosts[0].toString());
        OpenStackHostTrustLevelQuery array = fromJSON(jsonArray, OpenStackHostTrustLevelQuery.class);
        System.out.println("(array) count: "+array.hosts.length+", first: "+array.hosts[0].toString());
    }


    /**
No suitable constructor found for type [simple type, class com.intel.mtwilson.datatypes.ErrorResponse]: can not instantiate from JSON object (need to add/enable type information?)
 at [Source: java.io.StringReader@380076e8; line: 1, column: 2]
com.fasterxml.jackson.databind.JsonMappingException: No suitable constructor found for type [simple type, class com.intel.mtwilson.datatypes.ErrorResponse]: can not instantiate from JSON object (need to add/enable type information?)
 at [Source: java.io.StringReader@380076e8; line: 1, column: 2]
     * 
     * @throws Exception 
     */
    @Test
    public void testErrorResponse() throws Exception {
//        ErrorResponse response = new ErrorResponse(new ErrorMessage(ErrorCode.UNKNOWN_ERROR, "test"));
//        String json = toJSON(response);
        String json = "{\"error_code\":\"UNKNOWN\", \"error_message\":\"test\"}";
        log.debug("response: {}", json);
        ErrorResponse response2 = fromJSON(json, ErrorResponse.class);
        log.debug("response2: {} - {}", response2.getErrorCode(), response2.getErrorMessage());
    }
}
