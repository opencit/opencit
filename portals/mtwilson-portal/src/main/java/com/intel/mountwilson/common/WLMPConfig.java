/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mountwilson.common;

import com.intel.mtwilson.My;
import java.io.IOException;
import java.util.Properties;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
public class WLMPConfig  {

    private static final Logger log = LoggerFactory.getLogger(WLMPConfig.class);
    private static final WLMPConfig global = new WLMPConfig();
//        private final static Properties defaults = new Properties();

    public static Configuration getConfiguration() {
        return My.configuration().getConfiguration();
    }
       
    public Properties getDefaults() {
        Properties defaults = new Properties();
        
        // Properties for the API Client
        defaults.setProperty("mtwilson.api.baseurl", "https://127.0.0.1:8181");
        defaults.setProperty("mtwilson.wlmp.keystore.dir", "/var/opt/intel/whitelist-portal/users"); 
        defaults.setProperty("mtwilson.api.ssl.verifyHostname", "true"); // must be secure out of the box.  
        defaults.setProperty("mtwilson.api.ssl.requireTrustedCertificate", "true");  // must be secure out of the box. user registration process should download server ssl certs
        
        // White List Portal specific properties
        defaults.setProperty("mtwilson.wlmp.sessionTimeOut", "1000");
        defaults.setProperty("mtwilson.wlmp.hostTypes", "Xen,KVM,VMWare");
        defaults.setProperty("mtwilson.wlmp.apiKeyExpirationNoticeInMonths", "3");
        return defaults;
	}


}
