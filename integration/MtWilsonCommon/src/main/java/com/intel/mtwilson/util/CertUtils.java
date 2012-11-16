/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

//import org.apache.xerces.impl.xpath.regex.REUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dsmagadx
 */
public class CertUtils {

    static Logger log = LoggerFactory.getLogger(CertUtils.class.getName());

    public static Certificate getCertificate(byte[] certificateBytes) {
        if (certificateBytes != null) {
            try {
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                java.security.cert.X509Certificate cert = (java.security.cert.X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certificateBytes));
                return cert;
            } catch (CertificateException ex) {
                log.error("Cannot load certificate: " + ex.getMessage(), ex);
                return null;
            }
        }
        return null;
    }

    public static X509Certificate getX509Certificate(byte[] certificateBytes) throws CertificateException {

        try {
            log.info("Certificate input bytes " + certificateBytes.toString());

            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            java.security.cert.X509Certificate cert = (java.security.cert.X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certificateBytes));

            return cert;
        } catch (CertificateException ex) {
            log.error("Cannot load certificate: " + ex.getMessage(), ex);
            throw ex;
        }



    }

    /**
     * See also RsaCredential in the security project.
     * @param certificate
     * @return 
     */
    public static byte[] sha256fingerprint(X509Certificate certificate) throws NoSuchAlgorithmException, CertificateEncodingException {
        MessageDigest hash = MessageDigest.getInstance("SHA-256"); // NoSuchAlgorithmException
        byte[] digest = hash.digest(certificate.getEncoded()); // CertificateEncodingException
        return digest;
    }

    /**
     * Provided for compatibility with other systems.
     * @param certificate
     * @return 
     */
    public static byte[] sha1fingerprint(X509Certificate certificate) throws NoSuchAlgorithmException, CertificateEncodingException {
        MessageDigest hash = MessageDigest.getInstance("SHA-1"); // NoSuchAlgorithmException
        byte[] digest = hash.digest(certificate.getEncoded()); // CertificateEncodingException
        return digest;
    }
    
    /**
     * Use this for compatibility with other systems that use SHA-1 fingerprints.
     * Mt Wilson uses SHA-256 fingerprints for public keys and X509 certs to
     * look up in database.
     * @param certificate
     * @return
     * @throws NoSuchAlgorithmException
     * @throws CertificateEncodingException 
     */
    public static String getSha1Fingerprint(X509Certificate certificate)
            throws NoSuchAlgorithmException, CertificateEncodingException {
        return convertToHex(sha1fingerprint(certificate));

    }

    public static String convertToHex(byte bytes[]) {

        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

        StringBuffer buf = new StringBuffer(bytes.length * 2);

        for (int i = 0; i < bytes.length; ++i) {
            buf.append(hexDigits[(bytes[i] & 0xf0) >> 4]);
            buf.append(hexDigits[bytes[i] & 0x0f]);
        }

        return buf.toString();
    }

	public static X509Certificate getX509Certificate(File certFile) throws IOException, CertificateException {
    	byte[] bytes = new byte[(int)certFile.length()];
    	
    	FileInputStream inputStream = new FileInputStream(certFile);
    	try{
    		inputStream.read(bytes);
    		return getX509Certificate(bytes);
    	}finally{
    		if(inputStream != null){
    			inputStream.close();
    		}
    	}
    	
    
	}
}
