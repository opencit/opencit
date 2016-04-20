/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test.api;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.intel.mtwilson.ApiClient;
import com.intel.mtwilson.api.ApiException;
import com.intel.mtwilson.api.ApiRequest;
import com.intel.mtwilson.api.ApiResponse;
import com.intel.mtwilson.api.ClientException;
import com.intel.mtwilson.datatypes.ApiClientCreateRequest;
import java.io.IOException;
import java.security.SignatureException;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_XML_TYPE;
import org.apache.commons.configuration.Configuration;

/**
 *
 * @author rksavinx
 */
public class ApiClientTest extends ApiClient {
    
    
    public ApiClientTest(Configuration config) throws ClientException {
        super(config);
    }
    
    public ApiRequest toXML(Object value) throws IOException {
        XmlMapper xml = new XmlMapper();
        return new ApiRequest(APPLICATION_XML_TYPE, xml.writeValueAsString(value));
    }
    
    public void testMediaType(ApiClientCreateRequest apiClient) throws IOException, ApiException, SignatureException {
        ApiRequest savino = toXML(apiClient);
        System.out.println("savino: " + savino.content);
        System.out.println("savino: " + savino.contentType);
        httpPost(asurl("/test/testMediaType"), toXML(apiClient));
    }
//    
//    public String[] getLocales() throws IOException, ApiException, SignatureException {
//        String[] locales = fromJSON(httpGet(msurl("/i18n/locales")), String[].class);
//        return locales;
//    }
//    
//    public void testGetLocales(ApiClientCreateRequest apiClient) throws IOException, ApiException, SignatureException {
//        ApiRequest savino = toXML(apiClient);
//        System.out.println("savino: " + savino.content);
//        System.out.println("savino: " + savino.contentType);
//        httpPost(asurl("/test/testMediaType"), toXML(apiClient));
//    }
    
}
