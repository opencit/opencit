/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.util.shiro;

import java.io.File;
import java.security.SecureRandom;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.config.Ini;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.util.Factory;
import org.apache.shiro.util.StringUtils;

/**
 * How to use this class from a junit test that does not involve security
 * (you are testing functionality which happens to be annotated with requires
 * permissions but you don't want to be bothered with connecting to an
 * existing database or setting up any local configuration files):
 * 
 * <pre>
 * @BeforeClass
 * public static init() {
 *   com.intel.dcsg.cpg.util.shiro.Login.superuser();
 * }
 * </pre>
 * 
 * Or if you want to test with a user that has specific permissions:
 * 
 * <pre>
 * @BeforeClass
 * public static init() {
 *   com.intel.dcsg.cpg.util.shiro.Login.user("domain1:action1","domain2:action2"); // varargs, add as many as you need
 * }
 * </pre>
 * 
 * 
 * How to use this class from a junit integration test using existing logins
 * in a database:
 * 
 * <pre>
 * @BeforeClass
 * public static init() throws Exception {
 *   com.intel.dcsg.cpg.util.shiro.Login.existingUser(new File("/path/to/shiro-junit.ini", "username", "password");
 * }
 * </pre>
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
public class Login {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Login.class);
    private static final SecureRandom random = new SecureRandom();
    
    private static void init(String username, String password, String role, String... permissions) {
        Ini ini = new Ini();
        ini.setSectionProperty("users", username, String.format("%s,%s", password, role));
        ini.setSectionProperty("roles", role, StringUtils.toDelimitedString(permissions, ","));
        Factory<org.apache.shiro.mgt.SecurityManager> factory = new IniSecurityManagerFactory(ini);
        org.apache.shiro.mgt.SecurityManager securityManager = factory.getInstance();
        SecurityUtils.setSecurityManager(securityManager);
    }
    
    /**
     * 
     * @param shiroIni for example C:\mtwilson\configuration\shiro-junit.ini
     */
    private static void init(File shiroIni) {
        String filename = String.format("file:///%s",shiroIni.getAbsolutePath().replace(File.separator,"/"));
        Factory<org.apache.shiro.mgt.SecurityManager> factory = new IniSecurityManagerFactory(filename);
        org.apache.shiro.mgt.SecurityManager securityManager = factory.getInstance();
        SecurityUtils.setSecurityManager(securityManager);        
    }
    
    public static void superuser() {
        // define the known users with passwords, roles, and permissions
        String username = System.getProperty("user.name", "anonymous");
        String password = String.valueOf(random.nextDouble());
        init(username, password, "superuser", "*");
        // login
        UsernamePasswordToken loginToken = new UsernamePasswordToken(username, password);
        SecurityUtils.getSubject().login(loginToken);
        log.info("Logged in {} as superuser", username);
    }

    public static void user(String... permissions) {
        String username = System.getProperty("user.name", "anonymous");
        String password = String.valueOf(random.nextDouble());
        init(username, password, "user", permissions);
        // login
        UsernamePasswordToken loginToken = new UsernamePasswordToken(username, password);
        SecurityUtils.getSubject().login(loginToken);
        log.info("Logged in {} with permissions {}", username, StringUtils.toDelimitedString(permissions, ","));
    }
    
    public static void existingUser(File ini, String username, String password) {
        init(ini);
        // login
        UsernamePasswordToken loginToken = new UsernamePasswordToken(username, password);
        SecurityUtils.getSubject().login(loginToken);
        log.info("Logged in {} with password", username);
    }
}
