/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.ws.v2;

import com.intel.mountwilson.common.CommandUtil;
import com.intel.mountwilson.common.TAException;
import com.intel.mountwilson.trustagent.commands.hostinfo.HostInfoCmd;
import com.intel.mountwilson.trustagent.data.TADataContext;
import com.intel.mtwilson.launcher.ws.ext.V2;
import java.util.Date;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import com.intel.mtwilson.trustagent.model.HostInfo;


/**
 * Previously called host_info
 * 
 * @author jbuhacoff
 */
@V2
@Path("/host")
public class Host {
    private static HostInfo hostInfo = null;
    
    @GET
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public HostInfo getHostInformation() throws TAException {
        if( hostInfo == null ) {
            TADataContext context = new TADataContext();
            HostInfoCmd cmd = new HostInfoCmd(context);
            cmd.execute();
            HostInfo host = new HostInfo();
            host.timestamp = System.currentTimeMillis();
    //        host.clientIp = CommandUtil.getHostIpAddress();
            host.errorCode = context.getErrorCode().name();
            host.errorMessage = context.getErrorCode().getMessage();
            host.osName = context.getOsName();
            host.osVersion = context.getOsVersion();
            host.biosOem = context.getBiosOem();
            host.biosVersion = context.getBiosVersion();
            host.vmmName = context.getVmmName();
            host.vmmVersion = context.getVmmVersion();
            host.processorInfo = context.getProcessorInfo();
            host.hardwareUuid = context.getHostUUID();
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
            hostInfo = host;
        }
        return hostInfo;
    }
}
