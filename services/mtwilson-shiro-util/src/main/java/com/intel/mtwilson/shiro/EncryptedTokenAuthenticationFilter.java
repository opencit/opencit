/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro;

import com.intel.dcsg.cpg.authz.token.ExpiredTokenException;
import com.intel.dcsg.cpg.authz.token.Token;
import com.intel.dcsg.cpg.authz.token.TokenFactory;
import com.intel.dcsg.cpg.authz.token.TokenValidator;
import com.intel.dcsg.cpg.authz.token.UnsupportedTokenVersionException;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.key.KeyNotFoundException;
import com.intel.mtwilson.My;
import com.thoughtworks.xstream.XStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;

/**
 *
 * @author jbuhacoff
 */
public class EncryptedTokenAuthenticationFilter extends AuthenticatingFilter {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EncryptedTokenAuthenticationFilter.class);
    
    private TokenFactory tokenFactory = null;
    private TokenValidator tokenValidator = null;

    public void setTokenFactory(TokenFactory tokenFactory) {
        this.tokenFactory = tokenFactory;
    }

    public void setTokenValidator(TokenValidator tokenValidator) {
        this.tokenValidator = tokenValidator;
    }
    
    
    
    @Override
    protected AuthenticationToken createToken(ServletRequest sr, ServletResponse sr1) throws Exception {
        log.debug("createToken");
        if( !(sr instanceof HttpServletRequest)) {
            log.error("Cannot find token because ServletRequest is {}", sr.getClass().getName());
            throw new AuthenticationException("Cannot read encrypted token");
        }
        HttpServletRequest httpRequest = (HttpServletRequest)sr;
        HttpServletResponse httpResponse = (HttpServletResponse)sr1;

        if( !isTokenRequired(httpRequest)) {
            return null;
        }
        
        if( !isRequestMatchingReferer(httpRequest)) {
            throw new AuthenticationException("Referer");
        }
        
        String encryptedToken = getExistingToken(httpRequest);
        if( encryptedToken == null ) {
            throw new AuthenticationException("Missing token"); 
        }
        
        
        int duration = My.configuration().getConfiguration().getInt("mtwilson.portal.sessionTimeOut", 1800); // use same duration as the session timeout in Mt Wilson 1.2
        if( tokenFactory == null || tokenValidator == null ) {
            tokenFactory = new TokenFactory(); 
            tokenValidator = new TokenValidator(tokenFactory);
            tokenValidator.setExpiresAfter(duration);  // in seconds
        }
        
        Token validatedToken = isTokenValid(encryptedToken, "test foo username");
        if( validatedToken == null ) {
            throw new AuthenticationException("Invalid token");
        }

        httpResponse.addHeader("Cache-Control", "no-cache");
        
        // check if we should generate a new token for the user to use in next request, like if this one is about to expire.  but we still use the current token for authentication, not the new one.
        // automatically issue a new token if the curent token is almost expired
        if (tokenValidator.expiresSoon(validatedToken.getTimestamp())) {
            log.debug("processRequestToken: Token expires soon, sending replacement token");
            // replace it and allow request to continue

            XStream xs = new XStream();
            EncryptedTokenContent existingToken = (EncryptedTokenContent)xs.fromXML(new String(validatedToken.getContent(), Charset.forName("UTF-8")));
//                byte[] validatedToken.getContent();
                // this block of code repeated in EncryptedTokenAuthenticationFilter
                EncryptedTokenContent tokenContent = new EncryptedTokenContent();
                tokenContent.loginPasswordId = existingToken.loginPasswordId;
                tokenContent.userId = existingToken.userId;
                tokenContent.username = existingToken.username;
                String tokenContentXml = xs.toXML(tokenContent);
                log.debug("tokenContent xml: {}", tokenContentXml);
                
                String refreshToken = tokenFactory.create(tokenContentXml);
                log.debug("processRequestToken: DEBUGGING ONLY    NEW TOKEN: {}", existingToken);
                
                httpResponse.addHeader("Authorization-Token", refreshToken);
                return new EncryptedTokenAuthenticationToken(new Username(tokenContent.username), refreshToken, httpRequest.getRemoteHost());
        }
        else {
            XStream xs = new XStream();
            EncryptedTokenContent existingToken = (EncryptedTokenContent)xs.fromXML(new String(validatedToken.getContent(), Charset.forName("UTF-8")));
                httpResponse.addHeader("Authorization-Token", encryptedToken);
                return new EncryptedTokenAuthenticationToken(new Username(existingToken.username), httpRequest.getHeader("Authorization-Token"), httpRequest.getRemoteHost());
            
        }

    }

    @Override
    protected boolean onAccessDenied(ServletRequest sr, ServletResponse sr1) throws Exception {
        redirectToLogin(sr,sr1);
        return false;
    }
    
    // moved to CsrfFilter
    private boolean isTokenRequired(HttpServletRequest request) {
        boolean tokenRequired = true;// require it by default; then make exception for GET and OPTIONS http methods -- this assumes the service follows the web architecture and does not have side effects for GET and OPTIONS
        if (request.getMethod().equals("GET") || request.getMethod().equals("HEAD") || request.getMethod().equals("OPTIONS")) {
            tokenRequired = false;
        }
        log.debug("isTokenRequired? {}", tokenRequired);
        return tokenRequired;

    }
    
    // moved to CsrfFilter
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
    
    // moved to csrf filter
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
            log.debug("Ttoken username: {}", confirmUsername);
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
    
    
}
