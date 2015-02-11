/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package test.io;

import com.intel.mtwilson.ApacheHttpClient;
import com.intel.mtwilson.ApiClient;
import com.intel.mtwilson.api.*;
import com.intel.mtwilson.HtmlErrorParser;
import com.intel.mtwilson.MultivaluedMapImpl;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.RsaCredential;
import com.intel.dcsg.cpg.crypto.RsaCredentialX509;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.mtwilson.i18n.ErrorCode;
import com.intel.mtwilson.datatypes.ErrorResponse;
import com.intel.mtwilson.datatypes.HostTrustResponse;
import com.intel.mtwilson.datatypes.HostTrustStatus;
import com.intel.dcsg.cpg.configuration.CommonsConfigurationUtil;
import com.intel.mtwilson.model.*;
import com.intel.dcsg.cpg.rfc822.Rfc822Date;
import com.intel.dcsg.cpg.tls.policy.TlsUtil;
import com.intel.mtwilson.security.http.apache.ApacheHttpAuthorization;
import com.intel.mtwilson.security.http.HttpRequestURL;
import com.intel.mtwilson.security.http.RsaSignatureInput;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import static javax.ws.rs.core.MediaType.*;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
//import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class SecurityTest {
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    @Test
    public void testSaveSslCertificate() throws IOException, KeyManagementException, CryptographyException  {
        Configuration config = CommonsConfigurationUtil.fromResource("/localhost-0.5.2.properties");
//        ApiClient api = new ApiClient(config);
        SimpleKeystore keystore = new SimpleKeystore(new File(config.getString("mtwilson.api.keystore")), config.getString("mtwilson.api.keystore.password"));
        TlsUtil.addSslCertificatesToKeystore(keystore, new URL(config.getString("mtwilson.api.baseurl")));
//        keystore.save();
    }
    /*
    @Test
    public void testGetSamlCertificate() throws NoSuchAlgorithmException, KeyManagementException, MalformedURLException, KeyStoreException, IOException, CertificateException, UnrecoverableEntryException, ApiException, SignatureException {
        Configuration config = ConfigurationFactory.fromResource("/localhost-0.5.2.properties");
        ApiClient api = new ApiClient(config);
        X509Certificate certificate = api.getSamlCertificate();
        log.debug("SAML Certificate Subject: {}", certificate.getSubjectX500Principal().getName());
        log.debug("SAML Certificate Issuer: {}", certificate.getIssuerX500Principal().getName());
//        URL attestationService = new URL(config.getString("mtwilson.api.baseurl"));
//        SimpleKeystore keystore = new SimpleKeystore(new File(config.getString("mtwilson.api.keystore")), config.getString("mtwilson.api.keystore.password"));
//        keystore.addTrustedSamlCertificate(certificate, attestationService.getHost());
//        keystore.save();
//        log.debug("Saved SAML certificate in keystore");
    }
    */
    
    @Test
    public void testUnregisteredClient() throws IOException, NoSuchAlgorithmException, CryptographyException, CertificateEncodingException, KeyManagementException, ClientException, ApiException, SignatureException  {
        Configuration config = CommonsConfigurationUtil.fromResource("/localhost-0.5.2.properties");
        // create a new keypair to ensure it's not registered
        KeyPair keypair = RsaUtil.generateRsaKeyPair(RsaUtil.MINIMUM_RSA_KEY_SIZE);
        X509Certificate certificate = RsaUtil.generateX509Certificate("CN=unregistered", keypair, 1);
        RsaCredentialX509 credential = new RsaCredentialX509(keypair.getPrivate(), certificate);
        // use the keystore just because it already has the server ssl cert
        SimpleKeystore keystore = new SimpleKeystore(new File(config.getString("mtwilson.api.keystore")), config.getString("mtwilson.api.keystore.password"));
        
        ApiClient api = new ApiClient(new URL(config.getString("mtwilson.api.baseurl")), credential, keystore, config);
        api.getHostTrust(new Hostname("1.2.3.4")); // hostname doesn't matter since the request should be rejected by the security filter
    }
    
    @Test
    public void testInvalidAuthorizationHeader() throws IOException, KeyManagementException, FileNotFoundException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableEntryException, CertificateEncodingException, ApiException, SignatureException, com.intel.dcsg.cpg.crypto.CryptographyException {
        Configuration config = CommonsConfigurationUtil.fromResource("/localhost-0.5.2.properties");
        // use the keystore just because it already has the server ssl cert
        SimpleKeystore keystore = new SimpleKeystore(new File(config.getString("mtwilson.api.keystore")), config.getString("mtwilson.api.keystore.password"));
        RsaCredentialX509 rsaCredential = keystore.getRsaCredentialX509(config.getString("mtwilson.api.key.alias"), config.getString("mtwilson.api.key.password"));
        URL baseURL = new URL(config.getString("mtwilson.api.baseurl"));
        ApacheHttpAuthorization authority = new CustomizedApacheRsaHttpAuthorization(rsaCredential);
        ApacheHttpClient client = new ApacheHttpClient(baseURL, authority, keystore, config);
//        ApiClient api = new ApiClient(baseURL, rsaCredential, keystore, config);
        CustomizedApiClient api = new CustomizedApiClient(baseURL, client);
        api.getHostTrust(new Hostname("1.2.3.4")); // hostname doesn't matter since the request should be rejected by the security filter
    }
    
    /**
     * THE FOLLOWING "CUSTOMIZED" CLASSES EXIST ONLY TO SIMULATE BADLY BEHAVED
     * CLIENTS FOR THE PURPOSE OF TESTING THE ERROR MESSAGES FROM THE WEB SERVICES
     * SECURITY FILTER.
     * 
     */
    private static class CustomizedApiClient {
        private Logger log = LoggerFactory.getLogger(getClass());
        private URL baseURL;
        private ApacheHttpClient httpClient;
        protected static final ObjectMapper mapper = new ObjectMapper();
        public CustomizedApiClient(URL baseURL, ApacheHttpClient client) {
            this.baseURL = baseURL;
            this.httpClient = client;
            log.info("CUSTOMIZED API CLIENT CONSTRUCTOR");
        }
        private String querystring(MultivaluedMap<String,String> query) {
            URLCodec urlsafe = new URLCodec("UTF-8");
            String queryString = "";
            ArrayList<String> params = new ArrayList<String>();
            for( String key : query.keySet() ) {
                if( query.get(key) == null ) {
                    params.add(key+"=");
                }
                else {
                    for( String value : query.get(key) ) {
                        try {
                            params.add(key+"="+urlsafe.encode(value)); // XXX assumes that the keys don't have any special characters
                        } catch (EncoderException ex) {
                            log.error("Cannot encode query parameter: {}", value, ex);
                        }
                    }
                }
                queryString = StringUtils.join(params, "&");
            }
            return queryString;
        }
        //comment out unused function for removal (6/10 1.2)
        //private String asurl(String apiPath) {
        //    return baseURL.toExternalForm().concat("/AttestationService/resources").concat(apiPath);
        //}
        private String asurl(String apiPath, MultivaluedMap<String,String> query) {
            return baseURL.toExternalForm().concat("/AttestationService/resources").concat(apiPath).concat("?").concat(querystring(query));
        }
        // only call this if the Http Status is NOT OK in order to convert the response to an ApiException
        private ApiException error(ApiResponse response) throws IOException, ApiException {
            if( response.contentType.isCompatible(APPLICATION_JSON_TYPE) ) {
                // a json error response from the web application. we need to provide the error message to the user.
                ErrorResponse errorResponse;
                try {
                    log.debug("Parsing JSON error response: "+new String(response.content, "UTF-8"));
                    errorResponse = json(new String(response.content, "UTF-8"), ErrorResponse.class);
                }
                catch(Exception e) {
                    // cannot parse the json response, so include the entire response for the user. we ignore the exception "e" because it just means we couldn't parse the response.
                    return new ApiException(response.httpReasonPhrase+": "+response.content, ErrorCode.UNKNOWN_ERROR);
                }
                return new ApiException(response.httpReasonPhrase+": "+errorResponse.getErrorMessage(), ErrorCode.valueOf(errorResponse.getErrorCode()));
            }
            else if( response.contentType.isCompatible(TEXT_HTML_TYPE) ) {
                // typically html error message generated by web application container; we can ignore the html content because its generic
                String errorMessage = response.httpReasonPhrase;
                HtmlErrorParser errorParser = new HtmlErrorParser(new String(response.content, "UTF-8"));
                if( errorParser.getRootCause() != null ) {
                    errorMessage = errorMessage.concat(": "+errorParser.getRootCause());
                }
                return new ApiException(errorMessage, ErrorCode.UNKNOWN_ERROR);
            }
            else {
                // a non-json, non-html error response from the web application: so we include the response in the exception message.
                return new ApiException(response.httpReasonPhrase+": "+new String(response.content, "UTF-8"), ErrorCode.UNKNOWN_ERROR);
            }
        }

        private byte[] content(ApiResponse response) throws IOException, ApiException {
            log.trace("Status: {} {}", response.httpStatusCode, response.httpReasonPhrase);
            log.trace("Content-Type: {}", response.contentType.toString());
            log.trace("Content: {}", response.content);
            if( response.httpStatusCode == HttpStatus.SC_OK ) {
                return response.content;
            }
            else {
                throw error(response);
            }
        }        
        private <T> T json(ApiResponse response, Class<T> valueType) throws IOException, ApiException {
            if( response.httpStatusCode == HttpStatus.SC_OK && response.contentType.isCompatible(APPLICATION_JSON_TYPE) ) {
                return json(new String(response.content, "UTF-8"), valueType);
            }
            else if( response.httpStatusCode == HttpStatus.SC_OK ) {
                log.error("Unexpected content type {} in response", response.contentType.toString());
                throw new ApiException("Unexpected content type in response: "+response.contentType.toString());
            }
            else {
                throw error(response);
            }
        }


        private <T> T json(String document, Class<T> valueType) throws IOException, ApiException {
            if( document == null ) {
                throw new ApiException("Response from server has no content");
            }
            try {
                return mapper.readValue(document, valueType);
            }
            catch(com.fasterxml.jackson.core.JsonParseException e) {
                log.error("Cannot parse response: "+document);
                throw new ApiException("Cannot parse response: "+document, e);
            }        
        }
        private String text(ApiResponse response) throws IOException, ApiException {
            return new String(content(response), "UTF-8");
        }
        private HostTrustStatus parseHostTrustStatusString(String trustStatusString) {
                HostTrustStatus trustStatus = new HostTrustStatus();        
                String[] parts = trustStatusString.split(",");
                for (String part : parts) {
                        String[] subParts = part.split(":");
                        if (subParts[0].equals("BIOS")) {
                                trustStatus.bios = subParts[1].equals("1");
                        }
                        else if(subParts[0].equals("VMM")) {
                                trustStatus.vmm = subParts[1].equals("1");
                        }
                }
                return trustStatus;
        }

        public HostTrustResponse getHostTrust(Hostname hostname) throws IOException, ApiException, SignatureException {
            MultivaluedMap<String,String> query = new MultivaluedMapImpl();
            query.add("hostName", hostname.toString());
            // need to support both formats:  "BIOS:1,VMM:1" from 0.5.1 and JSON from 0.5.2
            ApiResponse response = httpClient.get(asurl("/hosts/trust", query));
            HostTrustResponse trust;
            if( response.httpStatusCode == HttpStatus.SC_OK) {
                if( APPLICATION_JSON_TYPE.equals(response.contentType) ) {
                    trust = json(response, HostTrustResponse.class);            
                }
                else if( TEXT_PLAIN_TYPE.equals(response.contentType) ) {
                    trust = new HostTrustResponse(hostname, parseHostTrustStatusString(text(response)));
                }
                else {
                    throw new ApiException("Unexpected content type in response: "+response.contentType, ErrorCode.UNKNOWN_ERROR.getErrorCode());
                }
                return trust;
            }
            else {
                throw error(response);
            }
        }
    }
    
    // just like ApacheRsaHttpAuthorization but using the CustomizedRsaAuthorization instead of the regular RsaAuthorization
    public static class CustomizedApacheRsaHttpAuthorization implements ApacheHttpAuthorization {
        private Logger log = LoggerFactory.getLogger(getClass());
        private CustomizedRsaAuthorization authority;
    
        public CustomizedApacheRsaHttpAuthorization(RsaCredential credential) {
            log.info("CUSTOMIZED APACHE RSA HTTP AUTHORIZATION CONSTRUCTOR");
            authority = new CustomizedRsaAuthorization(credential);
        }
    
        @Override
        public void addAuthorization(HttpRequest request) throws SignatureException {
            HashMap<String,String> headers = new HashMap<String,String>();
            request.addHeader("Authorization",
                    authority.getAuthorizationQuietly(request.getRequestLine().getMethod(), request.getRequestLine().getUri(), headers));
            // the RsaAuthorization class may generate headers for the request such as nonce and date, so we look for those and add them.
            for(String key : headers.keySet()) {
                request.addHeader(key, headers.get(key));
            }
        }

        @Override
        public void addAuthorization(HttpEntityEnclosingRequest request) throws SignatureException, IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
    
    public static class CustomizedRsaAuthorization {
        private Logger log = LoggerFactory.getLogger(getClass());
        private RsaCredential credential;
        private String realm;
        public CustomizedRsaAuthorization(RsaCredential credential) {
            this.credential = credential;
            
            // CUSTOMIZE:  try null, empty, or arbitrary attestation. if missing should get "Unauthorized: Authorization is missing realm"
            this.realm = "Attestation";
            
            log.info("CUSTOMIZED RSA AUTHORIZATION CONSTRUCTOR");
        }
        public String getAuthorizationQuietly(String httpMethod, String requestUrl, Map<String,String> headers) throws SignatureException {
            return getAuthorizationQuietly(httpMethod, requestUrl, null, headers, null);
        }
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
         * SECURITY FILTER TESTING - THIS IS THE METHOD TO CUSTOMIZE IN ORDER TO 
         * BREAK THE AUTHORIZATION HEADER IN DIFFERENT WAYS TO TEST THE SERVER
         * 
         * @param httpMethod
         * @param requestUrl
         * @param urlParams
         * @param headers
         * @param requestBody
         * @return
         * @throws NoSuchAlgorithmException
         * @throws InvalidKeyException
         * @throws IOException
         * @throws SignatureException 
         */
        public String getAuthorization(String httpMethod, String requestUrl, Map<String,Object> urlParams, Map<String,String> headers, String requestBody) throws NoSuchAlgorithmException, InvalidKeyException, IOException, SignatureException  {
            String nonce = new String(Base64.encodeBase64(nonce()));
            headers.put("X-Nonce", nonce);

            String username = new String(Base64.encodeBase64(credential.identity()));
            //String timestamp = ISO8601.DATETIME.format(System.currentTimeMillis());
            String timestamp;
            if( headers.containsKey("Date") ) {
                timestamp = headers.get("Date");
            }
            else {
                timestamp = Rfc822Date.format(new Date());    
                headers.put("Date", timestamp);
            }


            RsaSignatureInput signatureBlock = new RsaSignatureInput();
            signatureBlock.httpMethod = httpMethod;
            signatureBlock.url = new HttpRequestURL(requestUrl,urlParams).toString();
            signatureBlock.fingerprintBase64 = username;
            signatureBlock.body = requestBody;
            signatureBlock.signatureAlgorithm = credential.algorithm();

            headers.put("X-Nonce", nonce);
            headers.put("Date", timestamp);
            signatureBlock.headers = headers;
            signatureBlock.headerNames = new String[] { "X-Nonce", "Date" };

            String content = signatureBlock.toString();        
            //log.debug("CUSTOMIZED signed content follows... ("+content.length()+") \n"+content);

            byte[] signature = credential.signature(content.getBytes("UTF-8"));
            
            // CUSTOMIZE: try null signature, empty string, or prepend/append/modify the text in signature; be aware that the base64 decoder may ignore characters that are not in a-zA-Z0-9+/ ; also be aware that if the user is not registered (found in database) then signature is ignored
            String signatureBase64 = new String(Base64.encodeBase64(signature)); // CUSTOMIZE: .replace("x", "p");  will produce "Unauthorized: Authorization signature is invalid"

            // CUSTOMIZE:  try ABC123 or X509A instaed of X509. Should get "Unauthorized: Unsupported authorization scheme: ABC123"
            String authorization = String.format("X509 %s", headerParams( realm, username, signatureBlock.headerNames, signatureBlock.signatureAlgorithm,  signatureBase64));
            log.debug("CUSTOMIZED authorization: "+authorization);
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
                
    }
    
}
