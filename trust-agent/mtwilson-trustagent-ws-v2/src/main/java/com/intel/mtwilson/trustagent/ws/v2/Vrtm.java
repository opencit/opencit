/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.ws.v2;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mountwilson.common.TAException;
import com.intel.mountwilson.trustagent.commands.hostinfo.HostInfoCmd;
import com.intel.mountwilson.trustagent.data.TADataContext;
import com.intel.mtwilson.launcher.ws.ext.V2;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import com.intel.mtwilson.trustagent.model.HostInfo;
import com.intel.mtwilson.trustagent.model.VMAttestationRequest;
import com.intel.mtwilson.trustagent.model.VMAttestationResponse;
import com.intel.mtwilson.trustagent.vrtmclient.TCBuffer;
import com.intel.mtwilson.trustagent.vrtmclient.Factory;
import com.intel.mtwilson.trustagent.vrtmclient.RPCCall;
import com.intel.mtwilson.trustagent.vrtmclient.RPClient;
import com.intel.mtwilson.trustagent.vrtmclient.xml.MethodResponse;
import com.intel.mtwilson.trustagent.vrtmclient.xml.Param;
import com.intel.mtwilson.trustagent.vrtmclient.xml.Value;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.core.Context;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;


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
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_XML})
    public String getVMAttestationReport(VMAttestationRequest vmAttestationRequest) throws TAException {
        
        String vmInstanceId = vmAttestationRequest.getVmInstanceId();

        // Build the XML here.
        
        return null;

    }	
}
