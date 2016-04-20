/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.security.jersey;

import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.mtwilson.model.Md5Digest;
import com.intel.mtwilson.datatypes.Role;
import com.intel.mtwilson.security.core.HttpBasicUserFinder;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
public class HttpBasicRequestVerifier {

    private static Logger log = LoggerFactory.getLogger(HttpBasicRequestVerifier.class);
    private HttpBasicUserFinder finder;

    public HttpBasicRequestVerifier(HttpBasicUserFinder finder) {
        this.finder = finder;
    }

    public User getUserForRequest(String httpMethod, String requestUrl, MultivaluedMap<String, String> headers, String requestBody) throws CryptographyException {

        String userName;
        String userSpecifiedPassword;
        String authorizationHeader = headers.getFirst("Authorization");
        HttpBasicRequestVerifier.Authorization auth = parseAuthorization(authorizationHeader);

        String realm = new String(Base64.decodeBase64(auth.realm));
        //log.debug("VerifyAuthorization Header: Signed content (" + realm.length() + ") follows:\n" + realm);

        // In HttpBasic, the user name and password would be separated by :
        String[] loginInfo = realm.split(":");
        // Here we need to handle the scenario where in the password might be empty
        if (loginInfo.length == 0) {
            throw new CryptographyException("Login credentials specified are not valid.");
        }
        if (loginInfo.length == 1) {
            // Since the user has not specified the password, we will keep it blank. Some folks might want to use blank password.
            userName = loginInfo[0];
            userSpecifiedPassword = "";
        } else {
            userName = loginInfo[0];
            userSpecifiedPassword = loginInfo[1];
        }


        log.debug("VerifyAuthorization:   Username: " + userName);

        String userPasswordInSystem = finder.getPasswordForUser(userName);
        if (userPasswordInSystem == null) {
            throw new CryptographyException("User specified is currently not configured in the system.");
        }

        log.info("VerifyAuthorization:   Retrieved the user credentials from DB");

        if (!userPasswordInSystem.equals(userSpecifiedPassword)) {
            log.error("Request is NOT Authenticated");
            throw new CryptographyException("Either the user name or password specified is not correct.");
        }

        String insecureRequestSummary = httpMethod+ " "+requestUrl + " "+String.valueOf(headers.getFirst("Date")); // the hash of this complete value will not be used in anti-replay protection, but it will be logged in the database with the request.
        
        // Because of security reasons, the users using HttpBasic would be able to just retrieve the attestation status and will 
        // not have previleges for any any other operations.
        try {
            log.info("Request is authenticated");
            User userInfo = new User(userName, new Role[]{Role.Report}, userName, Md5Digest.valueOf(insecureRequestSummary.getBytes("UTF-8"))); 
            return userInfo;
        }
        catch(UnsupportedEncodingException e) {
            // unlikely to happen because we are using UTF-8 ...
            User userInfo = new User(userName, new Role[]{Role.Report}, userName, Md5Digest.valueOf(insecureRequestSummary.getBytes()));
            return userInfo;            
        }
    }

    /**
     * Below is a sample of the Authorization header that would be parsed. The base 64 encoded part is a combination of
     * user name and password separated by : Example: Authorization: Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==
     *
     * @param authorizationHeader
     * @return
     */
    private Authorization parseAuthorization(String authorizationHeader) {
        Authorization auth = new Authorization();
        String[] terms = authorizationHeader.split(" ");
        if (!"Basic".equals(terms[0])) {
            throw new IllegalArgumentException("Authorization type is not Basic");
        }
        // Return back the base64 encoded user name and password
        auth.realm = terms[1];
        return auth;
    }

    public static class Authorization {

        public String realm;
    }
}
