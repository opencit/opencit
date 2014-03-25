/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.authc.x509;

import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;
import com.intel.mtwilson.security.http.RsaSignatureInput;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import org.bouncycastle.asn1.x509.DigestInfo;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
/**
 *
 * @author jbuhacoff
 */
public class X509AuthenticationFilter extends AuthenticatingFilter {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(X509AuthenticationFilter.class);

    protected static final String AUTHORIZATION_HEADER = "Authorization";
    protected static final String AUTHENTICATE_HEADER = "WWW-Authenticate";    
    private String authcScheme = "X509";
    private String applicationName = "Mt Wilson";
    private int expiresAfter = 60 * 60 * 1000; // 1 hour, in milliseconds, max is Integer.MAX_VALUE
    private final String headerAttributeNameValuePair = "([a-zA-Z0-9_-]+)=\"([^\"]+)\"";
    private final Pattern headerAttributeNameValuePairPattern = Pattern.compile(headerAttributeNameValuePair);
    
    
    
    public String getAuthcScheme() {
        return authcScheme;
    }

    /**
     * Override the authentication scheme name. The default
     * value is "X509"
     * @param authcScheme 
     */
    public void setAuthcScheme(String authcScheme) {
        this.authcScheme = authcScheme;
    }

    public String getApplicationName() {
        return applicationName;
    }

    /**
     * Override the application name. The default is "Mt Wilson"
     * @param applicationName 
     */
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    /**
     * Override the expiration window. Default is 1 hour.
     * @param expiresAfter 
     */
    public void setExpiresAfter(int expiresAfter) {
        this.expiresAfter = expiresAfter;
    }

    public int getExpiresAfter() {
        return expiresAfter;
    }
    
    
    
    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        Authorization authorization = getAuthorization(httpRequest);
        RsaSignatureInput signatureInput = getSignatureInputFromHttpRequest(httpRequest, authorization);
        String content = signatureInput.toString(); // may throw IllegalArgumentException if any required field is null or invalid
        byte[] document = content.getBytes("UTF-8");
        byte[] signature = Base64.decodeBase64(authorization.signatureBase64);
        String signatureAlgorithm = signatureAlgorithm(authorization.signatureAlgorithm);
        byte[] fingerprint = Base64.decodeBase64(authorization.fingerprintBase64);
        
