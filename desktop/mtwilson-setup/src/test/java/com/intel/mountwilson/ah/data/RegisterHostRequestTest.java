/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mountwilson.ah.data;
/*
import static com.jayway.restassured.path.json.JsonPath.with;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import static org.junit.Assert.*;
import org.junit.Test;
*/
/**
 * These tests verify that the data model serializes and de-serializes properly.
 * 
 * Sample serialized object:
 * {"client_id":"clientId","user_name":"userName","password":"password","host_name":"host1","ip_address":null,"bios_build_no":null,"vmm_build_no":null,"email_address":null,"cache_validity_mins":0,"addon_connection_string":null,"hostName":"host1","port":0,"vmm":null,"ipAddress":null,"bios":null,"description":null,"addonConnectionString":null,"vmmbuildNo":null,"biosBuildNo":null,"cacheValidityMins":0,"emailAddress":null}
 *
 * @author jbuhacoff
 */
public final class RegisterHostRequestTest {

//    private static ObjectMapper mapper = new ObjectMapper();

    /**
     * Tests serializing the java object to JSON format
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    /*
    @Test
    public void writeJSON() throws JsonGenerationException,
            JsonMappingException, IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        mapper.writeValue(stream, new RegisterHostRequest("clientId", "userName", "password", "host1"));

        String json = stream.toString();
        System.out.println(json);

        with(json).getString("client_id").equals("clientId");
        with(json).getString("host_name").equals("host1");
    }
*/
    /**
     * Tests deserializing the JSON format into a Java object
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    /*
    @Test
    public void readJSON() throws JsonGenerationException,
            JsonMappingException, IOException {
    	InputStream inputStream = getClass().getResourceAsStream("/RegisterHostRequestTest.sample.json");
    	try{
        RegisterHostRequest obj = mapper.readValue(inputStream, RegisterHostRequest.class);
        assertEquals("clientId",obj.getClientId());
        assertEquals("userName",obj.getUserName());
        assertEquals("password",obj.getPassword());
        assertEquals("host1",obj.getHostName());
    	}finally{
    		inputStream.close();	
    	}
        
        
       
    }
    * 
    */
}
