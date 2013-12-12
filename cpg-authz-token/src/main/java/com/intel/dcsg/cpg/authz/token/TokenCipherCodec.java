/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.authz.token;

import com.intel.dcsg.cpg.crypto.Aes;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.key.CipherCodec;
//import com.intel.dcsg.cpg.crypto.key.Decrypted;
import com.intel.dcsg.cpg.crypto.key.Ciphertext;
import com.intel.dcsg.cpg.crypto.key.EncryptionKey;
import com.intel.dcsg.cpg.crypto.key.EncryptionKeySource;
import com.intel.dcsg.cpg.crypto.key.Plaintext;
import com.intel.dcsg.cpg.crypto.key.Protection;
import com.intel.dcsg.cpg.util.ByteArray;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 *
 * @author jbuhacoff
 */
public class TokenCipherCodec extends CipherCodec<Token> {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TokenCipherCodec.class);
    
    public TokenCipherCodec(EncryptionKeySource encryptionKeySource, Protection protection) {
        super(encryptionKeySource, protection);
    }

    
    @Override
    protected byte[] formatPlaintextMessage(Token object) {
        return object.encodeContent();
    }

    @Override
    protected byte[] formatPlaintext(Plaintext plaintext, Token object) {
        return ByteArray.concat(plaintext.getMessage(),plaintext.getDigest());
    }

    @Override
    protected byte[] formatCiphertext(Ciphertext ciphertext, Token object) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(buffer);
        try {
            out.writeByte(object.getVersion());
            out.write(ciphertext.getKeyId());
            out.write(ciphertext.getIv());
            out.write(ciphertext.getMessage());
            out.close();
        } catch (IOException e) {
            throw new IllegalStateException(e); // we should never get an IOException from ByteArrayOutputStream
        }
        return buffer.toByteArray();
    }

    /**
     * Version 1 token structure:
     * 
     * 1 byte version
     * 16 bytes key id
     * 16 bytes iv (aes128)
     * 16+ bytes nonce
     * 8 bytes timestamp
     * 0+ bytes body/payload
     * 32 bytes digest (sha256)
     * 
     * So minimum length of a valid version 1 token is 89 bytes. 
     * Any additional length is due to variable-length nonce and payload.
     * 
     * 
     * @param ciphertext
     * @return 
     */
    @Override
    protected Ciphertext parseCiphertext(byte[] ciphertext) {
        if( ciphertext.length < 89 ) {
            throw new IllegalArgumentException("Invalid token"); // see comment on method about minimum length of valid token
        }
        try {
            Ciphertext encrypted = new Ciphertext();
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(ciphertext));
            // first read the public non-protected header
            byte tokenVersion = in.readByte();
            if (tokenVersion != 1) {
                throw new IllegalArgumentException(String.format("Invalid token version %x", tokenVersion));
            }            
            byte[] keyId = new byte[16];
            in.read(keyId); // because we are reading from a byte array input stream and we already checked the minimum length we know the bytes are there; and version 1 tokens cannot be read from an input stream because they are variable length with no embedded length parameters
            byte[] iv = new byte[16];
            in.read(iv);
            // we finished reading the header:  version (1 byte) keyId (16 bytes) iv (16 bytes), the rest is the encrypted message
            int headerLength = 1 + keyId.length + iv.length;
            byte[] content = new byte[ciphertext.length - headerLength]; // safe because we ensured we have a minimum-length token up front
            in.read(content); // because we are reading here all the rest of the bytes of the byte array input stream this will always work; could also use readFully here
            in.close();
            
            encrypted.setIv(iv);
            encrypted.setKeyId(keyId);
            encrypted.setMessage(content);
            
            // hard-coded values for token version 1:
            encrypted.setProtection(getProtection());
            /*
            Protection protection = new Protection();
            protection.setAlgorithm("AES");
            protection.setBlockSizeBytes(16);
            protection.setDigestAlgorithm("SHA-256");
            protection.setKeyLengthBits(128);
            protection.setMode("OFB8");
            protection.setPadding("NoPadding");
            */
            return encrypted;
        }
        catch (IOException e) {
            throw new IllegalArgumentException("Cannot read token", e);
        } 
    }

    @Override
    protected Plaintext parsePlaintext(byte[] decrypted, Protection protection) {
        Plaintext plaintext = new Plaintext();
        plaintext.setProtection(protection);
        int mdlength = protection.getDigestSizeBytes();
        plaintext.setMessage( ByteArray.subarray(decrypted, 0, decrypted.length-mdlength) );
        plaintext.setDigest( ByteArray.subarray(decrypted, decrypted.length-mdlength, mdlength) );
        return plaintext;
        /*
        catch(NoSuchAlgorithmException e) {
            log.error("Token version 1 requires SHA-256");
            throw new UnsupportedTokenVersionException((byte)1, e);
        }
         * 
         */
    }

    @Override
    protected Token parsePlaintextMessage(byte[] message) {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
        try {
        short nonceLength = in.readShort();
           byte[] nonce = new byte[nonceLength];
            int confirmNonceLength = in.read(nonce);
            if (confirmNonceLength != nonceLength) {
                throw new IllegalArgumentException("Invalid token");
            }
            byte authenticatedTokenVersion = in.readByte();
            if (authenticatedTokenVersion != 1) { // XXX HARD-CODED TOKEN VERSION  because this is really the TokenV1CipherCodec  ... the token version read in parseCiphertext was checked to make sure we knew how to separate the untrusted headers from the protected message, and here we have a trusted message (decrypted & integrity checked) but the token version is not our version  so we need to leave it alone.  we can only get here if an attacker tampered with the insecure token version,   the token reader used to determine which codec to call, the outer format was compatible with the true version but now thatis' been authenticated we see that the true token version is not the one we handle. so we have to abort!
                throw new IllegalArgumentException("Invalid token");
            }
            long timestamp = in.readLong();
            /*
            if( expired(timestamp) ) {
                throw new IllegalArgumentException("Invalid token"); // expired
            }*/
            short contentLength = in.readShort();
            byte[] content = new byte[contentLength];
            int confirmContentLength = in.read(content);
            if (confirmContentLength != contentLength) {
                throw new IllegalArgumentException("Invalid token");
            }
//            byte[] content = new String(_userId, Charset.forName("UTF-8"));
            
            // prepare the token; application needs to know key id (might be linked to a security context such as user session, server cluster, etc), nonce (optionally to prevent replay attacks), timestamp (to decide if it's expired), and content (the protected message embedded in the token)
                    Token token = new Token();
//                    token.setKeyId(keyId);
                    token.setNonce(nonce);
                    token.setTimestamp(timestamp);
                    token.setContent(content);
                    return token;
        } 
        catch (IOException e) {
            throw new IllegalArgumentException("Cannot read token", e);
        } 
            }
}
