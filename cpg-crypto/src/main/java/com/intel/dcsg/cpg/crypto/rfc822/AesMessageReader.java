/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.rfc822;

import com.intel.dcsg.cpg.crypto.Aes;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.DigestAlgorithm;
import com.intel.dcsg.cpg.crypto.Sha256Digest;
import com.intel.dcsg.cpg.util.ByteArray;
import com.intel.dcsg.cpg.rfc822.GzipEncoder;
import com.intel.dcsg.cpg.rfc822.Message;
import com.intel.dcsg.cpg.rfc822.MessageReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;
import javax.mail.internet.ContentType;
import javax.mail.internet.ParameterList;
import javax.mail.internet.ParseException;
import org.apache.commons.codec.binary.Base64;

/**
 * Given String or byte[] input and an encryption key, generates a message/rfc822 entity with the encrypted input,
 * suitable for storing to disk or sending over the network. The encryption key must be labeled for the receiver to
 * identify it for decrypting and reading the String or byte[] back.
 *
 * This class requires cpg-rfc822, which is an optional dependency of cpg-crypto so you must list it separately.
 *
 * XXX TODO rename this class?
 *
 * @author jbuhacoff
 */
public class AesMessageReader {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AesMessageReader.class);

    private SecretKeyFinder finder;

    public void setSecretKeyFinder(SecretKeyFinder finder) {
        this.finder = finder;
    }
    // we return the message object so caller can add non-protected headers

    /**
     * Expects the encryptedMessageBytes to be a message/rfc822 entity with content-type encrypted/java enclosing a message/rfc822 object with content-type text/plain
     * @param encryptedMessageBytes
     * @return
     * @throws IOException
     * @throws CryptographyException
     * @throws KeyNotFoundException
     * @throws NoSuchAlgorithmException 
     */
    public String decryptString(byte[] encryptedMessageBytes) throws IOException, CryptographyException, KeyNotFoundException, NoSuchAlgorithmException {
        Message message = decryptMessage(encryptedMessageBytes);
        if( !message.getContentType().startsWith("text/plain") ) {
            throw new UnsupportedOperationException("Expected Content-Type text/plain but received "+message.getContentType());            
        }
        Map<String,String> contentTypeParameters = getHeaderParameters(message.getContentType());
        String charsetName = contentTypeParameters.get("charset");
        Charset charset = charsetName == null ? Charset.forName("UTF-8") : Charset.forName(charsetName);
        MessageReader reader = new MessageReader();
        byte[] textBytes = reader.read(message);
        return new String(textBytes, charset); 
    }

    public Message decryptMessage(byte[] encryptedMessageBytes) throws IOException, CryptographyException, KeyNotFoundException, NoSuchAlgorithmException {
        byte[] messageBytes = decrypt(encryptedMessageBytes);
        Message message = Message.parse(messageBytes);
        return message;
    }
    
    /**
     * The content-type of the wrapped message is not checked, it's simply decoded and returned
     * @param encryptedMessageBytes
     * @return
     * @throws IOException
     * @throws CryptographyException
     * @throws KeyNotFoundException
     * @throws NoSuchAlgorithmException 
     */
    public byte[] decryptByteArray(byte[] encryptedMessageBytes) throws IOException, CryptographyException, KeyNotFoundException, NoSuchAlgorithmException {
        byte[] messageBytes = decrypt(encryptedMessageBytes);
        log.debug("Message bytes length {}", messageBytes.length);
        return messageBytes;
    }
    
    public byte[] decrypt(byte[] encryptedMessageBytes) throws IOException, CryptographyException, KeyNotFoundException, NoSuchAlgorithmException {
        Message encryptedMessage = Message.parse(encryptedMessageBytes);
        if (!encryptedMessage.getContentType().startsWith("encrypted/java")) {
            throw new UnsupportedOperationException("Content-Type must be encrypted/java");
        }
        Map<String, String> contentTypeParameters = getHeaderParameters(encryptedMessage.getContentType());
        // read required parameters
        String algorithmName = contentTypeParameters.get("alg");
        String keyId = contentTypeParameters.get("key");
        String digestAlgorithmName = contentTypeParameters.get("digest-alg"); //"SHA256"; // XXX TODO ... let's pretend  the algorithm name was extracted from "digest-alg" parameter
        // obtain the key... XXX should the finder operate on key id only or key id + alg name? and if using alg name too then it should be just aes, rsa, ecc... w/o the cipher mode or padding mode
        SecretKey key = finder.find(keyId);
        if( key == null ) {
            throw new KeyNotFoundException(keyId); // XXX should this be a ava.security.KeyManagementException ??? callers ought to know specifically that the key was not found... maybe we need a subclass of that like KeyNotFoundException ?
        }
        // we can use any digest algorithm available on the platform. most jvms built-in crypto provider has at least MD2, MD5, SHA-1, SHA-256, SHA-384, SHA-512.
        MessageDigest md = MessageDigest.getInstance(digestAlgorithmName); // throws NoSuchAlgorithmException
        int mdlength = md.getDigestLength();
        if( mdlength == 0 ) {
            throw new UnsupportedOperationException("Unsupported digest algorithm: "+digestAlgorithmName); // the digest algorithm provider MUST tell us how long the digests are, so we can grab them from the end of the message
        }
        // currently we only support AES/OFB8/NoPadding but we should be able to extend this to the other algorithms and modes 
        if( !"AES/OFB8/NoPadding".equals(algorithmName)) {
            throw new UnsupportedOperationException("Unsupported encryption algorithm or mode: "+algorithmName);
        }
        // decrypt
        MessageReader reader = new MessageReader();
        byte[] encrypted = reader.read(encryptedMessage); // automatically base64-decodes if content-transfer-encoding was base64
        Aes aes = new Aes(key);
        byte[] messageBytesWithIntegrity = aes.decrypt(encrypted);
        log.debug("message bytes with integrity: {} bytes", messageBytesWithIntegrity.length);
        // verify message integrity
        byte[] messageBytes = ByteArray.subarray(messageBytesWithIntegrity, 0, messageBytesWithIntegrity.length-mdlength);
        byte[] inputDigest = ByteArray.subarray(messageBytesWithIntegrity, messageBytesWithIntegrity.length-mdlength, mdlength);
        log.debug("message bytes: {}", messageBytes.length);
        log.debug("digest bytes: {}", inputDigest.length);
        byte[] computedDigest = md.digest(messageBytes);
        if (!Arrays.equals(inputDigest, computedDigest)) {
            throw new CryptographyException("Message integrity digest failed verification"); // we intentionally do not provide either digest in the error message
        }
        // now we can simply returned the enclosed content... the reader can interpet it... they can use the "enclosed" parameter as a hint if they don't already know what to expect
        return messageBytes;
    }
    
    private Map<String,String> getHeaderParameters(String headerValue) throws IOException {
        HashMap<String, String> contentTypeParameters = new HashMap<String, String>();
        try {
            ContentType header = new ContentType(headerValue); // throws ParseException
            ParameterList plist = header.getParameterList();
            Enumeration<String> pnames = plist.getNames();
            while (pnames.hasMoreElements()) {
                String pname = pnames.nextElement();
                contentTypeParameters.put(pname, plist.get(pname));
            }
        } catch (ParseException e) {
            throw new IOException("Cannot parse Content-Type header parameters", e);
        }
        return contentTypeParameters;
    }
}
