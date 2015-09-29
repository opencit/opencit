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
import com.intel.mtwilson.shiro.file.LoginDAO;
import com.intel.mtwilson.shiro.file.cmd.Password;
import com.intel.mtwilson.shiro.file.model.UserPassword;
import com.intel.mtwilson.shiro.file.model.UserPermission;
import com.intel.mtwilson.trustagent.TrustagentConfiguration;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author ssbangal
 */
public class PreRegisterHostAccessDetails extends AbstractSetupTask {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PreRegisterHostAccessDetails.class);
    private static final String ipv6RegEx = "^([0-9A-Fa-f]{1,4}:){7}(.)*$";
    private TrustagentConfiguration trustagentConfiguration;
    private String trustagentUserName;
    private String trustagentPassword;
    private String url;
    private String username;
    private String password;
    private File keystoreFile;
    private String keystorePassword;

    private String dn;
    private String[] ip;
    private String[] dns;
    
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
        
        dn = trustagentConfiguration.getTrustagentTlsCertDn();
        ip = trustagentConfiguration.getTrustagentTlsCertIpArray();
        dns = trustagentConfiguration.getTrustagentTlsCertDnsArray();
        if( dn == null || dn.isEmpty() ) { configuration("DN not configured"); }

        if( (ip == null ? 0 : ip.length) + (dns == null ? 0 : dns.length) == 0 ) {
            configuration("At least one IP or DNS alternative name must be configured");
        }        
    }

    @Override
    protected void validate() throws Exception {
        trustagentConfiguration = new TrustagentConfiguration(getConfiguration());

        trustagentUserName = trustagentConfiguration.getTrustAgentLoginId();
        log.debug("Retrieving username {} from the configuration file.", trustagentUserName);        
        if( trustagentUserName == null || trustagentUserName.isEmpty() ) {
            validation("TrustAgent User name is not set.");
        }
        
        File userFile = trustagentConfiguration.getTrustagentUserFile();
        File permissionFile = trustagentConfiguration.getTrustagentPermissionsFile();
        
        log.debug("Verifying user & his permissions @ {} & {}.", userFile.getAbsolutePath(), permissionFile.getAbsolutePath());
        
        LoginDAO loginDAO = new LoginDAO(userFile, permissionFile);
        UserPassword userPassword = loginDAO.findUserByName(trustagentUserName);
        if( userPassword == null ) {
            validation("User does not exist: %s", trustagentUserName);
        }
        List<UserPermission> userPermissionList = loginDAO.getPermissions(trustagentUserName);
        if( userPermissionList == null ||  userPermissionList.isEmpty() ) {
            validation("User does not have permissions assigned: %s", trustagentUserName);
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
                trustagentPassword = System.getenv("LOGIN_PASSWORD");            
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
                List<String> hostNames = new ArrayList<>();
                hostNames.addAll(Arrays.asList(dns));
                hostNames.addAll(Arrays.asList(ip));
                // Remove the localhost and local ip from the list
                for (Iterator<String> iterator = hostNames.listIterator(); iterator.hasNext();) {
                    String hostName = iterator.next();
                    if (hostName.equalsIgnoreCase("localhost") || hostName.equalsIgnoreCase("127.0.0.1") || hostName.matches(ipv6RegEx))
                        iterator.remove();
                }
                hostClientObj.preRegisterHostDetails(hostNames, trustagentUserName, trustagentPassword);

                log.info("Successfully registered the host access information with attestation service.");
            }
                        
            // Store the user and its corresponding permissions
            Password pwd = new Password();
            pwd.execute(new String[] {trustagentUserName, trustagentPassword, "*:*"});
            
            // We need to store the user name here so that we can use for validation. Password will not be stored in the property file
            log.debug("Setting username {} in the configuration file.", trustagentUserName);
            getConfiguration().set(TrustagentConfiguration.TRUSTAGENT_LOGIN_ID, trustagentUserName);
            
            log.info("Successfully updated the property file with the username and shiro files with username and password.");
        } else {
            log.error("PreRegisterHostAccessDetails: Invalid user name or password specified.");
        }
    }    
}
