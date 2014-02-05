/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mountwilson.http.security;

import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.mtwilson.security.http.RsaAuthorization;
import com.intel.dcsg.cpg.crypto.RsaCredential;
import java.io.IOException;
import java.security.*;
import java.util.HashMap;
/*
import java.security.SecureRandom;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.KeyGenerationParameters;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.RSAKeyParameters;
*/
import org.junit.Test;
//import sun.security.rsa.RSAKeyPairGenerator;

/**
 *
 * @author jbuhacoff
 */
public class RsaAuthorizationTest {
    private static int keySizeInBits = 1024;
    private static String httpUrlWithHost = "http://www.example.com";
    
    private RsaCredential generateRsaCredential() throws NoSuchAlgorithmException, CryptographyException {
        KeyPairGenerator r = KeyPairGenerator.getInstance("RSA");
        r.initialize(keySizeInBits);
        KeyPair keypair = r.generateKeyPair();
        RsaCredential credential = new RsaCredential(keypair);
        return credential;
        /*
        RSAKeyPairGenerator r = new RSAKeyPairGenerator();
        r.init(new KeyGenerationParameters(new SecureRandom(), keySizeInBits));
        AsymmetricCipherKeyPair keys = r.generateKeyPair();
        RSAKeyParameters publicKeyParams = (RSAKeyParameters)keys.getPublic();
        RSAKeyParameters privateKeyParams = (RSAKeyParameters)keys.getPrivate();
        RsaCredential = new RsaCredential(keys);
        */
    }
    
    /**
     * Sample output:
Authorization: X509 realm="Attestation", fingerprint="lT3X7MoJCv3ih/5XBcomMm+cbSSDJaeqURfHeabtDJg=", headers="X-Nonce,Date", algorithm="SHA256withRSA", signature="J/j5dwZ7VWKgrmxiRdLjsTU5CFM+NXfLPcmdCJty1PgH12s7qIy60tkEbUiD1OwGPeucfUfEVvEws6QW2cZFtLC7KcnkpWfSdezInmSDge2nf0pvFwhLmlHBzVaINYzg8unqgmy3xaubgwE9U9at3Gqqxx4/+yvZkUjSUPMlKmQ="
X-Nonce: AAABOCUjcyVrmT6abk0LEKN508dmuU8r
Date: Mon, 25 Jun 2012 12:34:36 PDT
     * These three headers would be added to the HTTP request before sending to the server.
     * 
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws IOException
     * @throws SignatureException 
     */
    @Test
    public void testGenerateRsaAuthorizationHeaderWithGET() throws NoSuchAlgorithmException, InvalidKeyException, IOException, SignatureException, CryptographyException {
        RsaCredential credential = generateRsaCredential();
        RsaAuthorization auth = new RsaAuthorization(credential);
        auth.setRealm("Attestation");
        HashMap<String,String> headers = new HashMap<String,String>();
        String authorizationHeader = auth.getAuthorization("GET", httpUrlWithHost, headers);
        System.out.println("Authorization: "+authorizationHeader);
        for(String key : headers.keySet()) {
            System.out.println(key+": "+headers.get(key));
        }
    }
    
    /**
     * Sample output:
Authorization: X509 realm="Attestation", fingerprint="lT3X7MoJCv3ih/5XBcomMm+cbSSDJaeqURfHeabtDJg=", headers="X-Nonce,Date", algorithm="SHA256withRSA", signature="J/j5dwZ7VWKgrmxiRdLjsTU5CFM+NXfLPcmdCJty1PgH12s7qIy60tkEbUiD1OwGPeucfUfEVvEws6QW2cZFtLC7KcnkpWfSdezInmSDge2nf0pvFwhLmlHBzVaINYzg8unqgmy3xaubgwE9U9at3Gqqxx4/+yvZkUjSUPMlKmQ="
X-Nonce: AAABOCUjcyVrmT6abk0LEKN508dmuU8r
Date: Mon, 25 Jun 2012 12:34:36 PDT
     * These three headers would be added to the HTTP request before sending to the server.
     * 
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws IOException
     * @throws SignatureException 
     */
    @Test
    public void testGenerateRsaAuthorizationHeaderWithPOST() throws NoSuchAlgorithmException, InvalidKeyException, IOException, SignatureException, CryptographyException {
        RsaCredential credential = generateRsaCredential();
        RsaAuthorization auth = new RsaAuthorization(credential);
        auth.setRealm("Attestation");
        HashMap<String,String> headers = new HashMap<String,String>();
        String requestBody = "This is the request body";
        String authorizationHeader = auth.getAuthorization("POST", httpUrlWithHost, headers, requestBody);
        System.out.println("Authorization: "+authorizationHeader);
        for(String key : headers.keySet()) {
            System.out.println(key+": "+headers.get(key));
        }
    }
    
}
