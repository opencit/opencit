/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.shiro;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.data.MwApiClientHttpBasic;
import com.intel.mtwilson.ms.data.MwPortalUser;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.util.Factory;
//import org.apache.shiro.ShiroException;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.session.Session;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.realm.RealmFactory;
import org.junit.Test;

/**
 * NOTE:  this is just a test class;  in mtwilson 2.0 domains must be plural like "hosts" and "user_passwords"
 * 
 * References:
 * 
 * http://shiro.apache.org/architecture.html
 * 
 * Sample output:
 * 
Running test.shiro.ShiroCmdLineTest
2013-12-28 17:05:25,623 DEBUG [main] t.s.ShiroCmdLineTest [ShiroCmdLineTest.java:27] initializing shiro
2013-12-28 17:05:25,639 DEBUG [main] o.a.s.i.ResourceUtils [ResourceUtils.java:159] Opening resource from class path [shiro.ini]
2013-12-28 17:05:25,649 DEBUG [main] o.a.s.c.Ini [Ini.java:342] Parsing [users]
2013-12-28 17:05:25,651 DEBUG [main] o.a.s.c.Ini [Ini.java:342] Parsing [roles]
2013-12-28 17:05:25,653 DEBUG [main] o.a.s.c.IniFactorySupport [IniFactorySupport.java:122] Creating instance from Ini [sections=users,roles]
2013-12-28 17:05:25,691 DEBUG [main] o.a.s.r.t.IniRealm [IniRealm.java:179] Discovered the [roles] section.  Processing...
2013-12-28 17:05:25,695 DEBUG [main] o.a.s.r.t.IniRealm [IniRealm.java:185] Discovered the [users] section.  Processing...
2013-12-28 17:05:25,707 DEBUG [main] t.s.ShiroCmdLineTest [ShiroCmdLineTest.java:39] authenticating...
2013-12-28 17:05:25,708 DEBUG [main] o.a.s.r.AuthenticatingRealm [AuthenticatingRealm.java:569] Looked up AuthenticationInfo [guest] from doGetAuthenticationInfo
2013-12-28 17:05:25,708 DEBUG [main] o.a.s.r.AuthenticatingRealm [AuthenticatingRealm.java:507] AuthenticationInfo caching is disabled for info [guest].  Submitted token: [org.apache.shiro.authc.UsernamePasswordToken - guest, rememberMe=true].
2013-12-28 17:05:25,709 DEBUG [main] o.a.s.a.c.SimpleCredentialsMatcher [SimpleCredentialsMatcher.java:95] Performing credentials equality check for tokenCredentials of type [[C and accountCredentials of type [java.lang.String]
2013-12-28 17:05:25,709 DEBUG [main] o.a.s.a.c.SimpleCredentialsMatcher [SimpleCredentialsMatcher.java:101] Both credentials arguments can be easily converted to byte arrays.  Performing array equals comparison
2013-12-28 17:05:25,709 DEBUG [main] o.a.s.a.AbstractAuthenticator [AbstractAuthenticator.java:231] Authentication successful for token [org.apache.shiro.authc.UsernamePasswordToken - guest, rememberMe=true].  Returned account [guest]
2013-12-28 17:05:25,709 DEBUG [main] o.a.s.s.s.DefaultSubjectContext [DefaultSubjectContext.java:102] No SecurityManager available in subject context map.  Falling back to SecurityUtils.getSecurityManager() lookup.
2013-12-28 17:05:25,709 DEBUG [main] o.a.s.s.s.DefaultSubjectContext [DefaultSubjectContext.java:102] No SecurityManager available in subject context map.  Falling back to SecurityUtils.getSecurityManager() lookup.
2013-12-28 17:05:25,710 DEBUG [main] o.a.s.s.m.AbstractValidatingSessionManager [AbstractValidatingSessionManager.java:213] No sessionValidationScheduler set.  Attempting to create default instance.
2013-12-28 17:05:25,711 INFO [main] o.a.s.s.m.AbstractValidatingSessionManager [AbstractValidatingSessionManager.java:230] Enabling session validation scheduler...
2013-12-28 17:05:25,717 DEBUG [main] o.a.s.s.m.DefaultSessionManager [DefaultSessionManager.java:175] Creating new EIS record for new session instance [org.apache.shiro.session.mgt.SimpleSession,id=null]
2013-12-28 17:05:25,778 INFO [main] t.s.ShiroCmdLineTest [ShiroCmdLineTest.java:47] logged in as guest
2013-12-28 17:05:25,778 INFO [main] t.s.ShiroCmdLineTest [ShiroCmdLineTest.java:53] security notice
2013-12-28 17:05:25,779 DEBUG [main] t.s.ShiroCmdLineTest [ShiroCmdLineTest.java:79] user cannot delete host #5
2013-12-28 17:05:25,779 DEBUG [main] o.a.s.m.DefaultSecurityManager [DefaultSecurityManager.java:550] Logging out subject with primary principal guest
2013-12-28 17:05:25,780 DEBUG [main] o.a.s.s.m.AbstractSessionManager [AbstractNativeSessionManager.java:244] Stopping session with id [00e9c153-d2ef-4d3c-b400-0c3aef205ba1]
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.645 sec
 * 
 * @author jbuhacoff
 */
