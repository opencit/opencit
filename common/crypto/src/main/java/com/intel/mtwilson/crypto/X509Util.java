/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.crypto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.regex.Pattern;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author jbuhacoff
 */
public class X509Util {
    public static final String BEGIN_CERTIFICATE = "-----BEGIN CERTIFICATE-----";
    public static final String END_CERTIFICATE = "-----END CERTIFICATE-----";
    public static final String PEM_NEWLINE = "\r\n";

    /**
     * See also RsaCredential in the security project.
     * See also Sha256Digest in the datatypes project
     * @param certificate
     * @return
     */
    public static byte[] sha256fingerprint(X509Certificate certificate) throws NoSuchAlgorithmException, CertificateEncodingException {
        MessageDigest hash = MessageDigest.getInstance("SHA-256");
        byte[] digest = hash.digest(certificate.getEncoded());
        return digest;
    }

    /**
     * Provided for compatibility with other systems.
     * See also Sha1Digest in the datatypes project
     * @param certificate
     * @return
     */
    public static byte[] sha1fingerprint(X509Certificate certificate) throws NoSuchAlgorithmException, CertificateEncodingException {
        MessageDigest hash = MessageDigest.getInstance("SHA-1");
        byte[] digest = hash.digest(certificate.getEncoded());
        return digest;
    }
    
    /**
     * Converts an X509 Certificate to PEM encoding, with lines up to 76 characters long.
     * Newlines are carriage-return and line-feed. 
     * The end certificate tag also ends in a newline, so you can output a sequence of
     * pem certificates into a file without having to insert any newlines yourself.
     * @param certificate
     * @return
     * @throws CertificateEncodingException 
     */
    public static String encodePemCertificate(X509Certificate certificate) throws CertificateEncodingException {
        // the function Base64.encodeBase64String does not chunk to 76 characters per line
        String encoded = new String(Base64.encodeBase64(certificate.getEncoded(), true));
        return BEGIN_CERTIFICATE+PEM_NEWLINE+encoded.trim()+PEM_NEWLINE+END_CERTIFICATE+PEM_NEWLINE;
    }
    
    /**
     * This function converts a PEM-format certificate to an X509Certificate
     * object.
     *
     * Example PEM format:
     *
     * -----BEGIN CERTIFICATE----- (base64 data here) -----END CERTIFICATE-----
     *
     * You can also pass just the base64 certificate data without the header and
     * footer.
     * 
     * @param text
     * @return
     * @throws CertificateException 
     */
    public static X509Certificate decodePemCertificate(String text) throws CertificateException {
        String content = text.replace(BEGIN_CERTIFICATE, "").replace(END_CERTIFICATE, "");
        byte[] der = Base64.decodeBase64(content);
        return decodeDerCertificate(der);
    }

    /**
     * Reads a DER-encoded certificate and creates a corresponding X509Certificate
     * object.
     * @param certificateBytes
     * @return
     * @throws CertificateException 
     */
    public static X509Certificate decodeDerCertificate(byte[] certificateBytes) throws CertificateException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certificateBytes));
        return cert;
    }
    
    /**
     * For completeness.  The X509Certificate.getEncoded() method returns the DER
     * encoding of the certificate.
     * @param certificate
     * @return
     * @throws CertificateEncodingException 
     */
    public static byte[] encodeDerCertificate(X509Certificate certificate) throws CertificateEncodingException {
        return certificate.getEncoded();
    }
}
