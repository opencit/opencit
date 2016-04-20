/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package api.as;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.dcsg.cpg.io.ConfigurationUtil;
import com.intel.mtwilson.ApiClient;
import com.intel.mtwilson.api.ApiException;
import com.intel.mtwilson.api.ClientException;
import com.intel.mtwilson.datatypes.OemData;
import java.io.File;
import java.io.IOException;
import java.security.SignatureException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rksavinx
 */
public class WLMTest {
    private static Logger log = LoggerFactory.getLogger(ASHostTest.class);
    
    private static ApiClient c;
    @BeforeClass
    public static void setup() throws IOException, ClientException  {
        c = new ApiClient(ConfigurationUtil.fromPropertiesFile(new File("C:/Intel/CloudSecurity/RSATool.properties")));
    }
    
    @Test
    public void testConfigureWhiteList()throws IOException, ApiException, SignatureException {
        OemData oem = new OemData();
        oem.setDescription("Test OS");
        oem.setName("TEST");
        ObjectMapper mapper = new ObjectMapper();
        System.out.println("OemData Obj: " + mapper.writeValueAsString(oem));
        c.addOEM(oem);
    }
}
