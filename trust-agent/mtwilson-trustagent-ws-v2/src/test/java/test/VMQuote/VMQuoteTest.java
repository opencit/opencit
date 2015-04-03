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
import java.io.IOException;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
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
        
        try {
            VMQuote vmInstanceQuote = new VMQuote();
            vmInstanceQuote.setCumulativeHash("2284377e7a81243ab4305412669d90ba9253a64a");
            vmInstanceQuote.setVmInstanceId("efc8ac4b-bd45-49d2-880c-bdd64feeafd4");
            vmInstanceQuote.setDigestAlg("SHA-256");
            vmInstanceQuote.setNonce("abc8ac4b-bd45-49d2-880c-bdd64feeafd6");
            
            vmQuoteResponse.setVMQuote(vmInstanceQuote);
            
        } catch (Exception ex) {
            log.error("Error reading the vm quote file. {}", ex.getMessage());
        }
        
        try (FileInputStream measurementXMLFileStream = new FileInputStream(String.format("%s%s", instanceFolderPath, measurementXMLFileName))) {
        
            String measurementXML = IOUtils.toString(measurementXMLFileStream, "UTF-8");
            Measurements readMeasurements = jaxb.read(measurementXML, Measurements.class);
            vmQuoteResponse.setMeasurements(readMeasurements);

        } catch (Exception ex) {
            log.error("Error reading the measurement log. {}", ex.getMessage());
        }
        
//        try (FileInputStream signingKeyPemFileStream = new FileInputStream(String.format("%s%s", instanceFolderPath, signingKeyPemFileName))) {
        try {
            
            File signingKeyPemFile = new File(String.format("%s%s", instanceFolderPath, signingKeyPemFileName));
            //String signingKey = IOUtils.toString(signingKeyPemFileStream, "UTF-8");
            String signingKey = FileUtils.readFileToString(signingKeyPemFile);
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

        String quoteResponse = jaxb.write(vmQuoteResponse);
        FileUtils.write(new File(instanceFolderPath + "outputForMTW.xml"), quoteResponse);
        log.debug(quoteResponse);        
    }  
    
    @Test
    public void verifyVMQuoteResponse() {
        
        JAXB jaxb = new JAXB();
        try {
            VMQuoteResponse vmQuoteResponse = jaxb.read(FileUtils.readFileToString(new File(instanceFolderPath + "outputForMTW.xml")), VMQuoteResponse.class);
            log.debug(vmQuoteResponse.getSigningKeyCertificate());
            log.debug(vmQuoteResponse.getVMQuote().getCumulativeHash());
        } catch(IOException | JAXBException | XMLStreamException ex) {
            log.error("Error during deserialization of VM Quote Response. {}", ex.getMessage());
        }
        
    }
}
