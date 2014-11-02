/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.rfc822;

import com.intel.dcsg.cpg.crypto.Aes;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.DigestAlgorithm;
import com.intel.dcsg.cpg.crypto.Sha256Digest;
import com.intel.dcsg.cpg.io.ByteArray;
import com.intel.dcsg.cpg.rfc822.GzipEncoder;
import com.intel.dcsg.cpg.rfc822.Message;
import com.intel.dcsg.cpg.rfc822.MessageReader;
import com.intel.dcsg.cpg.rfc822.MessageWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.crypto.SecretKey;
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
public class AesMessageWriter {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AesMessageWriter.class);

    private SecretKeyFinder finder;

    public void setSecretKeyFinder(SecretKeyFinder finder) {
        this.finder = finder;
    }
    
    public byte[] encryptString(String input, String keyId) throws IOException, CryptographyException, KeyNotFoundException {
        SecretKey key = finder.find(keyId);
        if( key == null ) {
            throw new KeyNotFoundException(keyId);
        }
        return encryptString(input, key, keyId);
    }    

    public byte[] encryptByteArray(byte[] input, String keyId) throws IOException, CryptographyException, KeyNotFoundException {
        SecretKey key = finder.find(keyId);
        if( key == null ) {
            throw new KeyNotFoundException(keyId);
        }
        return encryptByteArray(input, key, keyId);
    }    

    public byte[] encryptMessage(Message input, String keyId) throws IOException, CryptographyException, KeyNotFoundException {
        SecretKey key = finder.find(keyId);
        if( key == null ) {
            throw new KeyNotFoundException(keyId);
        }
        return encryptMessage(input, key, keyId);
    }    
    
    // we return the message object so caller can add non-protected headers
    
    /**
     * 
     * @param input any string content... will be automatically wrapped in a message/rfc822 with content-type="text/plain; charset=UTF-8" and gzip transfer-encoding before encrypting
     * @param key
     * @param keyName
     * @return
     * @throws IOException
     * @throws CryptographyException 
     */
    public byte[] encryptString(String input, SecretKey key, String keyName) throws IOException, CryptographyException {
        Charset utf8 = Charset.forName("UTF-8");
        GzipEncoder gzip = new GzipEncoder();
        byte[] gzipContent = gzip.encode(input.getBytes(utf8));
        Message message = new Message();
        message.setContent(gzipContent);
        message.setContentLength(gzipContent.length);
        message.setContentType("text/plain; charset=\"UTF-8\"");
        message.setContentTransferEncoding("gzip");
        log.debug("Intermediate message headers (gzip body not shown):\n{}", message.getHeaderText());
        return encryptMessage(message, key, keyName);
    }
        
    /**
     * 
     * @param input whatever needs to be protected with confidentiality and integrity... the result is a serialized message/rfc822 with an enclosed application/octet-stream (the input)
     * @param key
     * @param keyName
     * @return
     * @throws IOException
     * @throws CryptographyException 
     */
    public byte[] encryptByteArray(byte[] input, SecretKey key, String keyName) throws IOException, CryptographyException {
        return encrypt(input, "application/octet-stream", key, keyName);
    }    
    
    /**
     * 
     * @param message with optional body content and headers that need confidentiality and integrity protection... the result is a serialized message/rfc822 with an enclosed (encrypted) message/rfc822 (the input) 
     * @param key
     * @param keyName
     * @return
     * @throws IOException
     * @throws CryptographyException 
     */
    public byte[] encryptMessage(Message message, SecretKey key, String keyName) throws IOException, CryptographyException {
        return encrypt(message.toByteArray(), "message/rfc822", key, keyName);
    }
    


    /**
     * Encrypts the input using the given key and wraps that in a message/rfc822 entity
     * 
     * @param input
     * @param contentType of the input
     * @param key to encrypt the input
     * @param keyName to identify the key used to encrypt the input
     * @return serialized message/rfc822 whose content is the input encrypted with the key and using base64 content-transfer-encoding
     * @throws IOException
     * @throws CryptographyException 
     */
    public byte[] encrypt(byte[] input, String contentType, SecretKey key, String keyName) throws IOException, CryptographyException {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256"); // default digest-alg to use
            return encrypt(input, contentType, key, keyName, sha256);
        }
        catch(NoSuchAlgorithmException e) {
            throw new CryptographyException(e);
        }
    }
    
    public byte[] encrypt(byte[] input, String contentType, SecretKey key, String keyName, MessageDigest md) throws IOException, CryptographyException {
        // encrypting it,  and base64-encode the encrypted message when saving it.
        byte[] digestBytes = md.digest(input);
        byte[] messageBytesWithIntegrity = ByteArray.concat(input, digestBytes);
        log.debug("message bytes with integrity: {} bytes", messageBytesWithIntegrity.length);
        // 3) encrypt the entire message with integrity protection, and use base64 transfer encoding to help against accidental/copy-paste corruption
//        SecretKey key = Aes.generateKey(128); // throws CryptographyException
        Aes aes = new Aes(key);
        byte[] encrypted = aes.encrypt(messageBytesWithIntegrity);
        Message encryptedMessage = new Message();
        encryptedMessage.setContent(Base64.encodeBase64Chunked(encrypted));
        encryptedMessage.setContentLength(encrypted.length);
        encryptedMessage.setContentType("encrypted/java; enclosed=\""+contentType+"\"; alg=\"AES/OFB8/NoPadding\"; key=\"" + keyName + "\"; digest-alg=\"" + md.getAlgorithm() + "\""); // rfc822 content-type parameters for encrypted/* may change ,especially the "enclosed" parameter name and the format of its value to be able to indicate wrapped content like our gzip'd text/plain...  we don't want to specify just text/plain because that doesn't indicate it's gzip'd, but we also don't want to indicate just gzip because that doesn't indicate what we get after we unzip...
        encryptedMessage.setContentTransferEncoding("base64"); // this encoding is applied to the encrypted content
        return encryptedMessage.toByteArray();
    }
    
}
