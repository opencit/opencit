/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.rpc;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.dcsg.cpg.crypto.digest.Digest;
import com.intel.mtwilson.as.rest.v2.model.WhitelistConfigurationData;
import com.intel.mtwilson.launcher.ws.ext.RPC;
import com.intel.mtwilson.model.Nonce;
import com.intel.mtwilson.ms.business.HostBO;
import com.intel.mtwilson.repository.RepositoryCreateException;
import com.intel.mtwilson.repository.RepositoryException;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 *
 * @author ssbangal
 */
@RPC("create-whitelist-with-options")
@JacksonXmlRootElement(localName="create_whitelist_with_options")
public class CreateWhiteListWithOptionsRunnable implements Runnable {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CreateWhiteListWithOptionsRunnable.class);
    
    private WhitelistConfigurationData wlConfig;
    private String challengeHex;
    private String result;

    public WhitelistConfigurationData getWlConfig() {
        return wlConfig;
    }

    public void setWlConfig(WhitelistConfigurationData wlConfig) {
        this.wlConfig = wlConfig;
    }
    
    public void setChallengeHex(String challengeHex) {
        this.challengeHex = challengeHex;
    }

    public String getChallengeHex() {
        return challengeHex;
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
        log.debug("Starting to process white list creation using host {}.", wlConfig.getTxtHostRecord().HostName);
        try {

            boolean configureWhiteListFromHost;
            if( challengeHex == null || challengeHex.isEmpty() ) {
                configureWhiteListFromHost = new HostBO().configureWhiteListFromCustomData(wlConfig);
            }
            else {
                if( !Digest.sha1().isValidHex(challengeHex) ) {
                    throw new RepositoryCreateException("Invalid challenge");
                }
                Nonce challenge = new Nonce(Digest.sha1().valueHex(challengeHex).getBytes());
                configureWhiteListFromHost = new HostBO().configureWhiteListFromCustomData(wlConfig, challenge);
            }
            
            result = Boolean.toString(configureWhiteListFromHost);
            log.debug("Completed processing of the white list using host {} with result {}", wlConfig.getTxtHostRecord().HostName, result);
            if (wlConfig.isRegisterHost()) {
                registerHost();
            }
        } catch (RepositoryException re) {
            throw re;
        } catch (Exception ex) {
            log.error("Error during white list configuration using custom options.", ex);
            throw new RepositoryCreateException();
        }
    }
    
    @RequiresPermissions({"hosts:create"})
    private void registerHost() {
        log.debug("Starting to process the registration request for host {}.", wlConfig.getTxtHostRecord().HostName);
        boolean registerHostFromCustomData = new HostBO().registerHostFromCustomData(wlConfig);
        result = Boolean.toString(registerHostFromCustomData);
        log.debug("Completed processing of the registering host {} with result {}", wlConfig.getTxtHostRecord().HostName, result);
    }
}
