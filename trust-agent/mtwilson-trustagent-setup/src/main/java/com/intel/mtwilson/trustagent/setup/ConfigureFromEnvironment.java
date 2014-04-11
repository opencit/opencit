/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.setup;

import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.configuration.EnvironmentConfiguration;
import com.intel.dcsg.cpg.configuration.KeyTransformerConfiguration;
import com.intel.dcsg.cpg.configuration.MutableConfiguration;
import com.intel.dcsg.cpg.util.AllCapsNamingStrategy;
import com.intel.mtwilson.setup.AbstractSetupTask;
import com.intel.mtwilson.trustagent.TrustagentConfiguration;
import java.util.ArrayList;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author jbuhacoff
 */
public class ConfigureFromEnvironment extends AbstractSetupTask {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConfigureFromEnvironment.class);
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
        ArrayList<String> updatelist = new ArrayList<>();
        for (String variable : variables) {
            String envValue = env.getString(variable);
            String confValue = configuration.getString(variable);
            log.debug("checking to see if environment variable [{}] needs to be added to configuration", variable);
            if (envValue != null && !envValue.isEmpty() && (confValue == null || !confValue.equals(envValue))) {
                log.debug("environment variable [{}] needs to be added to configuration", variable);
                updatelist.add(variable);
            }
        }
        
        if (!updatelist.isEmpty()) {
            validation("Updates available for %d settings: %s", updatelist.size(), StringUtils.join(updatelist, ","));
        }
    }

    @Override
    protected void execute() throws Exception {
        for (String variable : variables) {
            String envValue = env.getString(variable);
            if (envValue != null && !envValue.isEmpty()) {
                log.debug("Copying environment variable {} to configuration property {}", allcaps.toAllCaps(variable), variable);
                configuration.setString(variable, envValue);
            }
        }
    }
    
}
