/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.niarl.his.privacyca;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 *
 * @author dczech
 */
public class Tpm2Utils {

    private final static String IDENTITY = "IDENTITY";
    private final static String INTEGRITY = "INTEGRITY";
    private final static String STORAGE = "STORAGE";

    private final static int SHORT_BYTES = 2;
    private final static int INTEGER_BYTES = 4;

    private static boolean isSupportedAsymAlgorithm(String algorithm) {
        return isSupportedAsymAlgorithm(Tpm2Algorithm.Asymmetric.valueOf(algorithm));       
    }

    private static boolean isSupportedAsymAlgorithm(Tpm2Algorithm.Asymmetric asymAlg) {
        switch (asymAlg) {
            case RSA:
                return true;
            case ECDSA:
            case ECDH:
            default:
                return false; // will be true once supported            
        }
    }

    private static boolean isSupportedHashAlgorithm(Tpm2Algorithm.Hash hashAlg) {
        switch (hashAlg) {
            case SHA1:
            case SHA256:
                return true;
            default:
                return false;
        }
    }

    private static final int SHA1_SIZE = 20;
    private static final int SHA256_SIZE = 32;

    private static int getAlgorithmHashDigestLength(Tpm2Algorithm.Hash hashAlg) {
        switch (hashAlg) {
            case SHA1:
                return SHA1_SIZE;
            case SHA256:
                return SHA256_SIZE;
            default:
                return 0;
        }
    }

    @SuppressWarnings("ConvertToStringSwitch")
    public static Tpm2Credential makeCredential(PublicKey key, Tpm2Algorithm.Symmetric symmetricAlgorithm, int symKeySizeInBits, Tpm2Algorithm.Hash nameAlgorithm, byte[] credential, byte[] objectName)
            throws NoSuchAlgorithmException,
            NoSuchPaddingException,
            InvalidKeyException,
            InvalidAlgorithmParameterException,
            IllegalBlockSizeException,
            BadPaddingException,
            ShortBufferException,
            IOException {
        if (credential == null || credential.length <= 0) {
            throw new IllegalArgumentException("credential is null or empty");
        }
        // objectName has to be addressable by Uint16
        if (objectName == null || objectName.length > 65535) {
            throw new IllegalArgumentException("objectName is null or out of bounds");
        }

        if (!isSupportedAsymAlgorithm(key.getAlgorithm())) {
            throw new UnsupportedOperationException(key.getAlgorithm() + " is not (currently) supported");
        }

        if (!isSupportedHashAlgorithm(nameAlgorithm)) {
            throw new UnsupportedOperationException(nameAlgorithm + " is not supported");
        }

        int nameAlgDigestLength = getAlgorithmHashDigestLength(nameAlgorithm);

        if (credential.length > nameAlgDigestLength) {
            throw new IllegalArgumentException("credential cannot be larger than the digest size of " + nameAlgorithm);
        }

        // create random AES key and encrypt it with RSA/ECC
        byte[] seed;
        ByteBuffer encryptedSeed = ByteBuffer.allocate(Tpm2Credential.TPM2B_ENCRYPTED_SECRET_SIZE);
        switch (key.getAlgorithm()) {
            case "RSA": {
                byte[] secretData = TpmUtils.createRandomBytes(nameAlgDigestLength);
                seed = secretData;
                Cipher rsaCipher;
                Provider bcProvider = new BouncyCastleProvider();
                OAEPParameterSpec oaepSpec;
                if (nameAlgorithm == Tpm2Algorithm.Hash.SHA1) {
                    rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding", bcProvider);
                    oaepSpec = new OAEPParameterSpec("SHA-1", "MGF1", MGF1ParameterSpec.SHA1, new PSource.PSpecified(marshalStringToCString(IDENTITY)));
                } else if (nameAlgorithm == Tpm2Algorithm.Hash.SHA256) {
                    rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding", bcProvider);                    
                    oaepSpec = new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, new PSource.PSpecified(marshalStringToCString(IDENTITY)));
                } else {
                    throw new NoSuchAlgorithmException(nameAlgorithm + " is not (currently) supported");
                }

                rsaCipher.init(Cipher.PUBLIC_KEY, key, oaepSpec);
                byte[] encryptedSecret = rsaCipher.doFinal(secretData);
                encryptedSeed.order(ByteOrder.LITTLE_ENDIAN).putShort((short) encryptedSecret.length);
                encryptedSeed.put(encryptedSecret);
            }
            break;
            // TODO: ECC to go here
            default:
                throw new UnsupportedOperationException();
        }

        // encrypt credential with Symmetric Algorithm
        byte[] symKey = kDFa(nameAlgorithm, seed, STORAGE, objectName, null, symKeySizeInBits);
        ByteBuffer credentialBlob = ByteBuffer.allocate(Tpm2Credential.TPM2B_ID_OBJECT_SIZE);
        Cipher symCipher;
        byte[] encryptedCredential;
        if (symmetricAlgorithm == Tpm2Algorithm.Symmetric.AES) {
            byte[] zeroVector = new byte[16];
            IvParameterSpec iv = new IvParameterSpec(zeroVector);
            SecretKeySpec secretKey = new SecretKeySpec(symKey, "AES");
            symCipher = Cipher.getInstance("AES/CFB/NoPadding");
            symCipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
            symCipher.update(marshalInt16ToByteArray((short) credential.length, ByteOrder.BIG_ENDIAN));
            encryptedCredential = symCipher.doFinal(credential); // write encrypted credential to blob            
        } else {
            throw new UnsupportedOperationException(symmetricAlgorithm + " not supported");
        }

