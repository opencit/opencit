/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package as;
/*
import com.intel.mountwilson.as.rest.data.TxtHost;
import com.intel.mountwilson.as.rest.data.TxtHostRecord;
import com.intel.mountwilson.client.AttestationService;
*/
import com.intel.mtwilson.ApiClient;
import com.intel.mtwilson.TrustAssertion;
import com.intel.mtwilson.model.*;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.commons.configuration.MapConfiguration;
import org.junit.BeforeClass;
import org.junit.Test;
import security.RegisterApiClientTest;

/**
 *
 * @author jbuhacoff
 */
public class ASClientTest {
    /*
    @Test
    public void testAddHost() throws MalformedURLException, IOException {
        AttestationService client = new AttestationService(new URL("http://10.1.71.84:8080/AttestationService/resources"), "cloudportal@intel", "nU8jTeJaFJZ7TJdMb4g4wAOljEHqwyFoRGvrsPjxrST8icOU");
        TxtHostRecord hostDetails = new TxtHostRecord();
        hostDetails.BIOS_Name = "BIOS Name";
        hostDetails.BIOS_Oem = "BIOS OEM";
        hostDetails.BIOS_Version = "1.2";
        hostDetails.Description = "";
        hostDetails.Email = "";
        hostDetails.HostName = "1.2.3.4";
        hostDetails.IPAddress = "1.2.3.4";
        hostDetails.Location = "";
        hostDetails.Port = 9999;
        hostDetails.VMM_Name = "VMM Name";
        hostDetails.VMM_OSName = "VMM OS Name";
        hostDetails.VMM_OSVersion = "2.3";
        hostDetails.VMM_Version = "3.4";
        TxtHost host = new TxtHost(hostDetails);
        client.addHost(host);
    }
    * 
    */

    private static ApiClient api;
    
    @BeforeClass
    public static void configure() throws Exception {
        // look for the properties file in our java classpath since this is a test class not production code
        // there should be one properties file for each environment being tested. 
        // DO NOT change the configuration of an existing properties file without coordinating with the team
        String filename = "/mtwilson.properties";
        InputStream in = RegisterApiClientTest.class.getResourceAsStream(filename);
        if( in == null ) {
            throw new FileNotFoundException("Cannot find properties: "+filename);
        }
        Properties config = new Properties();
        config.load(in);
        api = new ApiClient(new MapConfiguration(config));
    }
    
    /*
    @Test
    public void testSamlAssertion() throws Exception {
        String saml = api.getSamlForHost(new Hostname("1.2.3.4"));
        TrustAssertion assertion = api.verifyTrustAssertion(saml);
        assertion.getAssertion().getIssueInstant().toDate();
    }*/
}
