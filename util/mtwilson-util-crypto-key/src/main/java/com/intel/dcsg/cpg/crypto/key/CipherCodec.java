/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.key;

//import com.intel.dcsg.cpg.crypto.CryptographyException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import java.util.HashMap;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;

/**
 * An abstract procedure to help create secure application-specific 
 * message transmission and storage mechanisms. 
 * 
 * An abstract procedure for encoding plaintext input to protect and for
 * decoding ciphertext input to validate, which also performs the encryption
 * and decryption on the intermediate objects.
 * 
 * Implementations of CipherCodec must be stateless - they must assume that
 * a new CipherCodec instance was created before each call to an override method.
 * Currently this is not being done but it might be in the future in order
 * to enforce this requirement and prevent unauthenticated information 
 * from being mistakenly trusted.
 * 
 * @author jbuhacoff
 */
public abstract class CipherCodec<T> {

    private final transient EncryptionKeySource encryptionKeySource;
    private final transient Protection protection;
//    private transient Cipher cipher;
//    private transient MessageDigest md;
    private final transient RandomSource random;
    private final transient HashMap<String,Cipher> ciphers;
    private final transient HashMap<String,MessageDigest> digests;
    private boolean decryptArchive = false; // when false, input crypto must meet the policy;  when true, allows to decrypt input crypto that does not meet policy (for example to view old archived data) ;   it should be a very deliberate action to allow it... so applications hould do it in special "archive viewing" area and not where the codec is normally used for current operations. 
    
    public CipherCodec(EncryptionKeySource encryptionKeySource, Protection protection) {
        this.ciphers = new HashMap<>();
        this.digests = new HashMap<>();
        this.encryptionKeySource = encryptionKeySource;
        this.protection = protection;
        this.random = new RandomSource();
    }
    public CipherCodec(EncryptionKeySource encryptionKeySource, Protection protection, RandomSource random) {
        this.ciphers = new HashMap<>();
        this.digests = new HashMap<>();
        this.encryptionKeySource = encryptionKeySource;
        this.protection = protection;
        this.random = random;
    }

    protected synchronized Protection getProtection() {
        return protection;
    }

    public void setDecryptArchive(boolean decryptArchive) {
        this.decryptArchive = decryptArchive;
    }

    
    public boolean isDecryptArchive() {
        return decryptArchive;
    }
    
    
    private Cipher getCipher(Protection protection) throws NoSuchAlgorithmException, NoSuchPaddingException {
        Cipher cipher = ciphers.get(protection.cipher);
        if( cipher == null ) {
            cipher = Cipher.getInstance(String.format("%s/%s/%s", protection.algorithm, protection.mode, protection.padding));
            ciphers.put(protection.cipher, cipher);
        }
        return cipher;
    }
    
    private MessageDigest getMessageDigest(Protection protection) throws NoSuchAlgorithmException, NoSuchPaddingException {
        MessageDigest md = digests.get(protection.digestAlgorithm);
        if( md == null ) {
            md = MessageDigest.getInstance(protection.digestAlgorithm);
            digests.put(protection.digestAlgorithm, md);
        }
        return md;
    }
    
    /*
    private void init() throws NoSuchAlgorithmException, NoSuchPaddingException {
        cipher = Cipher.getInstance(String.format("%s/%s/%s", protection.algorithm, protection.mode, protection.padding)); // throws NoSuchAlgorithmException, NoSuchPaddingException
        md = MessageDigest.getInstance(protection.digestAlgorithm);
    }*/
    