        // Compute Hmac Integrity of encrypted credential
        byte[] hmacKey = kDFa(nameAlgorithm, seed, INTEGRITY, null, null, nameAlgDigestLength * 8);
        SecretKeySpec hmacKeySpec;
        Mac hmac;
        if (nameAlgorithm == Tpm2Algorithm.Hash.SHA1) {
            hmac = Mac.getInstance("HmacSha1");
            hmacKeySpec = new SecretKeySpec(hmacKey, "HmacSha1");
        } else if (nameAlgorithm == Tpm2Algorithm.Hash.SHA256) {
            hmac = Mac.getInstance("HmacSha256");
            hmacKeySpec = new SecretKeySpec(hmacKey, "HmacSha256");
        } else {
            throw new UnsupportedOperationException(nameAlgorithm + " is not supported");
        }
        hmac.init(hmacKeySpec);
        hmac.update(encryptedCredential); // add the encrypted credential
        hmac.update(objectName); // add bytes
        byte[] integrity = hmac.doFinal();

        credentialBlob.order(ByteOrder.LITTLE_ENDIAN).putShort((short) (SHORT_BYTES + integrity.length + encryptedCredential.length));
        credentialBlob.order(ByteOrder.BIG_ENDIAN).putShort((short) integrity.length);
        credentialBlob.put(integrity).put(encryptedCredential);

        return new Tpm2Credential(credentialBlob.array(), encryptedSeed.array());
    }

    private static byte[] kDFa(Tpm2Algorithm.Hash hashAlgorithm, byte[] key, String label, byte[] contextU, byte[] contextV, int sizeInBits) throws NoSuchAlgorithmException, InvalidKeyException {

        String macAlgorithm;

        if (hashAlgorithm == Tpm2Algorithm.Hash.SHA1) {
            macAlgorithm = "HmacSha1";
        } else if (hashAlgorithm == Tpm2Algorithm.Hash.SHA256) {
            macAlgorithm = "HmacSha256";
        } else {
            throw new IllegalArgumentException(hashAlgorithm + " is not a supported hashing algorithm");
        }

        byte[] labelBuf = label == null ? null : marshalStringToCString(label);

        if (((sizeInBits + 7) / 8) > Short.MAX_VALUE) {
            throw new IllegalArgumentException();
        }
        int bytes = (sizeInBits + 7) / 8;
        int hashLen = getAlgorithmHashDigestLength(hashAlgorithm);
        int counter = 0;
        int curPos = 0;
        byte[] outBuf = new byte[bytes];

        for (; bytes > 0; curPos += hashLen, bytes -= hashLen) {
            if (bytes < hashLen) {
                hashLen = bytes;
            }
            ++counter;

            SecretKeySpec macKey = new SecretKeySpec(key, macAlgorithm);
            Mac mac = Mac.getInstance(macAlgorithm);

            mac.init(macKey);

            // Add counter
            mac.update(marshalInt32ToByteArray(counter, ByteOrder.BIG_ENDIAN));

            if (labelBuf != null) {
                // Careful! String.getBytes does NOT include the null terminator. 
                // We need to use our marshalled CString
                mac.update(labelBuf);
            } else {
                // View CryptHash.c to view why this is done

                // Add a null. SP108 is not very clear about when the 0 is needed but to
                // make this like the previous version that did not add an 0x00 after
                // a null-terminated string, this version will only add a null byte
                // if the label parameter did not end in a null byte, or if no label
                // is present.
                mac.update((byte) 0x00);
            }

            if (contextU != null) {
                mac.update(contextU);
            }

            if (contextV != null) {
                mac.update(contextV);
            }

            mac.update(marshalInt32ToByteArray(sizeInBits, ByteOrder.BIG_ENDIAN));

            byte[] hashVal = mac.doFinal();
            System.arraycopy(hashVal, 0, outBuf, curPos, hashLen); // copy the MIN(bytes, hashLen) to the outBuffer            
        }

        // Now we handle the case where N bits is not a multpile of 8, such as 1001 bit key, which means we have 7 extra bits in our buffer
        if ((sizeInBits % 8) != 0) {
            outBuf[0] &= ((1 << (sizeInBits % 8)) - 1);
        }

        return outBuf;
    }

    private static byte[] marshalInt16ToByteArray(short i, ByteOrder order) {
        return ByteBuffer.allocate(SHORT_BYTES).order(order).putShort(i).array();
    }

    private static byte[] marshalInt32ToByteArray(int i, ByteOrder order) {
        return ByteBuffer.allocate(INTEGER_BYTES).order(order).putInt(i).array();
    }

    private static byte[] marshalStringToCString(String str) {
        byte[] strBytes;
        try {
            strBytes = str.getBytes("UTF-8");
            byte[] strBuf = new byte[strBytes.length + 1];
            System.arraycopy(strBytes, 0, strBuf, 0, strBytes.length);
            return strBuf;
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Tpm2Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    // needs to be updated to support more algorithms
    public static PublicKey getPubKeyFromAikBlob(byte[] blob) throws NoSuchAlgorithmException, InvalidKeySpecException {
        BigInteger modI = new BigInteger(1, blob);
        BigInteger expI = BigInteger.valueOf(65537);
        RSAPublicKeySpec newKeySpec = new RSAPublicKeySpec(modI, expI);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        RSAPublicKey newKey = (RSAPublicKey) keyFactory.generatePublic(newKeySpec);
        return newKey;
    }
}


