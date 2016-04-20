/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.authz;

import com.intel.dcsg.cpg.authz.token.ExpiredTokenException;
import com.intel.dcsg.cpg.authz.token.Token;
import com.intel.dcsg.cpg.authz.token.TokenFactory;
import com.intel.dcsg.cpg.authz.token.TokenValidator;
import com.intel.dcsg.cpg.authz.token.UnsupportedTokenVersionException;
import com.intel.dcsg.cpg.crypto.key.KeyNotFoundException;
import java.util.HashSet;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.apache.shiro.web.filter.authz.AuthorizationFilter;
import com.intel.dcsg.cpg.net.InternetAddress;
import com.intel.mtwilson.My;
import com.intel.mtwilson.shiro.EncryptedTokenContent;
import com.intel.mtwilson.shiro.Username;
import com.thoughtworks.xstream.XStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.StringUtils;
import org.apache.shiro.web.util.WebUtils;

/**
 * 
 * @author jbuhacoff
 */
public class CsrfFilter extends AuthorizationFilter {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CsrfFilter.class);
    private HashSet<String> ignoreMethodSet = new HashSet<String>();
    private String ignoreMethods = "GET,HEAD,OPTIONS";
    private HashSet<String> ignoreAuthorizationSet = new HashSet<String>();
    private String ignoreAuthorizations = "HMAC,X509";
    private Pattern authorizationNamePattern = Pattern.compile("^([a-zA-Z]+) \\.+"); // for example  "BASIC ..."
    private TokenFactory tokenFactory = null;
    private TokenValidator tokenValidator = null;

    public CsrfFilter() throws IOException {
        int duration = My.configuration().getConfiguration().getInt("mtwilson.portal.sessionTimeOut", 1800); // use same duration as the session timeout in Mt Wilson 1.2
        tokenFactory = new TokenFactory(); 
        tokenValidator = new TokenValidator(tokenFactory);
        tokenValidator.setExpiresAfter(duration);  // in seconds
    }
    /**
     * Default value is "GET,HEAD,OPTIONS". You can override by calling this
     * setter.
     * 
     * @param ignoreMethods comma-separated list of HTTP method names to ignore
     */
    public void setIgnoreMethods(String ignoreMethods) {
        this.ignoreMethods = ignoreMethods;
        String[] list = ignoreMethods.replace(" ", "").split(",");
        ignoreMethodSet.clear();
        for (String item : list) {
            if(item.matches("^\\.*[^a-zA-Z]\\.*$") ) { throw new IllegalArgumentException("Invalid HTTP method"); } // http method names have only letters
            log.debug("Ignore HTTP Method {}", item);
            ignoreMethodSet.add(item.toUpperCase());
        }
    }

    public String getIgnoreMethods() {
        return ignoreMethods;
    }
    
    /**
     * Default value is "HMAC,X509". You can override by calling this
     * setter.
     * @param ignoreAuthorizations 
     */
    public void setIgnoreAuthorizations(String ignoreAuthorizations) {
        this.ignoreAuthorizations = ignoreAuthorizations;
        String[] list = ignoreAuthorizations.replace(" ", "").split(",");
        ignoreAuthorizationSet.clear();
        for (String item : list) {
            if(item.matches("^\\.*[^a-zA-Z]\\.*$") ) { throw new IllegalArgumentException("Invalid Authorization method"); } // http method names have only letters
            log.debug("Ignore Authorization Method {}", item);
            ignoreAuthorizationSet.add(item.toUpperCase());
        }
    }

    public String getIgnoreAuthorizations() {
        return ignoreAuthorizations;
    }

    
    public void setTokenFactory(TokenFactory tokenFactory) {
        this.tokenFactory = tokenFactory;
    }

    public void setTokenValidator(TokenValidator tokenValidator) {
        this.tokenValidator = tokenValidator;
    }
    
    /**
     * CSRF protection is only defined against HTTP requests that may change 
     * the server state such as POST, PUT, DELETE, and PATCH for browser
     * clients using HTTP BASIC, HTTP DIGEST, or COOKIE AUTHENTICATION.
     * If the ServletRequest is not an HttpServletRequest the access will
     * be allowed without checking for an anti-CSRF token.
     * If the HTTP method is GET, HEAD, or OPTIONS the access will be allowed
     * without checking for an anti-CSRF token.
     * If the authentication method is HMAC or X509 indicating that an application
     * other than a browser is accessing the API, the access will be allowed
     * without checking for an anti-CSRF token. 
     * If the token is checked, the username represented by the token must
     * match the currently authenticated user in order to allow access.
     * 
     * @param request
     * @param response
     * @param mappedValue
     * @return
     * @throws Exception 
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
        log.debug("csrf filter request {}", request.getClass().getName());
        if( !(request instanceof HttpServletRequest) ) {
            log.debug("csrf filter ignoring non-http request");
            return true;
        }
        HttpServletRequest httpRequest = (HttpServletRequest)request;
        // anti-csrf token not required for GET,HEAD,OPTIONS methods and also not required if client is using HMAC,X509 authentication (indicates non-browser usage - could have also been implemented as user-agent check but that has the disadvantage of checking for specific user agents)
        if( !isTokenRequired(httpRequest) ) {
            return true;
        }
        // referer must be our application 
        if( !isRequestMatchingReferer(httpRequest)) {
            return false;
        }
        // now we have an http request with a method like POST, PUT, DELETE, or PATCH and an authorization using HTTP BASIC or HTTP DIGEST
        // so we need to check the anti-CSRF token and make sure it matches the authenticated user name.
        
        // if a token is present, it must be valid - we don't fix invalid tokens here, user has to login again to get a new token
        String tokenText = getExistingToken(httpRequest);
        log.debug("token: {}", tokenText);

        Subject subject = getSubject(request,response);
        log.debug("got subject");
        for(Object p : subject.getPrincipals().asList()) { log.debug("principal: {}", p.getClass().getName()); }
        Username username = subject.getPrincipals().oneByType(Username.class);
        log.debug("got username: {}", username.getUsername());

        Token validatedToken = isTokenValid(tokenText, username.getUsername());
        if( validatedToken == null ) {
            log.debug("invalid token");
            return false; // throw new AuthenticationException("Invalid token"); 
        }
        
        // token is valid. we replace it with a new token, extending the expiration
        createToken((HttpServletResponse)response, username);
        
        return true;
    }
    
    
    private boolean isTokenRequired(HttpServletRequest httpRequest) {
        log.debug("csrf filter http method {}", httpRequest.getMethod());
        if( ignoreMethodSet.contains(httpRequest.getMethod())) {
            log.debug("csrf filter ignoring http method {}", httpRequest.getMethod());
            return false;
        }
        String authorizationHeader = httpRequest.getHeader("Authorization");
        log.debug("csrf filter authorizatino header {}", authorizationHeader); 
        if( authorizationHeader != null ) {
            Matcher matcher = authorizationNamePattern.matcher(authorizationHeader);
            if( matcher.matches() ) {
                String authorizationMethod = matcher.group(1);
                log.debug("csrf filter authorization method {}", authorizationMethod);
                if( ignoreAuthorizationSet.contains(authorizationMethod) ) {
                    log.debug("csrf filter ignoring authorization method {}", authorizationMethod);
                    return false;
                }
            }
        }
        return true;
    }
    
    private boolean isRequestMatchingReferer(HttpServletRequest request) {
        // fix for issue #1038 cross site request forgery (CSRF) is to ensure that logged-in users are in control of requests
        // so first we deny any request coming from a source other than a link or form in the portal by ensuring the referer 
        // header contains our own address;  there are situations where attackers can circumvent this  
        // but it's an easy first line of defense before we spend any resources checking something more intensive
        String refererHeader = request.getHeader("Referer");
        log.debug("isRequestMatchingReferer: referer = {}", refererHeader); // example CSRF: referer = http://localhost:8080/mtwilson-portal/home.html
        log.debug("isRequestMatchingReferer: request = {}", request.getRequestURL().toString()); // example CSRF: request = http://localhost:8080/mtwilson-portal/getData/getDashBoardData.html

        if (refererHeader == null) {
            return false;
        }
        try {
            URL refererUrl = new URL(refererHeader);
            URL requestUrl = new URL(request.getRequestURL().toString());
            boolean refererOk = requestUrl.getProtocol().equals(refererUrl.getProtocol())
                    && requestUrl.getHost().equals(refererUrl.getHost())
                    && requestUrl.getPort() == refererUrl.getPort();

            log.debug("isRequestMatchingReferer: {}", refererOk);
            return refererOk;
        } catch (Exception e) {
            log.error("isRequestMatchingReferer: Error while checking referer", e);
            return false;
        }
    }

    private String getExistingToken(HttpServletRequest request) {
        // second part of fix for issue #1038 is to use a secure token to deter attackers who are able to forge the referer header by exploiting the client's insecure software stack
        // the idea is that without the help of a cross-site scripting exploit, the attacker will not be able to predict the token and therefore the CSRF attack will fail
        // it's important to note that this does NOT prevent an attacker from hijacking the user's session using a cross-site scripting attack - in such an attack the attacker can smiply replay the token for as long as its valid just as a legitimate request does.
        // we defend against cross-site scripting elsewhere by validating input parameters and escaping the output in a way appropriate for its context
        String existingToken = request.getHeader("AuthorizationToken"); // we don't use the Authorization header by itself like in oath 2.0 "Bearer" authorization type because we don't want the browser to submit ti automatically!   should be submitted by javascript running in our html app.    that also prevents simple form submissions that attempt to bypass our client-side javascript.
        if (existingToken == null) {
            // if it's not in the header, check if it's a form post parameter
            String[] formParams = request.getParameterValues("AuthorizationToken");
            if (formParams != null && formParams.length == 1) {
                existingToken = request.getParameterValues("AuthorizationToken")[0];
            } else {
                log.debug("getExistingToken: Form does not include AuthorizationToken");
            }
        }
        if( existingToken != null && existingToken.equalsIgnoreCase("null") ) {
            existingToken = null; // fix for misbehaving clients that send AuthorizationToken: null   instead of not sending the header at all;  if we don't do this then we'll get an exception like "UnsupportedTokenVersionException: Unsupported token version 9e"
        }
        return existingToken;
    }
    
    // only call this for required tokens, will return the token if it's valid, null if it's not valid
    private Token isTokenValid(String token, String username) {
        log.debug("isTokenValid: Validating token for username '{}'", username);
        try {
            Token tokenObject = tokenValidator.validate(token); // throws UnsupportedTokenVersionException, CryptographyException, ExpiredTokenException, KeyNotFoundException
            // validator already checks the timestamp,  so we just check that the token belongs to this user 
            String confirmUsername = new String(tokenObject.getContent(), Charset.forName("UTF-8"));
            log.debug("Token username: {}", confirmUsername);
            if( username.equals(confirmUsername) ) { 
                return tokenObject;
            } 
            else { 
                log.debug("Input username does not match: {}", username.toCharArray()); 
                return null;
            }
        } catch (UnsupportedTokenVersionException e) {
            log.warn("Token version not supported", e);
            return null;
//        } catch (CryptographyException e) {
//            log.warn("Cannot validate token", e);
//            return null;
        } catch (ExpiredTokenException e) {
            log.warn("Token is expired", e);
            return null;
        } catch (KeyNotFoundException e) {
            log.warn("Token key not found", e);
            return null;
        } catch (GeneralSecurityException e) {
            log.warn("General security exception", e);
            return null;
        }
        
    }
    
    private void createToken(HttpServletResponse httpResponse, Username username) throws GeneralSecurityException {

                
                String refreshToken = tokenFactory.create(username.getUsername());
                log.debug("processRequestToken: DEBUGGING ONLY    NEW TOKEN: {}", refreshToken);
                
                httpResponse.addHeader("Authorization-Token", refreshToken);

        httpResponse.addHeader("Cache-Control", "no-cache");
    }
    
    /**
     * Because a client accessing the service from a denied address has no
     * possibility of logging in, the onAccessDenied behavior is modified to
     * either return an error or redirect to the unauthorizedUrl (which would
     * need to have the anon filter set to allow anyone to access it)
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws IOException {
        if (StringUtils.hasText(getUnauthorizedUrl())) {
            WebUtils.issueRedirect(request, response, getUnauthorizedUrl());
        } else {
            WebUtils.toHttp(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);

        }
        return false;
    }
}
