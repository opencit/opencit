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
 * @author Yuvrajx
 */
public class TDPConfig  {

    private static final Logger log = LoggerFactory.getLogger(TDPConfig.class);
    private static final TDPConfig global = new TDPConfig();

    public static Configuration getConfiguration() {
        return My.configuration().getConfiguration();
    }
        

    public Properties getDefaults() {
        Properties defaults = new Properties();
        
        // Properties for the API Client
        defaults.setProperty("mtwilson.api.baseurl", "https://127.0.0.1:8181");
        defaults.setProperty("mtwilson.api.ssl.verifyHostname", "true"); // must be secure out of the box.  
        defaults.setProperty("mtwilson.api.ssl.requireTrustedCertificate", "true");  // must be secure out of the box. user registration process should download server ssl certs
        
        
        // Trust Dashboard Portal specific properties 
        defaults.setProperty("mtwilson.tdbp.sessionTimeOut", "1800");
        defaults.setProperty("mtwilson.tdbp.paginationRowCount", "10");
        defaults.setProperty("mtwilson.tdbp.keystore.dir", "/var/opt/intel/trust-dashboard/users"); 
        return defaults;
                
	}

}

