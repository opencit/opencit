/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.mtwilson.datatypes.HostConfigDataList;

/**
 *
 * @author ssbangal
 */
@JacksonXmlRootElement(localName="register_hosts_with_options_rpc_input")
public class RegisterHostsWithOptionsRpcInput {
    private HostConfigDataList hosts;

    public HostConfigDataList getHosts() {
        return hosts;
    }

    public void setHosts(HostConfigDataList hosts) {
        this.hosts = hosts;
    }

    
}
