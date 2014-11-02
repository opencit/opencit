/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.key.password;

import com.intel.dcsg.cpg.crypto.key.Protection;
import com.intel.dcsg.cpg.crypto.key.ProtectionBuilder;
import com.intel.dcsg.cpg.crypto.key.password.PasswordCryptoCodecFactory.EncryptionAlgorithmInfo;
import com.intel.dcsg.cpg.crypto.key.password.PasswordCryptoCodecFactory.KeyAlgorithmInfo;
import com.intel.dcsg.cpg.rfc822.Rfc822Header;
import java.io.IOException;
import java.util.Map;

/**
 *
 * @author jbuhacoff
 */
public class PasswordProtectionBuilder extends ProtectionBuilder {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PasswordProtectionBuilder.class);
    protected PasswordProtection passwordProtection = new PasswordProtection();

    public static PasswordProtectionBuilder factory() {
        return new PasswordProtectionBuilder();
    }

    // TODO:  need to simplify this and move all the rfc822 code to PasswordEncryptedFile where it's actually used -- we should not do that kind of microformat here, and remove the rfc822 dependency
    public PasswordProtectionBuilder keyAlgorithm(String keyAlgorithm) {
        passwordProtection.keyAlgorithm = keyAlgorithm; // for example PBKDF2WithHmacSHA1
        KeyAlgorithmInfo keyAlgInfo = PasswordCryptoCodecFactory.getKeyAlgorithmInfo(keyAlgorithm);
        if (keyAlgInfo != null) {
            log.debug("found key algorithm info, iterations {} salt bytes {}", keyAlgInfo.getIterations(), keyAlgInfo.getSaltBytes());
            if (passwordProtection.iterations == 0) {
                iterations(keyAlgInfo.getIterations());
                log.warn("Using default iteration count {}; it should be specified explicitly", passwordProtection.iterations);
            }
            if (passwordProtection.saltBytes == 0) {
                saltBytes(keyAlgInfo.getSaltBytes());
                log.warn("Using default salt bytes {}; it should be specified explicitly", passwordProtection.saltBytes);
            }
            if (super.protection.getAlgorithm() == null && keyAlgInfo.isEncryptionAlgorithm()) {
                log.debug("using default encryption algorithm info");
                algorithm(passwordProtection.keyAlgorithm);
            }
        }
        return this;
    }

    public PasswordProtectionBuilder saltBytes(int saltBytes) {
        passwordProtection.saltBytes = saltBytes;
        return this;
    }

    public PasswordProtectionBuilder iterations(int iterations) {
        passwordProtection.iterations = iterations;
        return this;
    }

    /*
     PBEWithMD5AndDES("PBEWithMD5AndDES","PBEWithMD5AndDES",56,8,524288, new PBESecretKeyGenerator()),
     PBEWithMD5AndTripleDES("PBEWithMD5AndTripleDES","PBEWithMD5AndTripleDES",168,8,2097152, new PBESecretKeyGenerator()),
     PBEWithSHA1AndDESede("PBEWithSHA1AndDESede","PBEWithSHA1AndDESede",168,8,1048576, new PBESecretKeyGenerator()),
     //        PBEWithSHA1AndAES128("PBEWithSHA1AndAES","PBEWithSHA1AndAES",128,8,1048576), // oracle docs imply this is valid but we get NoSuchAlgorithmException: PBEWithSHA1AndAES SecretKeyFactory not available    http://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html
     //        PBEWithSHA1AndAES128("PBEWithMD5AndAES","PBEWithMD5AndAES",128,8,1048576), // oracle docs imply this is valid but we get NoSuchAlgorithmException
     PBKDF2WithHmacSHA1AndAES128("PBKDF2WithHmacSHA1","AES",128,8,1048576, new PBKDFSecretKeyGenerator()),
     PBKDF2WithHmacSHA1AndAES256("PBKDF2WithHmacSHA1","AES",256,8,1048576, new PBKDFSecretKeyGenerator());
     //        PBKDF2WithHMACSHA1(256,16,1);   //  currently not supported because in the cipher code, when we create the cipher we then need to request an "AES" cipher, which is a different name, and has different initializatino code, and an IV that needs to be stored along with the salt...
     * 
     */
    public PasswordProtectionBuilder pbkdf2WithHmacSha1() {
        passwordProtection.keyAlgorithm = "PBKDF2WithHmacSHA1";
        return this;
    }

    @Override
    public PasswordProtectionBuilder aes(int keyLengthBits) {
        super.aes(keyLengthBits);
        return this;
    }

    @Override
    public PasswordProtectionBuilder sha1() {
        super.sha1();
        return this;
    }

    @Override
    public PasswordProtectionBuilder sha256() {
        super.sha256();
        return this;
    }

    @Override
    public PasswordProtectionBuilder sha384() {
        super.sha384();
        return this;
    }

    @Override
    public PasswordProtectionBuilder sha512() {
        super.sha512();
        return this;
    }

    @Override
    public PasswordProtectionBuilder block() {
        super.block();
        return this;
    }

    @Override
    public PasswordProtectionBuilder stream() {
        super.stream();
        return this;
    }

    @Override
    public PasswordProtectionBuilder algorithm(String algorithm) {
        super.algorithm(algorithm);
        log.debug("algorithm set to {}", protection.getAlgorithm());
        EncryptionAlgorithmInfo info = PasswordCryptoCodecFactory.getEncryptionAlgorithmInfo(protection.getAlgorithm());
        if( info != null ) {
            log.debug("found encryption alg info for {} -> {}", algorithm, protection.getAlgorithm());
            if( info.isKeyAlgorithm() ) {
                log.debug("setting encryption algorithm {}", info.getCipherAlgorithm());
                keyAlgorithm(protection.getAlgorithm()); // no mode or padding
            }
        }
        return this;
    }

    @Override
    public PasswordProtectionBuilder mode(String mode) {
        super.mode(mode);
        return this;
    }

    @Override
    public PasswordProtectionBuilder padding(String padding) {
        super.padding(padding);
        return this;
    }

    @Override
    public PasswordProtectionBuilder digestAlgorithm(String digestAlgorithm) {
        super.digestAlgorithm(digestAlgorithm);
        return this;
    }

    @Override
    public PasswordProtectionBuilder keyLengthBits(int keyLengthBits) {
        super.keyLengthBits(keyLengthBits);
        return this;
    }

    @Override
    public PasswordProtectionBuilder blockSizeBytes(int blockSizeBytes) {
        super.blockSizeBytes(blockSizeBytes);
        return this;
    }

    @Override
    public PasswordProtectionBuilder digestSizeBytes(int digestSizeBytes) {
        super.digestSizeBytes(digestSizeBytes);
        return this;
    }

    @Override
    public PasswordProtection build() {
        super.build();
        if (passwordProtection.keyAlgorithm == null) {
            throw new IllegalArgumentException("Key algorithm is missing");
        }
        /*
         protected String mode; // mode like "OFB8"
         protected String padding; // padding like "NoPadding"
         protected int keyLengthBits;
         protected int blockSizeBytes;
         protected String digestAlgorithm; // like "SHA-256" used for integrity protection; may be null if the content is not protected
         protected int digestSizeBytes; // 20 for SHA-1, 32 for SHA-256, ...
         protected transient String cipher;    //  in the format  algorithm/mode/padding   or just algorithm  if mode and padding are not specified (dangerous - because the crypto provider will use its default so the platforma nd provider become a part of the message specification)
         * 
         */
        passwordProtection.setAlgorithm(protection.getAlgorithm());
        passwordProtection.setMode(protection.getMode());
        passwordProtection.setPadding(protection.getPadding());
        passwordProtection.setKeyLengthBits(protection.getKeyLengthBits());
        passwordProtection.setBlockSizeBytes(protection.getBlockSizeBytes());
        passwordProtection.setDigestAlgorithm(protection.getDigestAlgorithm());
        passwordProtection.setDigestSizeBytes(protection.getDigestSizeBytes());
        passwordProtection.setCipher(protection.getCipher());
//        passwordProtection.copy(protection);
        return passwordProtection;
    }
}
