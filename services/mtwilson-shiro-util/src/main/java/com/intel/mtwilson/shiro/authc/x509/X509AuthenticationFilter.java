/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.authc.x509;

import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.util.WebUtils;
import com.intel.mtwilson.security.http.RsaSignatureInput;
import com.intel.mtwilson.shiro.HttpAuthenticationFilter;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.asn1.x509.DigestInfo;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;

/**
 *
 * @author jbuhacoff
 */
public class X509AuthenticationFilter extends HttpAuthenticationFilter {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(X509AuthenticationFilter.class);
    private int expiresAfter = 60 * 60 * 1000; // 1 hour, in milliseconds, max is Integer.MAX_VALUE
    private final String headerAttributeNameValuePair = "([a-zA-Z0-9_-]+)=\"([^\"]+)\"";
    private final Pattern headerAttributeNameValuePairPattern = Pattern.compile(headerAttributeNameValuePair);

    public X509AuthenticationFilter() {
        super();
        setAuthenticationScheme("X509");
    }

    /**
     * Override the expiration window. Default is 1 hour.
     *
     * @param expiresAfter
     */
    public void setExpiresAfter(int expiresAfter) {
        this.expiresAfter = expiresAfter;
    }

    public int getExpiresAfter() {
        return expiresAfter;
    }

    @Override
    protected AuthenticationToken createToken(ServletRequest request) {
        log.debug("createToken");
        try {
            HttpServletRequest httpRequest = WebUtils.toHttp(request);
            Authorization authorization = getAuthorization(httpRequest);
            // clients MUST include a Date header in the request covered by a signature (we compare it to the anti-replay protection window)
            if(!ArrayUtils.contains(authorization.headerNames, "Date")) {
                throw new IllegalArgumentException("Request must include Date header"); 
            }
            
            RsaSignatureInput signatureInput = getSignatureInputFromHttpRequest(httpRequest, authorization);
            String content = signatureInput.toString(); // may throw IllegalArgumentException if any required field is null or invalid
//            log.debug("Document content (signature input):\n'{}'\n", content);
            byte[] document = content.getBytes("UTF-8");
            byte[] signature = Base64.decodeBase64(authorization.signatureBase64);
            String signatureAlgorithm = signatureAlgorithm(authorization.signatureAlgorithm);
            byte[] fingerprint = Base64.decodeBase64(authorization.fingerprintBase64);
            log.trace("Signature being added to token is {}", Hex.encodeHexString(signature));
            log.trace("Fingerprint being added to token is {}", Hex.encodeHexString(fingerprint));
            log.trace("Document being added to token is {}", Hex.encodeHexString(document));
            byte[] digest = getDigest(document, signatureAlgorithm); // example: 3031300d060960864801650304020105000420 8373ed7ae4a499534f3eb02fb898a0eafea48a334e2f0a5703e7dc474360786a   the space between the two hex parts shows where the alg id ends and the sha256 digest of the document itself begins
            log.debug("Digest with alg id included is: {}", Hex.encodeHexString(digest));
            X509AuthenticationToken token = new X509AuthenticationToken(new Fingerprint(fingerprint), new Credential(signature, digest), signatureInput, request.getRemoteAddr());
            log.debug("createToken: returning X509AuthenticationToken");
            return token;
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new AuthenticationException("Cannot authenticate request: " + e.getMessage(), e);
        }
    }

    private String getRequestBody(HttpServletRequest httpRequest) throws IOException {
        log.debug("Reading request body");
        // get the request body (even if empty) - the input stream must be repeatable
        // so the endpoint will be able to read it again for processing the request
        InputStream in = httpRequest.getInputStream();
        if (!in.markSupported()) {
            throw new IOException("Request input stream is not repeatable; evaluating X509 authorization would prevent further processing of request");
        }
        String requestBody = IOUtils.toString(in);
        in.reset(); // to allow other filters or servlets to process the request
        return requestBody;
    }

