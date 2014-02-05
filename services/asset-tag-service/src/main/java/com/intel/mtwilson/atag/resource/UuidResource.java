/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.intel.mtwilson.atag.resource;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.api.ApiException;
import com.intel.mtwilson.atag.Global;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import java.io.IOException;
import java.security.SignatureException;
import java.util.List;
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
    public UuidResponse search(/*TagSearchCriteria query*/) throws IOException, ApiException, SignatureException, Exception {
        String ip = getQuery().getFirstValue("ipaddress");
        log.debug("made it into actionAutomation! got ip of " + ip);
        //String ip = getQuery().getFirstValue("ipaddress");
        UuidResponse response = new UuidResponse();
        List<TxtHostRecord> hostList = Global.mtwilson().queryForHosts(ip,true);
        if(hostList == null || hostList.size() < 1) {
            throw new Exception("No host records found");
        }
        response.host_uuid = hostList.get(0).Hardware_Uuid;
        return response;
    }
}
