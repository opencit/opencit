/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.authz.token;

import com.intel.dcsg.cpg.crypto.key.RandomSource;
import com.intel.dcsg.cpg.crypto.Aes;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.key.EncryptionKey;
import com.intel.dcsg.cpg.crypto.key.EncryptionKeySource;
import com.intel.dcsg.cpg.crypto.key.HashMapMutableSecretKeyRepository;
import com.intel.dcsg.cpg.crypto.key.Protection;
import com.intel.dcsg.cpg.crypto.key.ProtectionBuilder;
import com.intel.dcsg.cpg.crypto.key.ProtectionPolicy;
import com.intel.dcsg.cpg.crypto.key.ProtectionPolicyBuilder;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.io.ByteArray;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import javax.crypto.SecretKey;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author jbuhacoff
 */
public class TokenFactory {
    private HashMapMutableSecretKeyRepository repository;
    private EncryptionKeySource encryptionKeySource;
    private TokenCipherCodec codec;
    private transient RandomSource random;
    private Protection protection;
    private ProtectionPolicy protectionPolicy;
    
    public TokenFactory() {
        protectionPolicy = ProtectionPolicyBuilder.factory().stream().aes128().sha2().build(); // we'll accept AES-128/192/256 and SHA-256/384/512 for incoming tokens
        protection = ProtectionBuilder.factory().aes(128).mode("OFB8").padding("NoPadding").sha256().build(); // we'll output AES-128 and SHA-256 for new tokens
        repository = new HashMapMutableSecretKeyRepository(new HashMap<String,EncryptionKey>());
        encryptionKeySource = new EncryptionKeySource(repository);
        codec = new TokenCipherCodec(encryptionKeySource, protection);
        random = new RandomSource();
    }
    public void setRandom(RandomSource random) {
        this.random = random;
    }

    public void setRandom(SecureRandom random) {
        this.random = new RandomSource(random);
    }

    public EncryptionKeySource getEncryptionKeySource() {
        return encryptionKeySource;
    }

    public Protection getProtection() {
        return protection;
    }

    public ProtectionPolicy getProtectionPolicy() {
        return protectionPolicy;
    }

    public TokenCipherCodec getCodec() {
        return codec;
    }
    
    

    public RandomSource getRandom() {
        return random;
    }

    public HashMapMutableSecretKeyRepository getRepository() {
        return repository;
    }
    
    public String create(String userId) throws GeneralSecurityException {
        Token token = new Token();
        int blocksize = protection.getBlockSizeBytes(); // in a later version we might get the block size from the key, for example key.getEncoded().length;   but in version 1 we always use AES-128 so 16 bytes
        token.setNonce(random.nextBytes(random.nextInt(256 - blocksize) + blocksize));
        token.setTimestamp(System.currentTimeMillis() / 1000L);
        token.setContent(userId.getBytes(Charset.forName("UTF-8")));
        byte[] encryptedToken = codec.encrypt(token); // throws GeneralSecurityException
        return Base64.encodeBase64String(encryptedToken);
    }
}
