package com.intel.mtwilson.security.jersey;

import com.intel.mtwilson.datatypes.Role;
import com.intel.mtwilson.security.core.SecretKeyFinder;
import com.intel.mtwilson.security.http.HmacSignatureInput;
import com.intel.dcsg.cpg.iso8601.Iso8601Date;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.mtwilson.model.Md5Digest;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
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
 * the HTTP "Authorization" header. The authentication scheme supported is "MtWilson"
 * which is a symmetric-key scheme using HMAC-SHA256 to sign requests.
 * 
 * See also PublicKeyAuthorization
 * 
 * @since 0.5.2
 * @author jbuhacoff
 */
public class HmacRequestVerifier {
    private static Logger log = LoggerFactory.getLogger(HmacRequestVerifier.class);
    private SecretKeyFinder finder;
    private int requestsExpireAfterMs = 60 * 60 * 1000; // 60 minutes
    
    private String headerAttributeNameValuePair = "([a-zA-Z0-9_-]+)=\"([^\"]+)\"";
    private Pattern headerAttributeNameValuePairPattern = Pattern.compile(headerAttributeNameValuePair);
    
    private boolean enforceSameHttpMethod = true;
    private boolean enforceSameQueryString = true;
    private boolean enforceSameURL = false;
    
    private final String SIGNATURE_ALGORITHM = "HmacSHA256";
    
    public HmacRequestVerifier(SecretKeyFinder finder) {
        this.finder = finder;
        
    }
    
