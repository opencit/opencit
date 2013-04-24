/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.crypto;

import com.intel.mtwilson.crypto.RsaUtil;
import com.intel.mtwilson.crypto.X509Builder;
import com.intel.mtwilson.crypto.X509Util;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.codec.binary.Base64;
import org.junit.Test;
import sun.security.x509.BasicConstraintsExtension;
import sun.security.x509.OIDMap;
import sun.security.x509.PKIXExtensions;

/**
 *
 * @author jbuhacoff
 */
public class X509UtilTest {
    @Test
    public void testCreateCaCertificate() throws NoSuchAlgorithmException, CertificateParsingException, IOException, CertificateEncodingException, CertificateException {
        KeyPair caKeys = RsaUtil.generateRsaKeyPair(1024);
        X509Certificate caCert = X509Builder.factory().selfSigned("CN=testca", caKeys).expires(30, TimeUnit.DAYS).keyUsageCertificateAuthority().build();
        System.out.println("created CA cert with CA flag: "+caCert.getBasicConstraints());
//        System.out.println("HERE IS THE PEM: "+X509Util.encodePemCertificate(caCert));
        System.out.println("basic constraints: "+caCert.getBasicConstraints());
        boolean keyUsage[] = caCert.getKeyUsage();
        if( keyUsage != null ) {
            System.out.println("key usage CA: "+keyUsage[5]);
        }
        else { System.out.println("keyUsage boolean[] is NULL"); }
        
        
    // not going to have extended key usages. the keys would be anyExtendedKeyUsage, serverAuth, clientAuth, codeSigning, emailProtection, ipsecEndSystem, ipsecTunnel, ipsecUser, timeStamping, OCSPSigning
            List<String> extKeyUsage = caCert.getExtendedKeyUsage();
            if( extKeyUsage != null ) {
            System.out.println("ExtendedKeyUsage count: "+extKeyUsage.size());
            for(String str: extKeyUsage) { System.out.println("ExtendedKeyUsage: "+str); }
            } else { System.out.println("ExtendedKeyUsage list is NULL"); }

    }
    
    
    @Test
    public void testReadCaCertificate() throws CertificateException {
        String pem = //"-----BEGIN CERTIFICATE-----\n" +
"MIIBwjCCASugAwIBAgIIUV/sIM8fxt0wDQYJKoZIhvcNAQELBQAwETEPMA0GA1UEAxMGdGVzdGNh\n" +
"MB4XDTEzMDEwNjA4MTkyNloXDTEzMDIwNTA4MTkyNlowETEPMA0GA1UEAxMGdGVzdGNhMIGfMA0G\n" +
"CSqGSIb3DQEBAQUAA4GNADCBiQKBgQCw4KY7iTN1O4fxVsZFnzqPM3EMDfduTbeNdvj0wIRCMffA\n" +
"lSlb8Ah6HEcw60jB93Fhc+a9ycd9k0VdmcRCAXlmAWd5RSk/Rw1G0Pr4M0rD4keUGGcu9ftXbPnm\n" +
"LM/wjnNaMjCJsItq6n591R7OFxeoWNd+wP5mKQQ0duIyZtMVjQIDAQABoyMwITAOBgNVHQ8BAf8E\n" +
"BAMCAgQwDwYDVR0TAQH/BAUwAwEB/zANBgkqhkiG9w0BAQsFAAOBgQCupt8BZuk1yBpuK+eA0SMh\n" +
"EeUXVCL3Hbc5dTjvnVqyUE+G1VFFZVttKuTtk5e2W8kjHY4A6Pab7HuWwlxAVDwBH/1OY3Nij1oS\n" +
"YEjPL8kq/nVEimCV87f2+OEkOH/jIiZPitwGKW+N+rARuBds9GF9s8njz/u5GETCdyuzvAVFQQ==\n" ;
//"-----END CERTIFICATE-----";
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        byte[] der = Base64.decodeBase64(pem);
        ByteArrayInputStream in = new ByteArrayInputStream(der);
        X509Certificate cert = (X509Certificate)certFactory.generateCertificate(in);
        int basicConstraints = cert.getBasicConstraints();
        boolean[] keyUsage = cert.getKeyUsage(); // index 5 is CA
//        List<String> extendedKeyUsage = cert.getExtendedKeyUsage();
        System.out.println("CA basic constraint: "+basicConstraints);
        System.out.println("CA key usage: "+keyUsage==null?"NULL":keyUsage[5]);
//        System.out.println("CA extended key usage: "+extendedKeyUsage==null?"NULL":extendedKeyUsage.get(0));
    }
    
