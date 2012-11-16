/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mountwilson.common;

import com.intel.mtwilson.util.ConfigBase;
import java.util.Properties;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
public class MCPConfig extends ConfigBase {

    private static final Logger log = LoggerFactory.getLogger(MCPConfig.class);
    private static final MCPConfig global = new MCPConfig();
    
    public static Configuration getConfiguration() { return global.getConfigurationInstance(); }
        
    private MCPConfig() {
        
        super("management-console.properties", getDefaults());
    }

    private static Properties getDefaults() {
        Properties defaults = new Properties();
        
        // Properties for the API Client
        defaults.setProperty("mtwilson.api.baseurl", "https://127.0.0.1:8181");
        //defaults.setProperty("mtwilson.api.keystore", "mw.jks"); // instead of one keystore for the app, we use a directory with one keystore per user:
        defaults.setProperty("mtwilson.mc.keystore.dir", "/var/opt/intel/management-console/users"); // XXX TODO make a linux default and windows default, utiilizing some centralized configuration functions suh as getDataDirectory() which would already provide an os-specific directory that has already been created (or with a function to create it)
//        defaults.setProperty("mtwilson.api.ssl.verifyHostname", "false");
//        defaults.setProperty("mtwilson.api.ssl.requireTrustedCertificate", "false");
        defaults.setProperty("mtwilson.api.ssl.verifyHostname", "true"); // must be secure out of the box.  TODO: installer should generate an ssl cert for glassfish that matches the url that will be used to access it.
        defaults.setProperty("mtwilson.api.ssl.requireTrustedCertificate", "true");  // must be secure out of the box. user registration process should download server ssl certs
        
        // Management Console Portal specific properties
        defaults.setProperty("mtwilson.mc.sessionTimeOut", "1800");
        defaults.setProperty("mtwilson.mc.hostTypes", "Xen;KVM;VMWare");
        defaults.setProperty("mtwilson.mc.apiKeyExpirationNoticeInMonths", "3");
	
        return defaults;
                
	}

    // for troubleshooting
    @Override
    public void dumpConfiguration(Configuration c, String label) {
        String keys[] = new String[] { "mtwilson.api.baseurl", "mtwilson.api.keystore", "mtwilson.mc.sessionTimeOut" };
        for(String key : keys) {
            String value = c.getString(key);
            log.info(String.format("MCPConfig [%s]: %s=%s", label, key, value));  
        }
    }

    
    // Not required for the utility since it is not doing any DB operations directly.
    /*public static Properties getJpaProperties() {
        Configuration config = getConfiguration();
        Properties prop = new Properties();
        prop.put("javax.persistence.jdbc.driver", config.getString("mountwilson.as.db.driver", "com.mysql.jdbc.Driver"));
        prop.put("javax.persistence.jdbc.url" ,String.format("jdbc:mysql://%s:3306/%s",
                    config.getString("mountwilson.as.db.host", "localhost"),
                    config.getString("mountwilson.as.db.schema", "mw_as")));
        prop.put("javax.persistence.jdbc.user" ,config.getString("mountwilson.as.db.user", "root"));
        prop.put("javax.persistence.jdbc.password", config.getString("mountwilson.as.db.password", "password"));
        return prop;
    }*/
    
}

