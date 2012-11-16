/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mountwilson.mscu.common;

import com.intel.mtwilson.util.ConfigBase;
import java.util.Properties;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
public class MSCUConfig extends ConfigBase {

    private static final Logger log = LoggerFactory.getLogger(MSCUConfig.class);
    private static final MSCUConfig global = new MSCUConfig();
    
    public static Configuration getConfiguration() { return global.getConfigurationInstance(); }
        
    private MSCUConfig() {
        
        super("management-cmdutil.properties", getDefaults());
    }

    private static Properties getDefaults() {
        Properties defaults = new Properties();
        
        // Properties for the API Client
        defaults.setProperty("mtwilson.api.baseurl", "https://127.0.0.1:8181");
        defaults.setProperty("mtwilson.api.keystore", "c:/intel/cloudsecurity/mw.jks");
        defaults.setProperty("mtwilson.api.keystore.password", "password");
        defaults.setProperty("mtwilson.api.key.alias", "Admin");
        defaults.setProperty("mtwilson.api.key.password", "password");
        defaults.setProperty("mtwilson.api.ssl.verifyHostname", "false");
        defaults.setProperty("mtwilson.api.ssl.requireTrustedCertificate", "false");
        
        // Management Service Command Line utility specific properties
        defaults.setProperty("mtwilson.mscu.gkvHostType", "VMware");
        defaults.setProperty("mtwilson.mscu.gkvHost", "127.0.0.1");
        defaults.setProperty("mtwilson.mscu.hostPort", "9999");
        defaults.setProperty("mtwilson.mscu.isolatedVCenter", "https://127.0.0.1:443/sdk;Administrator;Password");
        defaults.setProperty("mtwilson.mscu.productionVCenter", "https://127.0.0.1:443/sdk;Administrator;Password");
        defaults.setProperty("mtwilson.mscu.userApprovalRequired", "true"); 
        defaults.setProperty("mtwilson.mscu.hostInputOption", "Cluster");
        defaults.setProperty("mtwilson.mscu.fileName", "c:/temp/hostfile.txt");
        defaults.setProperty("mtwilson.mscu.clusterName", "MWDEV_Cluster");
        // Since the users of the command line utiltiy will be the same as the console users, we will use the same keystore directory.
        defaults.setProperty("mtwilson.mscu.keystore.dir", "/var/opt/intel/management-console/users"); // XXX TODO make a linux default and windows default, utiilizing some centralized configuration functions suh as getDataDirectory() which would already provide an os-specific directory that has already been created (or with a function to create it)

	
        return defaults;
                
	}

    // for troubleshooting
    @Override
    public void dumpConfiguration(Configuration c, String label) {
        String keys[] = new String[] { "mtwilson.api.baseurl", "mtwilson.api.keystore", "mtwilson.mscu.gkvHostType" };
        for(String key : keys) {
            String value = c.getString(key);
            log.info(String.format("MSCUConfig [%s]: %s=%s", label, key, value));  
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
