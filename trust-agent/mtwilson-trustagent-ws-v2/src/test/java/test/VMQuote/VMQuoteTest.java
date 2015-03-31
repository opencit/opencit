/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.VMQuote;

import com.intel.dcsg.cpg.xml.JAXB;
import org.junit.Test;
import com.intel.mtwilson.vmquote.xml.*;
import java.io.File;
import java.io.FileInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author ssbangal
 */
public class VMQuoteTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(VMQuoteTest.class);
    private static final String instanceFolderPath = "c:/temp/vmquotetest/";
    private static final String measurementXMLFileName = "measurement.xml";
    private static final String trustPolicyFileName = "TrustPolicy.xml";
    private static final String signingKeyPemFileName = "signingkey.pem";
    
    @Test
    public void CreateVMQuoteResponse() throws Exception {
        VMQuoteResponse vmQuoteResponse = new VMQuoteResponse();
        
        JAXB jaxb = new JAXB();
        
        try (FileInputStream measurementXMLFileStream = new FileInputStream(String.format("%s%s", instanceFolderPath, measurementXMLFileName))) {
        
            String measurementXML = IOUtils.toString(measurementXMLFileStream, "UTF-8");
            Measurements readMeasurements = jaxb.read(measurementXML, Measurements.class);
            vmQuoteResponse.setMeasurements(readMeasurements);

        } catch (Exception ex) {
            log.error("Error reading the measurement log. {}", ex.getMessage());
        }
        
        try (FileInputStream signingKeyPemFileStream = new FileInputStream(String.format("%s%s", instanceFolderPath, signingKeyPemFileName))) {
            
            String signingKey = IOUtils.toString(signingKeyPemFileStream, "UTF-8");
            vmQuoteResponse.setSigningKeyCertificate(signingKey);
            
        } catch (Exception ex) {
            log.error("Error reading the signing key file. {}", ex.getMessage());
        }
        
        
        try (FileInputStream trustPolicyFileStream = new FileInputStream(String.format("%s%s", instanceFolderPath, trustPolicyFileName))) {
        
            String trustPolicyXML = IOUtils.toString(trustPolicyFileStream, "UTF-8");
            TrustPolicy trustPolicy = jaxb.read(trustPolicyXML, TrustPolicy.class);
            vmQuoteResponse.setTrustPolicy(trustPolicy);

        } catch (Exception ex) {
            log.error("Error reading the measurement log. {}", ex.getMessage());
        }

        String writeMeasurements = jaxb.write(vmQuoteResponse);
        log.debug(writeMeasurements);
        
    }
    
}
