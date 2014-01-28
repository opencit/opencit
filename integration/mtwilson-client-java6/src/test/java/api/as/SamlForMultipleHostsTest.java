/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package api.as;

import com.intel.dcsg.cpg.xml.JAXB;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import com.intel.mtwilson.api.*;
import com.intel.mtwilson.datatypes.xml.HostTrustXmlResponse;
import com.intel.mtwilson.datatypes.xml.HostTrustXmlResponseList;
import java.io.StringReader;
import java.util.List;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.httpclient.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class SamlForMultipleHostsTest {
    private Logger log = LoggerFactory.getLogger(getClass());
    
    @Test
    public void testSamlForMultipleHosts() throws IOException, ApiException, JAXBException, XMLStreamException {
        InputStream in = getClass().getResourceAsStream("/InvalidSamlForMultipleHostResponse.txt");
        String responseText = IOUtils.toString(in, "UTF-8");
        IOUtils.closeQuietly(in);
        // the response contains "an invalid character" according to IE, so let's check it out:
        for(int i=0; i<responseText.length(); i++) {
            char c = responseText.charAt(i);
            if( Character.isLetter(c) ) { continue; }
            if( Character.isDigit(c) ) { continue; }
            if( Character.isWhitespace(c) ) { continue; }
            if( c == '<' || c == '>' || c == '/' || c == ':' || c == '=' || c=='"' || c == '[' || c == ']' || c == '-' || c == '.' || c == '_' ) { continue; }
            if( c == '&' || c == '#' || c == '+' || c == ';' || c == ',' || c=='!' || c == '?' ) { continue; }
            log.debug("Index: {}  Character: {}", i, c);
            responseText = responseText.replace(c, ' ');
        }
        
//        ApiResponse response = new ApiResponse(HttpStatus.SC_OK, HttpStatus.getStatusText(HttpStatus.SC_OK), MediaType.TEXT_XML_TYPE, responseText.getBytes("UTF-8"));
        HostTrustXmlResponseList statuslist = xml(responseText, HostTrustXmlResponseList.class);
        List<HostTrustXmlResponse> list = statuslist.getHost();
        for(HostTrustXmlResponse status :list) {
            log.debug("Got status for host: {}", status.getName());
        }
    }
    
    
    private <T> T xml(String document, Class<T> valueType) throws IOException, ApiException, JAXBException, XMLStreamException {
        JAXB jaxb = new JAXB(); // fix for bug #1038 xml external entity injection (XXE) vulnerability
        return jaxb.read(document,valueType);
    }
    
}
