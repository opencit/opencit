package com.intel.mtwilson.security.jersey;

import com.intel.mtwilson.security.core.X509UserFinder;
import com.intel.mtwilson.security.core.X509UserInfo;
import com.intel.mtwilson.security.http.RsaSignatureInput;
import com.intel.dcsg.cpg.rfc822.Rfc822Date;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.mtwilson.model.Md5Digest;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.cert.Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class requires the following libaries:
 * org.apache.commons.codec.binary.Base64 from commons-codec
 * 
 * It implements the server side authorization of incoming API requests using
 * the HTTP "Authorization" header. The authentication scheme supported is "X509Certificate"
 * which is an asymmetric-key scheme using RSA to sign requests.
 * 
 * See also X509RequestVerifier and HmacAuthorization
 * 
 * @since 0.5.2
 * @author jbuhacoff
 */
public class X509RequestVerifier {
    private static Logger log = LoggerFactory.getLogger(X509RequestVerifier.class);
    private X509UserFinder finder;
    private int requestsExpireAfterMs = 60 * 60 * 1000; // 1 hour, in milliseconds
    
    private String headerAttributeNameValuePair = "([a-zA-Z0-9_-]+)=\"([^\"]+)\"";
    private Pattern headerAttributeNameValuePairPattern = Pattern.compile(headerAttributeNameValuePair);
    
    public X509RequestVerifier(X509UserFinder finder) {
        this.finder = finder;
        
    }
    
