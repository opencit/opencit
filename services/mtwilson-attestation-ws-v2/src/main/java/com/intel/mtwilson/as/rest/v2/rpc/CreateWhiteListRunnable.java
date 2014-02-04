/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.rpc;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.api.ApiException;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.launcher.ws.ext.RPC;
import com.intel.mtwilson.ms.business.HostBO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
@RPC("create_whitelist")
@JacksonXmlRootElement(localName="create_whitelist")
public class CreateWhiteListRunnable implements Runnable{

    private Logger log = LoggerFactory.getLogger(getClass().getName());

    private TxtHostRecord host;
    private String result;

    public TxtHostRecord getHost() {
        return host;
    }

    public void setHost(TxtHostRecord host) {
        this.host = host;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
    
    @Override
    public void run() {
        try {
            log.debug("Starting to process white list creation using host {}.", host.HostName);
            boolean configureWhiteListFromHost = new HostBO().configureWhiteListFromHost(host);
            result = Boolean.toString(configureWhiteListFromHost);
            log.debug("Completed processing of the white list using host {} with result {}", host.HostName, result);
        } catch (ApiException aex) {
            throw new ASException(aex);
        }
    }
    
}