    private Map<String, String> getRequestHeaders(HttpServletRequest httpRequest, String[] headerNames) {
        HashMap<String, String> headerValues = new HashMap<>();
        for (String headerName : headerNames) {
            headerValues.put(headerName, httpRequest.getHeader(headerName));
        }
        return headerValues;
    }

    private Authorization getAuthorization(HttpServletRequest httpRequest) {
        String authorizationText = httpRequest.getHeader(getAuthorizationHeaderName());
        log.debug("Parsing authorization header: {}", authorizationText);
        Authorization authorization = parseAuthorization(authorizationText);
        log.debug("X509CertificateAuthorization: parsed authorization");
        return authorization;
    }

    /**
     * Example signature document: Request: GET
     * https://10.1.71.56:8443/mtwilson/v2/WLMService/resources/oem Realm: From:
     * Ca0ES/b4gqW6aExUoCvSOxb68fOIqrN9dPhYUmZImFM= Signature-Algorithm:
     * SHA256withRSA X-Nonce: AAABRQ9M90Y56zLFR/0hc8B6LDB+qO3r Date: Sat, 29 Mar
     * 2014 12:24:33 PDT
     *
     *
     *
     * @param httpRequest
     * @param a
     * @return
     * @throws IOException
     */
    private RsaSignatureInput getSignatureInputFromHttpRequest(HttpServletRequest httpRequest, Authorization a) throws IOException {
        RsaSignatureInput signatureInput = new RsaSignatureInput();
        signatureInput.httpMethod = httpRequest.getMethod();
        signatureInput.url = getURL(httpRequest); // protocol, host, port, path, and query string as sent by client
        signatureInput.realm = a.realm;
        signatureInput.fingerprintBase64 = a.fingerprintBase64;
        signatureInput.signatureAlgorithm = a.signatureAlgorithm;
        signatureInput.headerNames = a.headerNames;
        signatureInput.headers = getRequestHeaders(httpRequest, a.headerNames);
        signatureInput.body = getRequestBody(httpRequest); // throws IOException if error on read or if InputStream is not repeatable;
        log.debug("signature input body is {} bytes.", signatureInput.body.length());
        return signatureInput;
    }

    private String getURL(HttpServletRequest httpRequest) {
        String url = httpRequest.getRequestURL().toString();
        String query = httpRequest.getQueryString();
        if (query == null) {
            query = "";
        }
        log.debug("request URL is {} with query string {}", url, query);
        String queryDelimiter = query.isEmpty() ? "" : "?";
        return url + queryDelimiter + query;
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
        log.debug("Document digest is {}", Hex.encodeHexString(digest));

        // java format for the digest is algorithm oid followed by the hash
        AlgorithmIdentifier algId = new AlgorithmIdentifier(new DERObjectIdentifier(oid), null);
        DigestInfo digestInfo = new DigestInfo(algId, digest);
        return digestInfo.getEncoded(); // throws IOException
    }

    // reference: http://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html
    private String getDigestAlgorithm(String signatureAlgorithm) {
        if ("SHA1withRSA".equals(signatureAlgorithm)) {
            return "SHA-1";
        }
        if ("SHA256withRSA".equals(signatureAlgorithm)) {
            return "SHA-256";
        }
        if ("SHA384withRSA".equals(signatureAlgorithm)) {
            return "SHA-384";
        }
        if ("SHA512withRSA".equals(signatureAlgorithm)) {
            return "SHA-512";
        }
        throw new IllegalArgumentException("Unknown signature algorithm " + signatureAlgorithm);
    }

    private String getOidForAlgorithm(String digestAlgorithm) {
//        if( "MD5".equalsIgnoreCase(algorithm) ) {
//            return "1.2.840.113549.2.5";
//        }
        if ("SHA-1".equalsIgnoreCase(digestAlgorithm) || "SHA1".equalsIgnoreCase(digestAlgorithm)) {
            return "1.3.14.3.2.26";
        }
//        if( "SHA1withRSA".equalsIgnoreCase(algorithm) ) {
//            return "1.3.14.3.2.29";
//        }
        if ("SHA-256".equalsIgnoreCase(digestAlgorithm) || "SHA256".equalsIgnoreCase(digestAlgorithm)) {
            return "2.16.840.1.101.3.4.2.1";
        }
        if ("SHA-384".equalsIgnoreCase(digestAlgorithm) || "SHA384".equalsIgnoreCase(digestAlgorithm)) {
            return "2.16.840.1.101.3.4.2.2";
        }
        if ("SHA-512".equalsIgnoreCase(digestAlgorithm) || "SHA512".equalsIgnoreCase(digestAlgorithm)) {
            return "2.16.840.1.101.3.4.2.3";
        }
        throw new IllegalArgumentException("Unknown OID for algorithm " + digestAlgorithm);
    }

