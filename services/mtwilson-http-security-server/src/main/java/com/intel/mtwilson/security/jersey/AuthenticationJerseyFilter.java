/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.security.jersey;

import com.intel.mtwilson.model.Md5Digest;
import com.intel.mtwilson.datatypes.Role;
import com.intel.mtwilson.security.core.AuthorizationScheme;
import com.intel.mtwilson.security.core.IPAddressUtil;
import com.intel.mtwilson.security.core.RequestInfo;
import com.intel.mtwilson.security.core.RequestLog;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerRequestContext;
//import com.sun.jersey.spi.container.ContainerRequest;
//import com.sun.jersey.spi.container.ContainerRequestFilter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a SERVER SIDE FILTER to handle INCOMING REQUESTS.
 * This class requires the following libraries:
 * com.sun.jersey.spi.container.ContainerRequest and ContainerRequestFilter from the Jersey Server API's
 * @since 0.5.1
 * @author jbuhacoff
 * @deprecated use apache shiro
 */
public class AuthenticationJerseyFilter implements ContainerRequestFilter {
    private HmacRequestVerifier hmacAuthorization; // currently this is set via Spring Framework; example is new VerifyAuthorization(new LoginBO()); and new VerifyAuthorization(new HashMapSecretKeyFinder());
    private PublicKeyRequestVerifier publickeyAuthorization; // currently this is set via Spring Framework; example is new VerifyAuthorization(new LoginBO()); and new VerifyAuthorization(new HashMapSecretKeyFinder());
    private X509RequestVerifier x509Authorization;
    private HttpBasicRequestVerifier httpBasicAuthorization;
    private RequestLog requestLog;

    private static Logger log = LoggerFactory.getLogger(AuthenticationJerseyFilter.class);
    private String[] trustWhitelist = null;
    private boolean sslRequired = true;
//    private boolean antiReplayEnabled = false; // bug #380. permanently false;  if customer needs anti-replay protection they should use TLS
    
    public AuthenticationJerseyFilter() {
    }
    
    public void setRequestLog(RequestLog finder) { this.requestLog = finder; }
    public void setRequestValidator(HmacRequestVerifier validator) { this.hmacAuthorization = validator; }
    public void setRequestValidator(PublicKeyRequestVerifier validator) { this.publickeyAuthorization = validator; }
    public void setRequestValidator(X509RequestVerifier validator) { this.x509Authorization = validator; }
    public void setRequestValidator(HttpBasicRequestVerifier validator) {this.httpBasicAuthorization = validator;}
    public void setTrustedRemoteAddress(String[] ipAddressOrSubnet) { trustWhitelist = ipAddressOrSubnet; } // should be set by mtwilson.api.trust=ipaddresslist
    public void setSslRequired(boolean required) { sslRequired = required; } // should be set by mtwilson.ssl.required=true/false
//    public void setAntiReplayEnabled(boolean enabled) { antiReplayEnabled = enabled; } // bug #380. should be set by configuration...   
    
    @javax.ws.rs.core.Context HttpServletRequest servletRequest;
    
    /**
     * This filter authenticates the request. If the request is from an authenticated
     * user, it will be returned with the proper security context. If the request is
     * from a trusted IP Address, it will be returned with a security context showing
     * the IP Address as username and with all roles enabled. If the user is not found,
     * it will return the request without any security context. If there is any error
     * with the request itself such as improperly formed authorization header or any
     * problem with the cryptography, it will throw a WebApplicationException with UNAUTHORIZED. If the
     * request is insecure (non-SSL) and the server is configured to require secure
     * connections, it will throw a WebApplicationException with FORBIDDEN.
     * @param request
     * @return request object if processing should continue
     */
    @Override
    public void filter(ContainerRequestContext request) {
        log.debug("AuthenticationJerseyFilter request for {} {} with Authorization={}", request.getMethod(), request.getUriInfo().getPath(), request.getHeaderString("Authorization"));
        log.debug("AuthenticationJerseyFilter: HTTP method="+request.getMethod());
        log.debug("AuthenticationJerseyFilter: Request URI="+request.getUriInfo().getRequestUri());
        log.debug("AuthenticationJerseyFilter: Secure/https="+request.getSecurityContext().isSecure());
        
        if( servletRequest == null ) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Servlet request cannot be null").build());
        }
        log.debug("AuthenticationJerseyFilter: Remote Address=" + servletRequest.getRemoteAddr());

