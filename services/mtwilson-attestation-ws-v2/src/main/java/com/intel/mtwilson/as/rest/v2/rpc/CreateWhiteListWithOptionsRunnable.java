/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.rpc;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.mtwilson.as.rest.v2.model.WhitelistConfigurationData;
import com.intel.mtwilson.launcher.ws.ext.RPC;
import com.intel.mtwilson.ms.business.HostBO;

/**
 *
 * @author ssbangal
 */
@RPC("create-whitelist-with-options")
@JacksonXmlRootElement(localName="create_whitelist_with_options")
public class CreateWhiteListWithOptionsRunnable implements Runnable {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CreateWhiteListWithOptionsRunnable.class);
    
    private WhitelistConfigurationData wlConfig;
    private String result;

    public WhitelistConfigurationData getWlConfig() {
        return wlConfig;
    }

    public void setWlConfig(WhitelistConfigurationData wlConfig) {
        this.wlConfig = wlConfig;
    }
    
    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
    
    @Override
    public void run() {
        log.debug("Starting to process white list creation using host {}.", wlConfig.getTxtHostRecord().HostName);
        boolean configureWhiteListFromHost = new HostBO().configureWhiteListFromCustomData(wlConfig);
        result = Boolean.toString(configureWhiteListFromHost);
        log.debug("Completed processing of the white list using host {} with result {}", wlConfig.getTxtHostRecord().HostName, result);
    }
    
}
