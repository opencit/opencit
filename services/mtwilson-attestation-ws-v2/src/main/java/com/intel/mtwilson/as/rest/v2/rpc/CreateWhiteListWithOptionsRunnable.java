/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.rpc;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.mtwilson.datatypes.HostConfigData;
import com.intel.mtwilson.launcher.ws.ext.RPC;
import com.intel.mtwilson.ms.business.HostBO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
@RPC("create-whitelist-with-options")
@JacksonXmlRootElement(localName="create_whitelist_with_options")
public class CreateWhiteListWithOptionsRunnable implements Runnable {

    private Logger log = LoggerFactory.getLogger(getClass().getName());
    
    private HostConfigData config;
    private String result;

    public HostConfigData getConfig() {
        return config;
    }

    public void setConfig(HostConfigData config) {
        this.config = config;
    }
    
    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
    
    @Override
    public void run() {
        log.debug("Starting to process white list creation using host {}.", config.getTxtHostRecord().HostName);
        boolean configureWhiteListFromHost = new HostBO().configureWhiteListFromCustomData(config);
        result = Boolean.toString(configureWhiteListFromHost);
        log.debug("Completed processing of the white list using host {} with result {}", config.getTxtHostRecord().HostName, result);
    }
    
}
