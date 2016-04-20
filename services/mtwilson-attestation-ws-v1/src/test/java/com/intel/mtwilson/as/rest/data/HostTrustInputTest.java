package com.intel.mtwilson.as.rest.data;

import com.intel.mountwilson.as.common.ASException;
import com.intel.mountwilson.as.common.ValidationException;
import com.intel.mtwilson.i18n.ErrorCode;
import com.intel.mtwilson.datatypes.OpenStackHostTrustLevelQuery;
import com.intel.mtwilson.model.*;
//import static com.jayway.restassured.path.json.JsonPath.with;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
//import org.codehaus.jackson.JsonGenerationException;
//import org.codehaus.jackson.map.JsonMappingException;
//import org.codehaus.jackson.map.ObjectMapper;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * These tests verify that the data model serializes and de-serializes properly.
 *
 * @author jbuhacoff
 */
public class HostTrustInputTest {
    private static ObjectMapper mapper = new ObjectMapper();

    /**
     * Sample serialized object.
     * {"count":2,"pcrmask":"some pcr mask","hosts":["test-host-1","ESX host 2"]}
     *
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    @Test
    public void writeJSON() throws JsonGenerationException,
            JsonMappingException, IOException {
        OpenStackHostTrustLevelQuery test = new OpenStackHostTrustLevelQuery();
        test.count = 2;
        test.pcrMask = "some pcr mask";
        test.hosts = new Hostname[] { new Hostname("test-host-1"), new Hostname("ESX host 2") };
        
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        mapper.writeValue(stream, test);

        String json = stream.toString();
        System.out.println(json);
/*
        assertEquals(2, with(json).getInt("count"));
        with(json).getString("pcrmask").equals("userName");
        Arrays.asList(with(json).getList("hosts")).containsAll(Arrays.asList(new String[] { "test-host-1", "ESX host 2" }));
        */
    }
    
    /**
     * Sample serialized object.
     * {"count":2,"pcrmask":"some pcr mask","hosts":["test-host-1","ESX host 2"]}
     *
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    @Test
    public void readJSON() throws JsonGenerationException,
            JsonMappingException, IOException {
        
    	InputStream in = getClass().getResourceAsStream("HostTrustInputTest.sample.json");
    	try {
	        OpenStackHostTrustLevelQuery obj = mapper.readValue(in, OpenStackHostTrustLevelQuery.class);
	
	        assertEquals(2, obj.count);
	        assertEquals("some pcr mask",obj.pcrMask);
	        assertEquals("test-host-1",obj.hosts[0].toString());
	        assertEquals("ESX host 2",obj.hosts[1].toString());
    	}
    	finally {
    		if( in != null )
    			in.close();
    	}
    }

    
    @Test(expected=ValidationException.class)
    public void nullHostnameThrowsException() {  // datatype.Hostname
        Hostname h = new Hostname(null);
        System.err.println(h.toString());
    }

    @Test(expected=ValidationException.class)
    public void emptyHostnameThrowsException() {  // datatype.Hostname
    	Hostname h = new Hostname("");
        System.err.println(h.toString());
    }

    @Test(expected=ValidationException.class)
    public void invalidHostnameThrowsException() {  // datatype.Hostname
    	Hostname h = new Hostname("invalid, hostname has comma in it");
        System.err.println(h.toString());
    }

    @Test(expected=ASException.class)
    public void convertIllegalArgumentExceptionToASException() {
        try {
        	Hostname h = new Hostname("");            
            System.err.println(h.toString());
        }
        catch(IllegalArgumentException e) {
            throw new ASException(ErrorCode.AS_MISSING_INPUT, e.getMessage());
        }
    }
}
