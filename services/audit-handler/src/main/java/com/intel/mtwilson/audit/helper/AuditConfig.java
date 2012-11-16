package com.intel.mtwilson.audit.helper;


import com.intel.mtwilson.util.ConfigBase;
import java.util.Properties;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Attempts to use commons-configuration to load the Attestation Service
 * settings.
 *
 * The configuration is loaded in the following priority order: System
 * properties Properties in the file attestation-service.properties (create this
 * file in your classpath or home directory to customize local settings)
 * Properties in the file attestation-service-defaults.properties (included with
 * ASCommon) Hard-coded defaults (defined in this class)
 *
 * The attestation-service.properties file can be placed in your home directory
 * in order to customize the application settings for your machine.
 *
 * XXX CHANGE: no longer using the attestation-service-config.xml file or the
 * attestation-service-defaults.properties file in the classpath
 *
 * @author jbuhacoff
 */
public class AuditConfig extends ConfigBase {

    private static final AuditConfig global = new AuditConfig();

    public static Configuration getConfiguration() {
        return global.getConfigurationInstance();
    }
    private static final Logger log = LoggerFactory.getLogger(AuditConfig.class);

    public static boolean isAsyncEnabled() {
        if(getConfiguration().getString("mountwilson.audit.async", "false").equalsIgnoreCase("true") )
                return true;
        
        return false;        
    }

    private AuditConfig() {

        super("audit-handler.properties", getDefaults());
    }

    private static Properties getDefaults() {
        Properties defaults = new Properties();
        defaults.setProperty("mountwilson.audit.db.password", "password");
        defaults.setProperty("mountwilson.audit.db.user", "root");
        defaults.setProperty("mountwilson.audit.db.host", "localhost");
        defaults.setProperty("mountwilson.audit.db.schema", "mw_audit");
        defaults.setProperty("mountwilson.audit.enabled", "true");
        defaults.setProperty("mountwilson.audit.logunchangedcolumns", "true");
        defaults.setProperty("mountwilson.audit.async", "false");
        
        return defaults;
    }

    // for troubleshooting
    @Override
    public void dumpConfiguration(Configuration c, String label) {
//        String keys[] = new String[]{"mountwilson.audit.db.host", "mountwilson.audit.db.schema", "mountwilson.audit.db.user"};
//        for (String key : keys) {
//            String value = c.getString(key);
//        }
    }

    public static Properties getJpaProperties() {
        Configuration config = getConfiguration();
        Properties prop = new Properties();
        prop.put("javax.persistence.jdbc.driver", config.getString("mountwilson.audit.db.driver", "com.mysql.jdbc.Driver"));
        prop.put("javax.persistence.jdbc.url", String.format("jdbc:mysql://%s:3306/%s?autoReconnect=true",
                config.getString("mountwilson.audit.db.host", "localhost"),
                config.getString("mountwilson.audit.db.schema", "mw_audit")));
        prop.put("javax.persistence.jdbc.user", config.getString("mountwilson.audit.db.user", "root"));
        prop.put("javax.persistence.jdbc.password", config.getString("mountwilson.audit.db.password", "password"));
        return prop;
    }
    
    public static boolean isAuditEnabled(){
        if(getConfiguration().getString("mountwilson.audit.enabled", "true").equalsIgnoreCase("false") )
            return false;
        
        return true;
    }
    
    public static boolean isUnchangedColumnsRequired(){
        if(getConfiguration().getString("mountwilson.audit.logunchangedcolumns", "true").equalsIgnoreCase("false") )
            return false;
        
        return true;
        
    }
    
    
}
