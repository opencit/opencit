/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.ws.v2;

import com.intel.mountwilson.common.TAConfig;
import com.intel.mountwilson.common.TAException;
import com.intel.mountwilson.trustagent.commands.SetAssetTag;
import com.intel.mountwilson.trustagent.commands.SetAssetTagWindows;
import com.intel.mountwilson.trustagent.data.TADataContext;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.trustagent.TrustagentConfiguration;
import com.intel.mtwilson.trustagent.model.TagWriteRequest;
import com.intel.mtwilson.trustagent.tpmmodules.TpmModuleProvider;
import gov.niarl.his.privacyca.TpmModule;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.codec.binary.Hex;
import com.intel.mtwilson.trustagent.tpmmodules.Tpm;
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
    public void writeTag(TagWriteRequest tagInfo, @Context HttpServletResponse response) throws IOException, TpmModule.TpmModuleException {
        log.debug("writeTag uuid {} sha1 {}", tagInfo.getHardwareUuid(), Hex.encodeHexString(tagInfo.getTag()));       
        TrustagentConfiguration config = new TrustagentConfiguration(TAConfig.getConfiguration());        
        
        Tpm.getModule().setAssetTag(config.getTpmOwnerSecret(), tagInfo.getTag());                
        
        log.debug("writeTag returning 204 status");
        response.setStatus(Response.Status.NO_CONTENT.getStatusCode());
        log.debug("writeTag done");
        


    }
    
}
