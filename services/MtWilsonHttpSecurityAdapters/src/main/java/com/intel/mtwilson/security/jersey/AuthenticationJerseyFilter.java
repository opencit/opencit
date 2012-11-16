/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.security.jersey;

import com.intel.mtwilson.datatypes.Role;
import com.intel.mtwilson.security.core.AuthorizationScheme;
import com.intel.mtwilson.security.core.IPAddressUtil;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a SERVER SIDE FILTER to handle INCOMING REQUESTS.
 * This class requires the following libraries:
 * com.sun.jersey.spi.container.ContainerRequest and ContainerRequestFilter from the Jersey Server API's
 * @since 0.5.1
 * @author jbuhacoff
 */
public class AuthenticationJerseyFilter implements ContainerRequestFilter {
    private HmacRequestVerifier hmacAuthorization; // currently this is set via Spring Framework; example is new VerifyAuthorization(new LoginBO()); and new VerifyAuthorization(new HashMapSecretKeyFinder());
    private PublicKeyRequestVerifier publickeyAuthorization; // currently this is set via Spring Framework; example is new VerifyAuthorization(new LoginBO()); and new VerifyAuthorization(new HashMapSecretKeyFinder());
    private X509RequestVerifier x509Authorization;
    private static Logger log = LoggerFactory.getLogger(AuthenticationJerseyFilter.class);
    private String[] trustWhitelist = null;
    private boolean sslRequired = true;
    
    public AuthenticationJerseyFilter() {
    }
    
    public void setRequestValidator(HmacRequestVerifier validator) { this.hmacAuthorization = validator; }
    public void setRequestValidator(PublicKeyRequestVerifier validator) { this.publickeyAuthorization = validator; }
    public void setRequestValidator(X509RequestVerifier validator) { this.x509Authorization = validator; }
    public void setTrustedRemoteAddress(String[] ipAddressOrSubnet) { trustWhitelist = ipAddressOrSubnet; } // should be set by mtwilson.api.trust=ipaddresslist
    public void setSslRequired(boolean required) { sslRequired = required; } // should be set by mtwilson.ssl.required=true/false
    
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
    public ContainerRequest filter(ContainerRequest request) {
        log.info("AuthenticationJerseyFilter request for {} {} with Authorization={}", new String[] { request.getMethod(), request.getPath(), request.getHeaderValue("Authorization") });
        log.debug("AuthenticationJerseyFilter: HTTP method="+request.getMethod());
        log.debug("AuthenticationJerseyFilter: Request URI="+request.getRequestUri());
        log.debug("AuthenticationJerseyFilter: Secure/https="+request.isSecure());
        
        // TODO: the servletRequest is not being populated. maybe it's for "servlets" and the info doesn't get passed to the filter. fidn another way!!!
        if( servletRequest != null ) {
            log.debug("AuthenticationJerseyFilter: Remote Address="+servletRequest.getRemoteAddr());            
        }
        
        if( !request.isSecure() && sslRequired ) {
            log.warn("AuthenticationJerseyFilter: rejecting insecure (http) request");
            throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN).entity("Secure connection required").build());            
        }
        
        // the administrator may specify a list of IP addresses to trust without requiring an Authorization header. this requirement was added in 0.5.1-sp1 (0.5.2).        
        log.debug("Trusted remote addresses: {}", StringUtils.join(trustWhitelist, " and "));
        log.debug("Client remote address: {}", servletRequest != null ? servletRequest.getRemoteAddr() : "(NO SERVLET REQUEST)");
        if( trustWhitelist != null && trustWhitelist.length > 0 && servletRequest != null && servletRequest.getRemoteAddr() != null ) {
            String trustedAddress = IPAddressUtil.matchAddressInList(servletRequest.getRemoteAddr(), trustWhitelist);
            if( trustedAddress != null ) {
                log.info("Request from trusted remote addr "+servletRequest.getRemoteAddr()+" matches "+trustedAddress+" in mtwilson.api.trust");
                User user = new User(servletRequest.getRemoteAddr(), new Role[] { Role.Attestation, Role.Audit, Role.Report, Role.Security, Role.Whitelist });
                request.setSecurityContext(new MtWilsonSecurityContext(user, request.isSecure()));
                // MtWilsonThreadLocal.set(new MtWilsonSecurityContext(user, request.isSecure()));
                return request;
            }
        }
        
        String requestBody = readEntityBodyQuietly(request);
        
        if( request.getHeaderValue("Authorization") != null ) {
            try {
                // support both the symmetric-key HMAC-SHA256 method "MtWilson" and the RSA method "PublicKey"
                User user = null;
                AuthorizationScheme scheme = getAuthorizationScheme(request.getHeaderValue("Authorization"));
                log.debug("Authorization scheme is {}", scheme.name());
                if( x509Authorization != null && scheme.equals(AuthorizationScheme.X509) ) {
                    user = x509Authorization.getUserForRequest(request.getMethod(), request.getRequestUri().toString(), request.getRequestHeaders(), requestBody);
                }
                else if( publickeyAuthorization != null && scheme.equals(AuthorizationScheme.PublicKey) ) {
                    user = publickeyAuthorization.getUserForRequest(request.getMethod(), request.getRequestUri().toString(), request.getRequestHeaders(), requestBody);
                }
                else if( hmacAuthorization != null && scheme.equals(AuthorizationScheme.MtWilson) ) {
                    user = hmacAuthorization.getUserForRequest(request.getMethod(), request.getRequestUri().toString(), request.getRequestHeaders(), requestBody);                
                }
                if( user != null ) {
                    log.debug("AuthenticationJerseyFilter: Got user, setting security context");
                    request.setSecurityContext(new MtWilsonSecurityContext(user, request.isSecure()));
                    log.debug("AuthenticationJerseyFilter: Set security context");
                    // MtWilsonThreadLocal.set(new MtWilsonSecurityContext(user, request.isSecure()));
                    return request;
                }
            }
            catch(Exception e) {
                throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build());                
            }
        }
        log.debug("AuthenticationJerseyFilter: request is NOT AUTHENTICATED (continuing)");
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
        return request; // let the roles allowed resource filter throw an exception if the user does not have the proper roles
    }
    
    /**
     * Reads the request input stream and then resets it so that other filters can read it too.
     * @param request
     * @return 
     */
    private String readEntityBodyQuietly(ContainerRequest request) {
        String requestBody = null;
        try {
            InputStream in = request.getEntityInputStream();
            ByteArrayOutputStream content = new ByteArrayOutputStream();
            IOUtils.copy(in, content);
            byte[] contentBytes = content.toByteArray();
            request.setEntityInputStream(new ByteArrayInputStream(contentBytes));
            requestBody = new String(contentBytes);
            log.debug("AuthenticationJerseyFilter: content follows:\n"+requestBody+"\n");
        }
        catch(IOException e) {
            log.error("AuthenticationJerseyFilter: cannot read input stream");
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
}
