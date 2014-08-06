package com.intel.mtwilson.security.http;

import com.intel.dcsg.cpg.crypto.RsaCredential;
import com.intel.dcsg.cpg.rfc822.Rfc822Date;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RSA-based signatures for Mt Wilson 0.5.2 in Authorization header using custom "PublicKey" and "X509" scheme
 * This class requires the following libraries:
 * org.apache.commons.codec.binary.Base64 from commons-codec
 * org.apache.commons.lang.StringUtils from commons-lang
 * @since 0.5.2
 * @author jbuhacoff
 */
public class RsaAuthorization {
    private static Logger log = LoggerFactory.getLogger(RsaAuthorization.class);
    private RsaCredential credential;
    private String realm = null;
    
    public RsaAuthorization(RsaCredential credential) { 
        this.credential = credential;
    }
    
    public void setRealm(String realmName) {
    	realm = realmName;
    }
    
    /**
     * Use this method to sign requests that do not have a message body,
     * such as GET and DELETE.
     * @param httpMethod the string "GET" or "DELETE"
     * @param requestUrl the URL to access
     * @return content of the Authorization header to add to the request
     */
    public String getAuthorizationQuietly(String httpMethod, String requestUrl, Map<String,String> headers) throws SignatureException {
        return getAuthorizationQuietly(httpMethod, requestUrl, null, headers, null);
    }
    
    /**
     * Use this method to sign requests that have a message body,
     * such as PUT and POST. You can also use it to sign requests with
     * no message body by passing null for the requestBody parameter.
     * @param httpMethod the string "GET" or "DELETE"
     * @param requestUrl the URL to access
     * @param requestBody the body of the request
     * @return content of the Authorization header to add to the request
     */
    public String getAuthorizationQuietly(String httpMethod, String requestUrl, Map<String,String> headers, String requestBody) throws SignatureException {
        return getAuthorizationQuietly(httpMethod, requestUrl, null, headers, requestBody);
    }

    /**
     * Use this method to sign requests that do have a message body and URL
     * parameters. 
     * @param httpMethod the string "GET" or "DELETE"
     * @param requestUrl the URL to access
     * @return content of the Authorization header to add to the request
     */
    public String getAuthorizationQuietly(String httpMethod, String requestUrl, Map<String,Object> urlParams, Map<String,String> headers, String requestBody) throws SignatureException {
        try {
            return getAuthorization(httpMethod, requestUrl, urlParams, headers, requestBody);
        }
        catch(NoSuchAlgorithmException e) {
            log.error("Algorithm not available: "+e.getMessage());
        }
        catch(InvalidKeyException e) {
            log.error("Password not suitable for signature: "+e.getMessage());
        }
        catch(IOException e) {
            log.error("Error creating signature: "+e.getMessage());
        }
        return null;
    }

    /**
     * Generates the content for the Authorization header, using the 
     * Signature format:   client-token : nonce : signature
     * 
     * @param httpMethod such as "GET" or "POST"
     * @param requestUrl complete URL such as https://example.com/some/path
     * @return
     * @throws NoSuchAlgorithmException if your environment is missing the HmacSHA256 algorithm
     * @throws InvalidKeyException if the secretKey value you provided to the constructor is not suitable for use with HmacSHA256
     * @throws IOException if there was a problem generating the nonce
     */
    public String getAuthorization(String httpMethod, String requestUrl, Map<String,String> headers) throws NoSuchAlgorithmException, InvalidKeyException, IOException, SignatureException  {
        return getAuthorization(httpMethod, requestUrl, null, headers, null);
    }
    
    /**
     * Generates the content for the Authorization header, using the 
     * Signature format:   client-token : nonce : signature
     * 
     * @param httpMethod such as "GET" or "POST"
     * @param requestUrl complete URL such as https://example.com/some/path
     * @param requestBody only required if you are sending POST or PUT message body; can be null (will be converted to empty string)
     * @return
     * @throws NoSuchAlgorithmException if your environment is missing the HmacSHA256 algorithm
     * @throws InvalidKeyException if the secretKey value you provided to the constructor is not suitable for use with HmacSHA256
     * @throws IOException if there was a problem generating the nonce
     */
    public String getAuthorization(String httpMethod, String requestUrl, Map<String,String> headers, String requestBody) throws NoSuchAlgorithmException, InvalidKeyException, IOException, SignatureException  {
        return getAuthorization(httpMethod, requestUrl, null, headers, requestBody);
    }
    
