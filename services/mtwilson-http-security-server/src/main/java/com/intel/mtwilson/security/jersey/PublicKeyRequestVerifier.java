package com.intel.mtwilson.security.jersey;

import com.intel.mtwilson.security.core.PublicKeyUserFinder;
import com.intel.mtwilson.security.core.PublicKeyUserInfo;
import com.intel.mtwilson.security.http.RsaSignatureInput;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.mtwilson.model.Md5Digest;
//import com.sun.jersey.core.header.HttpDateFormat;
import org.glassfish.jersey.message.internal.HttpDateFormat;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.cert.Certificate;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class requires the following libaries:
 * org.apache.commons.codec.binary.Base64 from commons-codec
 * 
 * It implements the server side authorization of incoming API requests using
 * the HTTP "Authorization" header. The authentication scheme supported is "PublicKey"
 * which is an asymmetric-key scheme using RSA to sign requests.
 * 
 * See also PublicKeyRequestVerifier/MtWilsonAuthorization
 * 
 * @since 0.5.2
 * @author jbuhacoff
 */
public class PublicKeyRequestVerifier {
    private static Logger log = LoggerFactory.getLogger(PublicKeyRequestVerifier.class);
    private PublicKeyUserFinder finder;
    private int requestsExpireAfterMs = 60 * 60 * 1000; // 60 minutes
    
    private String headerAttributeNameValuePair = "([a-zA-Z0-9_-]+)=\"([^\"]+)\"";
    private Pattern headerAttributeNameValuePairPattern = Pattern.compile(headerAttributeNameValuePair);
    
