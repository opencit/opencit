package com.intel.mountwilson.as.common;

import com.intel.mtwilson.util.ConfigBase;
import java.util.Properties;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Attempts to use commons-configuration to load the Attestation Service settings.
 * 
 * The configuration is loaded in the following priority order:
 * System properties
 * Properties in the file attestation-service.properties (create this file in your classpath or home directory to customize local settings)
 * Properties in the file attestation-service-defaults.properties (included with ASCommon)
 * Hard-coded defaults (defined in this class)
 * 
 * The attestation-service.properties file can be placed in your home directory 
 * in order to customize the application settings for your machine.
 * 
 * XXX CHANGE:  no longer using the attestation-service-config.xml file or the attestation-service-defaults.properties file in the classpath
 * 
 * @author jabuhacx
 */
public class ASConfig extends ConfigBase{
    
    private static final ASConfig global = new ASConfig();
    public static Configuration getConfiguration() { return global.getConfigurationInstance(); }
    
    private static final Logger log = LoggerFactory.getLogger(ASConfig.class);
    
    private ASConfig() {
        
        super("attestation-service.properties", getDefaults());
    }

	private static Properties getDefaults() {
		Properties defaults = new Properties();
        defaults.setProperty("mountwilson.as.db.password", "");
        defaults.setProperty("mountwilson.as.db.user", "root");
        defaults.setProperty("mountwilson.as.db.host", "localhost");
        defaults.setProperty("mountwilson.as.db.schema", "mw_as");
        defaults.setProperty("com.intel.mountwilson.as.home", "C:/work/aikverifyhome"); // used by TAHelper
        defaults.setProperty("com.intel.mountwilson.as.openssl.cmd", "openssl.bat"); // used by TAHelper
        defaults.setProperty("com.intel.mountwilson.as.aikqverify.cmd", "aikqverify.exe"); // used by TAHelper
        defaults.setProperty("daa.enabled", "false");
        defaults.setProperty("com.intel.mountwilson.as.trustagent.timeout", "3"); // seconds
        // mtwilson.as.dek = base64-encoded AES key used by HostBO
        // mtwilson.taca.keystore.password
        // mtwilson.taca.key.alias
        // mtwilson.taca.key.password
		return defaults;
	}

    // for troubleshooting
	@Override
    public void dumpConfiguration(Configuration c, String label) {
            /*
        String keys[] = new String[] { "mountwilson.as.db.host", "mountwilson.as.db.schema", "mountwilson.as.db.user" };
        for(String key : keys) {
            String value = c.getString(key);
            log.info(String.format("ASConfig [%s]: %s=%s", label, key, value)); // XXX getting NullPointerException on the "log" object. 20120626 
        }*/
    }

        
    public static Properties getJpaProperties() {
        Configuration config = getConfiguration();
        Properties prop = new Properties();
        prop.put("javax.persistence.jdbc.driver", config.getString("mountwilson.as.db.driver", "com.mysql.jdbc.Driver"));
        prop.put("javax.persistence.jdbc.url" ,String.format("jdbc:mysql://%s:3306/%s?autoReconnect=true",
                    config.getString("mountwilson.as.db.host", "localhost"),
                    config.getString("mountwilson.as.db.schema", "mw_as")));
        prop.put("javax.persistence.jdbc.user" ,config.getString("mountwilson.as.db.user", "root"));
        prop.put("javax.persistence.jdbc.password", config.getString("mountwilson.as.db.password", "password"));
        return prop;
    }
    
    public static int getTrustAgentTimeOutinMilliSecs(){
        // Return timeout in milliseconds
        return getConfiguration().getInt("com.intel.mountwilson.as.trustagent.timeout", 3) * 1000;
    }
}
