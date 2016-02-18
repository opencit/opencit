/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mountwilson.common;

import com.intel.dcsg.cpg.configuration.CommonsConfigurationAdapter;
import com.intel.mtwilson.Folders;
import com.intel.mtwilson.configuration.ConfigurationFactory;
import com.intel.mtwilson.configuration.ConfigurationProvider;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;


import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Attempts to use commons-configuration to load the Trust Agent settings.
 * 
 * The configuration is loaded in the following priority order:
 * System properties
 * Properties in the file trustagent.properties (create this file in your classpath to customize local settings)
 * Hard-coded defaults (defined in this class)
 * 
 * The available configuration sources (such as trustagent.properties) are configured in the ta-config.xml
 * included with Trust Agent
 * 
 * @author jbuhacoff
 */
public class TAConfig {

    private static final TAConfig global = new TAConfig();
    public static final Configuration getConfiguration() { return global.getConfigurationInstance(); }
    
    private final Configuration config;
    private Configuration getConfigurationInstance() { return config; }
    private Logger log = LoggerFactory.getLogger(getClass().getName());
    
    private TAConfig() {
        Properties defaults = new Properties();
//        defaults.setProperty("app.path", MyFilesystem.getApplicationFilesystem().getApplicationPath());
        defaults.setProperty("debug", "false"); // allowed values: false, true (case insensitive)
//        defaults.setProperty("nonce.filename", "nonce"); // only used from TADataContext.getNonceFileName by appending to var dir
//        defaults.setProperty("aikquote.filename", "aikquote"); // only used from TADataContext.getQuoteFileName by appending to var dir
        defaults.setProperty("aikblob.filename", "aik.blob");
        defaults.setProperty("aikcert.filename", "aik.pem"); // issue #878 the aikcert is in PEM format so we label it properly
        defaults.setProperty("ekcert.filename", "ekcert.cer");
        defaults.setProperty("daa.challenge.filename", "daa-challenge");
        defaults.setProperty("daa.response.filename.filename", "daa-response");        
//        defaults.setProperty("cert.folder", "cert");
//        defaults.setProperty("data.folder", "data");
//        defaults.setProperty("secure.port", "9999");
//        defaults.setProperty("nonsecure.port", "9998");
//        defaults.setProperty("daa.enabled", "false");
        // Additional properties to support module attestation
//        defaults.setProperty("modules.folder", "modules"); 
//        defaults.setProperty("modulesXml.filename", "measureLog.xml"); // only used from TADataContext.getMeasureLogXmlFile()
//        defaults.setProperty("modulesScript.filename", "module_analysis.sh");        
        config = gatherConfiguration(defaults);
    }
    
    // for troubleshooting
    private void dumpConfiguration(Configuration c, String label) {
        String keys[] = new String[] { /*"app.path",*/ "debug", "trustagent.http.tls.port", "mtwilson.api.url" };
        for(String key : keys) {
            String value = c.getString(key);
            System.out.println(String.format("TAConfig [%s]: %s=%s", label, key, value));
        }
    }

    private Configuration gatherConfiguration(Properties defaults)  {
        try {
        CompositeConfiguration composite = new CompositeConfiguration();
        
        // first priority is the configuration file
        File file = new File(Folders.configuration() + File.separator + "trustagent.properties");
        
        ConfigurationProvider provider = ConfigurationFactory.createConfigurationProvider(file);
        Configuration standard = new CommonsConfigurationAdapter(provider.load());
        
        dumpConfiguration(standard, "file:"+file.getAbsolutePath());
        composite.addConfiguration(standard);
        
        // second priority are the defaults that were passed in, we use them if no better source was found
        if( defaults != null ) {
            MapConfiguration defaultconfig = new MapConfiguration(defaults);
            dumpConfiguration(defaultconfig, "default");
            composite.addConfiguration(defaultconfig);
        }
        dumpConfiguration(composite, "composite");
        return composite;
        }
        catch(Exception e) {
            throw new RuntimeException("Cannot load properties configuration", e);
        }
    }
    

    
}