    /**
     * Generates the content for the Authorization header, using the 
     * Signature format:   client-token : nonce : signature
     * 
     * @param httpMethod such as "GET" or "POST"
     * @param requestUrl complete URL such as https://example.com/some/path
     * @param urlParams a key-value map representing url parameters; the value can be null, or a single string value, a string array, or a list of strings
     * @return
     * @throws NoSuchAlgorithmException if your environment is missing the HmacSHA256 algorithm
     * @throws InvalidKeyException if the secretKey value you provided to the constructor is not suitable for use with HmacSHA256
     * @throws IOException if there was a problem generating the nonce
     */
    public String getAuthorization(String httpMethod, String requestUrl, Map<String,Object> urlParams, Map<String,String> headers) throws NoSuchAlgorithmException, InvalidKeyException, IOException, SignatureException  {
        return getAuthorization(httpMethod, requestUrl, urlParams, headers, null);
    }
    
    /**
     * Generates the content for the Authorization header, using the authentication
     * scheme "PublicKey" or "X509".
     * 
     * The nonce and identity/fingerprint are base64-encoded with no wrapping/chunking. 
     * Implementation note: must use new String(Base64.encodeBase64(...)) to
     * avoid the wrapping. The Base.encodeBase64String automatically wraps.
     * 
     * If the urlParams are given, they will be added to the request URL in 
     * ascending alphabetical order. If any parameter is multi-valued, its values
     * will be listed in ascending alphabetical order.
     * 
     * @param httpMethod such as "GET" or "POST"
     * @param requestUrl complete URL such as https://example.com/some/path or /some/path
     * @param urlParams a Map containing keys of type String and values of either type String or String[]
     * @param headers a Map which is used for INPUT OR OUTPUT, it will contain the "Date" header to be added to the request; or if it already contains a "Date" header that date will be used.  Also the nonce will be added to this map with the key "X-Nonce".  Both "Date" and "X-Nonce" must be added to the http request and sent to the server!
     * @param requestBody only required if you are sending POST or PUT message body; can be null (will be converted to empty string)
     * @return
     * @throws NoSuchAlgorithmException if your environment is missing the HmacSHA256 algorithm
     * @throws InvalidKeyException if the secretKey value you provided to the constructor is not suitable for use with HmacSHA256
     * @throws IOException if there was a problem generating the nonce
     */
    public String getAuthorization(String httpMethod, String requestUrl, Map<String,Object> urlParams, Map<String,String> headers, String requestBody) throws NoSuchAlgorithmException, InvalidKeyException, IOException, SignatureException  {
        String nonce = new String(Base64.encodeBase64(nonce()));
        headers.put("X-Nonce", nonce);
        
        String username = new String(Base64.encodeBase64(credential.identity()));
        //String timestamp = ISO8601.DATETIME.format(System.currentTimeMillis());
        String timestamp;
        if( headers.containsKey("Date") ) {
            timestamp = headers.get("Date");
            log.debug("request already contains date header: {}", timestamp);
        }
        else {
            timestamp = Rfc822Date.format(new Date());    
            log.debug("creating new date header for request: {}", timestamp);
            headers.put("Date", timestamp);
        }
        
        RsaSignatureInput signatureBlock = new RsaSignatureInput();
        signatureBlock.httpMethod = httpMethod;
        signatureBlock.url = new HttpRequestURL(requestUrl,urlParams).toString();
        signatureBlock.fingerprintBase64 = username;
        signatureBlock.body = requestBody;
        log.debug("signature input body is {} bytes.", (signatureBlock.body==null?0:signatureBlock.body.length()));
        signatureBlock.signatureAlgorithm = credential.algorithm();
        
        signatureBlock.headers = headers;
        signatureBlock.headerNames = new String[] { "X-Nonce", "Date" };

        String content = signatureBlock.toString();        
        log.debug("signed content is {} bytes.", content.length());
        
        byte[] signature = credential.signature(content.getBytes("UTF-8"));
        String signatureBase64 = new String(Base64.encodeBase64(signature));
        
        // right now we're sending it as "X509" instead of "PublicKey" because
        // we're using a certificate generated by java keytool and the signature
        // includes the SHA256withRSA algorithm OID, which is a java specific format
        // for rsa signatures, and pure public key signatures don't have that.
        String authorization = String.format("X509 %s", headerParams( realm, username, signatureBlock.headerNames, signatureBlock.signatureAlgorithm,  signatureBase64));
        //log.debug("authorization: "+authorization);
        return authorization;
    }
    

