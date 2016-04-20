/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.mtwilson.datatypes.TxtHostRecord;

/**
 *
 * @author ssbangal
 */
@JacksonXmlRootElement(localName="create_whitelist_rpc_input")
public class CreateWhiteListRpcInput {
    private TxtHostRecord host;

    public TxtHostRecord getHost() {
        return host;
    }

    public void setHost(TxtHostRecord host) {
        this.host = host;
    }    
}
