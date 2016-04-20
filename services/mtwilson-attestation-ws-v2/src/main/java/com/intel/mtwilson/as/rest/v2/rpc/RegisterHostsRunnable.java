/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.rpc;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.mtwilson.datatypes.HostConfigResponse;
import com.intel.mtwilson.datatypes.HostConfigResponseList;
import com.intel.mtwilson.datatypes.TxtHostRecordList;
import com.intel.mtwilson.datatypes.TxtHostRecordList;
import com.intel.mtwilson.launcher.ws.ext.RPC;
import com.intel.mtwilson.ms.business.HostBO;
import java.util.List;
import org.apache.shiro.authz.annotation.RequiresPermissions;


/**
 *
 * @author ssbangal
 */
// The same api can be used for both single host registration and bulk host registrations. For single host
// registration the user will pass in the list with a single item.
@RPC("register-hosts")
@JacksonXmlRootElement(localName="register_hosts")
public class RegisterHostsRunnable implements Runnable{
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RegisterHostsRunnable.class);
    
    private TxtHostRecordList hosts;
    private HostConfigResponseList result;

    public TxtHostRecordList getHosts() {
        return hosts;
    }

    public void setHosts(TxtHostRecordList hosts) {
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