    /**
    * Generates a 24-byte nonce comprised of 8 bytes current time (milliseconds) and 16 bytes random data.
    * @return
    * @throws IOException if there was a problem generating the nonce
    */
    private byte[] nonce() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        long currentTime = System.currentTimeMillis();
        dos.writeLong(currentTime);

        SecureRandom r = new SecureRandom();
        byte[] nonce = new byte[16];
        r.nextBytes(nonce);
        dos.write(nonce);

        dos.flush();
        //byte[] noncedata = bos.toByteArray(); // should be 8 bytes timestamp + 16 bytes random numbers
        //System.out.println("nonce data length = "+noncedata.length);
        //assert noncedata.length == 24;

        dos.close();
        return bos.toByteArray();
    }
    

    /**
     * Generates the parameters of the Authorization header.
     * Sample output (with newlines for clarity - actual output is one line)
     * 
  username="0685bd9184jfhq22",
  httpMethod="GET",
  uri="/reports/trust?hostName=example",
  timestamp="2012-02-14T08:15:00PST", 
  nonce="4572616e48616d6d65724c61686176",
  signature_method="HMAC-SHA256", 
  signature="wOJIO9A2W5mFwDgiDvZbTSMK%2FPY%3D"
     * 
     * If any parameter is null, then it is not included in the output. The
     * sample output above was generated with realm=null so the realm attribute
     * was not included.
     * 
     * @param httpMethod
     * @param absoluteUrl
     * @param fromToken
     * @param nonce
     * @param signatureMethod
     * @param timestamp
     * @return 
     */
    // realm, username, signatureBlock.headerNames, signatureBlock.signatureAlgorithm,  signature
    private String headerParams(String realm, String username, String[] headerNames, String signatureAlgorithm, String signatureBase64) {
        String headerNamesCSV = StringUtils.join(headerNames, ",");
        String[] input = new String[] { realm,   username,  headerNamesCSV,  signatureAlgorithm,  signatureBase64 };
        String[] label = new String[] {"realm", "fingerprint", "headers", "algorithm", "signature"};
        ArrayList<String> errors = new ArrayList<>();
        ArrayList<String> params = new ArrayList<>();
        for(int i=0; i<input.length; i++) {
            if( input[i] != null && input[i].contains("\"") ) { errors.add(String.format("%s contains quotes", label[i])); }
            if( input[i] != null ) { params.add(String.format("%s=\"%s\"", label[i], encodeHeaderAttributeValue(input[i]))); }
        }
        if( !errors.isEmpty() ) { throw new IllegalArgumentException("Cannot create authorization header: "+StringUtils.join(errors, ", ")); }
        return StringUtils.join(params, ", ");
    }
    
    /**
     * Encodes a string for use as an attribute value in the Authorization header.
     * None of the values should include quotes. URL should be URL-encoded, and
     * none of the other values allow quotes in their formats with the exception
     * of "realm" and "username" which are application dependent but which we 
     * define as not including quotes. 
     * So, instead of encoding here, we throw errors (see headerParams function) 
     * when a value contains a quote or newline character.
     * @param value
     * @return 
     */
    private String encodeHeaderAttributeValue(String value) {
        return value;
    }
        
}
