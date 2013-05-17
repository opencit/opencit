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
    
    private static Logger log = LoggerFactory.getLogger(MSConfig.class);
    private static final MSConfig global = new MSConfig();
    public static Configuration getConfiguration() { return global.getConfigurationInstance(); }
    public MSConfig() {
        super("management-service.properties");
    }
    public MSConfig(Properties custom) {
        super("management-service.properties", custom);
    }

    @Override
    public Properties getDefaults() {
        Properties defaults = new Properties();

//        defaults.setProperty("mtwilson.saml.certificate", "saml.cer");// XXX TODO remove this; deprecated in mtwilson-1.1  in favor of mtwilson.saml.certificate.file (PEM format)
        
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
        // mtwilson.privacyca.certificate.list.file=PrivacyCA.p12.pem
        // default props used by CA rest service
        // XXX-TODO generate the ssl file name based on ip address during install
        defaults.setProperty("mtwilson.tls.certificate.file", "/etc/intel/ssl.crt.pem");
        defaults.setProperty("mtwilson.privacyca.cert.file", "/etc/intel/cloudsecurity/PrivacyCA.pem");
        defaults.setProperty("mtwilson.rootca.certficate.file", "/etc/intel/cloudsecurity/MtWilsonRootCA.crt.pem"); 
        defaults.setProperty("mtwilson.saml.certificate.file", "/etc/intel/cloudsecurity/saml.crt.pem");
        defaults.setProperty("mtwilson.privacyca.certificate.list.file", "/etc/intel/cloudsecurity/PrivacyCA.p12.pem");
        return defaults;
    }
    
        /** 
         * note that there are two levels of defaults:   if we dont' find a property mountwilson.ms.db.X, we look for mtwilson.db.X and THEN to our default
         */
     public static Properties getJpaProperties(Configuration config) {
        /*
        Configuration config = getConfiguration();
        Properties prop = new Properties();
        prop.put("javax.persistence.jdbc.driver", 
                config.getString("mountwilson.ms.db.driver", 
                config.getString("mtwilson.db.driver",
                "com.mysql.jdbc.Driver")));
        if( prop.get("javax.persistence.jdbc.driver").equals("com.mysql.jdbc.Driver") ) {
            prop.put("javax.persistence.jdbc.scheme", "mysql"); // NOTE: this is NOT a standard javax.persistence property, we are setting it for our own use
        }
        else if( prop.get("javax.persistence.jdbc.driver").equals("org.postgresql.Driver") ) {
            prop.put("javax.persistence.jdbc.scheme", "postgresql"); // NOTE: this is NOT a standard javax.persistence property, we are setting it for our own use
        }
        else {
            prop.put("javax.persistence.jdbc.scheme", "unknown-scheme");
        }        
        prop.put("javax.persistence.jdbc.url" , 
                config.getString("mountwilson.ms.db.url",
                config.getString("mtwilson.db.url",
                String.format("jdbc:%s://%s:%s/%s?autoReconnect=true",
                    prop.getProperty("javax.persistence.jdbc.scheme"),
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
                config.getString("mountwilson.ms.db.driver", 
                config.getString("mtwilson.db.driver",
                "org.postgresql.Driver")));
        //System.err.println("stdalex msConfig getJpaConfig driver == " + config.getString("mountwilson.ms.db.driver", config.getString("mtwilson.db.driver", "com.mysql.jdbc.Driver")));
        String dbms = (config.getString("mountwilson.ms.db.driver", config.getString("mtwilson.db.driver", "org.postgresql.Driver")).contains("mysql")) ? "mysql" : "postgresql";
        //System.err.println("stdalex msConfig getJpaConfig dbms == " + dbms);
        prop.put("javax.persistence.jdbc.url" , 
                config.getString("mountwilson.ms.db.url",
                config.getString("mtwilson.db.url",
                String.format("jdbc:"+dbms+"://%s:%s/%s?autoReconnect=true",
                    config.getString("mountwilson.ms.db.host", config.getString("mtwilson.db.host","127.0.0.1")),
                    config.getString("mountwilson.ms.db.port", config.getString("mtwilson.db.port","3306")),
                    config.getString("mountwilson.ms.db.schema", config.getString("mtwilson.db.schema","mw_as"))))));
        //System.err.println("stdalex msConfig url == " + prop.getProperty("javax.persistence.jdbc.url")); 
        prop.put("javax.persistence.jdbc.user",
                config.getString("mountwilson.ms.db.user",
                config.getString("mtwilson.db.user",
                "root")));
        prop.put("javax.persistence.jdbc.password", 
                config.getString("mountwilson.ms.db.password", 
                config.getString("mtwilson.db.password", 
                "password")));
        return prop;
    }
    public static Properties getJpaProperties() {
        return getJpaProperties(getConfiguration());
    }
    
}
