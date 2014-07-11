/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.shiro;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.v2.file.model.File;
import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import javax.ws.rs.core.MediaType;
import org.junit.Test;
import com.intel.mtwilson.shiro.jaxrs.PasswordLoginRequest;
import java.io.IOException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;

/**
 *
 * @author jbuhacoff
 */
public class ShiroClientAuthorizationTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ShiroClientAuthorizationTest.class);
    
    public static class SecurityTestResource extends MtWilsonClient {
        public SecurityTestResource() throws Exception {
            super(My.configuration().getClientProperties());
            log.debug("BASIC username {} password {}", My.configuration().getClientProperties().getProperty("mtwilson.api.username"), My.configuration().getClientProperties().getProperty("mtwilson.api.password"));
        }
        
        public String getVersion() {
            return getTarget().path("/version").request(MediaType.APPLICATION_XML).get(String.class);
        }
        public String getDefault() {
            return getTarget().path("/test/security/default").request(MediaType.TEXT_PLAIN).get(String.class);
        }
        public String getHelloGuest() {
            return getTarget().path("/test/security/guest").request(MediaType.TEXT_PLAIN).get(String.class);
        }
        public String getHelloAuthenticated() {
            return getTarget().path("/test/security/authenticated").request(MediaType.TEXT_PLAIN).get(String.class);
        }
        public String getHelloRolebased1() {
            return getTarget().path("/test/security/rolebased1").request(MediaType.TEXT_PLAIN).get(String.class);
        }
        public String getHelloRolebased2() {
            return getTarget().path("/test/security/rolebased2").request(MediaType.TEXT_PLAIN).get(String.class);
        }
        public String getHelloUser() {
            return getTarget().path("/test/security/user").request(MediaType.TEXT_PLAIN).get(String.class);
        }
        public String getHelloPermission() {
            return getTarget().path("/test/security/permission").request(MediaType.TEXT_PLAIN).get(String.class);
        }
        
        public String postLoginForm() throws IOException {
            PasswordLoginRequest form = new PasswordLoginRequest();
            form.setUsername(My.configuration().getClientProperties().getProperty("mtwilson.api.username"));
            form.setPassword(My.configuration().getClientProperties().getProperty("mtwilson.api.password"));
            Response response = getTarget().path("/login").request().post(Entity.entity(form, MediaType.APPLICATION_XML));
//            return response.getHeaderString("Authorization-Token");
            return null; // TODO:  above line used to work and is now broken because of chagne to Response calss
        }
    }
    
    @Test
    public void testBasicAuthorization() throws Exception {
        SecurityTestResource test = new SecurityTestResource();
//        log.debug("default: {}", test.getDefault());
//        log.debug("version: {}", test.getVersion());
//        log.debug("guest: {}", test.getHelloGuest());
//        log.debug("authenticated: {}", test.getHelloAuthenticated());
//        log.debug("rolebased1: {}", test.getHelloRolebased1());
//        log.debug("rolebased2: {}", test.getHelloRolebased2());
//        log.debug("user: {}", test.getHelloUser());
//        log.debug("permission: {}", test.getHelloPermission());
        log.debug("login token: {}", test.postLoginForm());
    }

    public static class TokenRequestFilter implements ClientRequestFilter {
        private String token;
        public TokenRequestFilter(String token) {
            this.token = token;
        }
        @Override
        public void filter(ClientRequestContext requestContext) throws IOException {
            requestContext.getHeaders().add("Authorization-Token", token);
        }
        
    }
    
    /**
     * First, run testBasicAuthorization  postLoginForm  to get a token like this:
     * AVZxbyEjWkG2gqT4UCZi5Ja7VSvikxhD2z8coYA2sN7PmpTpaLR9GOX1QA8wWGl6bVhopIl+mRUTSVXiqRTvB3EEFYGLTsOyCsLHLIBt66QYMmT1NDZA6UmzoAMVSm6WGqTbskJ3ppW7QEIiv07MmHlYaWxqDA4sGeU75sxUonQBd9F76dJhRtrguBL1Fz/nQF2Z6UPrkx85CyGittAB5UBJQBlW/hFv0Njgkr50GLkpUWw6sj02JfGf1GTBNXuw2ghwC/9sCROKosEqNCgD+f8VyKmtiYWCuq59jJX2h0beUJb88XEGEa/Jt3cyK8olrrMbfYoEjKGkP1LlELU43ncfUOS4B+GGMXE5fbRZq39ufeafyAdw/8ijVlI2P5yfsLqWREq0XBBC5QumgOfxeslfC1Glclpz/hnhNF//e2htcoue5YNTCu7F0EyfSIXwQbeM8Qk0mMILw2ENlcpCDNUOfRBlS9hQpHgAD2h/h7Ujon0EgVeIJbcwBgVhRQnsRcuCjpwDb+tRefir1Y1vLBZ5c6pF1a7pfaZdf+ARaTX/Ar/3Lv3N0tacqUGkst8rsTcejVp9FTlvwep2BttvIdjkYd1fW95JPrXPgmGUxZig0lOJB39hDK6p2Fmq0+H71NV9G6mfZOQ5NAloWlWHmB50K4dkbrQnwgDf9Y+ypQrX6v6T5/+7pYcG22UJDUKtIhQj0tjj
     * Then set it here and run the test
     * @throws Exception 
     */
    @Test
    public void testTokenAuthorization() throws Exception {
        String token = "AVZxbyEjWkG2gqT4UCZi5Ja7VSvikxhD2z8coYA2sN7PmpTpaLR9GOX1QA8wWGl6bVhopIl+mRUTSVXiqRTvB3EEFYGLTsOyCsLHLIBt66QYMmT1NDZA6UmzoAMVSm6WGqTbskJ3ppW7QEIiv07MmHlYaWxqDA4sGeU75sxUonQBd9F76dJhRtrguBL1Fz/nQF2Z6UPrkx85CyGittAB5UBJQBlW/hFv0Njgkr50GLkpUWw6sj02JfGf1GTBNXuw2ghwC/9sCROKosEqNCgD+f8VyKmtiYWCuq59jJX2h0beUJb88XEGEa/Jt3cyK8olrrMbfYoEjKGkP1LlELU43ncfUOS4B+GGMXE5fbRZq39ufeafyAdw/8ijVlI2P5yfsLqWREq0XBBC5QumgOfxeslfC1Glclpz/hnhNF//e2htcoue5YNTCu7F0EyfSIXwQbeM8Qk0mMILw2ENlcpCDNUOfRBlS9hQpHgAD2h/h7Ujon0EgVeIJbcwBgVhRQnsRcuCjpwDb+tRefir1Y1vLBZ5c6pF1a7pfaZdf+ARaTX/Ar/3Lv3N0tacqUGkst8rsTcejVp9FTlvwep2BttvIdjkYd1fW95JPrXPgmGUxZig0lOJB39hDK6p2Fmq0+H71NV9G6mfZOQ5NAloWlWHmB50K4dkbrQnwgDf9Y+ypQrX6v6T5/+7pYcG22UJDUKtIhQj0tjj";
        String baseurl = My.configuration().getClientProperties().getProperty("mtwilson.api.url"); // for example "http://localhost:8080/v2"
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.register(com.intel.mtwilson.jaxrs2.provider.JacksonObjectMapperProvider.class);
        clientConfig.register(com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider.class);
        Client client = ClientBuilder.newClient(clientConfig);
        client.register(new TokenRequestFilter(token));
        WebTarget target = client.target(baseurl);
        
        String version = target.path("/version").request(MediaType.APPLICATION_JSON).get(String.class);
        log.debug("version: {}", version);
        File file = new File();
        file.setId(new UUID());
        file.setContent("hello");
        file.setName("hello");
        file.setContentType("plaintext"); // need a slash 
//        File refreshFile = target.path("/version").request(MediaType.APPLICATION_JSON).post(Entity.entity(file, MediaType.APPLICATION_JSON)).readEntity(File.class);
        
//        ObjectMapper mapper = new ObjectMapper();
//        log.debug("refresh file: {}", mapper.writeValueAsString(refreshFile));
    }
}
