package com.intel.mtwilson.security.http;

import com.intel.dcsg.cpg.crypto.HmacCredential;
import com.intel.dcsg.cpg.iso8601.Iso8601Date;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Symmetric/shared-secret signatures for Mt Wilson 0.5.1 in Authorization header using custom "MtWilson" scheme
 * This class requires the following libraries:
 * org.apache.commons.codec.binary.Base64 from commons-codec
 * org.apache.commons.lang.StringUtils from commons-lang
 * @since 0.5.1
 * @author jbuhacoff
 */
public class HmacAuthorization {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private HmacCredential credentials;
    private String signatureMethod;
    private String realm = null;
    
    /*
    public RequestAuthorization(Credentials credentials) {
        this(new String(credentials.identity()), "", "SHA256"); 
    }*/
    
    public HmacAuthorization(HmacCredential credentials) {
        this(credentials,"HmacSHA256");
    }

    public HmacAuthorization(HmacCredential credentials, String signatureMethod) { 
        this.credentials = credentials;
        // this just accommodates some common variations on the names of known algorithms
        HashMap<String,String> algorithms = new HashMap<String,String>();
        algorithms.put("HmacSHA256", "HmacSHA256");
        algorithms.put("HMAC-SHA256", "HmacSHA256");

        if( algorithms.containsKey(signatureMethod) ) {
            this.signatureMethod = algorithms.get(signatureMethod);
        }
        else {
            this.signatureMethod = signatureMethod;
        }
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
    public String getAuthorizationQuietly(String httpMethod, String requestUrl) {
        return getAuthorizationQuietly(httpMethod, requestUrl, null, null);
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
    public String getAuthorizationQuietly(String httpMethod, String requestUrl, String requestBody) {
        return getAuthorizationQuietly(httpMethod, requestUrl, null, requestBody);
    }

    /**
     * Use this method to sign requests that do have a message body and URL
     * parameters. 
     * @param httpMethod the string "GET" or "DELETE"
     * @param requestUrl the URL to access
     * @return content of the Authorization header to add to the request
     */
    public String getAuthorizationQuietly(String httpMethod, String requestUrl, Map<String,Object> urlParams, String requestBody) {
        try {
            return getAuthorization(httpMethod, requestUrl, urlParams, requestBody);
        }
        catch(NoSuchAlgorithmException e) {
            log.error("Algorithm not available: "+e.getMessage(), e);
        }
        catch(InvalidKeyException e) {
            log.error("Password not suitable for signature: "+e.getMessage(), e);
        }
        catch(IOException e) {
            log.error("Error creating signature: "+e.getMessage(), e);
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
    public String getAuthorization(String httpMethod, String requestUrl) throws NoSuchAlgorithmException, InvalidKeyException, IOException  {
        return getAuthorization(httpMethod, requestUrl, null, null);
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
    public String getAuthorization(String httpMethod, String requestUrl, String requestBody) throws NoSuchAlgorithmException, InvalidKeyException, IOException  {
        return getAuthorization(httpMethod, requestUrl, null, requestBody);
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
    public String getAuthorization(String httpMethod, String requestUrl, Map<String,Object> urlParams) throws NoSuchAlgorithmException, InvalidKeyException, IOException  {
        return getAuthorization(httpMethod, requestUrl, urlParams, null);
    }
    
    /**
     * Generates the content for the Authorization header, using the 
     * Signature format:   client-token : nonce : signature
     * 
     * If the urlParams are given, they will be added to the request URL in 
     * ascending alphabetical order. If any parameter is multi-valued, its values
     * will be listed in ascending alphabetical order.
     * 
     * @param httpMethod such as "GET" or "POST"
     * @param requestUrl complete URL such as https://example.com/some/path or /some/path
     * @param urlParams a Map containing keys of type String and values of either type String or String[]
     * @param requestBody only required if you are sending POST or PUT message body; can be null (will be converted to empty string)
     * @return
     * @throws NoSuchAlgorithmException if your environment is missing the HmacSHA256 algorithm
     * @throws InvalidKeyException if the secretKey value you provided to the constructor is not suitable for use with HmacSHA256
     * @throws IOException if there was a problem generating the nonce
     */
    public String getAuthorization(String httpMethod, String requestUrl, Map<String,Object> urlParams, String requestBody) throws NoSuchAlgorithmException, InvalidKeyException, IOException  {
        String nonce = new String(Base64.encodeBase64(nonce()));
        String username = new String(Base64.encodeBase64(credentials.identity()));
        String timestamp = new Iso8601Date(new Date(System.currentTimeMillis())).toString();
        
        HmacSignatureInput signatureBlock = new HmacSignatureInput();
        signatureBlock.httpMethod = httpMethod;
        signatureBlock.absoluteUrl = new HttpRequestURL(requestUrl,urlParams).toString();
        signatureBlock.fromToken = username;
        signatureBlock.nonce = nonce;
        signatureBlock.body = requestBody;
        signatureBlock.signatureMethod = signatureMethod;
        signatureBlock.timestamp = timestamp;
        String content = signatureBlock.toString();
        
        //log.debug("signed content follows... ("+content.length()+") \n"+content);
        String signature = sign(content); 
        String authorization = String.format("MtWilson %s", headerParams( httpMethod,  signatureBlock.absoluteUrl,  username,  nonce,  signatureMethod,  timestamp,  realm,  signature));
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
    private String headerParams(String httpMethod, String absoluteUrl, String fromToken, String nonce, String signatureMethod, String timestamp, String realm, String signature) {
        String[] input = new String[] { httpMethod,   absoluteUrl,  fromToken,  nonce,  signatureMethod,   timestamp,  realm,  signature };
        String[] label = new String[] {"http_method","uri",        "username", "nonce","signature_method","timestamp","realm","signature"};
        ArrayList<String> errors = new ArrayList<String>();
        ArrayList<String> params = new ArrayList<String>();
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
        
    /**
    * Given arbitrary text, returns a base64-encoded version of the text's HMAC using the
    * previously specified secret key.
    * 
    * The text is decoded as UTF-8 for hashing, and the result is base64-encoded.
    * 
    * @param text
    * @return
    * @throws NoSuchAlgorithmException if your environment is missing the HmacSHA256 algorithm
    * @throws InvalidKeyException if the secretKey value you provided to the constructor is not suitable for use with HmacSHA256
    */
    private String sign(String text) throws NoSuchAlgorithmException,InvalidKeyException, UnsupportedEncodingException {
        return new String(Base64.encodeBase64(credentials.signature(text.getBytes("UTF-8"))));
    }

}