    /**
     * Standardizes signature algorithm names to the Java name.
     * "SHA256withRSA".equals(signatureAlgorithm("RSA-SHA256")); // true
     *
     * @param name
     * @return
     */
    private String signatureAlgorithm(String name) {
        if ("RSA-SHA256".equals(name)) {
            return "SHA256withRSA";
        }
        return name;
    }

    /**
     *
     * Authorization header format is like this: "Signature" *<SP
     * <attribute-name "=" quoted-attribute-value>>
     *
     * Sample Authorization header:
     *     * 
Authorization: X509 realm="Example", fingerprint="0685bd9184jfhq22",
     * headers="X-Nonce,Date", algorithm="RSA-SHA256",
     * signature="wOJIO9A2W5mFwDgiDvZbTSMK%2FPY%3D"
     *
     * @param authorizationHeader
     * @return
     */
    private Authorization parseAuthorization(String authorizationHeader) {
        Authorization a = new Authorization();
        // splitting on spaces should yield "X509" followed by attribute name-value pairs
        String[] terms = authorizationHeader.split(" ");
        if (!"X509".equals(terms[0])) {
            throw new IllegalArgumentException("Authorization type is not X509");
        }
        for (int i = 1; i < terms.length; i++) {
            // each term after "PublicKey" is an attribute name-value pair, like realm="Example"
            Matcher attributeNameValue = headerAttributeNameValuePairPattern.matcher(terms[i]);
            if (attributeNameValue.find()) {
                String name = attributeNameValue.group(1);
                String value = attributeNameValue.group(2);
                if (name.equals("realm")) {
                    a.realm = value;
                }
                if (name.equals("fingerprint") || name.equals("id")) {
                    a.fingerprintBase64 = value;
                }
                if (name.equals("headers")) {
                    a.headerNames = value.split(",");
                }
                if (name.equals("algorithm") || name.equals("digest")) {
                    a.signatureAlgorithm = value;
                }
                if (name.equals("signature")) {
                    a.signatureBase64 = value;
                }
            }
        }
        if (a.realm == null || a.realm.isEmpty()) {
            log.warn("Authorization is missing realm"); //            throw new IllegalArgumentException("Authorization is missing realm"); // currently we allow undefined realm because we only have one database of users. in the future we could require a realm if we have moer than one and we need to know where to look things up.
            }
        if (a.fingerprintBase64 == null || a.fingerprintBase64.isEmpty()) {
            throw new IllegalArgumentException("Authorization is missing id/fingerprint");
        }
        if (a.signatureAlgorithm == null || a.signatureAlgorithm.isEmpty()) {
            throw new IllegalArgumentException("Authorization is missing signature algorithm");
        }
        if (a.signatureBase64 == null || a.signatureBase64.isEmpty()) {
            throw new IllegalArgumentException("Authorization is missing signature");
        }
        return a;
    }

    /**
     * This class represents the content of the HTTP Authorization header. It is
     * very closely related to the RsaSignatureInput class but not the same
     * because this class includes the base64-encoded signature from the
     * Authorization header. Also, the selected HTTP header names to include in
     * the signature are identified in the Authorization header so they are
     * included here, but the values for those headers are not included here.
     */
    public static class Authorization {

        public String realm;
        public String fingerprintBase64;
        public String[] headerNames = ArrayUtils.EMPTY_STRING_ARRAY;
        public String signatureAlgorithm;
        public String signatureBase64;
    }
}
