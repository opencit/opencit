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
public class WLMPConfig extends ConfigBase {

    private static final Logger log = LoggerFactory.getLogger(WLMPConfig.class);
    private static final WLMPConfig global = new WLMPConfig();
        private final static Properties defaults = new Properties();

    public static Configuration getConfiguration() { return global.getConfigurationInstance(); }
        
    private WLMPConfig() {
        super("whitelist-portal.properties", defaults);
    }

    static  {
        
        // Properties for the API Client
        defaults.setProperty("mtwilson.api.baseurl", "https://127.0.0.1:8181");
        defaults.setProperty("mtwilson.wlmp.keystore.dir", "/var/opt/intel/whitelist-portal/users"); // XXX TODO make a linux default and windows default, utiilizing some centralized configuration functions suh as getDataDirectory() which would already provide an os-specific directory that has already been created (or with a function to create it)
        defaults.setProperty("mtwilson.api.ssl.verifyHostname", "true"); // must be secure out of the box.  TODO: installer should generate an ssl cert for glassfish that matches the url that will be used to access it.
        defaults.setProperty("mtwilson.api.ssl.requireTrustedCertificate", "true");  // must be secure out of the box. user registration process should download server ssl certs
        
        // White List Portal specific properties
        defaults.setProperty("mtwilson.wlmp.sessionTimeOut", "1000");
        defaults.setProperty("mtwilson.wlmp.hostTypes", "Xen,KVM,VMWare");
        defaults.setProperty("mtwilson.wlmp.apiKeyExpirationNoticeInMonths", "3");
	}

    // for troubleshooting
    @Override
    public void dumpConfiguration(Configuration c, String label) {
        String keys[] = new String[] { "mtwilson.api.baseurl", "mtwilson.api.keystore", "mtwilson.mc.sessionTimeOut" };
        for(String key : keys) {
            String value = c.getString(key);
            log.debug(String.format("MCPConfig [%s]: %s=%s", (label==null?"null":label), (key==null?"null":key), (value==null?"null":value)));  
        }
    }

   
    public static Properties getJpaProperties() {
        Configuration config = getConfiguration();
        Properties prop = new Properties();
        prop.put("javax.persistence.jdbc.driver", 
                config.getString("mountwilson.wlmp.db.driver", 
                config.getString("mtwilson.db.driver",
                "com.mysql.jdbc.Driver")));
        prop.put("javax.persistence.jdbc.url" , 
                config.getString("mountwilson.wlmp.db.url",
                config.getString("mtwilson.db.url",
                String.format("jdbc:mysql://%s:%s/%s?autoReconnect=true",
                    config.getString("mountwilson.wlmp.db.host", config.getString("mtwilson.db.host","127.0.0.1")),
                    config.getString("mountwilson.wlmp.db.port", config.getString("mtwilson.db.port","3306")),
                    config.getString("mountwilson.wlmp.db.schema", config.getString("mtwilson.db.schema","mw_as"))))));
        prop.put("javax.persistence.jdbc.user",
                config.getString("mountwilson.wlmp.db.user",
                config.getString("mtwilson.db.user",
                "root")));
        prop.put("javax.persistence.jdbc.password", 
                config.getString("mountwilson.wlmp.db.password", 
                config.getString("mtwilson.db.password", 
                "password")));
        return prop;
    }    
}
