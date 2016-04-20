/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mountwilson.ah.data;
/*
import static com.jayway.restassured.path.json.JsonPath.with;
import static org.junit.Assert.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
*/
/**
 * These tests verify that the data model serializes and de-serializes properly.
 *
 * @author jbuhacoff
 */
public final class AuthRequestTest {

//    private static ObjectMapper mapper = new ObjectMapper();

    /**
     * Sample serialized object.
     * {"client_id":"clientId","password":"password","user_name":"userName"}
     *
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    /*
    @Test
    public void writeJSON() throws JsonGenerationException,
            JsonMappingException, IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        mapper.writeValue(stream, new AuthRequest("clientId", "userName", "password"));

        String json = stream.toString();

        with(json).getString("client_id").equals("clientId");
        with(json).getString("user_name").equals("userName");
        with(json).getString("password").equals("password");
    }
*/
    /**
     * Sample serialized object.
     * {"client_id":"clientId","password":"password","user_name":"userName"}
     *
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    /*
    @Test
    public void readJSON() throws JsonGenerationException,
            JsonMappingException, IOException {
        InputStream stream = getClass().getResourceAsStream("/AuthRequestTest.sample.json");
        
        try{
        AuthRequest obj = mapper.readValue(stream, AuthRequest.class);

        
        
        assertEquals("default",obj.getClientId());
        assertEquals("admin",obj.getUserName());
        assertEquals("password",obj.getPassword());
        }finally{
        	stream.close();	
        }
    }*/


    
}
