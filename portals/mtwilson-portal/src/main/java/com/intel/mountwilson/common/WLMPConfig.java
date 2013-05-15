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
//        private final static Properties defaults = new Properties();

    public static Configuration getConfiguration() { return global.getConfigurationInstance(); }
        
    private WLMPConfig() {
        super("mtwilson-portal.properties");
    }

    @Override
    public Properties getDefaults() {
        Properties defaults = new Properties();
        
        // Properties for the API Client
        defaults.setProperty("mtwilson.api.baseurl", "https://127.0.0.1:8181");
        defaults.setProperty("mtwilson.wlmp.keystore.dir", "/var/opt/intel/whitelist-portal/users"); // XXX TODO make a linux default and windows default, utiilizing some centralized configuration functions suh as getDataDirectory() which would already provide an os-specific directory that has already been created (or with a function to create it)
        defaults.setProperty("mtwilson.api.ssl.verifyHostname", "true"); // must be secure out of the box.  TODO: installer should generate an ssl cert for glassfish that matches the url that will be used to access it.
        defaults.setProperty("mtwilson.api.ssl.requireTrustedCertificate", "true");  // must be secure out of the box. user registration process should download server ssl certs
        
        // White List Portal specific properties
        defaults.setProperty("mtwilson.wlmp.sessionTimeOut", "1000");
        defaults.setProperty("mtwilson.wlmp.hostTypes", "Xen,KVM,VMWare");
        defaults.setProperty("mtwilson.wlmp.apiKeyExpirationNoticeInMonths", "3");
        return defaults;
	}


   
       public static Properties getJpaProperties(Configuration config) {
        /*
        Configuration config = getConfiguration();
        Properties prop = new Properties();
        prop.put("javax.persistence.jdbc.driver", 
                config.getString("mountwilson.ms.db.driver", 
                config.getString("mtwilson.db.driver",
                "com.mysql.jdbc.Driver")));
        prop.put("javax.persistence.jdbc.url" , 
                config.getString("mountwilson.ms.db.url",
                config.getString("mtwilson.db.url",
                String.format("jdbc:mysql://%s:%s/%s?autoReconnect=true",
                    config.getString("mountwilson.ms.db.host", config.getString("mtwilson.db.host","127.0.0.1")),
                    config.getString("mountwilson.ms.db.port", config.getString("mtwilson.db.port","3306")),
                    config.getString("mountwilson.ms.db.schema", config.getString("mtwilson.db.schema","mw_as"))))));
        prop.put("javax.persistence.jdbc.user",
                config.getString("mountwilson.ms.db.user",
                config.getString("mtwilson.db.user",
                "root")));
        prop.put("javax.persistence.jdbc.password", 
                config.getString("mountwilson.ms.db.password", 
                config.getString("mtwilson.db.password", 
                "password")));
        return prop;
        */
        
        Properties prop = new Properties();
        prop.put("javax.persistence.jdbc.driver", 
                config.getString("mountwilson.mcp.db.driver", 
                config.getString("mtwilson.db.driver",
                "org.postgresql.Driver")));
        //System.err.println("stdalex mcpConfig getJpaConfig driver == " + config.getString("mountwilson.mcp.db.driver", config.getString("mtwilson.db.driver", "com.mysql.jdbc.Driver")));
        String dbms = (config.getString("mountwilson.mcp.db.driver", config.getString("mtwilson.db.driver", "com.mysql.jdbc.Driver")).contains("mysql")) ? "mysql" : "postgresql";
        //System.err.println("stdalex mcpConfig getJpaConfig dbms == " + dbms);
        prop.put("javax.persistence.jdbc.url" , 
                config.getString("mountwilson.mcp.db.url",
                config.getString("mtwilson.db.url",
                String.format("jdbc:"+dbms+"://%s:%s/%s?autoReconnect=true",
                    config.getString("mountwilson.mcp.db.host", config.getString("mtwilson.db.host","127.0.0.1")),
                    config.getString("mountwilson.mcp.db.port", config.getString("mtwilson.db.port","3306")),
                    config.getString("mountwilson.mcp.db.schema", config.getString("mtwilson.db.schema","mw_as"))))));
        //System.err.println("stdalex msConfig url == " + prop.getProperty("javax.persistence.jdbc.url")); 
        prop.put("javax.persistence.jdbc.user",
                config.getString("mountwilson.mcp.db.user",
                config.getString("mtwilson.db.user",
                "root")));
        prop.put("javax.persistence.jdbc.password", 
                config.getString("mountwilson.mcp.db.password", 
                config.getString("mtwilson.db.password", 
                "password")));
        return prop;
    }
    public static Properties getJpaProperties() {
        return getJpaProperties(getConfiguration());
    }
}
