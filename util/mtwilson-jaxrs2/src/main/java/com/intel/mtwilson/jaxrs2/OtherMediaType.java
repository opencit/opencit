/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jaxrs2;

/**
 * Media types not included in javax.ws.rs.core.MediaType. New
 * MediaType instances can be created with any type, and this class 
 * only provides the text constants for convenience. The static
 * MediaType.valueOf(String) method can be used to convert these
 * constants to instances of MediaType.
 * 
 * @author jbuhacoff
 */
public class OtherMediaType {
    // jsonapi format, see http://jsonapi.org
    public static final String APPLICATION_VND_API_JSON = "application/vnd.api+json";
    
    // yaml formats
    public static final String APPLICATION_YAML = "application/yaml";
    public static final String TEXT_YAML = "text/yaml";
    
    // patch formats
    public static final String APPLICATION_JSON_PATCH = "application/json-patch+json"; // RFC 6902
    public static final String APPLICATION_RELATIONAL_PATCH_JSON = "application/relational-patch+json";
    
    // encryption, signature, and related formats
    public static final String ENCRYPTED_OPENSSL = "encrypted/openssl";
    public static final String ENCRYPTED_JAVA = "encrypted/java";
    public static final String APPLICATION_SIGNATURE_OPENSSL = "application/signature.openssl";
    public static final String APPLICATION_SIGNATURE_JAVA = "application/signature.java";
    public static final String MESSAGE_RFC822 = "message/rfc822";
    public static final String MULTIPART_ENCRYPTED = "multipart/encrypted";
    public static final String MULTIPART_SIGNED = "multipart/signed";

    // certificate and key exchange formats
    public static final String APPLICATION_X_PEM_FILE = "application/x-pem-file";   // .pem 
    public static final String APPLICATION_PKCS12 = "application/x-pkcs12"; // .p12   .pfx
    public static final String APPLICATION_PKIX_CERT = "application/pkix-cert"; // http://tools.ietf.org/search/rfc2585  mime type for X509 certificate .cer or .crt DER-encoded
    public static final String APPLICATION_PKIX_CRL = "application/pkix-crl"; // http://tools.ietf.org/search/rfc2585 mime type for X509 cert revocation list .crl   DER-encoded
    public static final String APPLICATION_PKCS7_MIME = "application/pkcs7-mime"; // http://tools.ietf.org/search/rfc2797  CMS enrollment messages  .p7c, .p7m, 
    public static final String APPLICATION_OCSP_REQUEST = "application/ocsp-request";
    public static final String APPLICATION_OCSP_RESPONSE = "application/ocsp-response";
    
    // compression formats
    public static final String APPLICATION_ZIP = "application/zip";
    public static final String APPLICATION_GZIP = "application/gzip"; // replaces application/x-gzip, application/x-gzip-compressed, etc. http://www.ietf.org/rfc/rfc6713.txt
    public static final String APPLICATION_ZLIB = "application/zlib"; // replaces application/x-zlib, application/x-zlib-compressed, etc. http://www.ietf.org/rfc/rfc6713.txt
    public static final String APPLICATION_X_BZIP2 = "application/x-bzip2";
    public static final String APPLICATION_SAML = "application/samlassertion+xml";
    
}
