/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.setup;

import com.intel.dcsg.cpg.configuration.EnvironmentConfiguration;
import com.intel.dcsg.cpg.configuration.KeyTransformerConfiguration;
import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.mtwilson.text.transform.AllCapsNamingStrategy;
import com.intel.mtwilson.setup.AbstractSetupTask;
import com.intel.mtwilson.tls.policy.creator.impl.CertificateDigestTlsPolicyCreator;
import com.intel.mtwilson.tls.policy.creator.impl.CertificateTlsPolicyCreator;
import com.intel.mtwilson.tls.policy.creator.impl.InsecureTlsPolicyCreator;
import com.intel.mtwilson.tls.policy.creator.impl.InsecureTrustFirstPublicKeyTlsPolicyCreator;
import com.intel.mtwilson.tls.policy.creator.impl.PublicKeyDigestTlsPolicyCreator;
import com.intel.mtwilson.tls.policy.creator.impl.PublicKeyTlsPolicyCreator;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyCreator;
import com.intel.mtwilson.trustagent.TrustagentConfiguration;
import java.util.ArrayList;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author jbuhacoff
 */
public class ConfigureFromEnvironment extends AbstractSetupTask {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConfigureFromEnvironment.class);
    private Configuration configuration;
    private String[] variables;
    private AllCapsNamingStrategy allcaps;
    private Configuration env;
    
    @Override
    protected void configure() throws Exception {
        log.debug("setup task configuration instance {}", getConfiguration());
        configuration = getConfiguration();
        variables = new String[] {
            TrustagentConfiguration.MTWILSON_API_URL,
            TrustagentConfiguration.MTWILSON_API_USERNAME, // NOTE: excluded from storing in trustagent.properties by com.intel.mtwilson.trustagent.cmd.Setup beforeStore
            TrustagentConfiguration.MTWILSON_API_PASSWORD, // NOTE: excluded from storing in trustagent.properties by com.intel.mtwilson.trustagent.cmd.Setup beforeStore
            TrustagentConfiguration.MTWILSON_TLS_CERT_SHA256,
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
            TrustagentConfiguration.DAA_ENABLED,
            TrustagentConfiguration.HARDWARE_UUID
        };
        allcaps = new AllCapsNamingStrategy();
        env = new KeyTransformerConfiguration(allcaps, new EnvironmentConfiguration()); // transforms mtwilson.ssl.cert.sha1 to MTWILSON_SSL_CERT_SHA1
        
        // TODO: load extensions temporarily so that installer works
        Extensions.register(TlsPolicyCreator.class, CertificateTlsPolicyCreator.class);
        Extensions.register(TlsPolicyCreator.class, CertificateDigestTlsPolicyCreator.class);
        Extensions.register(TlsPolicyCreator.class, PublicKeyTlsPolicyCreator.class);
        Extensions.register(TlsPolicyCreator.class, PublicKeyDigestTlsPolicyCreator.class);
        Extensions.register(TlsPolicyCreator.class, InsecureTlsPolicyCreator.class);
        Extensions.register(TlsPolicyCreator.class, InsecureTrustFirstPublicKeyTlsPolicyCreator.class);
    }

    @Override
    protected void validate() throws Exception {
        ArrayList<String> updatelist = new ArrayList<>();
        for (String variable : variables) {
            String envValue = env.get(variable, null);
            String confValue = configuration.get(variable, null);
            log.debug("checking to see if environment variable [{}] needs to be added to configuration", variable);
            log.debug("env {} property {}", envValue, confValue);
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
            String envValue = env.get(variable, null);
            if (envValue != null && !envValue.isEmpty()) {
                log.debug("Copying environment variable {} to configuration property {} with value {}", allcaps.toAllCaps(variable), variable, envValue);
                configuration.set(variable, envValue);
            }
        }
    }
    
}