    public PublicKeyRequestVerifier(PublicKeyUserFinder finder) {
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
    public User getUserForRequest(String httpMethod, String requestUrl, MultivaluedMap<String,String> headers,  String requestBody) throws UnsupportedEncodingException, CryptographyException {
        String authorizationHeader = headers.getFirst("Authorization");
//        try {
            Authorization a = parseAuthorization(authorizationHeader);
            
            log.info("PublicKeyAuthorization: Request timestamp ok");
            RsaSignatureInput signatureBlock = new RsaSignatureInput();
            
            signatureBlock.httpMethod = a.httpMethod;
            
            /*
             * Bug #383 disabling support for this because it creates a security vulnerability
            if( headers.containsKey("X-Original-URL") ) {
                signatureBlock.url = headers.getFirst("X-Original-URL");
            }
            else if( headers.containsKey("X-Original-Request") ) {
                signatureBlock.url = headers.getFirst("X-Original-Request");            
            }
            else {
                signatureBlock.url = a.url;
            }
            */
            signatureBlock.url = a.url;
            
            signatureBlock.realm = a.realm;
            signatureBlock.fingerprintBase64 = a.fingerprintBase64;
            signatureBlock.signatureAlgorithm = a.signatureAlgorithm;
            signatureBlock.headerNames = a.headerNames;
            HashMap<String,String> headerValues = new HashMap<String,String>();
            if( a.headerNames != null ) {
                for(String headerName : a.headerNames) {
                    headerValues.put(headerName, headers.getFirst(headerName));
                }
            }
            signatureBlock.headers = headerValues;
            signatureBlock.body = requestBody;
            String content = signatureBlock.toString(); // may throw IllegalArgumentException if any required field is null or invalid

            //log.debug("PublicKeyAuthorization: Signed content ("+content.length()+") follows:\n"+content);
            
            // locate the public key or x509 certificate that can verify the signature
            byte[] document = content.getBytes("UTF-8");
            byte[] signature = Base64.decodeBase64(a.signatureBase64);
            String signatureAlgorithm = signatureAlgorithm(a.signatureAlgorithm);
            byte[] fingerprint = Base64.decodeBase64(a.fingerprintBase64);
            PublicKeyUserInfo userInfo = finder.getUserForIdentity(fingerprint);

            log.debug("PublicKeyAuthorization Fingerprint: "+a.fingerprintBase64);
            log.debug("PublicKeyAuthorization:  Signature: "+a.signatureBase64);
            log.debug("PublicKeyAuthorization:  Algorithm: "+a.signatureAlgorithm);
            
            
            if( userInfo == null ) { 
                log.error("PublicKeyAuthorization cannot find user with fingerprint "+a.fingerprintBase64);
                return null;
            }
            
            boolean isValid = false;
            if( userInfo.publicKey != null ) {
                try {
                    isValid = verifySignature(document, userInfo.publicKey, signatureAlgorithm, signature);
                    log.debug("PublicKeyAuthorization verified signature using public key; result= "+isValid);
                } catch (NoSuchAlgorithmException ex) {
                    throw new CryptographyException("Signature algorithm not supported: "+signatureAlgorithm, ex);
                } catch (InvalidKeyException ex) {
                    throw new CryptographyException("Invalid key in certificate: "+ex.getMessage(), ex);
                } catch (SignatureException ex) {
                    throw new CryptographyException("Unable to verify signature: "+ex.getMessage(), ex);
                }
            }
            

            // show a warning if actual URL doesn't match signed URL, because it means we need to be careful when
            // routing and authorizing the requested actions... must be done according to signed URL, not actual URL
            if( a.url == null || !a.url.equals(requestUrl) ) {
                log.warn("PublicKeyAuthorization: Actual URL did not match Signed URL");
                log.debug("  Actual URL: "+requestUrl);
                log.debug("  Signed URL: "+a.url);
            }
            

            if( isValid ) {
                log.info("Request is authenticated");
                
                // check if the request has expired by looking at the HTTP Date header, but only if it was signed.
                if( signatureBlock.headers.containsKey("Date") ) {
                    try {
                        Date requestDate = HttpDateFormat.readDate(signatureBlock.headers.get("Date")); // http date must be in RFC 1123 date format (as specified by email RFC 822 and http RFC 2616)
                        if( isRequestExpired(requestDate) ) {
                            log.error("PublicKeyAuthorization: Request expired; date="+requestDate);
                            return null;
                        }
                    }
                    catch(ParseException e) {
                        throw new IllegalArgumentException("Authorization timestamp must conform to ISO 8601 format", e);
                    }
                }

                
                return new User(a.fingerprintBase64, userInfo.roles, "", Md5Digest.valueOf(signature));
            }
        /*}
        catch (ParseException e) {
            log.error("Request authorization timestamp must conform to ISO 8601 format", e);
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
            log.warn(String.format("Request expired (%s)", DurationFormatUtils.formatDurationHMS(diff)));
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
    public boolean isSignatureValid(String httpMethod, String requestUrl, MultivaluedMap<String,String> headers, String requestBody) throws UnsupportedEncodingException, CryptographyException {
        User user = getUserForRequest(httpMethod, requestUrl, headers, requestBody);
        return user != null;
    }
    
    /**
     * 
     * Authorization header format is like this:   "Signature" *<SP <attribute-name "=" quoted-attribute-value>>
     * 
     * Sample Authorization header:
     * 
Authorization: MtWilson
  realm="Example", 
  username="0685bd9184jfhq22",
  http_method="GET",
  uri="/reports/trust?hostName=example",
  timestamp="2012-02-14T08:15:00PST", 
  nonce="4572616e48616d6d65724c61686176",
  signature_method="HMAC-SHA256", 
  signature="wOJIO9A2W5mFwDgiDvZbTSMK%2FPY%3D"
     * 
     * @param authorizationHeader
     * @return 
     */
    private Authorization parseAuthorization(String authorizationHeader) {
         Authorization a = new Authorization();
         // splitting on spaces should yield "MtWilson" followed by attribute name-value pairs
        String[] terms = authorizationHeader.split(" ");
        if( !"PublicKey".equals(terms[0]) ) { throw new IllegalArgumentException("Authorization type is not PublicKey"); }
        for(int i=1; i<terms.length; i++) {
            // each term after "PublicKey" is an attribute name-value pair, like realm="Example"
            Matcher attributeNameValue = headerAttributeNameValuePairPattern.matcher(terms[i]);
            if( attributeNameValue.find() ) {
                String name = attributeNameValue.group(1);
                String value = attributeNameValue.group(2);
                if( name.equals("realm") ) { a.realm = value; }
                if( name.equals("fingerprint") ) { a.fingerprintBase64 = value; }
                if( name.equals("headers") ) { a.headerNames = value.split(","); }
                if( name.equals("algorithm") || name.equals("digest") ) { a.signatureAlgorithm = value; }
                if( name.equals("signature") ) { a.signatureBase64 = value; }
            }
        }
        return a;
    }
    
    


    private boolean verifySignature(byte[] document, PublicKey publicKey, String signatureAlgorithm, byte[] signature) throws NoSuchAlgorithmException,InvalidKeyException, SignatureException {
        Signature rsa = Signature.getInstance(signatureAlgorithm); 
        rsa.initVerify(publicKey);
        rsa.update(document);
        return rsa.verify(signature);
    }
    // commenting out unused function (6/11 1.2)
    /*
    private boolean verifySignature(byte[] document, Certificate certificate, String signatureAlgorithm, byte[] signature) throws NoSuchAlgorithmException,InvalidKeyException, SignatureException {
        Signature rsa = Signature.getInstance(signatureAlgorithm); 
        rsa.initVerify(certificate);
        rsa.update(document);
        return rsa.verify(signature);
    }
    */
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
        public String httpMethod;
        public String url;
        public String realm;
        public String fingerprintBase64;
        public String[] headerNames;
        public String signatureAlgorithm;
        public String signatureBase64;
    }
    
}