    /**
     * You get this exception:
     *  // InvalidKeyException: IOException: DerInputStream.getLength(): lengthTag=111, too big.
     * If you try to decode the RSA PUBLIC KEY with the -----BEGIN PUBLIC KEY----- and -----END PUBLIC KEY----- tags still in there.
     * You have to strip those out first, then base64-decode the contents, and pass that to the key factory.
     * 
     * @throws Exception 
     */
    @Test
    public void testReadAikKey() throws Exception {
        String pem = "-----BEGIN PUBLIC KEY-----\n"+
"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvNEz3+TStAAndHTc1qwT\n"+
"NGvZYyB7DD1FshQf+mbQUGJ9HccOXNn5oHB7fWQjODjlDrYyCs7FclSMTLxA3lHX\n"+
"98QWeWHL2O8t8qrJQQEUWZITmr/ddiNJOOvMeYF0K5if4m84vjgx/pTwwAVyU0Yo\n"+
"MMXPnRozO8o7zSyRsH4jixALDugrsveEjLQI/cIEFvNjqlhyfumHyJKywNkMH1oJ\n"+
"4e/f89FkpeDV694lsLs1jguuLLnvroXYJ5Uzeos+F0Pj1zFDUvhWrjVwxsUfAxS8\n"+
"5uFGTUm6EEl9XiKwi+mgg8ODrY5dh3uE2yKB2T1Qj8BfK55zB8cYbORSsm6/f6Bi\n"+
"BwIDAQAB\n"+
"-----END PUBLIC KEY-----\n";
//        String pem = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvNEz3+TStAAndHTc1qwTNGvZYyB7DD1FshQf+mbQUGJ9HccOXNn5oHB7fWQjODjlDrYyCs7FclSMTLxA3lHX98QWeWHL2O8t8qrJQQEUWZITmr/ddiNJOOvMeYF0K5if4m84vjgx/pTwwAVyU0YoMMXPnRozO8o7zSyRsH4jixALDugrsveEjLQI/cIEFvNjqlhyfumHyJKywNkMH1oJ4e/f89FkpeDV694lsLs1jguuLLnvroXYJ5Uzeos+F0Pj1zFDUvhWrjVwxsUfAxS85uFGTUm6EEl9XiKwi+mgg8ODrY5dh3uE2yKB2T1Qj8BfK55zB8cYbORSsm6/f6BiBwIDAQAB";
//        byte[] pemBytes = Base64.decodeBase64(pem);
//        PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(pemBytes));
        
        PublicKey willNotWork = X509Util.decodePemPublicKey(pem);  // you would get an exception like DerInputStream.getLength(): lengthTag=127, too big   ... ebcause this isn't an X509 certificate, it's an RSA public key w/o the certificate.
        //System.out.println("got public key? alg :"+willNotWork.getAlgorithm()+" format: "+willNotWork.getFormat());
    }

    @Test
    public void testReadAikKeyWithCarriageReturn() throws Exception {
        String pem = "-----BEGIN RSA PUBLIC KEY-----\r\n"+
"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvNEz3+TStAAndHTc1qwT\r\n"+
"NGvZYyB7DD1FshQf+mbQUGJ9HccOXNn5oHB7fWQjODjlDrYyCs7FclSMTLxA3lHX\r\n"+
"98QWeWHL2O8t8qrJQQEUWZITmr/ddiNJOOvMeYF0K5if4m84vjgx/pTwwAVyU0Yo\r\n"+
"MMXPnRozO8o7zSyRsH4jixALDugrsveEjLQI/cIEFvNjqlhyfumHyJKywNkMH1oJ\r\n"+
"4e/f89FkpeDV694lsLs1jguuLLnvroXYJ5Uzeos+F0Pj1zFDUvhWrjVwxsUfAxS8\r\n"+
"5uFGTUm6EEl9XiKwi+mgg8ODrY5dh3uE2yKB2T1Qj8BfK55zB8cYbORSsm6/f6Bi\r\n"+
"BwIDAQAB\r\n"+
"-----END RSA PUBLIC KEY-----\r\n";
//        String pem = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvNEz3+TStAAndHTc1qwTNGvZYyB7DD1FshQf+mbQUGJ9HccOXNn5oHB7fWQjODjlDrYyCs7FclSMTLxA3lHX98QWeWHL2O8t8qrJQQEUWZITmr/ddiNJOOvMeYF0K5if4m84vjgx/pTwwAVyU0YoMMXPnRozO8o7zSyRsH4jixALDugrsveEjLQI/cIEFvNjqlhyfumHyJKywNkMH1oJ4e/f89FkpeDV694lsLs1jguuLLnvroXYJ5Uzeos+F0Pj1zFDUvhWrjVwxsUfAxS85uFGTUm6EEl9XiKwi+mgg8ODrY5dh3uE2yKB2T1Qj8BfK55zB8cYbORSsm6/f6BiBwIDAQAB";
//        byte[] pemBytes = Base64.decodeBase64(pem);
//        PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(pemBytes));
        PublicKey willNotWork = X509Util.decodePemPublicKey(pem);  // you would get an exception like DerInputStream.getLength(): lengthTag=127, too big   ... ebcause this isn't an X509 certificate, it's an RSA public key w/o the certificate.
        System.out.println("got public key? alg :"+willNotWork.getAlgorithm()+" format: "+willNotWork.getFormat());
    }
    
