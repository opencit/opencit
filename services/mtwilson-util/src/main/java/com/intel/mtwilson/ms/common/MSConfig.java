/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.ms.common;

/**
 *
 * @author dsmagadx
 */

import com.intel.mtwilson.My;
import java.util.Properties;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MSConfig  {
    
    private static Logger log = LoggerFactory.getLogger(MSConfig.class);
    private static final MSConfig global = new MSConfig();
    
    public static Configuration getConfiguration() {
        return My.configuration().getConfiguration();
    }

    public Properties getDefaults() {
        Properties defaults = new Properties();

//        defaults.setProperty("mtwilson.saml.certificate", "saml.cer");
        
        defaults.setProperty("mtwilson.ms.biosPCRs", "0;17");
        defaults.setProperty("mtwilson.ms.vmmPCRs", "18;19;20");
//        defaults.setProperty("mtwilson.ms.portalDBConnectionString", "jdbc:mysql://127.0.0.1:3306/cloudportal"); 
//        defaults.setProperty("mtwilson.ms.portalDBUserName", "root");
//        defaults.setProperty("mtwilson.ms.portalDBPassword", "password");        
        defaults.setProperty("mtwilson.ms.keystore.dir", "/var/opt/intel/management-service/users"); 

        defaults.setProperty("mtwilson.api.baseurl", "https://127.0.0.1:8181");
        defaults.setProperty("mtwilson.api.keystore", "/etc/intel/cloudsecurity/mw.jks");
        defaults.setProperty("mtwilson.api.keystore.password", "password");
        defaults.setProperty("mtwilson.api.key.alias", "Admin");
        defaults.setProperty("mtwilson.api.key.password", "password");
        defaults.setProperty("mtwilson.api.ssl.verifyHostname", "true"); 
        defaults.setProperty("mtwilson.api.ssl.requireTrustedCertificate", "true");
        
        defaults.setProperty("mtwilson.ssl.required", "true"); // secure by default; must set to false to allow non-SSL connections
        //defaults.setProperty("mtwilson.api.trust", "127.0.0.1"); // this setting is disabled because it violates "secure by default"
        // mtwilson.privacyca.certificate.list.file=PrivacyCA.list.pem
        // default props used by CA rest service
        defaults.setProperty("mtwilson.tls.certificate.file", "/etc/intel/cloudsecurity/ssl.crt.pem");
        defaults.setProperty("mtwilson.privacyca.cert.file", "/etc/intel/cloudsecurity/PrivacyCA.pem");
        defaults.setProperty("mtwilson.rootca.certficate.file", "/etc/intel/cloudsecurity/MtWilsonRootCA.crt.pem"); 
        defaults.setProperty("mtwilson.saml.certificate.file", "/etc/intel/cloudsecurity/saml.crt.pem");
        defaults.setProperty("mtwilson.privacyca.certificate.list.file", "/etc/intel/cloudsecurity/PrivacyCA.list.pem");
        return defaults;
    }
    
    
}