    /**
     * Verifies the signature for a given request method, url, Authorization header, and request body.
     * Information from the request is collected and signed by the secret key
     * provided by the SecretKeyFinder provided to the constructor. 
     * The SecretKeyFinder retrieves the appropriate secret key to validate the request
     * by looking up the userId token included in the request.
     * 
     * @param httpMethod such as "GET" or "POST"
     * @param requestUrl a complete request URL such as http://example.com/some/path
     * @param headers all the request headers, including the Authorization header from the request
     * @param requestBody the body of the request, or null
     * @return a User object if the signature on the request is valid for a known user, or null if it's invalid or user cannot be found
     * 
     */
    public User getUserForRequest(String httpMethod, String requestUrl, MultivaluedMap<String,String> headers,  String requestBody) throws CryptographyException, UnsupportedEncodingException {
        String authorizationHeader = headers.getFirst("Authorization");
//        try {
            log.debug("Parsing authorization header: {}", authorizationHeader);
            Authorization a = parseAuthorization(authorizationHeader);

            log.info("X509CertificateAuthorization: Request timestamp ok");
            RsaSignatureInput signatureBlock = new RsaSignatureInput();
            
            signatureBlock.httpMethod = httpMethod;
            
            /**
             * Bug #383 disabling support for this because it creates a security vulnerability
            if( headers.containsKey("X-Original-URL") ) {
                signatureBlock.url = headers.getFirst("X-Original-URL");
                log.debug("X509CertificateAuthorization: Using X-Original-URL");
            }
            else if( headers.containsKey("X-Original-Request") ) {
                signatureBlock.url = headers.getFirst("X-Original-Request");            
                log.debug("X509CertificateAuthorization: Using X-Original-Request");
            }
            else {
                signatureBlock.url = requestUrl;
            }
            */
            signatureBlock.url = requestUrl;
            
            signatureBlock.realm = a.realm;
            signatureBlock.fingerprintBase64 = a.fingerprintBase64;
            signatureBlock.signatureAlgorithm = a.signatureAlgorithm;
            signatureBlock.headerNames = a.headerNames;
            HashMap<String,String> headerValues = new HashMap<String,String>();
            for(String headerName : a.headerNames) {
                headerValues.put(headerName, headers.getFirst(headerName));
            }
            signatureBlock.headers = headerValues;
            signatureBlock.body = requestBody;
            String content = signatureBlock.toString(); // may throw IllegalArgumentException if any required field is null or invalid

            //log.debug("X509CertificateAuthorization: Signed content ("+content.length()+") follows:\n{}", content);
            
            // locate the public key or x509 certificate that can verify the signature
            byte[] document = content.getBytes("UTF-8");
            byte[] signature = Base64.decodeBase64(a.signatureBase64);
            String signatureAlgorithm = signatureAlgorithm(a.signatureAlgorithm);
            byte[] fingerprint = Base64.decodeBase64(a.fingerprintBase64);
            X509UserInfo userInfo = finder.getUserForX509Identity(fingerprint);

            log.debug("X509CertificateAuthorization Fingerprint: {}",a.fingerprintBase64);
            log.debug("X509CertificateAuthorization:  Signature: {}",a.signatureBase64);
            log.debug("X509CertificateAuthorization:  Algorithm: {}",a.signatureAlgorithm);
            
            
            if( userInfo == null ) { 
                log.error("X509CertificateAuthorization cannot find user with fingerprint: {} ", a.fingerprintBase64);
                return null;
            }
            
            boolean isValid = false;
            if( userInfo.certificate != null ) {
                try {
                    isValid = verifySignature(document, userInfo.certificate, signatureAlgorithm, signature);
                } catch (NoSuchAlgorithmException ex) {
                    throw new CryptographyException("Signature algorithm not supported: "+signatureAlgorithm, ex);
                } catch (InvalidKeyException ex) {
                    throw new CryptographyException("Invalid key in certificate: "+ex.getMessage(), ex);
                } catch (SignatureException ex) {
                    throw new CryptographyException("Unable to verify signature: "+ex.getMessage(), ex);
                }
                log.debug("X509CertificateAuthorization verified signature using certificate; result= {}", isValid);
                if( !isValid ) {
                    throw new IllegalArgumentException("Authorization signature is invalid");
                }
            }
            

            // show a warning if actual URL doesn't match signed URL, because it means we need to be careful when
            // routing and authorizing the requested actions... must be done according to signed URL, not actual URL
            if( !signatureBlock.url.equals(requestUrl) ) {
                log.warn("X509CertificateAuthorization: Actual URL did not match Signed URL");
                log.debug("  Actual URL: "+requestUrl);
                log.debug("  Signed URL: "+signatureBlock.url);
            }
            

            if( isValid ) {
                log.info("Request is authenticated");
                
                // check if the request has expired by looking at the HTTP Date header... but only if it was signed.
                if( signatureBlock.headers.containsKey("Date") ) {
                    Date requestDate = Rfc822Date.parse(signatureBlock.headers.get("Date"));
                    if( isRequestExpired(requestDate) ) {
                        log.error("X509CertificateAuthorization: Request expired; date="+requestDate);
                        throw new IllegalArgumentException("Request expired"); //; current time is "+Iso8601Date.format(new Date()));
                    }
                }
                else {
                    throw new IllegalArgumentException("Missing date header in request");                
                }

                
                
                return new User(a.fingerprintBase64, userInfo.roles, userInfo.loginName,  Md5Digest.valueOf(signature));
            }
            /*
        }
        catch (IllegalArgumentException e) {
            log.error("Required parameters are missing or invalid: "+e.getMessage(), e);
        }
        catch (NoSuchAlgorithmException e) {
            log.error("Unsupported signature algorithm", e);
        }
        catch (InvalidKeyException e) {
            log.error("Password is not a valid key for signature algorithm", e);            
        }
        catch (Exception e) {
            log.error("Unknown error while verifying signature", e);            
        }*/
        log.error("Request is NOT AUTHENTICATED");
        return null;
    }
    
    private boolean isRequestExpired(Date timestamp) {
        // request expiration policy
        Calendar expirationTime = Calendar.getInstance();
        expirationTime.setTime(timestamp);
        expirationTime.add(Calendar.MILLISECOND, requestsExpireAfterMs);
        Calendar currentTime = Calendar.getInstance();
        
        if( currentTime.after(expirationTime)) {
            long diff = currentTime.getTimeInMillis() - expirationTime.getTimeInMillis();
            log.warn("Request expired: {}", DurationFormatUtils.formatDurationHMS(diff));
            return true;
        }
        return false;
    }

    /**
     * Verifies the signature for a given request method, url, Authorization header, and request body.
     * Information from the request is collected and signed by the secret key
     * provided by the SecretKeyFinder provided to the constructor. 
     * The SecretKeyFinder retrieves the appropriate secret key to validate the request
     * by looking up the userId token included in the request.
     * 
     * @param httpMethod such as "GET" or "POST"
     * @param requestUrl a complete request URL such as http://example.com/some/path
     * @param headers all the request headers, including the Authorization header from the request
     * @param requestBody the body of the request, or null
     * @return true if the signature on the request is valid
     */
    public boolean isSignatureValid(String httpMethod, String requestUrl, MultivaluedMap<String,String> headers, String requestBody) throws CryptographyException, UnsupportedEncodingException {
        User user = getUserForRequest(httpMethod, requestUrl, headers, requestBody);
        return user != null;
    }
    
