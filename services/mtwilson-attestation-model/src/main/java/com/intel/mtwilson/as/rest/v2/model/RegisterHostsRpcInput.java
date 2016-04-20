/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.mtwilson.datatypes.TxtHostRecordList;

/**
 *
 * @author ssbangal
 */
@JacksonXmlRootElement(localName="register_hosts_rpc_input")
public class RegisterHostsRpcInput {
    private TxtHostRecordList hosts;

    public TxtHostRecordList getHosts() {
        return hosts;
    }

    public void setHosts(TxtHostRecordList hosts) {
        this.hosts = hosts;
    }

    
}
