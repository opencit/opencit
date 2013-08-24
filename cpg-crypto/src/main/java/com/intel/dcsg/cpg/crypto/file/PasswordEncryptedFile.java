/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.file;

import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.DigestAlgorithm;
import com.intel.dcsg.cpg.crypto.PasswordCipher;
import com.intel.dcsg.cpg.crypto.PasswordHash;
import com.intel.dcsg.cpg.crypto.Sha256Digest;
import com.intel.dcsg.cpg.io.ByteArray;
import com.intel.dcsg.cpg.io.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convenience class to using PasswordCipher, PasswordHash, DataEnvelope, and Sha256Digest to 
 * read/write an encrypted file with integrity checking.
 * 
 * For simplicity, this class does not support streaming. In the future, another class may provide
 * a streaming interface to encrypt/decrypt files with integrity checking, possibly using the Java
 * cipher input/output streams.
 * 
 * Security provided:  confidentiality and integrity.
 * 
 * This class does NOT provide authentication: it's somewhat implied by knowing the decryption password
 * but since there can only be one password per file, if it is shared with anyone then it is not possible
 * to know who entered the password.
 * 
 * This class does NOT provide availability protection (denial of service protection): it is trivial to prevent someone from being
 * able to open an encrypted file, by simply deleting the encryption key id header or by tampering with
 * the body so it fails the integrity check (change just one character...), or by deleting the file, or
 * by overwriting its content.
 * 
 * How to create a new password-encrypted file:
 * FileResource resource = new FileResource(new File("/path/to/file"));
 * PasswordEncryptedFile encFile = new PasswordEncryptedFile(resource, "password");
 * encFile.saveString("content to encrypt");
 * 
 * How to decrypt an existing password-encrypted file:
 * ExistingFileResource resource = new ExistingFileResource(new File("/path/to/file"));
 * PasswordEncryptedFile encFile = new PasswordEncryptedFile(resource, "password");
 * String content = encFile.loadString();
 * 
 * The file is always stored using a data envelope. The following example is the text
 * "hello world" encrypted using the password "password":
 * 
-----BEGIN ENCRYPTED DATA-----
Content-Encoding: base64
Encryption-Algorithm: PBEWithSHA1AndDESede/CBC/PKCS5Padding
Encryption-Key-Id: 64F+NNoqOBw=:pqoRIUsHQtb+nCj7sKTrrocJNN7T6MT+Xi6N8b7nxD0=
Integrity-Algorithm: SHA256

ZxcjQ9OMUFqGmgcJ88HK8j7spnFKD0sZxMpZZfNbg9DmLgYW23DUgPsCl7HEppqohg8GFqeI7qo=
-----END ENCRYPTED DATA-----
 * 
 * 
 * The encoded content has this structure:   base64(salt||pbe(content-sha256||content))
 * 
 * The integrity digest is encrypted together with the content in order to avoid 
 * leaking information about well-known content (the sha256 of "hello world" is always
 * the same so if the encrypted file contains "hello world" and the sha256 of it is 
 * not encrypted, someone can easily tell what is the content of the file without 
 * having to decrypt it).  HMAC is not necessary because the integrity digest is encrypted
 * and is not used to authenticate the message, and because the digest is only useful when
 * the plaintext content is available -- it would do a user no good to "verify" an HMAC
 * for an encrypted file without first decrypting it to compute the sha256 digest.
 * 
 * See also:
 * RFC 3230, Instance Digests in HTTP 
 * RFC 1864, The Content-MD5 Header Field
 * 
 * @since 0.1.1
 * @author jbuhacoff
 */
public class PasswordEncryptedFile {
    public static final String ENCRYPTION_ALGORITHM = "Encryption-Algorithm";
    public static final String ENCRYPTION_KEY_ID = "Encryption-Key-Id";
    public static final String CONTENT_ENCODING = "Content-Encoding";
//    public static final String CONTENT_SHA256 = "Content-SHA256";
    public static final String INTEGRITY_ALGORITHM = "Integrity-Algorithm";
    private Logger log = LoggerFactory.getLogger(getClass());
    
    private Resource resource;
    private String password;
    private PasswordCipher cipher;
    public PasswordEncryptedFile(Resource resource, String password) {
        this.resource = resource;
        this.password = password;
        this.cipher = new PasswordCipher(password);
    }
    
    public Resource getResource() { return resource; }
    
