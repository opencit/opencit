package com.intel.mountwilson.security;

import com.intel.dcsg.cpg.authz.token.ExpiredTokenException;
import com.intel.dcsg.cpg.authz.token.Token;
import com.intel.dcsg.cpg.authz.token.TokenFactory;
import com.intel.dcsg.cpg.authz.token.TokenValidator;
import com.intel.dcsg.cpg.authz.token.UnsupportedTokenVersionException;
import com.intel.dcsg.cpg.crypto.key.KeyNotFoundException;
import com.intel.mountwilson.as.common.ASConfig;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecurityFilter implements Filter {

    Logger log = LoggerFactory.getLogger(getClass().getName());
    private TokenFactory factory;
    private TokenValidator validator;

    @Override
    public void init(FilterConfig arg0) throws ServletException {
        int duration = ASConfig.getConfiguration().getInt("mtwilson.portal.sessionTimeOut", 1800); // use same duration as the session timeout
        factory = new TokenFactory();
        validator = new TokenValidator(factory);
        validator.setExpiresAfter(duration); 
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
        log.debug("Query String is: {}", request.getQueryString());
        
        if ((existingToken == null) && !request.getQueryString().isEmpty())
            existingToken = request.getQueryString();
            
//        if (existingToken == null) {
//            log.debug("Did not get the authorization token from the header");
//            // if it's not in the header, check if it's a form post parameter
//            String[] formParams = request.getParameterValues("AuthorizationToken");
//            if (formParams != null && formParams.length == 1) {
//                log.debug("Got the authorization token from the form params");
//                existingToken = request.getParameterValues("AuthorizationToken")[0];
//            } else {
//                
//                log.debug("getExistingToken Content type is {}", request.getContentType());
//
//                try {
//                    StringBuilder stringBuilder = new StringBuilder(2000);
//                    Scanner scanner = new Scanner(request.getInputStream());
//                    while (scanner.hasNextLine()) {
//                        stringBuilder.append(scanner.nextLine());
//                    }
//
//                    String body = stringBuilder.toString();
//                    log.debug("Payload data is {}", body);
//                    
//                    if (body.contains("AuthorizationToken")) {
//                    
//                        existingToken = getAuthTokenFromPayload(request.getContentType(), body);
//                        log.debug ("Retrieved the auth token from pay load {}.", existingToken);
//                    } else {
//                        log.error("getExistingToken: Form does not include AuthorizationToken");
//                    }
//                } catch (Exception exception) {
//                    log.error("Exception while reading payload.", exception);
//                }                
//            }
//        }
        if( existingToken != null && existingToken.equalsIgnoreCase("null") ) {
            existingToken = null; // fix for misbehaving clients that send AuthorizationToken: null   instead of not sending the header at all;  if we don't do this then we'll get an exception like "UnsupportedTokenVersionException: Unsupported token version 9e"
        }
        return existingToken;
    }

    // for GET, HEAD and OPTIONS  the token is not required as input;  but we'll provide a token as ouptut on all requests
    private boolean isTokenRequired(HttpServletRequest request) {
        boolean tokenRequired = true;// require it by default; then make exception for GET and OPTIONS http methods -- this assumes the service follows the web architecture and does not have side effects for GET and OPTIONS
        if (request.getMethod().equals("GET") || request.getMethod().equals("HEAD") || request.getMethod().equals("OPTIONS")) {
            tokenRequired = false;
        }
        log.debug("isTokenRequired? {}", tokenRequired);
        return tokenRequired;

    }

    // only call this for required tokens, will return the token if it's valid, null if it's not valid
    private Token isTokenValid(String token, String username) {
        log.debug("isTokenValid: Validating token for username '{}'", username); 
        try {
            Token tokenObject = validator.validate(token); // throws UnsupportedTokenVersionException, CryptographyException, ExpiredTokenException, KeyNotFoundException
            // validator already checks the timestamp,  so we just check that the token belongs to this user 
            String confirmUsername = new String(tokenObject.getContent(), Charset.forName("UTF-8"));
            log.debug("Token username: {}", confirmUsername.toCharArray()); 
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
        } catch (GeneralSecurityException e) {
            log.warn("Cannot validate token", e);
            return null;
        } catch (ExpiredTokenException e) {
            log.warn("Token is expired", e);
            return null;
        } catch (KeyNotFoundException e) {
            log.warn("Token key not found", e);
            return null;
        }
    }

    /**
     *
     * @param request
     * @param response
     * @return true if request processing should continue, false if it should
     * stop
     */
    private boolean processRequestToken(String username, HttpServletRequest request, HttpServletResponse response) {
        if (!isRequestMatchingReferer(request)) {
            return false; // direct user to login page... from the login page the next request will match the referer so we'll be able to continue past this poitn next time
        }
        if (!isTokenRequired(request)) {
            log.debug("processRequestToken: Adding token to request");
            try {
                // token was not required for this request , for example a GET, so add a new token and let it continue
                // when the request does not require a token,  we simply create one and return it.  at this point the user session has already been authetnicated using the cookie ;  this still prevents CSRF attacks because in CSRF the attacker cannot see our response.
                String token = factory.create(username);
                 log.debug("processRequestToken: DEBUGGING ONLY    NEW TOKEN: {}", token);
               response.addHeader("AuthorizationToken", token);
               response.addHeader("Cache-Control", "no-cache");
                // if the response is html,  the jsp tag can copy the token into a <meta> tag so that javascript can get it without making an additional ajax request;  it's also very useful for resolving an issue with the initial login page where javascript would not be able to request a token before the user logs in, but the login form requires a token
            request.setAttribute("AuthorizationToken", token); 
                return true;
            } catch (GeneralSecurityException e) {
                log.error("Cannot add token to request", e);
                return false; // we could let the request processing continue but if we cannot create new tokens the token validator will not allow any requests through anyway, so we stop early.
            }
        }
        // after this point a token is required so it must be presented and valdiated
        log.debug("processRequestToken: Checking token for username {}", username);
        String existingToken = getExistingToken(request);
        if (existingToken == null) {
            log.debug("processRequestToken: Missing required AuthorizationToken");
            return false;
        }
        if (!Base64.isBase64(existingToken)) {
            log.error("processRequestToken: AuthorizationToken is not formatted as a hexadecimal string");
            return false;
        }

        log.debug("processRequestToken: Received AuthorizationToken, validating");
        Token token = isTokenValid(existingToken, username);
        if (token == null) {
            log.debug("processRequestToken: Invalid request, possible CSRF with stolen token because token username does not match logged in username; forwarding request to Login page.");
            return false;
        }

        log.debug("processRequestToken: Confirmed username {} in token", username);
        // automatically issue a new token if the curent token is almost expired
        if (validator.expiresSoon(token.getTimestamp())) {
            log.debug("processRequestToken: Token expires soon, sending replacement token");
            // replace it and allow request to continue
            try {
                existingToken = factory.create(username);
                log.debug("processRequestToken: DEBUGGING ONLY    NEW TOKEN: {}", existingToken);
            } catch (GeneralSecurityException e) {
                log.error("Cannot create replacement token", e);
                // we let the request continue because this is not a client error; however if the server issue is not fixed when the token expires the client will be locked out due to no new tokens
            }

        }
        log.debug("processRequestToken: Adding token to response");
        response.addHeader("AuthorizationToken", existingToken);
        response.addHeader("Cache-Control", "no-cache");
                // if the response is html,  the jsp tag can copy the token into a <meta> tag so that javascript can get it without making an additional ajax request;  it's also very useful for resolving an issue with the initial login page where javascript would not be able to request a token before the user logs in, but the login form requires a token
        request.setAttribute("AuthorizationToken", existingToken); 
        return true;

    }
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        log.info("SecurityFilter >>");
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String url = request.getServletPath();

        HttpSession httpSession = request.getSession(false);
        System.out.println(httpSession + "----" + url);
        if (httpSession == null || httpSession.isNew()) {
            log.info("User's Session expired, forwarding request to Login page.");
            redirectToLogin(request, response);
            return;
        }
        Object obj = httpSession.getAttribute("logged-in");
        if (obj == null) {
            log.debug("User is not logged in or session expired, forwarding request to Login page.");
            goToLogin(request, response);
            return;
        }
        boolean logged = (Boolean) obj;
        log.debug("Logged in? {}", logged);
        if (!logged) {
            log.debug("User is not logged in or session expired, forwarding request to Login page.");
            goToLogin(request, response);
            return;
        }
        try {
            String username = (String) httpSession.getAttribute("username"); // this is set by CheckLoginController during login
            if (processRequestToken(username, request, response)) {
                chain.doFilter(request, response);
            } else {
                log.debug("Invalid request token, forwarding to Login page");
                redirectToLogin(request, response);
            }
        } catch (Exception e) {
            log.debug("Cannot process request token, forwarding request to Login page.", e);
            redirectToLogin(request, response);
        }


    }

    private void goToLogin(HttpServletRequest req, HttpServletResponse res) {
        try {
            //RequestDispatcher dispatcher = req.getRequestDispatcher("/login.htm");
            //req.setAttribute("message", "Your Login Session is Expired. Please Login again.");
            //dispatcher.forward(req, res);
            //res.setHeader("Refresh", "0;"+req.getContextPath()+"/login.htm");
            //res.sendRedirect(req.getContextPath()+"/login.htm");
//            res.addHeader("message", "Your Login Session is Expired. Please Login again.");
            res.addHeader("message", "error.login_expired");
            res.addHeader("requiredLogin", "true");
            res.sendRedirect(req.getContextPath() + "/login.htm");
            //res.addHeader("Location", req.getContextPath()+"/login.htm");
            //res.addHeader("message", "Your Login Session is Expired. Please Login again.");
        } catch (IOException e) {
            log.error("IOException Exception, Error While forwarding request to Login page.");
            e.printStackTrace();
        }

    }

    private void redirectToLogin(HttpServletRequest req, HttpServletResponse res) {
        try {
            RequestDispatcher dispatcher = req.getRequestDispatcher("/login.htm");
//            req.setAttribute("message", "Your Login Session is Expired. Please Login again.");
            req.setAttribute("message", "error.login_expired");
            dispatcher.forward(req, res);
        } catch (Exception e) {
            log.error("IOException Exception, Error While forwarding request to Login page.");
            e.printStackTrace();
        }

    }

    @Override
    public void destroy() {
    }
}