    @Test
    public void testReadAikKeyWithStewartsKey() throws Exception {
        String pem = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvNEz3+TStAAndHTc1qwTNGvZYyB7DD1FshQf+mbQUGJ9HccOXNn5oHB7fWQjODjlDrYyCs7FclSMTLxA3lHX98QWeWHL2O8t8qrJQQEUWZITmr/ddiNJOOvMeYF0K5if4m84vjgx/pTwwAVyU0YoMMXPnRozO8o7zSyRsH4jixALDugrsveEjLQI/cIEFvNjqlhyfumHyJKywNkMH1oJ4e/f89FkpeDV694lsLs1jguuLLnvroXYJ5Uzeos+F0Pj1zFDUvhWrjVwxsUfAxS85uFGTUm6EEl9XiKwi+mgg8ODrY5dh3uE2yKB2T1Qj8BfK55zB8cYbORSsm6/f6BiBwIDAQAB-----END PUBLIC KEY-----";

//        String pem = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvNEz3+TStAAndHTc1qwTNGvZYyB7DD1FshQf+mbQUGJ9HccOXNn5oHB7fWQjODjlDrYyCs7FclSMTLxA3lHX98QWeWHL2O8t8qrJQQEUWZITmr/ddiNJOOvMeYF0K5if4m84vjgx/pTwwAVyU0YoMMXPnRozO8o7zSyRsH4jixALDugrsveEjLQI/cIEFvNjqlhyfumHyJKywNkMH1oJ4e/f89FkpeDV694lsLs1jguuLLnvroXYJ5Uzeos+F0Pj1zFDUvhWrjVwxsUfAxS85uFGTUm6EEl9XiKwi+mgg8ODrY5dh3uE2yKB2T1Qj8BfK55zB8cYbORSsm6/f6BiBwIDAQAB";
//        byte[] pemBytes = Base64.decodeBase64(pem);
//        PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(pemBytes));
        PublicKey willNotWork = X509Util.decodePemPublicKey(pem);  // you would get an exception like DerInputStream.getLength(): lengthTag=127, too big   ... ebcause this isn't an X509 certificate, it's an RSA public key w/o the certificate.
        System.out.println("testSTDkey got public key? alg :"+willNotWork.getAlgorithm()+" format: "+willNotWork.getFormat());
    }
    
    @Test(expected=InvalidKeySpecException.class)
    public void testReadAikKey2() throws Exception {
        String pem = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvNEz3+TStAAndHTc1qwTNGvZYyB7DD1FshQf+mbQUGJ9HccOXNn5oHB7fWQjODjlDrYyCs7FclSMTLxA3lHX98QWeWHL2O8t8qrJQQEUWZITmr/ddiNJOOvMeYF0K5if4m84vjgx/pTwwAVyU0YoMMXPnRozO8o7zSyRsH4jixALDugrsveEjLQI/cIEFvNjqlhyfumHyJKywNkMH1oJ4e/f89FkpeDV694lsLs1jguuLLnvroXYJ5Uzeos+F0Pj1zFDUvhWrjVwxsUfAxS85uFGTUm6EEl9XiKwi+mgg8ODrY5dh3uE2yKB2T1Qj8BfK55zB8cYbORSsm6/f6BiBwIDAQAB-----END PUBLIC KEY-----";
        byte[] der = Base64.decodeBase64(pem);
        PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(der)); // InvalidKeyException: IOException: DerInputStream.getLength(): lengthTag=111, too big.        
    }
    
    @Test
    public void testReadAikKey3() throws Exception {
        String pem = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvNEz3+TStAAndHTc1qwTNGvZYyB7DD1FshQf+mbQUGJ9HccOXNn5oHB7fWQjODjlDrYyCs7FclSMTLxA3lHX98QWeWHL2O8t8qrJQQEUWZITmr/ddiNJOOvMeYF0K5if4m84vjgx/pTwwAVyU0YoMMXPnRozO8o7zSyRsH4jixALDugrsveEjLQI/cIEFvNjqlhyfumHyJKywNkMH1oJ4e/f89FkpeDV694lsLs1jguuLLnvroXYJ5Uzeos+F0Pj1zFDUvhWrjVwxsUfAxS85uFGTUm6EEl9XiKwi+mgg8ODrY5dh3uE2yKB2T1Qj8BfK55zB8cYbORSsm6/f6BiBwIDAQAB-----END PUBLIC KEY-----";
        PublicKey publicKey = X509Util.decodePemPublicKey(pem);
    }
        
    
}