    public final synchronized byte[] encrypt(T object) throws GeneralSecurityException /*CryptographyException*/ {
        EncryptionKey key = encryptionKeySource.getEncryptionKey(protection);
        Plaintext plaintext = new Plaintext();
//        plaintext.header = formatPlaintextHeader(object);
        plaintext.message = formatPlaintextMessage(object);
        plaintext.protection = key.protection;
//        Encrypted encrypted = new Encrypted();
//        try {
            if (/*plaintext.*/protection.digestAlgorithm != null) { // SHA-256
                MessageDigest md = getMessageDigest(key.protection);  // throws NoSuchAlgorithmException
//                if( plaintext.header != null ) { md.update(plaintext.header); }
                plaintext.digest = md.digest(plaintext.message);
            }

            byte[] iv = generateIV();
            AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);
            Cipher cipher = getCipher(protection);
            cipher.init(Cipher.ENCRYPT_MODE, key.secretKey, paramSpec); // throws InvalidKeyException, InvalidAlgorithmParameterException
            byte[] plaintextBytes = formatPlaintext(plaintext, object); // TODO:  cpg-codec now has an interface for Object->byte[] encoding so formatPlaintext can become an implementation
            byte[] ciphertext = cipher.doFinal(plaintextBytes); // throws IllegalBlockSizeException, BadPaddingException
            Ciphertext encrypted = new Ciphertext();
            encrypted.protection = key.protection;
            encrypted.keyId = key.getKeyId();
            encrypted.iv = iv;
            encrypted.message = ciphertext;
            return formatCiphertext(encrypted, object); // ByteArray.concat(iv, ciphertext);
//        } catch (GeneralSecurityException e) { // includes InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException
//            throw new CryptographyException(e);
//        }
    }

    private byte[] generateIV() {
        assert protection.blockSizeBytes > 0;
        byte[] iv = random.nextBytes(protection.blockSizeBytes);
        return iv;
    }

    /**
     * Serializes the object into a plaintext byte array that is the
     * the message to encrypt (not including headers, digests, or other
     * information that might also be encrypted with it)
     * For example, formatPlaintextMessage(String object) could 
     * simply return  object.getBytes(Charset.forName("UTF-8")).
     * 
     * TODO:  the new cpg-codec package has an ObjectCodec interface
     * with encode Object->byte[] and decode byte[]->Object so use that
     * interface here 
     * 
     * @param object to be serialized
     * @return the message, which is a serialized form of the object
     */
    abstract protected byte[] formatPlaintextMessage(T object);

    /**
     * Formats a plaintext byte array including all the information that
     * requires confidentiality and integrity protection. The resulting
     * byte array is what will be encrypted.
     * For example, this method could return  ByteArray.concat(plaintext.message,plaintext.digest)
     * @param plaintext object containing the message formatted by formatPlaintextMessage, and a digest of that message using the specified digest algorithm.
     * @param object the same object instance that was provided to formatPlaintextMessage
     * @return 
     */
    abstract protected byte[] formatPlaintext(Plaintext plaintext, T object);
    
    /**
     * Formats an object containing the ciphertext and crypto metadata into a
     * ciphertext byte array that will be transmitted or stored
     *
     * Typically this means taking the encrypted and authenticated message and
     * prepending unauthenticated headers or appending other unauthenticated 
     * information. 
     * 
     * The header does NOT have integrity protection; typically headers are
     * used to allow a generic reader to determine
     * which specific parser to use for reading the protected content.
     * For example an unauthenticated header might contain a protocol version
     * which then determines the size and order of the rest of the fields. 
     * Such a version number cannot be encrypted because if it were encrypted
     * then the reader would not be able to read it.  Another example is a
     * content-length field which tells the reader the length of the entire
     * protected message; this also would be useless if encrypted. 
     * 
     * It is possible to provide integrity protection for such headers by
     * repeating them inside the protected message. 
     * 
     * TODO: A future version of this class should enable implementations to specify
     * authenticated headers that should be included in the digest but are 
     * not repeated in the encrypted message.
     * 
     * If someone tampers
     * with the header the parser will not be able to read the protected content
     * but it won't affect the security of the token since the header values
     * themselves are not trusted by the system unless the entire Token is
     * first validated. The IV is public but its length and nature depends on
     * the algorithm associated with keyId. For example, a secret key using
     * AES-128 would use IV 16 bytes. We assume an attacker knows the algorithms
     * used and therefore the IV length also.
     * 
     * 
     * @param encrypted
     * @return
     */
    abstract protected byte[] formatCiphertext(Ciphertext ciphertext, T object);

    /**
     * Applications have some flexibility in how they decrypt their data:
     * 
     * 1) define a single hard-coded algorithm specification like AES128/OFB8/NoPadding with SHA-256 and always use that;  reject any incoming messages with a different algorithm
     * 
     * 2) define a policy like  AES128+/OFB8|CBC/NoPadding|PKCS5Padding with SHA-2 and accept anything that meets the policy 
     * 
     * 3) specify a single key to use - can simply use an immutable encryption key source initialized with that one key
     * 
     * 4) use a repository of keys and let the ciphercodec automatically add new keys to it as it encrypts data
     * 
     * 5) give the repository the protection policy so it knows what kinds of keys to generate or search for 
     * 
     * 6) allow decryption with keys that don't meet the policy  -- so you can still decrypt old data after the policy has been changed -- but this is not recommended ;  it's best to define a separate method for this kind of application (for example, viewing archives) so that it's clear it's not to be used for authorizing new things.
     * 
     * XXX TODO  right now the decryption is assuming teh same protection as the encryption...
     * but the point of having diffferent formats is that some can assume a spceciif c cryptossytem
     * and some embed all encesasry info rso we can be flexible whe nreading... in the second
     * case what we want is a minimum protection poicy and to cehck it agains twhat is 
     * on the input and if it's fine then use what is n theinput as the parameters. 
     * in that case,  checing the protection hashcode directly won't work for caching
     * because we would createa  new protection instance for each decryption .....  even 
     * though their vlues would be similar.  
     * @param ciphertext
     * @return
     * @throws CryptographyException 
     */
    public final synchronized T decrypt(byte[] ciphertext) throws GeneralSecurityException /*CryptographyException*/, KeyNotFoundException {
//        try {
            Ciphertext encrypted = parseCiphertext(ciphertext); // split up a byte array  into an iv, and actual ciphertext to decrypt
            EncryptionKey key;
            Cipher cipher;
            if( decryptArchive ) {
                key = encryptionKeySource.getDecryptionKey(encrypted.keyId);
                if( key == null ) {
                    throw new KeyNotFoundException(encrypted.keyId);
                }
            }
            else {
                key = encryptionKeySource.getDecryptionKey(encrypted.keyId);
                if( key == null || !key.protection.cipher.equals(protection.cipher) ) { // if the specified key was not found, or it's not the right specifications,  we make a bogus key so the decryption will appear to have worked - defend against timing side channel attacks
                    // the specified key was not found; create a new key for th
                    key = encryptionKeySource.createDecryptionKey(protection);
                    key.bogus = true;
                }
            }
            cipher = getCipher(key.protection);
            cipher.init(Cipher.DECRYPT_MODE, key.secretKey, new IvParameterSpec(encrypted.iv));
            byte[] plaintextBytes = cipher.doFinal(encrypted.message); // XXX for performance it would be better to use the doFinal(byte[], int, int) method and just give positions in the original byte array instead of making a copy of it in content ?      TODO look into using ByteBuffer  with its position, limit, and mark  values and read-only views asReadOnlyBuffer  to 
            if( key.isBogus() ) {
                throw new MessageIntegrityException("Invalid key"); // we cannot continue after this point if the key is bogus because next step is to parse the decrypted message and using a bogus key that message is all garbage, and different parsers will react to that in different ways depending on what they expect.... so we exit here to ensurea  uniform exit code i this case and not leak any other information.   
            }
            Plaintext plaintext = parsePlaintext(plaintextBytes, encrypted.protection);
            // verify integrity            
            if( protection.digestAlgorithm != null ) {
                MessageDigest md = getMessageDigest(key.protection);
                byte[] computedDigest = md.digest(plaintext.getMessage());
                if (!Arrays.equals(plaintext.getDigest(), computedDigest)) {
                    throw new MessageIntegrityException("Message integrity digest failed verification"); // we intentionally do not provide either digest in the error message
                }
            }
            return parsePlaintextMessage(plaintext.getMessage());
//        } catch (GeneralSecurityException e) {
//            throw new CryptographyException(e);
//        }
    }

    /**
     * Parses a ciphertext byte array to extract any available crypto metadata
     *
     * For example, given a blob that is iv||ciphertext, this method would do:
     *
     * iv = System.arraycopy(blob, 0, blockSizeBytes) ciphertext =
     * System.arraycopy(blob, blockSizeBytes, blob.length - blobSizeBytes);
     *
     * @param ciphertext
     * @return object with ciphertext and crypto metadata
     */
    abstract protected Ciphertext parseCiphertext(byte[] ciphertext);

    /**
     * Intermediate step between parseCiphertext (which accepts the entire 
     * input blob and separates non-protected information from the encrypted/signed
     * content) , and parsePlaintext (which accepts the decrypted content only
     * and creates the object out of it)
     * 
     * This is the step where,  if the ciphertext = encrypt(plaintext||digest)  it
     * can separated the decrypted plaintext from the digest.
     * 
     * @param plaintext
     * @return 
     */
    abstract protected Plaintext parsePlaintext(byte[] plaintext, Protection protection);
    
    /**
     * Parses a plaintext byte array into an object to be returned from the
     * decrypt method.
     *
     * TODO:  the new cpg-codec package has an ObjectCodec interface
     * with encode Object->byte[] and decode byte[]->Object so use that
     * interface here 
     * 
     * @param plaintext object
     * @return
     */
    abstract protected T parsePlaintextMessage(byte[] message);
}
