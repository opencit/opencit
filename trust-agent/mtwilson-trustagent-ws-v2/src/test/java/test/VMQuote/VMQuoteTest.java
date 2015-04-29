/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.VMQuote;

import com.intel.dcsg.cpg.xml.JAXB;
import com.intel.mtwilson.vmquote.xml.TrustPolicy;
import com.intel.mtwilson.vmquote.xml.VMQuote;
import org.junit.Test;
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
    private static final String trustPolicyFileName = "TrustPolicy-201503161031.xml";
    private static final String vmQuoteFileName = "VMQuote.xml";
    
    
    @Test
    public void CreateVMQuote() throws Exception {
                
        //com.intel.mtwilson.trustagent.model.VMQuoteResponse vmQuoteResponse = new com.intel.mtwilson.trustagent.model.VMQuoteResponse();
        //vmQuoteResponse.setVmMeasurements(FileUtils.readFileToByteArray(new File(String.format("%s%s", instanceFolderPath, measurementXMLFileName))));
        //vmQuoteResponse.setVmTrustPolicy(FileUtils.readFileToByteArray(new File(String.format("%s%s", instanceFolderPath, trustPolicyFileName))));

//        String trustPolicyXML = IOUtils.toString(vmQuoteResponse.getVmTrustPolicy(), "UTF-8");
//        boolean valid = ValidateSignature.isValid(trustPolicyXML);
//        log.debug("Validation result is {}.", valid);
    }
    
    @Test
    public void CreateVMQuoteResponse() throws Exception {

        VMQuote vmquote = new VMQuote();
        byte[] vmQuoteBytes = FileUtils.readFileToByteArray(new File(String.format("%s%s", instanceFolderPath, vmQuoteFileName)));
        String vmQuoteString = IOUtils.toString(vmQuoteBytes, "UTF-8");
        
        byte[] tpBytes = FileUtils.readFileToByteArray(new File(String.format("%s%s", instanceFolderPath, trustPolicyFileName)));
        String tpString = IOUtils.toString(tpBytes, "UTF-8");
        
        JAXB jaxb = new JAXB();
        VMQuote read = jaxb.read(vmQuoteString, VMQuote.class);
        TrustPolicy tp = jaxb.read(tpString, TrustPolicy.class);
        
        log.debug(tp.getLaunchControlPolicy());
        log.debug(read.getCumulativeHash());
        
    }  
    
    /*
    @Test
    public void CreateVMQuoteSimpleResponse() throws Exception {
    
        JAXB jaxb = new JAXB();
        VMQuoteSimple simpleResponse = new VMQuoteSimple();
        
        try (FileInputStream measurementXMLFileStream = new FileInputStream(String.format("%s%s", instanceFolderPath, measurementXMLFileName))) {
        
            String measurementXML = IOUtils.toString(measurementXMLFileStream);
            simpleResponse.setMeasurementLog(measurementXML);

        } catch (Exception ex) {
            log.error("Error reading the measurement log. {}", ex.getMessage());
        }
                
        try (FileInputStream trustPolicyFileStream = new FileInputStream(String.format("%s%s", instanceFolderPath, trustPolicyFileName))) {
        
            String trustPolicyXML = IOUtils.toString(trustPolicyFileStream);
            simpleResponse.setTrustPolicy(trustPolicyXML);

        } catch (Exception ex) {
            log.error("Error reading the measurement log. {}", ex.getMessage());
        }

        String quoteResponse = jaxb.write(simpleResponse);
        VMQuoteSimple ret = jaxb.read(quoteResponse, VMQuoteSimple.class);
        log.debug("Testing {}", ret.getTrustPolicy().toString());
        
        boolean valid = ValidateSignature.isValid(ret.getTrustPolicy().toString());
        log.debug("Signature validation result is {}", valid);
        
        FileUtils.write(new File(instanceFolderPath + "outputForMTWSimple.xml"), quoteResponse);
        log.debug(quoteResponse);        
        
    }*/
    
    /*
    
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
    public void verifyVMQuoteResponse() throws Exception {
        
        JAXB jaxb = new JAXB();
        try {
            CreateVMQuoteSimpleResponse();
            VMQuoteResponse vmQuoteResponse = jaxb.read(FileUtils.readFileToString(new File(instanceFolderPath + "TrustPolicy.xml")), VMQuoteResponse.class);
            String trustPolicyXml = FileUtils.readFileToString(new File(instanceFolderPath + "TrustPolicy-201503161031.xml")); //jaxb.write(vmQuoteResponse.getTrustPolicy());
            
            String testXML = jaxb.write(jaxb.read(trustPolicyXml, TrustPolicy.class));
            FileUtils.write(new File(instanceFolderPath + "outputTest.xml"), testXML);
            
            boolean valid = ValidateSignature.isValid(trustPolicyXml);
            log.debug("Signature validation result is {}", valid);

            valid = ValidateSignature.isValid(testXML);
            log.debug("Signature validation result is {}", valid);
            
        } catch(IOException | JAXBException | XMLStreamException ex) {
            log.error("Error during deserialization of VM Quote Response. {}", ex.getMessage());
        }
        
    }*/
}
