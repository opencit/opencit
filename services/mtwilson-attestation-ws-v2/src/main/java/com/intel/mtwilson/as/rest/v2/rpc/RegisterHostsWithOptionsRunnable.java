/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.rpc;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.mtwilson.datatypes.HostConfigDataList;
import com.intel.mtwilson.datatypes.HostConfigResponse;
import com.intel.mtwilson.datatypes.HostConfigResponseList;
import com.intel.mtwilson.launcher.ws.ext.RPC;
import com.intel.mtwilson.ms.business.HostBO;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
@RPC("register_hosts_with_options")
@JacksonXmlRootElement(localName="register_hosts_with_options")
public class RegisterHostsWithOptionsRunnable implements Runnable{

    private Logger log = LoggerFactory.getLogger(getClass().getName());

    private HostConfigDataList hosts;
    private HostConfigResponseList result;

    public HostConfigDataList getHosts() {
        return hosts;
    }

    public void setHosts(HostConfigDataList hosts) {
        this.hosts = hosts;
    }

    public HostConfigResponseList getResult() {
        return result;
    }

    public void setResult(HostConfigResponseList result) {
        this.result = result;
    }
        
    @Override
    public void run() {
        log.debug("Got request to register # {} of servers.", hosts.getHostRecords().size());
        result = new HostBO().registerHosts(hosts);
        List<HostConfigResponse> hostRecords = result.getHostRecords();
        for (HostConfigResponse hcr : hostRecords) {
            log.debug("Processed server {} with status {}.", hcr.getHostName(), hcr.getStatus());
        }    
    }
    
}
