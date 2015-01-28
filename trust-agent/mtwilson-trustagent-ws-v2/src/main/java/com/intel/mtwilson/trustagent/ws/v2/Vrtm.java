/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.ws.v2;

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
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.Context;


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
    
    @GET
    @Path("/reports")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public String getVMAttesationReport(@Context HttpServletRequest request) throws TAException {
        String outs = "Hello World" + request.getQueryString();
        return outs;
        /*
        String responseXML =
                "<host_info>"
                + "<timeStamp>" + new Date(System.currentTimeMillis()).toString() + "</timeStamp>"
                + "<clientIp>" + CommandUtil.getHostIpAddress() + "</clientIp>"
                + "<errorCode>" + context.getErrorCode().getErrorCode() + "</errorCode>"
                + "<errorMessage>" + context.getErrorCode().getMessage() + "</errorMessage>"
                + "<osName>" + context.getOsName() + "</osName>"
                + "<osVersion> " + context.getOsVersion() + "</osVersion>"
                + "<biosOem>" + context.getBiosOem() + "</biosOem>"
                + "<biosVersion> " + context.getBiosVersion()+ "</biosVersion>"
                + "<vmmName>" + context.getVmmName() + "</vmmName>"
                + "<vmmVersion>" + context.getVmmVersion() + "</vmmVersion>"
                + "<processorInfo>" + context.getProcessorInfo() + "</processorInfo>"
                +"<hostUUID>" + context.getHostUUID() + "</hostUUID>"
                + "</host_info>";
        return responseXML;
        */
    
    }
}
