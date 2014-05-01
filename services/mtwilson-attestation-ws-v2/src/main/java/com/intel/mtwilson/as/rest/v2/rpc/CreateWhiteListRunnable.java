/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.rpc;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.api.ApiException;
import com.intel.mtwilson.as.rest.v2.model.CreateWhiteListRpcInput;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.launcher.ws.ext.RPC;
import com.intel.mtwilson.ms.business.HostBO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
@RPC("create-whitelist")
@JacksonXmlRootElement(localName="create_whitelist")
public class CreateWhiteListRunnable implements Runnable{

    private Logger log = LoggerFactory.getLogger(getClass().getName());

    public CreateWhiteListRpcInput rpcInput;
    /*private TxtHostRecord host;
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
    }*/
    
    @Override
    public void run() {
        try {
            if (rpcInput != null && rpcInput.getHost() != null) {
                log.debug("Starting to process white list creation using host {}.", rpcInput.getHost().HostName);
                boolean configureWhiteListFromHost = new HostBO().configureWhiteListFromHost(rpcInput.getHost());
                rpcInput.setResult(configureWhiteListFromHost); //= Boolean.toString(configureWhiteListFromHost);
                log.debug("Completed processing of the white list using host {} with result {}", rpcInput.getHost().HostName, rpcInput.isResult());
            }
        } catch (ApiException aex) {
            throw new ASException(aex);
        }
    }
    
}
