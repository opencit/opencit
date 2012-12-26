/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package test.io;

import com.intel.mtwilson.ApiException;
import com.intel.mtwilson.datatypes.Hostname;
import com.intel.mtwilson.datatypes.OpenStackHostTrustLevelQuery;
import java.io.IOException;
import org.codehaus.jackson.map.DeserializationConfig;
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

    @Test
    public void testMultipleDeserializeJsonToHostnameSinglePOJO() throws IOException, ApiException {
        String jsonSingle = "{\"hosts\":\"myHostname\"}";
        String jsonArray = "{\"hosts\":[\"myHostname\"]}";
        mapper.configure(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        OpenStackHostTrustLevelQuery single = fromJSON(jsonSingle, OpenStackHostTrustLevelQuery.class);
        System.out.println("(single) count: "+single.hosts.length+", first: "+single.hosts[0].toString());
        OpenStackHostTrustLevelQuery array = fromJSON(jsonArray, OpenStackHostTrustLevelQuery.class);
        System.out.println("(array) count: "+array.hosts.length+", first: "+array.hosts[0].toString());
    }

}
