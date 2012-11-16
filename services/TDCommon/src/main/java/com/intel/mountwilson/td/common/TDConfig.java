/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mountwilson.td.common;

import com.intel.mtwilson.util.ConfigBase;
import java.util.Properties;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
public class TDConfig extends ConfigBase{
    
    private static final TDConfig global = new TDConfig();
    public static Configuration getConfiguration() { return global.getConfigurationInstance(); }
    
    private Logger log = LoggerFactory.getLogger(getClass().getName());
    
    private TDConfig() {
        super("trust-dashboard.properties",getDefaults());
    }

	private static Properties getDefaults() {
		Properties defaults = new Properties();
        defaults.setProperty("mountwilson.td.db.password", "");
        defaults.setProperty("mountwilson.td.db.user", "root");
        defaults.setProperty("mountwilson.td.db.host", "localhost");
        defaults.setProperty("mountwilson.td.db.schema", "cloudportal");
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
        prop.put("javax.persistence.jdbc.url" ,String.format("jdbc:mysql://%s:3306/%s",
                    config.getString("mountwilson.td.db.host", "localhost"),
                    config.getString("mountwilson.td.db.schema", "cloudportal")));
        prop.put("javax.persistence.jdbc.user" ,config.getString("mountwilson.td.db.user", "root"));
        prop.put("javax.persistence.jdbc.password", config.getString("mountwilson.td.db.password", "password"));
        return prop;
    }
    
    
}
