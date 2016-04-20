/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jaxrs2.mediatype;

/**
 * Media types not included in javax.ws.rs.core.MediaType. New
 * MediaType instances can be created with any type, and this class 
 * only provides the text constants for convenience. The static
 * MediaType.valueOf(String) method can be used to convert these
 * constants to instances of MediaType.
 * 
 * @author jbuhacoff
 */
public class CryptoMediaType {
    
    // encryption, signature, and related formats
    public static final String ENCRYPTED_OPENSSL = "encrypted/openssl";
    public static final String ENCRYPTED_JAVA = "encrypted/java";
    public static final String APPLICATION_SIGNATURE_OPENSSL = "application/signature.openssl";
    public static final String APPLICATION_SIGNATURE_JAVA = "application/signature.java";
    public static final String MESSAGE_RFC822 = "message/rfc822";
    public static final String MULTIPART_ENCRYPTED = "multipart/encrypted";
    public static final String MULTIPART_SIGNED = "multipart/signed";
    public static final String APPLICATION_SAML = "application/samlassertion+xml";

    // certificate and key exchange formats
    public static final String APPLICATION_X_PEM_FILE = "application/x-pem-file";   // .pem 
    public static final String APPLICATION_PKCS12 = "application/x-pkcs12"; // .p12   .pfx
    public static final String APPLICATION_PKIX_CERT = "application/pkix-cert"; // http://tools.ietf.org/search/rfc2585  mime type for X509 certificate .cer or .crt DER-encoded
    public static final String APPLICATION_PKIX_ATTR_CERT = "application/pkix-attr-cert"; // http://tools.ietf.org/html/rfc5877  mime type for single X509 attribute certificate DER-encoded
    public static final String APPLICATION_PKIX_CRL = "application/pkix-crl"; // http://tools.ietf.org/search/rfc2585 mime type for X509 cert revocation list .crl   DER-encoded
    public static final String APPLICATION_PKCS7_MIME = "application/pkcs7-mime"; // http://tools.ietf.org/search/rfc2797  CMS enrollment messages  .p7c, .p7m, 
    public static final String APPLICATION_OCSP_REQUEST = "application/ocsp-request";
    public static final String APPLICATION_OCSP_RESPONSE = "application/ocsp-response";
    
}
