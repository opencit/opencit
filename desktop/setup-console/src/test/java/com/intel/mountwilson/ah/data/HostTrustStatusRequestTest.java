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
 * @author jbuhacoff
 */
public final class HostTrustStatusRequestTest {

//	private static ObjectMapper mapper = new ObjectMapper();

	/**
	 * Sample serialized object.
	 * {"hostAddresses":["host1","host2"],"force_verify"
	 * :false,"client_id":"clientId"
	 * ,"password":"password","user_name":"userName"}
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
		mapper.writeValue(stream, new HostTrustStatusRequest("clientId",
				"userName", "password", "host1,host2", false));

		String json = stream.toString();

		with(json).getString("client_id").equals("clientId");
		with(json).getList("hostAddresses", String.class).containsAll(
				Arrays.asList(new String[] { "host1", "host2" }));
	}
*/
	/**
	 * Sample serialized object.
	 * {"hostAddresses":["host1","host2"],"force_verify"
	 * :false,"client_id":"clientId"
	 * ,"password":"password","user_name":"userName"}
	 * 
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
    /*
	@Test
	public void readJSON() throws JsonGenerationException,
			JsonMappingException, IOException {
		InputStream inputStream = getClass().getResourceAsStream(
				"/HostTrustStatusRequestTest.sample.json");

		try {
			HostTrustStatusRequest obj = mapper.readValue(inputStream,
					HostTrustStatusRequest.class);

			assertEquals("clientId", obj.getClientId());
			assertEquals("userName", obj.getUserName());
			assertEquals("password", obj.getPassword());
			assertTrue(Arrays.asList(new String[] { "host1", "host2" })
					.containsAll(obj.getHostAddresses()));
			assertFalse(obj.getForceVerify());
		} finally {
			inputStream.close();
		}
	}
        * 
        */
}