public class ShiroCmdLineTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ShiroCmdLineTest.class);

    @Test
    public void testCmdLine() throws Exception {
        // pretend to be a cmd line tool
        log.debug("initializing shiro");
        
        // initialize shiro ... should be in mtwilson-launcher  (to intialize for stand-alone app, or for an app hosted on a java web server)
        Factory<SecurityManager> factory = new IniSecurityManagerFactory("classpath:shiro.ini");
        SecurityManager securityManager = factory.getInstance();
        SecurityUtils.setSecurityManager(securityManager); // sets a single shiro security manager to be used for entire jvm... fine for a stand-alone app but when running inside a web app container or in a multi-user env. it needs to be maintained by some container and set on every thread that will do work ... 

        // get current user
        Subject currentUser = SecurityUtils.getSubject();        
        
        // check if user is authenticated (username/password env vars or http Authorization header)
        if( !currentUser.isAuthenticated() ) {
            log.debug("authenticating...");
            // for this junit test we're using mtwilson.api.username and mtwilson.api.password properties from  mtwilson.properties on the local system, c:/mtwilson/configuration/mtwilson.properties is default location on windows 
            UsernamePasswordToken token = new UsernamePasswordToken(getBasicUsername(), getBasicPassword()); // guest doesn't need a password
//            UsernamePasswordToken token = new UsernamePasswordToken("root", "root"); // guest doesn't need a password
            token.setRememberMe(true); // in web environment this might relate to cookies, but what about command line or others?
            currentUser.login(token); // throws UnknownAccountException , IncorrectCredentialsException , LockedAccountException , other specific exceptions, and AuthenticationException 
        }
        
        // show who is the user
        log.info("logged in as {}", currentUser.getPrincipal());
        
        // you can make information available to the user based on their security access, or check if they have already changed their password or seen a notice or whatever during the current session 
        Session session = currentUser.getSession();        
        Object loginSecurityNotice = session.getAttribute("loginSecurityNotice");
        if( loginSecurityNotice == null || !(Boolean)loginSecurityNotice ) {
            log.info("security notice");
            session.setAttribute("loginSecurityNotice", Boolean.TRUE);
        }
        
        // check if the user has a specific role
        if ( currentUser.hasRole( "root" ) ) {
            log.warn("user is root" );
        }      
        
        // this is how permissions should be checked throughout the application:
        // host.store() should check isPermitted("host:write") or isPermitted("host:create") or isPermitted("host:replace")
        // host.retrieve() should check isPermitted("host:read")
        // host.delete() should check isPermitted("host:delete")
        // etc.
        if( currentUser.isPermitted( "host_attestation:read" ) ) {
            log.debug("user can get attestation reports");
        }
        if( currentUser.isPermitted( "host_trustpolicy:read" ) ) {
            log.debug("user can see trust policies");
        }
        
        // there is also support for row-level authentication:  a user might be able to register a host but then only edit hosts s/he registered, not ALL hosts:
        if( currentUser.isPermitted("host:delete:5")) { // need a shortcut for identifying records associated with current user, owned by current user, creatd by current user, read/write by current user's gruop, etc. .... not just instance ids. also need a way to filter by something other than an id, such as user can edit only citrix hosts , not vmware or kvm hosts...
            log.debug("user can delete host #5");
        }
        else {
            log.debug("user cannot delete host #5");
        }
        
        
        if( currentUser.isPermitted("user_password:read")) { // need a shortcut for identifying records associated with current user, owned by current user, creatd by current user, read/write by current user's gruop, etc. .... not just instance ids. also need a way to filter by something other than an id, such as user can edit only citrix hosts , not vmware or kvm hosts...
            log.debug("user can read passwords");
            UserPasswords service = new UserPasswords();
            UserPassword userPassword = service.retrieve(new UUID().toString());
            log.debug("got user password {} : {}", userPassword.getName(), userPassword.getPassword());
        }
        else {
            log.debug("user cannot read passwords");
        }
        
        
        
        // logout the user
        currentUser.logout(); // removes all identifying information and invalidates session
        
        
    }
    
    private String getBasicUsername() throws Exception {
        return My.configuration().getConfiguration().getString("mtwilson.api.username", System.getProperty("user.name", "guest"));
    }
    private String getBasicPassword() throws Exception {
        return My.configuration().getConfiguration().getString("mtwilson.api.password", "");
    }
    
    /**
     * See createUser tests in test.jdbi.RepositoryTest  in mtwilson-shiro-jdbi
     * 
     * @throws Exception
     * @deprecated
     */
    @Deprecated
    @Test
    public void createUser() throws Exception {
        /*
        MwApiClientHttpBasic userPassword = new MwApiClientHttpBasic();
        userPassword.setUserName(getBasicUsername());
        userPassword.setPassword(getBasicPassword());
        log.debug("Creating login username {} password {}", userPassword.getUserName(), userPassword.getPassword());
//        My.jpa().mwApiClientHttpBasic().create(userPassword);
        MwPortalUser portalUser = new MwPortalUser();
        portalUser.setEnabled(true);
        portalUser.setStatus("APPROVED");
        portalUser.setUsername(getBasicUsername());
        portalUser.setUuid_hex(new UUID().toString());
        // keystore was required for mtwilson 1.2 portal user, but in 2.0 it's moving to a separate table ... until that happens we have to provide SOME value here so we use an empty byte array:
        portalUser.setKeystore(new byte[0]);
        log.debug("Creating profile username {} uuid {}", portalUser.getUsername(), portalUser.getUuid_hex()); // for example  jonathan cc1de7dd-3f1b-41e6-92ab-ed62250979db
//        My.jpa().mwPortalUser().create(portalUser);
*/
        
    }
}
