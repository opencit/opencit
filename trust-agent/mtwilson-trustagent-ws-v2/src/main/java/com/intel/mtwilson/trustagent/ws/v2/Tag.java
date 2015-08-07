/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.ws.v2;

import com.intel.mountwilson.common.TAException;
import com.intel.mountwilson.trustagent.commands.SetAssetTag;
import com.intel.mountwilson.trustagent.commands.SetAssetTagWindows;
import com.intel.mountwilson.trustagent.data.TADataContext;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.trustagent.model.TagWriteRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.codec.binary.Hex;

/**
 *
 * @author jbuhacoff
 */
@V2
@Path("/tag")
public class Tag {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Tag.class);
    
    @POST
    @Consumes({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
    public void writeTag(TagWriteRequest tagInfo, @Context HttpServletResponse response) throws TAException {
        log.debug("writeTag uuid {} sha1 {}", tagInfo.getHardwareUuid(), Hex.encodeHexString(tagInfo.getTag()));
        TADataContext context = new TADataContext();
        context.setAssetTagHash(Hex.encodeHexString(tagInfo.getTag()));
        
        String osName = System.getProperty("os.name");
        if (osName.toLowerCase().contains("windows"))
            new SetAssetTagWindows(context).execute();
        else
            new SetAssetTag(context).execute();
        
        log.debug("writeTag returning 204 status");
        response.setStatus(Response.Status.NO_CONTENT.getStatusCode());
        log.debug("writeTag done");
        


    }
    
}
