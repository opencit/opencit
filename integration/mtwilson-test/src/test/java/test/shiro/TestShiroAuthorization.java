/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.shiro;

import com.intel.mtwilson.My;
import com.intel.mtwilson.client.jaxrs.MtWilsonClient;
import javax.ws.rs.core.MediaType;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class TestShiroAuthorization {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TestShiroAuthorization.class);
    
    public static class SecurityTestResource extends MtWilsonClient {
        public SecurityTestResource() throws Exception {
            super(My.configuration().getClientProperties());
        }
        
        public String getHelloGuest() {
            return getTarget().path("/test/security/guest").request(MediaType.TEXT_PLAIN).get(String.class);
        }
        public String getHelloAuthenticated() {
            return getTarget().path("/test/security/authenticated").request(MediaType.TEXT_PLAIN).get(String.class);
        }
        public String getHelloRolebased() {
            return getTarget().path("/test/security/rolebased").request(MediaType.TEXT_PLAIN).get(String.class);
        }
        public String getHelloUser() {
            return getTarget().path("/test/security/user").request(MediaType.TEXT_PLAIN).get(String.class);
        }
        public String getHelloPermission() {
            return getTarget().path("/test/security/permission").request(MediaType.TEXT_PLAIN).get(String.class);
        }
    }
    
    @Test
    public void testBasicAuthorization() throws Exception {
        SecurityTestResource test = new SecurityTestResource();
//        log.debug("guest: {}", test.getHelloGuest());
        log.debug("authenticated: {}", test.getHelloAuthenticated());
//        log.debug("rolebased: {}", test.getHelloRolebased());
//        log.debug("user: {}", test.getHelloUser());
//        log.debug("permission: {}", test.getHelloPermission());
    }
}