        if( sslRequired &&  !request.getSecurityContext().isSecure() ) {
            log.error("AuthenticationJerseyFilter: rejecting insecure (http) request");
            throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN).entity("Secure connection required").build());            
        }
        
        String requestBody = readEntityBodyQuietly(request);
        
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.instance = servletRequest.getLocalAddr();
        requestInfo.received = new Date();
        requestInfo.content = createRequestString(request, requestBody);
        
        // the administrator may specify a list of IP addresses to trust without requiring an Authorization header. this requirement was added in 0.5.1-sp1 (0.5.2).        
        log.debug("Trusted remote addresses: {}", StringUtils.join(trustWhitelist, " and "));
        log.debug("Client remote address: {}", servletRequest.getRemoteAddr());
        if( trustWhitelist != null && trustWhitelist.length > 0 && servletRequest != null && servletRequest.getRemoteAddr() != null ) {
            String trustedAddress = IPAddressUtil.matchAddressInList(servletRequest.getRemoteAddr(), trustWhitelist);
            if( trustedAddress != null ) {
                try {
                    log.debug("Request from trusted remote addr "+servletRequest.getRemoteAddr()+" matches "+trustedAddress+" in mtwilson.api.trust");
                    User user = new User(servletRequest.getRemoteAddr(), new Role[] { Role.Attestation, Role.Audit, Role.Report, Role.Security, Role.Whitelist }, servletRequest.getRemoteAddr(), Md5Digest.valueOf((request.getMethod()+" "+request.getUriInfo().getPath()+" "+String.valueOf(request.getHeaderString("Date"))).getBytes("UTF-8")));
                    request.setSecurityContext(new MtWilsonSecurityContext(user, request.getSecurityContext().isSecure()));
                    requestInfo.source = servletRequest.getRemoteAddr();
                    requestInfo.md5Hash = user.getMd5Hash().toByteArray(); 
                    // MtWilsonThreadLocal.set(new MtWilsonSecurityContext(user, request.isSecure()));
                    //requestLog.logRequestInfo(requestInfo); // bug #380.  NOTE: for trusted ip clients, we DO NOT check for replay attacks. 
//                    return request;
                }
                catch(Exception e) {
                    throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Cannot log request: "+e.getMessage()).build());                
                }
            }
        }
        
        if( request.getHeaderString("Authorization") != null ) {
            try {
                // support both the symmetric-key HMAC-SHA256 method "MtWilson" and the RSA method "PublicKey"
                User user = null;
                AuthorizationScheme scheme = getAuthorizationScheme(request.getHeaderString("Authorization"));
                log.debug("Authorization scheme is {}", scheme.name());
                if( x509Authorization != null && scheme.equals(AuthorizationScheme.X509) ) {
                    user = x509Authorization.getUserForRequest(request.getMethod(), request.getUriInfo().getRequestUri().toString(), request.getHeaders(), requestBody);
                }
                else if( publickeyAuthorization != null && scheme.equals(AuthorizationScheme.PublicKey) ) {
                    user = publickeyAuthorization.getUserForRequest(request.getMethod(), request.getUriInfo().getRequestUri().toString(), request.getHeaders(), requestBody);
                }
                else if( hmacAuthorization != null && scheme.equals(AuthorizationScheme.MtWilson) ) {
                    user = hmacAuthorization.getUserForRequest(request.getMethod(), request.getUriInfo().getRequestUri().toString(), request.getHeaders(), requestBody);                
                }
                else if(httpBasicAuthorization != null && scheme.equals(AuthorizationScheme.Basic)) {
                    user = httpBasicAuthorization.getUserForRequest(request.getMethod(), request.getUriInfo().getRequestUri().toString(), request.getHeaders(), requestBody);
                }
                
                if( user != null ) {
                    requestInfo.source = user.getName();
                    requestInfo.md5Hash = user.getMd5Hash().toByteArray();
                    
                    // at this point we have a regular authenticated request with a username (not a trusted-ip request)
                    // we have an opportunity to check for a replay attack, but:
                    // 1. if the request is over https, it is already protected against replay by the TLS connection itself.  
                    // 2. if there is a TLS connection but it is only from a proxy to our server, and the client actually has SSL turned off, then we won't know that and the client will not get replay protection.
                    // 3. if the client's TLS connection extends only to a proxy, and between the proxy and our server there is no SSL, then we won't know the request is protected and we will be doing redundant work by checking.
                    // for these 3 reasons, the only sane thing to do is allow the system administrator to turn replay protection on or off in the configuration file.
                    // either way, replay protection only applies to a request using hmac, publickey, or x509 authentication. we don't bother with http basic since there is no way to protect against replay at this level -- it's either protected by TLS or it's not protected at all.
                    // finally -- anti replay protection is disabled right now (value is always false) ... if customer needs anti-replay protection they should just use TLS
                    /*
                    if( antiReplayEnabled && scheme.equals(AuthorizationScheme.Basic) ) { // bug #380. always false
                        List<RequestInfo> recentRequests = requestLog.findRequestFromSourceWithMd5HashAfter(requestInfo.source, requestInfo.md5Hash, requestInfo.received);
                        if( !recentRequests.isEmpty() ) {
                            log.warn("Request has {} duplicates", recentRequests.size());
                            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).entity("Duplicate request").build());
                        }
                    }
                    */
                    
                    //requestLog.logRequestInfo(requestInfo); // bug #380.
                    log.debug("User {} with roles {} is authenticated. Security context is being set.", user.getLoginName(), user.getRoles());
                    log.info("AuthenticationJerseyFilter: Got user, setting security context");
                    request.setSecurityContext(new MtWilsonSecurityContext(user, request.getSecurityContext().isSecure()));
                    log.info("AuthenticationJerseyFilter: Set security context");
                    // MtWilsonThreadLocal.set(new MtWilsonSecurityContext(user, request.isSecure()));
//                    return request;
                }
            }
            catch(Exception e) {
                throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build());                
            }
        }
        log.info("AuthenticationJerseyFilter: request is NOT AUTHENTICATED (continuing)");
        /*
        // we need to send back a challenge but we're only going to challenge on the authentication schemes that are 
        // supported by the current runtime configuration
        ResponseBuilder forbidden = Response.status(Response.Status.FORBIDDEN);
        if( x509Authorization != null ) {
            forbidden = forbidden.header("WWW-Authenticate", "X509 realm=\"Attestation\"");
        }
        if( publickeyAuthorization != null ) {
            forbidden = forbidden.header("WWW-Authenticate", "PublicKey realm=\"Attestation\"");
        }
        if( hmacAuthorization != null ) {
            forbidden = forbidden.header("WWW-Authenticate", "MtWilson realm=\"Attestation\"");
        }
        throw new WebApplicationException(forbidden.build()); // send one WWW-Authenticate header for each supported scheme. 
        * 
        */
