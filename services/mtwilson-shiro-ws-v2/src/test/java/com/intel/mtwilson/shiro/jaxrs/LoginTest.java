/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.jaxrs;

import com.intel.mtwilson.shiro.EncryptedTokenContent;
import com.intel.mtwilson.My;
import com.intel.mtwilson.shiro.authc.password.LoginPasswordId;
import com.intel.mtwilson.shiro.UserId;
import com.intel.mtwilson.shiro.Username;
import com.thoughtworks.xstream.XStream;
import java.util.Collection;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Factory;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class LoginTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoginTest.class);
    
    @BeforeClass
    public static void initShiro() throws Exception {
        // initialize shiro ... should be in mtwilson-launcher  (to intialize for stand-alone app, or for an app hosted on a java web server)
        Factory<SecurityManager> factory = new IniSecurityManagerFactory("classpath:shiro.ini");
        SecurityManager securityManager = factory.getInstance();
        SecurityUtils.setSecurityManager(securityManager); // sets a single shiro security manager to be used for entire jvm... fine for a stand-alone app but when running inside a web app container or in a multi-user env. it needs to be maintained by some container and set on every thread that will do work ... 
        
    }
    
    
    @Test
    public void testLogin() throws Exception {
        // authenticate the user with JdbcPasswordRealm and PasswordCredentialsMatcher (configured in shiro.ini)
        Subject currentUser = SecurityUtils.getSubject();        
//        if( !currentUser.isAuthenticated() ) { // shouldn't need this because we have @RequiresGuest annotation...
            log.debug("authenticating...");
            // for this junit test we're using mtwilson.api.username and mtwilson.api.password properties from  mtwilson.properties on the local system, c:/mtwilson/configuration/mtwilson.properties is default location on windows 
        UsernamePasswordToken loginToken = new UsernamePasswordToken(getBasicUsername(), getBasicPassword());
//            UsernamePasswordToken token = new UsernamePasswordToken("root", "root"); // guest doesn't need a password
            loginToken.setRememberMe(false); // we could pass in a parameter with the form but we don't need this
            currentUser.login(loginToken); // throws UnknownAccountException , IncorrectCredentialsException , LockedAccountException , other specific exceptions, and AuthenticationException 
            
        log.info("logged in as {}", currentUser.getPrincipal());
        PrincipalCollection principals = currentUser.getPrincipals();

        Collection<Username> usernames = principals.byType(Username.class);
            Collection<UserId> userIds = principals.byType(UserId.class);
            Collection<LoginPasswordId> passwordLoginIds = principals.byType(LoginPasswordId.class);
        
        XStream xs = new XStream();
        String principalsXml = xs.toXML(principals);
        log.debug("principalsXml: {}", principalsXml);
        
        
        EncryptedTokenContent tokenContent = new EncryptedTokenContent();
        tokenContent.loginPasswordId = passwordLoginIds.iterator().next().getLoginPasswordId().toString();
        tokenContent.userId = userIds.iterator().next().getUserId().toString();
        tokenContent.username = usernames.iterator().next().getUsername().toString();
        String tokenContentXml = xs.toXML(tokenContent);
        log.debug("tokenContentXml: {}", tokenContentXml);

        
        /**
         * Example principalsXml:
         * 
<org.apache.shiro.subject.SimplePrincipalCollection serialization="custom">
  <org.apache.shiro.subject.SimplePrincipalCollection>
    <default>
      <realmPrincipals class="linked-hash-map">
        <entry>
          <string>jdbcPasswordRealm</string>
          <linked-hash-set>
            <com.intel.mtwilson.shiro.UserId>
              <userId>
                <bytes>hP8S9GpoSVynDRdMsH5Fzg==</bytes>
              </userId>
            </com.intel.mtwilson.shiro.UserId>
            <com.intel.mtwilson.shiro.Username>
              <username>jonathan</username>
            </com.intel.mtwilson.shiro.Username>
            <com.intel.mtwilson.shiro.LoginPasswordId>
              <userId reference="../../com.intel.mtwilson.shiro.UserId/userId"/>
              <loginPasswordId>
                <bytes>Ob7g+JKETVWavs83LiAOeQ==</bytes>
              </loginPasswordId>
            </com.intel.mtwilson.shiro.LoginPasswordId>
          </linked-hash-set>
        </entry>
      </realmPrincipals>
    </default>
    <boolean>true</boolean>
    <linked-hash-map reference="../default/realmPrincipals"/>
  </org.apache.shiro.subject.SimplePrincipalCollection>
</org.apache.shiro.subject.SimplePrincipalCollection>
         * 
         * 
         * Example tokenContentXml:
         * 
<com.intel.mtwilson.shiro.jaxrs.AuthorizationTokenContent>
  <userId>84ff12f4-6a68-495c-a70d-174cb07e45ce</userId>
  <username>jonathan</username>
  <loginPasswordId>39bee0f8-9284-4d55-9abe-cf372e200e79</loginPasswordId>
</com.intel.mtwilson.shiro.jaxrs.AuthorizationTokenContent>
         * 
         * 
         */
        
    }

    private String getBasicUsername() throws Exception {
        return My.configuration().getConfiguration().getString("mtwilson.api.username", System.getProperty("user.name", "guest"));
    }
    private String getBasicPassword() throws Exception {
        return My.configuration().getConfiguration().getString("mtwilson.api.password", "");
    }

}
