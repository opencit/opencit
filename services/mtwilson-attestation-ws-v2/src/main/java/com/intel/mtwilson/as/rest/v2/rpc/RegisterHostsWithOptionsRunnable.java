/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.rpc;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.mtwilson.datatypes.HostConfigDataList;
import com.intel.mtwilson.datatypes.HostConfigDataList;
import com.intel.mtwilson.datatypes.HostConfigResponse;
import com.intel.mtwilson.datatypes.HostConfigResponseList;
import com.intel.mtwilson.launcher.ws.ext.RPC;
import com.intel.mtwilson.ms.business.HostBO;
import java.util.List;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 *
 * @author ssbangal
 */
@RPC("register-hosts-with-options")
@JacksonXmlRootElement(localName="register_hosts_with_options")
public class RegisterHostsWithOptionsRunnable implements Runnable{

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RegisterHostsWithOptionsRunnable.class);

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
    @RequiresPermissions({"hosts:create,store"})    
    public void run() {
        log.debug("Got request to register # {} of servers.", hosts.getHostRecords().size());
        result = new HostBO().registerHosts(hosts);
        List<HostConfigResponse> hostRecords = result.getHostRecords();
        for (HostConfigResponse hcr : hostRecords) {
            log.debug("Processed server {} with status {}.", hcr.getHostName(), hcr.getStatus());
        }    
    }
    
}
