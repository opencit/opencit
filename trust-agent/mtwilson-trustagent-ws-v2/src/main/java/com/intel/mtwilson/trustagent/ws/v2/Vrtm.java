/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.ws.v2;

import com.intel.mountwilson.common.TAException;
import com.intel.mtwilson.launcher.ws.ext.V2;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import com.intel.mtwilson.trustagent.model.VMQuoteResponse;
import com.intel.mtwilson.trustagent.model.VMAttestationRequest;
import com.intel.mtwilson.trustagent.model.VMAttestationResponse;
import com.intel.mtwilson.trustagent.vrtmclient.RPClient;
import java.io.File;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import java.io.IOException;
import org.apache.commons.io.FileUtils;


/**
 * 
 * XML input “<vm_challenge_request_json><vm_instance_id></ vm_instance_id><vm_challenge_request_json>”
 * JSON input {"vm_challenge_request": {“vm_instance_id":“dcc4a894-869b-479a-a24a-659eef7a54bd"}}
 * JSON output: {“vm_trust_response":{“host_name”:”10.1.1.1”,“vm_instance_id":“dcc4a894-869b-479a-a24a-659eef7a54bd","trust_status":true}}
 * 
 * @author hxia
 */
@V2
@Path("/vrtm")
public class Vrtm {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Vrtm.class);
    private static final String measurementXMLFileName = "measurement.xml";
    private static final String trustPolicyFileName = "trustpolicy.xml";
    private static final String vmQuoteFileName = "signed_report.xml";
    
    @POST
    @Path("/status")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public VMAttestationResponse getVMAttestationStatus(VMAttestationRequest vmAttestationRequest) throws TAException, IOException {
        
        String vmInstanceId = vmAttestationRequest.getVmInstanceId();
        VMAttestationResponse vmAttestationResponse = new VMAttestationResponse();        

        RPClient rpcInstance = new RPClient("127.0.0.1", 16005); // create instance of RPClient
        boolean vmstatus = rpcInstance.getVmStatus(vmInstanceId);    // send tcBuffer to rpcore 
        rpcInstance.close();   // close RPClient
        
        //set report
        vmAttestationResponse.setVmInstanceId(vmInstanceId);
        vmAttestationResponse.setTrustStatus(vmstatus);
        
        return vmAttestationResponse;

    }
	
    @POST
    @Path("/report")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public VMQuoteResponse getVMAttestationReport(VMAttestationRequest vmAttestationRequest) {
        try {
            
            String vmInstanceId = vmAttestationRequest.getVmInstanceId();
            String nonce = vmAttestationRequest.getNonce();
            
            // Call into the vRTM API and get the path information
            RPClient rpcInstance = new RPClient("127.0.0.1", 16005);
            String instanceFolderPath = rpcInstance.getVMAttestationReportPath(vmInstanceId, nonce);
            rpcInstance.close();
            
            if (instanceFolderPath == null || instanceFolderPath.isEmpty()) {
                String errorInfo = "Error during retrieval of the instance path. Please verify the input parameters.";
                log.error (errorInfo);
                return null;
                //throw new WebApplicationException(Response.serverError().header("Error", errorInfo).build());
            }
                
            VMQuoteResponse vmQuoteResponse = new VMQuoteResponse();
            vmQuoteResponse.setVmMeasurements(FileUtils.readFileToByteArray(new File(String.format("%s%s", instanceFolderPath, measurementXMLFileName))));
            vmQuoteResponse.setVmTrustPolicy(FileUtils.readFileToByteArray(new File(String.format("%s%s", instanceFolderPath, trustPolicyFileName))));
            vmQuoteResponse.setVmQuote(FileUtils.readFileToByteArray(new File(String.format("%s%s", instanceFolderPath, vmQuoteFileName))));
            vmQuoteResponse.setVmQuoteType(VMQuoteResponse.QuoteType.XML_DSIG);
            return vmQuoteResponse;
            
        } catch (IOException ex) {
            log.error("Error during reading of VM quote information. {}", ex.getMessage());
            //throw new WebApplicationException(ex);
        }
        return null;
    }	

/*    @POST
    @Path("/report")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public String getVMAttestationReport(VMAttestationRequest vmAttestationRequest) throws TAException {
        
        JAXB jaxb = new JAXB();        
        // Call into the vRTM API and get the path information
        String instanceFolderPath = "/var/lib/nova/instances/" + vmAttestationRequest.getVmInstanceId() + "/";
        
        // Build the XML here.
        VMQuoteResponse vmQuoteResponse = new VMQuoteResponse();
        
        // TODO: The below object should be created by reading from the VMQuote.xml file in the instance folder
        try {
            VMQuote vmInstanceQuote = new VMQuote();
            vmInstanceQuote.setCumulativeHash("2284377e7a81243ab4305412669d90ba9253a64a");
            vmInstanceQuote.setVmInstanceId(vmAttestationRequest.getVmInstanceId());
            vmInstanceQuote.setDigestAlg("SHA-256");
            vmInstanceQuote.setNonce(vmAttestationRequest.getNonce());
            
            vmQuoteResponse.setVMQuote(vmInstanceQuote);
            
        } catch (Exception ex) {
            log.error("Error reading the vm quote file. {}", ex.getMessage());
            throw new WebApplicationException(Response.serverError().header("Error", 
                    String.format("%s. %s", "Error reading the vm quote file.", ex.getMessage())).build());
        }
        
        try (FileInputStream measurementXMLFileStream = new FileInputStream(String.format("%s%s", instanceFolderPath, measurementXMLFileName))) {
        
            String measurementXML = IOUtils.toString(measurementXMLFileStream, "UTF-8");
            Measurements readMeasurements = jaxb.read(measurementXML, Measurements.class);
            vmQuoteResponse.setMeasurements(readMeasurements);

        } catch (Exception ex) {
            log.error("Error reading the measurement log. {}", ex.getMessage());
            throw new WebApplicationException(Response.serverError().header("Error", 
                    String.format("%s. %s", "Error reading the measurement log.", ex.getMessage())).build());
        }
                
        try (FileInputStream trustPolicyFileStream = new FileInputStream(String.format("%s%s", instanceFolderPath, trustPolicyFileName))) {
        
            String trustPolicyXML = IOUtils.toString(trustPolicyFileStream, "UTF-8");
            TrustPolicy trustPolicy = jaxb.read(trustPolicyXML, TrustPolicy.class);
            vmQuoteResponse.setTrustPolicy(trustPolicy);

        } catch (Exception ex) {
            log.error("Error reading the Trust policy. {}", ex.getMessage());
            throw new WebApplicationException(Response.serverError().header("Error", 
                    String.format("%s. %s", "Error reading the Trust policy.", ex.getMessage())).build());
        }
        
        try {
            String quoteResponse = jaxb.write(vmQuoteResponse);
            return quoteResponse;
        } catch (JAXBException ex) {
            log.error("Error serializing the VM quote response. {}", ex.getMessage());
            throw new WebApplicationException(Response.serverError().header("Error", 
                    String.format("%s. %s", "Error serializing the VM quote response.", ex.getMessage())).build());
        }        
    }	
*/    

}
