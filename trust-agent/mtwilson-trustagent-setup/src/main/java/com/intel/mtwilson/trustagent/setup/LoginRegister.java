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
public class LoginRegister extends AbstractSetupTask {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoginRegister.class);
    private static final String ipv6RegEx = "^([0-9A-Fa-f]{1,4}:){7}(.)*$";
    private TrustagentConfiguration trustagentConfiguration;
    private String trustagentLoginUserName;
    private String trustagentLoginPassword;
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
        username = trustagentConfiguration.getMtWilsonApiUsername();
        password = trustagentConfiguration.getMtWilsonApiPassword();
        if( username == null || username.isEmpty() ) {
            configuration("Mt Wilson username is not set");
        }
        if( password == null || password.isEmpty() ) {
            configuration("Mt Wilson password is not set");
        }
        
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

        trustagentLoginUserName = trustagentConfiguration.getTrustAgentLoginUserName();
        log.debug("Retrieving username {} from the configuration file.", trustagentLoginUserName);        
        if( trustagentLoginUserName == null || trustagentLoginUserName.isEmpty() ) {
            validation("TrustAgent User name is not set.");
        }
        
        File userFile = trustagentConfiguration.getTrustagentUserFile();
        File permissionFile = trustagentConfiguration.getTrustagentPermissionsFile();
        
        log.debug("Verifying user & his permissions @ {} & {}.", userFile.getAbsolutePath(), permissionFile.getAbsolutePath());
        
        LoginDAO loginDAO = new LoginDAO(userFile, permissionFile);
        UserPassword userPassword = loginDAO.findUserByName(trustagentLoginUserName);
        if( userPassword == null ) {
            validation("User does not exist: %s", trustagentLoginUserName);
        }
        List<UserPermission> userPermissionList = loginDAO.getPermissions(trustagentLoginUserName);
        if( userPermissionList == null ||  userPermissionList.isEmpty() ) {
            validation("User does not have permissions assigned: %s", trustagentLoginUserName);
        }  
    }

    @Override
    protected void execute() throws Exception {
        log.info("Starting the process to create and pre-register the host login and password with attestation service.");

        // First check if the user has provided the login name and password
        if (System.getenv("TRUSTAGENT_LOGIN_USERNAME") != null) 
            trustagentLoginUserName = System.getenv("TRUSTAGENT_LOGIN_USERNAME");

        if (System.getenv("TRUSTAGENT_LOGIN_PASSWORD") != null) 
            trustagentLoginPassword = System.getenv("TRUSTAGENT_LOGIN_PASSWORD");            
        
        if ((trustagentLoginUserName == null || trustagentLoginUserName.isEmpty()) && (trustagentLoginPassword == null || trustagentLoginPassword.isEmpty())) {
            log.info("Administrator has not specified the login username and password. Checking if TRUSTAGENT_LOGIN_REGISTER flag is set or not");
            
            if (System.getenv("TRUSTAGENT_LOGIN_REGISTER") != null && System.getenv("TRUSTAGENT_LOGIN_REGISTER").equalsIgnoreCase("true")) {                
                log.info("Generating random user name and password for trust agent access since administrator has not specified the username and password.");
                trustagentLoginUserName = RandomUtil.randomHexString(20);
                log.debug("Generated random user name {}", trustagentLoginUserName);         

                trustagentLoginPassword = RandomUtil.randomHexString(20);
                log.debug("Generated random password.");                 
            }            
        }
                
        if (trustagentLoginUserName != null && !trustagentLoginUserName.isEmpty() && trustagentLoginPassword != null && !trustagentLoginPassword.isEmpty()) {
            
            if( System.getenv("TRUSTAGENT_LOGIN_REGISTER") != null && System.getenv("TRUSTAGENT_LOGIN_REGISTER").equalsIgnoreCase("true")) {
            
                TlsPolicy tlsPolicy = TlsPolicyBuilder.factory().strictWithKeystore(trustagentConfiguration.getTrustagentKeystoreFile(), 
                        trustagentConfiguration.getTrustagentKeystorePassword()).build();
                TlsConnection tlsConnection = new TlsConnection(new URL(url), tlsPolicy);

                Properties clientConfiguration = new Properties();
                clientConfiguration.setProperty(TrustagentConfiguration.MTWILSON_API_USERNAME, username);
                clientConfiguration.setProperty(TrustagentConfiguration.MTWILSON_API_PASSWORD, password);

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
                hostClientObj.preRegisterHostDetails(hostNames, trustagentLoginUserName, trustagentLoginPassword);

                log.info("Successfully registered the host access information with attestation service.");
            }
                        
            // Store the user and its corresponding permissions
            Password pwd = new Password();
            pwd.execute(new String[] {trustagentLoginUserName, trustagentLoginPassword, "*:*"});
            
            // We need to store the user name here so that we can use for validation. Password will not be stored in the property file
            log.debug("Setting username {} in the configuration file.", trustagentLoginUserName);
            getConfiguration().set(TrustagentConfiguration.TRUSTAGENT_LOGIN_USERNAME, trustagentLoginUserName);
            
            log.info("Successfully updated the property file with the username and shiro files with username and password.");
        } else {
            log.error("PreRegisterHostAccessDetails: Invalid user name or password specified. Either specify both TRUSTAGENT_LOGIN_USERNAME & TRUSTAGENT_LOGIN_PASSWORD "
                    + "or don't specify them so that it would be autogenerated. If the login and password is not specified, ensure TRUSTAGENT_LOGIN_REGISTER flag is set"
                    + "to true.");
        }
    }    
}