    public String loadString() throws IOException {
        // read & decrypt the file, then provide a reader to the in-memory decrypted contents
        InputStream in = resource.getInputStream(); // may throw FileNotFoundException
        String content = IOUtils.toString(in); // throws IOException
        DataEnvelope envelope = DataEnvelope.fromPem(content); // throws IllegalArgumentException if the input is not in PEM format
        IOUtils.closeQuietly(in);
        
        try {
            // we check the password separately from the content integrity check
            // in order to give the user better fidelity: is the password wrong or is the file corrupted? 
            // compare the password we got in the constructor to the envelope-key-id mentioned in the envelope
            if(!isCorrectPassword(envelope)) {
                throw new CryptographyException("Incorrect password");
            }
            
            byte[] ciphertext = envelope.getContent(); // XXX TODO needs to take into account the Content-Encoding... maybe pass it as a parameter? or maybe the envelope class should recognize that header by itself?
            byte[] plaintext = cipher.decrypt(ciphertext);
            
            String integrityAlgorithm = envelope.getHeader(INTEGRITY_ALGORITHM);
            if( integrityAlgorithm.equals(DigestAlgorithm.SHA256.name())) { // SHA256, not SHA-256 ... maybe allow both?
                log.debug("Integrity algorithm: SHA256");
                byte[] plaintextAfterIntegrity = getPlaintextWithIntegrity(plaintext, DigestAlgorithm.SHA256);
                log.debug("Decrypted text length: {}", plaintextAfterIntegrity.length);
                return new String(plaintextAfterIntegrity);
            }
            else if( integrityAlgorithm.equals(DigestAlgorithm.SHA1.name())) { // SHA1, not SHA-1 ... maybe allow both?
                log.debug("Integrity algorithm: SHA1");
                byte[] plaintextAfterIntegrity = getPlaintextWithIntegrity(plaintext, DigestAlgorithm.SHA1);
                log.debug("Decrypted text length: {}", plaintextAfterIntegrity.length);
                return new String(plaintextAfterIntegrity);
            }
            else if( integrityAlgorithm.equals(DigestAlgorithm.MD5.name())) { // MD5
                log.debug("Integrity algorithm: MD5");
                byte[] plaintextAfterIntegrity = getPlaintextWithIntegrity(plaintext, DigestAlgorithm.MD5);
                log.debug("Decrypted text length: {}", plaintextAfterIntegrity.length);
                return new String(plaintextAfterIntegrity);
            }
            else {
                throw new IOException("Unsupported integrity algorithm: "+integrityAlgorithm);
            }
            
        }
        catch(CryptographyException e) {
            throw new IOException(e);
        }        
    }
    
    public void saveString(String content) throws IOException {
        byte[] plaintext = content.getBytes();
        try {
            Sha256Digest sha256 = Sha256Digest.digestOf(plaintext);
            byte[] plaintextWithIntegrity = ByteArray.concat(sha256.toByteArray(), plaintext);
            
            byte[] ciphertext = cipher.encrypt(plaintextWithIntegrity);
            
            // wrap the cipher text in a data envelope so we can record the integrity check and the cipher details
            DataEnvelope envelope = new DataEnvelope();
            envelope.setHeader(ENCRYPTION_ALGORITHM, cipher.getAlgorithm());
            PasswordHash passwordHash = new PasswordHash(password); // generates a random salt
            envelope.setHeader(ENCRYPTION_KEY_ID, passwordHash.toString()); // produces salt-base64:sha256-base64
            envelope.setHeader(CONTENT_ENCODING, "base64"); // XXX currently it's the only supported method, so it's ignored when reading in the file
//            envelope.setHeader("PBE-Iterations", cipher.getIterations()); // not supported yet;  currently number of iterations is hard-coded in the PasswordCipher based on the chosen algorithm
//            envelope.setHeader("SHA256-Iterations", sha256Interations); // not implemented yet;  need to do a benchmark for sha256 similar to the one for PBE, so that a similar delay is introduced, so that both the hash and the PBE delay brute force attacks to a similar degree
            envelope.setHeader(INTEGRITY_ALGORITHM, sha256.algorithm());
//            envelope.setHeader("IntegrityEncoding", "hex"); // XXX probably should specify this but currently we don't support any other encoding, and it's easy enough to detect if a sha256 value is hex or base64 by checking its length and character set
//            log.debug("sha256: {}", sha256);
//            log.debug("envelope: {}", envelope);
//            envelope.setHeader(CONTENT_SHA256, sha256.toBase64());
            envelope.setContent(ciphertext);
            OutputStream out = resource.getOutputStream();
            IOUtils.write(envelope.toPem(), out);
            IOUtils.closeQuietly(out);
        }
        catch(CryptographyException e) {
            throw new IOException(e);
        }        
    }
    
    private boolean isCorrectPassword(DataEnvelope envelope) throws CryptographyException {
        PasswordHash savedPasswordHash = PasswordHash.valueOf(envelope.getHeader(ENCRYPTION_KEY_ID));
        PasswordHash comparePasswordHash = new PasswordHash(password, savedPasswordHash.getSalt());
        return comparePasswordHash.getHashBase64().equals(savedPasswordHash.getHashBase64());
    }
    
    private byte[] getPlaintextWithIntegrity(byte[] plaintext, DigestAlgorithm algorithm) throws IOException {
        // extract the saved integrity measurement from the plaintext
        byte[] integrity = new byte[algorithm.length()];
        System.arraycopy(plaintext, 0, integrity, 0, algorithm.length());
        // calculate the digest of the plaintext not including the prepended integrity hash
        byte[] plaintextAfterIntegrity = ByteArray.subarray(plaintext, integrity.length);
        byte[] digest = algorithm.digest(plaintextAfterIntegrity);
        // compare the asved measurement to the one we just calculated
        if(!Arrays.equals(digest, integrity)) {
            throw new IOException("Content integrity check failed"); // if we had a NamedResource interface, we might say "Content integrity check failed for resource: "+resource.getName(); or maybe +resource.getURL();
        }
        return plaintextAfterIntegrity;
    }
}
