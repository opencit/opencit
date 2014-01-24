/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.intel.mtwilson.atag.resource;

import com.intel.dcsg.cpg.io.UUID;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author stdalex
 */
public class UuidResource extends ServerResource{
    private Logger log = LoggerFactory.getLogger(getClass());
    
   @Override
    protected void doInit() throws ResourceException {
        super.doInit();
    }


    @Override
    protected void doRelease() throws ResourceException {
        super.doRelease();
    }
    
    public class UuidResponse {
        public String host_uuid;
        
        public UuidResponse(){}
        
        public void setHostUuid(String host_uuid) {
            this.host_uuid = host_uuid;
        }
        
        public String getHostUuid() {
            return this.host_uuid;
        }
    }
    
    @Get("json")
    public UuidResponse search(/*TagSearchCriteria query*/) {
        String ip = getQuery().getFirstValue("ipaddress");
        log.debug("made it into actionAutomation! got ip of " + ip);
        //String ip = getQuery().getFirstValue("ipaddress");
        UuidResponse response = new UuidResponse();
        response.host_uuid = "F4B17194-CAE7-11DF-B40B-001517FA9844";
        return response;
    }
}