    /**
     * 
     * Authorization header format is like this:   "Signature" *<SP <attribute-name "=" quoted-attribute-value>>
     * 
     * Sample Authorization header:
     * 
Authorization: X509
  realm="Example", 
  fingerprint="0685bd9184jfhq22",
  headers="X-Nonce,Date",
  algorithm="RSA-SHA256", 
  signature="wOJIO9A2W5mFwDgiDvZbTSMK%2FPY%3D"
     * 
     * @param authorizationHeader
     * @return 
     */
    private Authorization parseAuthorization(String authorizationHeader) {
         Authorization a = new Authorization();
         // splitting on spaces should yield "X509" followed by attribute name-value pairs
        String[] terms = authorizationHeader.split(" ");
        if( !"X509".equals(terms[0]) ) { throw new IllegalArgumentException("Authorization type is not X509"); }
        for(int i=1; i<terms.length; i++) {
            // each term after "PublicKey" is an attribute name-value pair, like realm="Example"
            Matcher attributeNameValue = headerAttributeNameValuePairPattern.matcher(terms[i]);
            if( attributeNameValue.find() ) {
                String name = attributeNameValue.group(1);
                String value = attributeNameValue.group(2);
                if( name.equals("realm") ) { a.realm = value; }
                if( name.equals("fingerprint") || name.equals("id") ) { a.fingerprintBase64 = value; }
                if( name.equals("headers") ) { a.headerNames = value.split(","); }
                if( name.equals("algorithm") || name.equals("digest") ) { a.signatureAlgorithm = value; }
                if( name.equals("signature") ) { a.signatureBase64 = value; }
            }
        }
        if( a.realm == null || a.realm.isEmpty() ) {
            log.warn("Authorization is missing realm"); //            throw new IllegalArgumentException("Authorization is missing realm"); // currently we allow undefined realm because we only have one database of users. in the future we could require a realm if we have moer than one and we need to know where to look things up.
        }
        if( a.fingerprintBase64 == null || a.fingerprintBase64.isEmpty() ) {
            throw new IllegalArgumentException("Authorization is missing id/fingerprint");
        }
        if( a.signatureAlgorithm == null || a.signatureAlgorithm.isEmpty() ) {
            throw new IllegalArgumentException("Authorization is missing signature algorithm");
        }
        if( a.signatureBase64 == null || a.signatureBase64.isEmpty() ) {
            throw new IllegalArgumentException("Authorization is missing signature");
        }
        return a;
    }
    
    

    // commenting out unused function (6/11 1.2)
    /*
    private boolean verifySignature(byte[] document, PublicKey publicKey, String signatureAlgorithm, byte[] signature) throws NoSuchAlgorithmException,InvalidKeyException, SignatureException {
        Signature rsa = Signature.getInstance(signatureAlgorithm); 
        rsa.initVerify(publicKey);
        rsa.update(document);
        return rsa.verify(signature);
    }
    */
    
    private boolean verifySignature(byte[] document, Certificate certificate, String signatureAlgorithm, byte[] signature) throws NoSuchAlgorithmException,InvalidKeyException, SignatureException {
        Signature rsa = Signature.getInstance(signatureAlgorithm); 
        rsa.initVerify(certificate);
        rsa.update(document);
        return rsa.verify(signature);
    }
    
    /**
     * Standardizes signature algorithm names to the Java name.
     * "SHA256withRSA".equals(signatureAlgorithm("RSA-SHA256a")); // true
     * @param name
     * @return 
     */
    private String signatureAlgorithm(String name) {
        if( "RSA-SHA256".equals(name) ) { return "SHA256withRSA"; }
        return name;
    }
    
    /**
     * This class represents the content of the HTTP Authorization header.
     * It is very closely related to the RsaSignatureInput class but not
     * the same because this class includes the base64-encoded signature
     * from the Authorization header. Also, the selected HTTP header names
     * to include in the signature are identified in the Authorization header
     * so they are included here, but the values for those headers are not
     * included here.
     */
    public static class Authorization {
        public String realm;
        public String fingerprintBase64;
        public String[] headerNames = ArrayUtils.EMPTY_STRING_ARRAY;
        public String signatureAlgorithm;
        public String signatureBase64;
    }
    
}