        byte[] digest = getDigest(document, signatureAlgorithm);
        X509AuthenticationToken token = new X509AuthenticationToken(new Fingerprint(fingerprint), new Credential(signature, digest), request.getRemoteAddr());
        log.debug("Created X509AuthenticationToken");
        return token;
    }
    
    private String getRequestBody(HttpServletRequest httpRequest) throws IOException {
        // get the request body (even if empty) - the input stream must be repeatable
        // so the endpoint will be able to read it again for processing the request
        InputStream in = httpRequest.getInputStream();
        if( !in.markSupported() ) {
            throw new IOException("Request input stream is not repeatable; evaluating X509 authorization would prevent further processing of request");
        }
        String requestBody = IOUtils.toString(in);
        in.reset(); // to allow other filters or servlets to process the request
        return requestBody;
    }
    
    private Map<String,String> getRequestHeaders(HttpServletRequest httpRequest, String[] headerNames) {
        HashMap<String,String> headerValues = new HashMap<>();
        for(String headerName : headerNames) {
            headerValues.put(headerName, httpRequest.getHeader(headerName));
        }
        return headerValues;
    }
    
    private Authorization getAuthorization(HttpServletRequest httpRequest) {
        String authorizationText = httpRequest.getHeader(AUTHORIZATION_HEADER);
        log.debug("Parsing authorization header: {}", authorizationText);
        Authorization authorization = parseAuthorization(authorizationText);
        log.info("X509CertificateAuthorization: Request timestamp ok");
        return authorization;
    }
    
    private RsaSignatureInput getSignatureInputFromHttpRequest(HttpServletRequest httpRequest, Authorization a) throws IOException {
        RsaSignatureInput signatureInput = new RsaSignatureInput();
        signatureInput.httpMethod = httpRequest.getMethod();
        signatureInput.url = httpRequest.getRequestURI();
        signatureInput.realm = a.realm;
        signatureInput.fingerprintBase64 = a.fingerprintBase64;
        signatureInput.signatureAlgorithm = a.signatureAlgorithm;
        signatureInput.headerNames = a.headerNames;
        signatureInput.headers = getRequestHeaders(httpRequest, a.headerNames);
        signatureInput.body = getRequestBody(httpRequest); // throws IOException if error on read or if InputStream is not repeatable;
        return signatureInput;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletResponse httpResponse = WebUtils.toHttp(response);
        httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        String authcHeader = getAuthcScheme() + " realm=\"" + getApplicationName() + "\"";
        httpResponse.setHeader(AUTHENTICATE_HEADER, authcHeader);
        return false;
    }
    
    private byte[] getDigest(byte[] document, String signatureAlgorithm) throws NoSuchAlgorithmException, IOException {
        log.debug("Signature algorithm {}", signatureAlgorithm);
        String digestAlgorithm = getDigestAlgorithm(signatureAlgorithm);
        log.debug("Digest algorithm {}", digestAlgorithm);
        String oid = getOidForAlgorithm(digestAlgorithm);
        log.debug("OID for {} is {}", digestAlgorithm, oid);
        
        // compute the digest of the document using the digest algorithm
        MessageDigest md = MessageDigest.getInstance(digestAlgorithm); // like SHA1; throws NoSuchAlgorithmException
        byte[] digest = md.digest(document);
        
        // java format for the digest is algorithm oid followed by the hash
        AlgorithmIdentifier algId = new AlgorithmIdentifier(new DERObjectIdentifier(oid), null);
        DigestInfo digestInfo = new DigestInfo(algId, digest);
        return digestInfo.getEncoded(); // throws IOException
    }

    private String getDigestAlgorithm(String signatureAlgorithm) {
        if( "SHA1withRSA".equals(signatureAlgorithm) ) {
            return "SHA1";
        }
        if( "SHA256withRSA".equals(signatureAlgorithm) ) {
            return "SHA256";
        }
        if( "SHA384withRSA".equals(signatureAlgorithm) ) {
            return "SHA384";
        }
        if( "SHA512withRSA".equals(signatureAlgorithm) ) {
            return "SHA512";
        }
        throw new IllegalArgumentException("Unknown signature algorithm "+signatureAlgorithm);
    }
    
    private String getOidForAlgorithm(String digestAlgorithm) {
//        if( "MD5".equalsIgnoreCase(algorithm) ) {
//            return "1.2.840.113549.2.5";
//        }
        if( "SHA1".equalsIgnoreCase(digestAlgorithm) ) {
            return "1.3.14.3.2.26";
        }
//        if( "SHA1withRSA".equalsIgnoreCase(algorithm) ) {
//            return "1.3.14.3.2.29";
//        }
        if( "SHA256".equalsIgnoreCase(digestAlgorithm) ) {
            return "2.16.840.1.101.3.4.2.1";
        }
        if( "SHA384".equalsIgnoreCase(digestAlgorithm) ) {
            return "2.16.840.1.101.3.4.2.2";
        }
        if( "SHA512".equalsIgnoreCase(digestAlgorithm) ) {
            return "2.16.840.1.101.3.4.2.3";
        }
        throw new IllegalArgumentException("Unknown OID for algorithm "+digestAlgorithm);
    }
    
    /**
       
       * @param timestamp
       * @return          
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
     * 
     */
    // XXX should this be in the matcher? because at this point here we can't verify the signature yet so we can't reall ytrust the time to know that it's not been tampered with to stay within the window...
    private boolean isRequestExpired(Date timestamp) {
        // request expiration policy
        Calendar expirationTime = Calendar.getInstance();
        expirationTime.setTime(timestamp);
        expirationTime.add(Calendar.MILLISECOND, expiresAfter);
        Calendar currentTime = Calendar.getInstance();
        
        if( currentTime.after(expirationTime)) {
            long diff = currentTime.getTimeInMillis() - expirationTime.getTimeInMillis();
            log.warn("Request expired: {}", DurationFormatUtils.formatDurationHMS(diff));
            return true;
        }
        return false;
    }
    
    
    /**
     * Standardizes signature algorithm names to the Java name.
     * "SHA256withRSA".equals(signatureAlgorithm("RSA-SHA256")); // true
     * @param name
     * @return 
     */
    private String signatureAlgorithm(String name) {
        if( "RSA-SHA256".equals(name) ) { return "SHA256withRSA"; }
        return name;
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
