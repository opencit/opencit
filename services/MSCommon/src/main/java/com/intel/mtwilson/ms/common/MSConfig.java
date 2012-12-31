/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.ms.common;

/**
 *
 * @author dsmagadx
 */

import java.util.Properties;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.mtwilson.util.ConfigBase;

public class MSConfig extends ConfigBase {
    
    private Logger log = LoggerFactory.getLogger(getClass().getName());
    private static final MSConfig global = new MSConfig();
    public static Configuration getConfiguration() { return global.getConfigurationInstance(); }
       
    private MSConfig() {
        super("management-service.properties",getDefaults());
    }

    private static Properties getDefaults() {
	Properties defaults = new Properties();

        defaults.setProperty("mountwilson.ms.db.password", "password");
        defaults.setProperty("mountwilson.ms.db.user", "root");
        defaults.setProperty("mountwilson.ms.db.host", "localhost");
        defaults.setProperty("mountwilson.ms.db.schema", "mw_ms");

        defaults.setProperty("mtwilson.saml.certificate", "saml.cer");
        
        defaults.setProperty("mtwilson.ms.biosPCRs", "0");
        defaults.setProperty("mtwilson.ms.vmmPCRs", "17;18;19;20");
//        defaults.setProperty("mtwilson.ms.portalDBConnectionString", "jdbc:mysql://127.0.0.1:3306/cloudportal"); // XXX TODO deprecated;   cloudportal database has been dropped as of 1.0-RC2
//        defaults.setProperty("mtwilson.ms.portalDBUserName", "root");
//        defaults.setProperty("mtwilson.ms.portalDBPassword", "password");        
        defaults.setProperty("mtwilson.ms.keystore.dir", "/var/opt/intel/management-service/users"); // XXX TODO make a linux default and windows default, utiilizing some centralized configuration functions suh as getDataDirectory() which would already provide an os-specific directory that has already been created (or with a function to create it)

        defaults.setProperty("mtwilson.api.baseurl", "https://127.0.0.1:8181");
        defaults.setProperty("mtwilson.api.keystore", "/etc/intel/cloudsecurity/mw.jks");
        defaults.setProperty("mtwilson.api.keystore.password", "password");
        defaults.setProperty("mtwilson.api.key.alias", "Admin");
        defaults.setProperty("mtwilson.api.key.password", "password");
        defaults.setProperty("mtwilson.api.ssl.verifyHostname", "true"); 
        defaults.setProperty("mtwilson.api.ssl.requireTrustedCertificate", "true");
        
        defaults.setProperty("mtwilson.ssl.required", "true"); // secure by default; must set to false to allow non-SSL connections
        //defaults.setProperty("mtwilson.api.trust", "127.0.0.1"); // this setting is disabled because it violates "secure by default"
        
        return defaults;
    }

    // for troubleshooting
	@Override
    public void dumpConfiguration(Configuration c, String label) {
//        String keys[] = new String[] { "mountwilson.ms.db.host", "mountwilson.ms.db.schema", "mountwilson.ms.db.user" };
//        for(String key : keys) {
//            String value = c.getString(key);
//            log.info(String.format("MSConfig [%s]: %s=%s", label, key, value));
//        }
    }
    
    public static Properties getJpaProperties() {
        Configuration config = getConfiguration();
        Properties prop = new Properties();
        prop.put("javax.persistence.jdbc.driver", config.getString("mountwilson.ms.db.driver", "com.mysql.jdbc.Driver"));
        prop.put("javax.persistence.jdbc.url" , config.getString("mountwilson.ms.db.url",String.format("jdbc:mysql://%s:3306/%s?autoReconnect=true",
                    config.getString("mountwilson.ms.db.host", "localhost"),
                    config.getString("mountwilson.ms.db.schema", "mw_as"))));
        prop.put("javax.persistence.jdbc.user" ,config.getString("mountwilson.ms.db.user", "root"));
        prop.put("javax.persistence.jdbc.password", config.getString("mountwilson.ms.db.password", "password"));
        return prop;
    }
    
    public static String getSamlCertificateName(){
    	return getConfiguration().getString("mtwilson.saml.certificate", "saml.cer");
    }

}
