    /*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.mtwilson.v2.rpc.RpcUtil;
import org.apache.http.HeaderElement;
//import org.apache.commons.httpclient.HeaderElement;
import org.apache.http.message.BasicHeaderValueParser;
import org.apache.http.util.CharArrayBuffer;
import org.apache.http.message.ParserCursor;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class ParseAcceptHeaderTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ParseAcceptHeaderTest.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    
    @Test
    public void testParseAcceptHeader() throws JsonProcessingException {
        String accept = "application/json;q=0.9, application/xml;q=0.8, text/plain, */*";
        CharArrayBuffer buffer = new CharArrayBuffer(accept.length());
        buffer.append(accept);
        BasicHeaderValueParser parser = new BasicHeaderValueParser();
        HeaderElement[] headerElements = parser.parseElements(buffer, new ParserCursor(0,accept.length())); // xxx do we need accept.length - 1 ? 
        log.debug("Header elements: {}", mapper.writeValueAsString(headerElements));
        // sample output:
        //  Header elements: [{"name":"application/json","value":null,"parameters":[{"name":"0.9","value":null}],"parameterCount":1},{"name":"application/xml","value":null,"parameters":[{"name":"0.8","value":null}],"parameterCount":1},{"name":"text/plain","value":null,"parameters":[],"parameterCount":0},{"name":"*/*","value":null,"parameters":[],"parameterCount":0}]
        
    }
    
    @Test
    public void testParseAcceptHeader2() throws Exception {
        String accept = "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8";
        String preferred = RpcUtil.getPreferredTypeFromAccept(accept);
        log.debug("Preferred: {}", preferred);
    }
    
}
