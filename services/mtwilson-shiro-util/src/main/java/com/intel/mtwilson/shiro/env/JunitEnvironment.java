/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.env;

import com.intel.mtwilson.My;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.config.Ini;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Factory;

/**
 * How to use this class from a junit test that does not involve security
 * (you are testing functionality which happens to be annotated with requires
 * permissions but you don't want to be bothered with connecting to an
 * existing database or setting up any local configuration files):
 * 
 * <pre>
 * @BeforeClass
 * public static login() throws Exception {
 *   com.intel.mtwilson.shiro.env.JunitEnvironment.superuser();
 * }
 * </pre>
 * 
 * How to use this class from a junit integration test using existing logins
 * in a database:
 * 
 * <pre>
 * @BeforeClass
 * public static login() throws Exception {
 *   com.intel.mtwilson.shiro.env.JunitEnvironment.login();
 * }
 * </pre>
 * 
 * The above code will load "shiro.init" and "test.properties" from your local 
 * mtwilson
 * configuration folder (for example C:\mtwilson\configuration\test.properties),
 * initialize the SecurityManager,
 * and use the properties "login.username" and "login.password" to login.
 * 
 * See also the AbstractIntegrationTest class in mtwilson-test. 
 * 
 * If you need to login again during the test as another user, you can 
 * use the standard Shiro method:
 * 
 * <pre>
 * SecurityUtils.getSubject().login(new UsernamePasswordToken(username,password));
 * </pre>
 * 
 * You can use any AuthenticationToken with the standard Shiro method.
 * 
 * @author jbuhacoff
 */
public class JunitEnvironment {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JunitEnvironment.class);
    
    public static void superuser() {
        // define the known users with passwords, roles, and permissions
        String username = System.getProperty("user.name", "anonymous");
        String password = String.valueOf(Math.random());
        String role = "developer";
        Ini ini = new Ini();
        ini.setSectionProperty("users", username, String.format("%s,%s", password, role));
        ini.setSectionProperty("roles", role, "*");
        Factory<org.apache.shiro.mgt.SecurityManager> factory = new IniSecurityManagerFactory(ini);
        org.apache.shiro.mgt.SecurityManager securityManager = factory.getInstance();
        SecurityUtils.setSecurityManager(securityManager);
        // login
        UsernamePasswordToken loginToken = new UsernamePasswordToken(username, password);
        SecurityUtils.getSubject().login(loginToken);            
    }
    
    public static void init() {      
        String filename = "file:///"+(My.filesystem().getConfigurationPath()+File.separator+"shiro-junit.ini").replace(File.separator,"/");
        Factory<org.apache.shiro.mgt.SecurityManager> factory = new IniSecurityManagerFactory(filename);
        org.apache.shiro.mgt.SecurityManager securityManager = factory.getInstance();
        SecurityUtils.setSecurityManager(securityManager);
    }
    
    public static void login() throws Exception {
        init();
        String filename = My.filesystem().getConfigurationPath()+File.separator+"test.properties";
        File file = new File(filename);
        try(FileInputStream in = new FileInputStream(file)) {
            Properties properties = new Properties();
            properties.load(in);
            String username = properties.getProperty("login.username");
            String password = properties.getProperty("login.password");
            Subject currentUser = SecurityUtils.getSubject();        
            UsernamePasswordToken loginToken = new UsernamePasswordToken(username, password);
            currentUser.login(loginToken);            
        }
    }
}
