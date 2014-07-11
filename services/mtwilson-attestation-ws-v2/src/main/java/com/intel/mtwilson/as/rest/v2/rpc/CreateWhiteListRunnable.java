/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.rpc;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.launcher.ws.ext.RPC;
import com.intel.mtwilson.ms.business.HostBO;
import com.intel.mtwilson.repository.RepositoryCreateException;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 *
 * @author ssbangal
 */
@RPC("create-whitelist")
@JacksonXmlRootElement(localName="create_whitelist")
public class CreateWhiteListRunnable implements Runnable{

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CreateWhiteListRunnable.class);

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
    @RequiresPermissions({"oems:create","oss:create","mles:create","mle_pcrs:create,store","mle_modules:create","mle_sources:create"})
    public void run() {
        try {
            if (host != null) {
                log.debug("Starting to process white list creation using host {}.", host.HostName);
                boolean configureWhiteListFromHost = new HostBO().configureWhiteListFromHost(host);
                result = Boolean.toString(configureWhiteListFromHost);
                log.debug("Completed processing of the white list using host {} with result {}", host.HostName, result);
            }
        } catch (Exception ex) {
            log.error("Error during white list configuration.", ex);
            throw new RepositoryCreateException();
        }
    }
    
}