    public void setEnforceSameHttpMethod(boolean enabled) {
    	enforceSameHttpMethod = enabled;
    }
    public void setEnforceSameQueryString(boolean enabled) {
    	enforceSameQueryString = enabled;
    }
    public void setEnforceSameURL(boolean enabled) {
    	enforceSameURL = enabled;
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
     * @param authorizationHeader the Authorization header from the request
     * @param requestBody the body of the request, or null
     * @return a User object if the signature on the request is valid for a known user, or null if it's invalid or user cannot be found
     * 
     */
    public User getUserForRequest(String httpMethod, String requestUrl, MultivaluedMap<String,String> headers,  String requestBody) throws CryptographyException {
        String authorizationHeader = headers.getFirst("Authorization");
//        try {
            Authorization a = parseAuthorization(authorizationHeader);
            
            log.info("VerifyAuthorization: Request timestamp ok");
            HmacSignatureInput signatureBlock = new HmacSignatureInput();
            signatureBlock.httpMethod = a.httpMethod;
            signatureBlock.absoluteUrl = a.url;
            signatureBlock.fromToken = a.username;
            signatureBlock.nonce = a.nonce;
            signatureBlock.body = requestBody;
            signatureBlock.signatureMethod = a.signatureMethod;
            signatureBlock.timestamp = a.timestamp;
            String content = signatureBlock.toString(); // may throw IllegalArgumentException if any required field is null or invalid

            //log.debug("VerifyAuthorization: Signed content ("+content.length()+") follows:\n"+content);
            String username = new String(Base64.decodeBase64(a.username));
            String secretKey = finder.getSecretKeyForUserId(username);
            String signature;
            try {
                signature = sign(content, secretKey); // may throw NoSuchAlgorithmException, InvalidKeyException
            } catch (NoSuchAlgorithmException ex) {
                throw new CryptographyException("Signature algorithm not supported: "+SIGNATURE_ALGORITHM, ex);
            } catch (InvalidKeyException ex) {
                throw new CryptographyException("Invalid key in signature: "+ex.getMessage(), ex);
            }

            log.debug("VerifyAuthorization:   Username: "+username);
            log.debug("VerifyAuthorization:  Signature: "+signature);
            // REST protection:  make sure the actual HTTP method matches the one that was used to create the signature.
            // We don't verify the full URL right now because it's legal for a proxy to change it... we'd have to allow
            // for a configurable "authorized rewriting" in order to verify it. 
            // Or, we can verify it and there will be errors if a proxy rewrites it, which might be ok  ???
            if( enforceSameHttpMethod ) {
            	if( a.httpMethod == null ) { return null; }
            	else if( !a.httpMethod.equals(httpMethod) ) {
                    log.info("Actual HTTP method did not match Signed HTTP method");
                    log.debug("  Actual method: "+httpMethod);
                    log.debug("  Signed method: "+a.httpMethod);
                    throw new CryptographyException("Request HTTP method did not match Signed HTTP method");
            	}
            }
            
            if( enforceSameURL ) {
            	if( a.url == null ) { return null; }
            	else if( !a.url.equals(requestUrl) ) {
	                log.warn("VerifyAuthorization: Actual URL did not match Signed URL");
	                log.debug("  Actual URL: "+requestUrl);
	                log.debug("  Signed URL: "+a.url);
                        throw new CryptographyException("Request URL method did not match Signed URL");
            	}
            }
            
            if( enforceSameQueryString ) {
                // not implemented yet... need to parse both URL's and compare the query strings. 
            	log.warn("VerifyAuthorization: enforceSameQueryString is enabled but not implemented yet");
            }
            
            if( signature.equals(a.signature) ) {
                log.info("Request is authenticated");
                
                try {
                    if( signatureBlock.timestamp == null || isRequestExpired(signatureBlock.timestamp) ) { // may throw ParseException
                        return null;
                    }
                }
                catch(ParseException e) {
                        throw new IllegalArgumentException("Date is not in ISO8601 format: "+signatureBlock.timestamp, e);
                }
                
                return new User(username, new Role[] { Role.Attestation, Role.Whitelist }, username,  Md5Digest.valueOf(Base64.decodeBase64(signature))); 
            }
    /*
        }
        catch (ParseException e) {
            log.error("Request authorization timestamp must conform to ISO 8601 format", e);
        }        
        catch (IllegalArgumentException e) {
            log.error("Required parameters are missing or invalid", e);
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
        log.info("Request is NOT AUTHENTICATED");
        return null;
    }
    
    private boolean isRequestExpired(String timestamp) throws ParseException {
        // request expiration policy
        Calendar expirationTime = Calendar.getInstance();
        expirationTime.setTime(Iso8601Date.valueOf(timestamp));
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
     * @param authorizationHeader the Authorization header from the request
     * @param requestBody the body of the request, or null
     * @return true if the signature on the request is valid
     */
    public boolean isSignatureValid(String httpMethod, String requestUrl, MultivaluedMap<String,String> headers, String requestBody) throws CryptographyException {
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
        if( !"MtWilson".equals(terms[0]) ) { throw new IllegalArgumentException("Authorization type is not MtWilson"); }
        for(int i=1; i<terms.length; i++) {
            // each term after "MtWilson" is an attribute name-value pair, like realm="Example"
            Matcher attributeNameValue = headerAttributeNameValuePairPattern.matcher(terms[i]);
            if( attributeNameValue.find() ) {
                String name = attributeNameValue.group(1);
                String value = attributeNameValue.group(2);
                if( name.equals("realm") ) { a.realm = value; }
                if( name.equals("username") ) { a.username = value; }
                if( name.equals("nonce") ) { a.nonce = value; }
                if( name.equals("http_method") ) { a.httpMethod = value; }
                if( name.equals("uri") ) { a.url = value; }
                if( name.equals("timestamp") ) { a.timestamp = value; }
                if( name.equals("signature") ) { a.signature = value; }
                if( name.equals("signature_method") ) { a.signatureMethod = value; }
            }
        }
        return a;
    }
    
    


    /**
        * Given arbitrary text, returns a base64-encoded version of the text's HMAC using the
        * previously specified secret key.
        * 
        * @param text
        * @return
        * @throws NoSuchAlgorithmException
        * @throws InvalidKeyException 
        */
    private String sign(String text, String secretKey) throws NoSuchAlgorithmException,InvalidKeyException {
        SecretKeySpec key = new SecretKeySpec(secretKey.getBytes(), SIGNATURE_ALGORITHM);
        Mac mac = Mac.getInstance(SIGNATURE_ALGORITHM);
        mac.init(key);
        return new String(Base64.encodeBase64(mac.doFinal(text.getBytes())));
    }

    public static class Authorization {
        public String realm;
        public String username;
        public String nonce;
        public String signature;
        public String signatureMethod;
        public String httpMethod;
        public String url;
        public String timestamp;
    }
    
}
