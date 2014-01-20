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
    
    @Get("txt")
    public String search(/*TagSearchCriteria query*/) {
        log.debug("made it into actionAutomation!");
        //String ip = getQuery().getFirstValue("ipaddress");
        return "F4B17194-CAE7-11DF-B40B-001517FA9844";
    }
}
