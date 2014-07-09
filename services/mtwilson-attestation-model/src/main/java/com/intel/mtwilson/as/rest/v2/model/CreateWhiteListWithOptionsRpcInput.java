/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 *
 * @author ssbangal
 */
@JacksonXmlRootElement(localName="create_whitelist_with_options_rpc_input")
public class CreateWhiteListWithOptionsRpcInput {
    
    private WhitelistConfigurationData wlConfig;

    public WhitelistConfigurationData getWlConfig() {
        return wlConfig;
    }

    public void setWlConfig(WhitelistConfigurationData wlConfig) {
        this.wlConfig = wlConfig;
    }

}