//        return request; // let the roles allowed resource filter throw an exception if the user does not have the proper roles
    }
    
    /**
     * Reads the request input stream and then resets it so that other filters can read it too.
     * @param request
     * @return 
     */
    private String readEntityBodyQuietly(ContainerRequestContext request) {
        String requestBody=null;
        try {
            InputStream in = request.getEntityStream();
            ByteArrayOutputStream content = new ByteArrayOutputStream();
            IOUtils.copy(in, content);
            byte[] contentBytes = content.toByteArray();
            request.setEntityStream(new ByteArrayInputStream(contentBytes));
            requestBody = new String(contentBytes);
            
            //log.debug("AuthenticationJerseyFilter: content follows:\n"+requestBody+"\n");
            
        }
        catch(IOException e) {
            log.info("AuthenticationJerseyFilter: cannot read input stream");
        }  
        return requestBody;
    }
    
    
    /**
     * Identifies the authorization scheme used by the client. 
     * Supported values are "MtWilson" for HMAC-SHA256 and "PublicKey" for RSA
     * @param authorization the content of the Authorization header
     * @return 
     */
    private AuthorizationScheme getAuthorizationScheme(String authorizationHeader) {
        if( authorizationHeader == null ) {
            throw new IllegalArgumentException("Authorization header is missing");
        }
        String[] terms = authorizationHeader.split(" ");
        if( terms.length == 0 ) {
            throw new IllegalArgumentException("Invalid authorization header");
        }
        String name = terms[0];
        try {
            AuthorizationScheme scheme = AuthorizationScheme.valueOf(name);
            return scheme;        
        }
        catch(Exception e) {
            throw new UnsupportedOperationException("Unsupported authorization scheme: "+name, e);
        }
    }
    
    private String createRequestString(ContainerRequestContext request, String requestBody) {
        StringBuilder content = new StringBuilder();
        content.append(String.format("%s %s\n", request.getMethod(), request.getUriInfo().getRequestUri()));
        MultivaluedMap<String,String> headers = request.getHeaders();
        ArrayList<String> headerNames = new ArrayList<String>(headers.keySet());
        Collections.sort(headerNames);
        for(String headerName : headerNames) {
            List<String> values = headers.get(headerName);
            if( values != null ) {
                for(String value : values) {
                    content.append(String.format("%s: %s\n", headerName, value));
                }
            }
        }
        content.append(String.format("\n%s", requestBody)); // empty line separates body from headers
        return content.toString();
    }
}
