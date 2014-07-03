package com.intel.mtwilson.audit.helper;


import com.intel.mtwilson.My;
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
 * @author jbuhacoff
 */
public class AuditConfig  {

    private static final Logger log = LoggerFactory.getLogger(AuditConfig.class);

    public static Configuration getConfiguration() {
        return My.configuration().getConfiguration();
    }
    
    public static boolean isAsyncEnabled() {
        if(getConfiguration().getString("mountwilson.audit.async", "false").equalsIgnoreCase("true") )
                return true;
        
        return false;        
    }


    public Properties getDefaults() {
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
