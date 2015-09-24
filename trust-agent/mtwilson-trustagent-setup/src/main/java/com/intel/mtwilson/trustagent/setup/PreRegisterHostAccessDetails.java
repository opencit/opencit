/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.setup;

import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.dcsg.cpg.tls.policy.TlsPolicyBuilder;
import com.intel.mtwilson.attestation.client.jaxrs.Hosts;
import com.intel.mtwilson.setup.AbstractSetupTask;
import com.intel.mtwilson.shiro.file.cmd.Password;
import com.intel.mtwilson.trustagent.TrustagentConfiguration;
import java.io.File;
import java.net.URL;
import java.util.Properties;

/**
 *
 * @author ssbangal
 */
public class PreRegisterHostAccessDetails extends AbstractSetupTask {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PreRegisterHostAccessDetails.class);
    private TrustagentConfiguration trustagentConfiguration;
    private String trustagentUserName;
    private String trustagentPassword;
    private String url;
    private String username;
    private String password;
    private File keystoreFile;
    private String keystorePassword;
    
    @Override
    protected void configure() throws Exception {
        trustagentConfiguration = new TrustagentConfiguration(getConfiguration()); 
        
        url = trustagentConfiguration.getMtWilsonApiUrl();
        if( url == null || url.isEmpty() ) {
            configuration("Mt Wilson URL is not set");
        }
        /*username = trustagentConfiguration.getMtWilsonApiUsername();
        password = trustagentConfiguration.getMtWilsonApiPassword();
        if( username == null || username.isEmpty() ) {
            configuration("Mt Wilson username is not set");
        }
        if( password == null || password.isEmpty() ) {
            configuration("Mt Wilson password is not set");
        }*/
        
        keystoreFile = trustagentConfiguration.getTrustagentKeystoreFile();
        if( keystoreFile == null || !keystoreFile.exists() ) {
            configuration("Trust Agent keystore does not exist");
        }
        keystorePassword = trustagentConfiguration.getTrustagentKeystorePassword();
        if( keystorePassword == null || keystorePassword.isEmpty() ) {
            configuration("Trust Agent keystore password is not set");
        }        
    }

    @Override
    protected void validate() throws Exception {
        trustagentConfiguration = new TrustagentConfiguration(getConfiguration());
        
        // TODO: Do we validate the users.txt file or the properties file.
        trustagentUserName = trustagentConfiguration.getTrustAgentUserName();
        if( trustagentUserName == null || trustagentUserName.isEmpty() ) {
            validation("TrustAgent User name is not set.");
        }

        trustagentPassword = trustagentConfiguration.getTrustAgentPassword();
        if( trustagentPassword == null || trustagentPassword.isEmpty() ) {
            validation("TrustAgent password is not set.");
        }                
    }

    @Override
    protected void execute() throws Exception {
        
        log.info("Starting the process to create and pre-register the host login and password with attestation service.");

        if( System.getenv("AUTO_GENERATE_LOGIN_DETAILS") != null && System.getenv("AUTO_GENERATE_LOGIN_DETAILS").equalsIgnoreCase("true")) {
            trustagentUserName = RandomUtil.randomHexString(20);
            log.debug("Generated random user name {}", trustagentUserName);         

            trustagentPassword = RandomUtil.randomHexString(20);
            log.debug("Generated random password {}", trustagentPassword); 
        } else {
            if (System.getenv("LOGIN_ID") != null) 
                trustagentUserName = System.getenv("LOGIN_ID");

            if (System.getenv("LOGIN_PASSWORD") != null) 
                trustagentUserName = System.getenv("LOGIN_PASSWORD");            
        }
        
        if (trustagentUserName != null && trustagentPassword != null) {
            
            if( System.getenv("PRE_REGISTER_HOST") != null && System.getenv("PRE_REGISTER_HOST").equalsIgnoreCase("true")) {
            
                TlsPolicy tlsPolicy = TlsPolicyBuilder.factory().strictWithKeystore(trustagentConfiguration.getTrustagentKeystoreFile(), 
                        trustagentConfiguration.getTrustagentKeystorePassword()).build();
                TlsConnection tlsConnection = new TlsConnection(new URL(url), tlsPolicy);

                Properties clientConfiguration = new Properties();
                clientConfiguration.setProperty(TrustagentConfiguration.MTWILSON_API_USERNAME, "admin");
                clientConfiguration.setProperty(TrustagentConfiguration.MTWILSON_API_PASSWORD, "password");

                Hosts hostClientObj = new Hosts(clientConfiguration, tlsConnection);
                hostClientObj.preRegisterHostDetails("localhost", trustagentUserName, trustagentPassword);

                log.info("Successfully registered the host access information with attestation service.");
            }
                        
            // Store the user and its corresponding permissions
            Password pwd = new Password();
            pwd.execute(new String[] {trustagentUserName, trustagentPassword, "*:*"});
            
            getConfiguration().set(TrustagentConfiguration.TRUSTAGENT_USER_NAME, trustagentUserName);
            getConfiguration().set(TrustagentConfiguration.TRUSTAGENT_PASSWORD, trustagentPassword);
            
            log.info("Successfully updated the property files with the username & password.");
        } else {
            log.error("PreRegisterHostAccessDetails: Invalid user name or password specified.");
        }
    }    
}
