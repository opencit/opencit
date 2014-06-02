package com.intel.mtwilson.ms.business;

//import com.fasterxml.jackson.annotation.JsonInclude;
//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.security.cert.CertificateException;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

import com.intel.mtwilson.datatypes.ApiClientCreateRequest;
import com.intel.mtwilson.datatypes.ApiClientInfo;
import com.intel.mtwilson.datatypes.ApiClientSearchCriteria;
import com.intel.mtwilson.datatypes.Role;
import java.util.HashSet;
import java.util.List;

public class ApiClientBOTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ApiClientBOTest.class);
    
    @Test
    public void getApiClientList() {
        
        ApiClientSearchCriteria apiSearchObj = new ApiClientSearchCriteria();
        apiSearchObj.enabledEqualTo = true;
        ApiClientBO clientBO = new ApiClientBO();
        List<ApiClientInfo> searchResult = clientBO.search(apiSearchObj);
        for (ApiClientInfo apiObj : searchResult)
            System.out.println(apiObj.name);
    }

	@Test
	public void testCreate() throws CertificateException, IOException {
		
		
		try {
			ApiClientCreateRequest apiClientRequest = new ApiClientCreateRequest();
			
			apiClientRequest.setCertificate(readCertificate("C:/work/temp/mw-0.5.2-integration-new/services/ManagementService/src/test/java/com/intel/mountwilson/ms/business/206.cer").getBytes());
			apiClientRequest.setRoles(new String[]{Role.Security.toString()});
			
			new ApiClientBO().create(apiClientRequest);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

    public static String readCertificate(String fileName) throws CertificateException, IOException {
			javax.security.cert.X509Certificate cert = javax.security.cert.X509Certificate
					.getInstance(readfile(fileName));
			return "-----BEGIN CERTIFICATE-----\n"
			+ new String(Base64.encodeBase64(cert.getEncoded(),true))
			+ "-----END CERTIFICATE-----";
			
    }
        public static byte[] readfile(String fileName) throws IOException  {

            
            InputStream fStream = null;
            try {
            	int fileLength = (int) new File(fileName).length();
                fStream = new FileInputStream(fileName);
                byte[] fileContents = new byte[fileLength];
                fStream.read(fileContents);
                return fileContents;
            }finally{
    		try {
                    fStream.close();
    		} catch (IOException e) {
                    System.out.println("Error while closing stream" +e.getMessage());
    		}
            }
            
            
        }
	
//        @JacksonXmlRootElement(localName="user_comment")
//        @JsonInclude(JsonInclude.Include.NON_EMPTY) // jackson 2.0
//        @JsonIgnoreProperties(ignoreUnknown=true)
        public static class UserComment {
            public HashSet<String> roles = new HashSet<>();
        }
    private ObjectMapper createDefaultMapper() {
        YAMLFactory yamlFactory = new YAMLFactory();
        yamlFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        yamlFactory.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
        ObjectMapper mapper = new ObjectMapper(yamlFactory);
        mapper.setPropertyNamingStrategy(new PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy());
        return mapper;
    }
        
        @Test
        public void testRequestedRolesInComment() throws JsonProcessingException {
            UserComment comment = new UserComment();
            comment.roles.add("Attestation");
            comment.roles.add("Whitelist");
            ObjectMapper mapper = createDefaultMapper();
            log.debug(mapper.writeValueAsString(comment));
        }
	
}
