/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.setup;

import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.configuration.EnvironmentConfiguration;
import com.intel.dcsg.cpg.configuration.KeyTransformerConfiguration;
import com.intel.dcsg.cpg.configuration.MutableConfiguration;
import com.intel.dcsg.cpg.configuration.PropertiesConfiguration;
import com.intel.dcsg.cpg.util.AllCapsNamingStrategy;
import com.intel.mtwilson.MyFilesystem;
import com.intel.mtwilson.setup.AbstractSetupTask;
import com.intel.mtwilson.trustagent.TrustagentConfiguration;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 *
 * @author jbuhacoff
 */
public class ConfigureFromEnvironment extends AbstractSetupTask {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConfigureFromEnvironment.class);
    PropertiesConfiguration fileconfig;
    MutableConfiguration configuration;
    String[] variables;
    AllCapsNamingStrategy allcaps;
    Configuration env;
    
    @Override
    protected void configure() throws Exception {
        configuration = getConfiguration();
        variables = new String[] {
            TrustagentConfiguration.MTWILSON_API_URL,
            TrustagentConfiguration.MTWILSON_API_USERNAME,
            TrustagentConfiguration.MTWILSON_API_PASSWORD,
            TrustagentConfiguration.MTWILSON_TLS_CERT_SHA1,
            TrustagentConfiguration.TPM_QUOTE_IPV4,
            TrustagentConfiguration.TPM_OWNER_SECRET,
            TrustagentConfiguration.TPM_SRK_SECRET,
            TrustagentConfiguration.TRUSTAGENT_HTTP_TLS_PORT,
            TrustagentConfiguration.TRUSTAGENT_KEYSTORE_PASSWORD,
            TrustagentConfiguration.TRUSTAGENT_TLS_CERT_DN,
            TrustagentConfiguration.TRUSTAGENT_TLS_CERT_DNS,
            TrustagentConfiguration.TRUSTAGENT_TLS_CERT_IP,
            TrustagentConfiguration.AIK_SECRET,
            TrustagentConfiguration.AIK_INDEX,
            TrustagentConfiguration.DAA_ENABLED
        };
        allcaps = new AllCapsNamingStrategy();
        env = new KeyTransformerConfiguration(allcaps, new EnvironmentConfiguration()); // transforms mtwilson.ssl.cert.sha1 to MTWILSON_SSL_CERT_SHA1
    }

    @Override
    protected void validate() throws Exception {
        File file = new File(MyFilesystem.getApplicationFilesystem().getConfigurationPath() + File.separator + "trustagent.properties");
        if (file.exists()) {
            log.debug("Loading just the configuration file {}", file.getAbsolutePath());
            try (FileInputStream in = new FileInputStream(file)) {
                Properties properties = new Properties();
                properties.load(in);
                fileconfig = new PropertiesConfiguration(properties);
            }
        } else {
            fileconfig = new PropertiesConfiguration();
        }
        
        for(String variable : variables) {
            String confValue = configuration.getString(variable);
//            String envValue = env.getString(variable);
            if( confValue == null || confValue.isEmpty() ) {
//                validation("trustagent.properties variable [" + variable + "] cannot be null or empty");
//                log.warn("trustagent.properties variable [{}] is null or empty", variable);
            }
//            if( !confValue.equals(envValue)) {
//                validation("[{}] variable for configuration [{}] does not match variable for environment [{}]", variable, confValue, envValue);
//            }
            log.debug("loading configuration variable [" + variable + "] with value [" + confValue + "]");
        }
        if( fileconfig.getString(TrustagentConfiguration.MTWILSON_API_URL) == null || fileconfig.getString(TrustagentConfiguration.MTWILSON_API_URL).isEmpty()) {
            validation("trustagent.properties variable [" + TrustagentConfiguration.MTWILSON_API_URL + "] cannot be null or empty");
        }
        if( fileconfig.getString(TrustagentConfiguration.MTWILSON_API_USERNAME) == null || fileconfig.getString(TrustagentConfiguration.MTWILSON_API_USERNAME).isEmpty()) {
            validation("trustagent.properties variable [" + TrustagentConfiguration.MTWILSON_API_USERNAME + "] cannot be null or empty");
        }
        if( fileconfig.getString(TrustagentConfiguration.MTWILSON_API_PASSWORD) == null || fileconfig.getString(TrustagentConfiguration.MTWILSON_API_PASSWORD).isEmpty()) {
            validation("trustagent.properties variable [" + TrustagentConfiguration.MTWILSON_API_PASSWORD + "] cannot be null or empty");
        }
        if( fileconfig.getString(TrustagentConfiguration.MTWILSON_TLS_CERT_SHA1) == null || fileconfig.getString(TrustagentConfiguration.MTWILSON_TLS_CERT_SHA1).isEmpty()) {
            validation("trustagent.properties variable [" + TrustagentConfiguration.MTWILSON_TLS_CERT_SHA1 + "] cannot be null or empty");
        }
    }

    @Override
    protected void execute() throws Exception {
        for(String variable : variables) {
            String value = env.getString(variable);
            if( value != null && !value.isEmpty() ) {
                log.debug("Copying environment variable {} to configuration property {} with value {}", allcaps.toAllCaps(variable), variable, value);
                configuration.setString(variable, value);
            }
        }
    }
    
}
