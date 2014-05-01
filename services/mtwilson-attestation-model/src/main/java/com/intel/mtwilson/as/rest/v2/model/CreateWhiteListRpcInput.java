/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.jersey.Document;

/**
 *
 * @author ssbangal
 */
@JacksonXmlRootElement(localName="create_whitelist_rpc_input")
public class CreateWhiteListRpcInput extends Document{
    private TxtHostRecord host;
    private boolean result;

    public TxtHostRecord getHost() {
        return host;
    }

    public void setHost(TxtHostRecord host) {
        this.host = host;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    
}
