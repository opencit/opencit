/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.rfc822;

import com.intel.dcsg.cpg.crypto.Aes;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.rfc822.AesMessageWriter;
import com.intel.dcsg.cpg.rfc822.Message;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class EncryptedMessageTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EncryptedMessageTest.class);

    /**
     * Example encrypted message, notice the String is wrapped in a message/rfc822 that is compressed and also indicates the utf-8 character set 
Content-Length: 276
Content-Transfer-Encoding: base64
Content-Type: encrypted/java; enclosed="message/rfc822"; alg="AES/OFB8/NoPadding"; key="key1"; digest-alg="SHA-256"

/OgfCpzdtNJ2YypBCtevK7v7RYHcBOF6LcR6TyRX5F2HaEWfPkqeoooBuU2br6lOnnCj83NBf1fh
1uQ3PqSOUjvkVLDisJVloXwvnxP0TuDKFzabRyBLzhHjNXd0rK5Ohma+OKNlFnValcBRJJaPXMnn
eeTLKsOyOM9aVzmnLMVoOfRmktIXyLERxDS0Dt3LDF+XWKf4MyiE2ApUjUbqY3eDh5cizW9LF0vc
hvcq8D13Q1iGNDxWi3tq8roIcS9lfBccN1k/zlsp1hohkdIwBwAg9zPe7aVRfY6xTnh26zej2A5x
wRpNFS4950LeHkBMQ9Bn79MR8JvCue01MlwvbiIHDtE2zW0/MKQGbjtNtuB+ObIO
     * 
     * 
     * @throws CryptographyException
     * @throws KeyNotFoundException
     * @throws IOException
     * @throws NoSuchAlgorithmException 
     */
    @Test
    public void testAesMessageString() throws CryptographyException, KeyNotFoundException, IOException, NoSuchAlgorithmException {
        String text = "In cryptography and computer security, length extension attacks are a type of attack on certain types of hashes which allow inclusion of extra information."; // a sentence from wikipedia, http://en.wikipedia.org/wiki/Length_extension_attack
        // to write an encrypted message we need a repository to store our encryption keys, so we make an in-memory repository with a random key for testing
        HashMapSecretKeyRepository keys = new HashMapSecretKeyRepository();
        keys.put("key1", Aes.generateKey(128)); // throws CryptographyException
        AesMessageWriter writer = new AesMessageWriter();
        writer.setSecretKeyFinder(keys);
        // encrypt the message with the test key
        byte[] encrypted = writer.encryptString(text, "key1"); // throws CryptographyException, KeyNotFoundException, IOException
        log.debug("Encrypted message:\n{}", new String(encrypted, "UTF-8")); // we know it's printable because AesMessageWriter(String,String) produces a base64-encoded message body
        // now decrypt it
        AesMessageReader reader = new AesMessageReader();
        reader.setSecretKeyFinder(keys);
        String text2 = reader.decryptString(encrypted);// throws NoSuchAlgorithmException, CryptographyException, KeyNotFoundException, IOException
        assertEquals(text, text2);
    }
    
    /**
     * Example message, notice the byte array is NOT wrapped with a Message object, it is encrypted as-is and that's why the wrapper indicates an "enclosed" type of application/content-stream
     * 
Content-Length: 560
Content-Transfer-Encoding: base64
Content-Type: encrypted/java; enclosed="application/octet-stream"; alg="AES/OFB8/NoPadding"; key="key1"; digest-alg="SHA-256"

IFxDLNLHzVHOxNK/bMQtMe6xTPzdN4MMHYAVETDgDrryqeAGhFuMkHBr3xTBHFyxriNQrcbMpfgL
Han4GqAJaeLBy1mfHrW3enDAxaJ8se0nIJvm2XiWXa377JMO/iVWghvwgmQ3ECF4DN3Ni5RbFI+q
Mpmu6JG3Ehwa1SwAL1VsEnN3lkDg3ctd3fOElSyVaG2LzMn0vWNN+HbOgeobLSCAAN9QaDKgLS8+
EhbSnj7zIzaCdh4vx04/T4cO/KG2KRuCeehX52dECE1LL/oKIFn6Ge6QzKeUJZEvgLC6cENe2WY/
Kr7QcTmv18pg7DvV740BC/hWx+HR4PnZXS1Bl/VQFm5jgey7txKciltgn+tWPIkGQb+Ny7UkFmr1
l20aFOLZAuH3kgM873uXZNvkeX7X1Fh+802i4lM1LWcRhrIkwHW/Ndj5h0tTQe/jLnV9OHa/P5Om
s/RA+oDGobZIE5gOVztkFAObthegifyi/le2aXdBMpFXQLGsa+URGhtM8x2eJPdiXSr5B4xsf8PU
gWRhE/KtU7r3OPMe6i/ctYrACM/5nFZU0H1OaRbsuP8Ub1drOczjUJ0Jl64F6rpCMw/TIwhaq4zk
4gM1FgJJP/6MRSOBuE3iT9a0g2fQsnsQ+XbPWJzZ0GCVE8vU2s3zK4K/pMu57s6gAyGnGUFomRe1
LfL9uWWHPj1pBk5WJlESb0G8GPcUHGlrhtQzuKWogqoY2GoJ59nrUFUx9OYPqmw=
     * 
     * @throws CryptographyException
     * @throws KeyNotFoundException
     * @throws IOException
     * @throws NoSuchAlgorithmException 
     */
    @Test
    public void testAesMessageByteArray() throws CryptographyException, KeyNotFoundException, IOException, NoSuchAlgorithmException {
        byte[] data = new byte[512];  // will store a random value to encrypt for this test
        Random random = new Random();
        random.nextBytes(data);
        // to write an encrypted message we need a repository to store our encryption keys, so we make an in-memory repository with a random key for testing
        HashMapSecretKeyRepository keys = new HashMapSecretKeyRepository();
        keys.put("key1", Aes.generateKey(128)); // throws CryptographyException
        AesMessageWriter writer = new AesMessageWriter();
        writer.setSecretKeyFinder(keys);
        // encrypt the message with the test key
        byte[] encrypted = writer.encryptByteArray(data, "key1"); // throws CryptographyException, KeyNotFoundException, IOException
        log.debug("Encrypted message:\n{}", new String(encrypted, "UTF-8")); // we know it's printable because AesMessageWriter(String,String) produces a base64-encoded message body
        // now decrypt it
        AesMessageReader reader = new AesMessageReader();
        reader.setSecretKeyFinder(keys);
        byte[] data2 = reader.decryptByteArray(encrypted);// throws NoSuchAlgorithmException, CryptographyException, KeyNotFoundException, IOException
        assertArrayEquals(data, data2);
    }
    
    /**
     * Original message containing a favico.ico in base64:
Filename: favicon.ico
Owner: alice
Owner-Access: rwx

AAABAAEAEBAAAAEAIABoBAAAFgAAACgAAAAQAAAAIAAAAAEAIAAAAAAAAAAAABMLAAATCwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA59S8/+fUvP/fxqb/3cOj/93Dov/gxab/4cir/+jWwP/p3Mf/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA3cCd/7uAOf+vcCD/sW8g/7Z8M/+5fjf/sGsc/6xmFP+ycB7/tXYr/8yhbf/k0bb/AAAAAAAAAAAAAAAA3MCe/6hkEP/NpnP/6NbA/+jWwP8AAAAAAAAAAAAAAADo1sD/4ceo/86ldf+yciP/o1gA/+HHqP8AAAAAAAAAALBxJP/dwaL/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAANu7mf8AAAAAAAAAAAAAAAC4fTT/AAAAANe2kf/DkFP/06+D/8+jb//Vs4r/wItQ/8aVW//Illn/tXkt/8+peP/ewaD/xpVb/9Wxh/8AAAAAuX03/+bTvP/JmWL/uX0z/8mfa//CkVX/zaRz/61pF//Us4j/rWgU/8icZv/GlVz/v4tL/9Ougv+1dyv/1rKH/8WWXf/o1sD/zaNv/7yGQv/DklX/uoA5/8+pev+ubRz/2raO/7FwIv/CkE//s3Ek/7yCPP/o1sD/5Myx/7BvH//q2cL/xJZb/9Oziv/Pq3z/1K19/8iYXv/m0LT/snIn/9e1jP/YupL/zaVx/9m7lv++iUj/4smq/wAAAACvbh7/AAAAAOnXv//Op3b/17WO/wAAAAAAAAAAAAAAANq8mf8AAAAAAAAAAAAAAAAAAAAAzad3/9m7lf/m0LT/sHAg/wAAAAAAAAAA6de//+POs//Vs4z/28Gg/+TNsf8AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAADkzbL/tnov/8+ndf8AAAAAAAAAAAAAAAAAAAAA4Miq/9W1jf/SrXz/zaVy/8ufaf/UsIb/1LKH/82kcf/HlVr/wpJX/9Oug//n0bf/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA5M2x/+TNsf/fw6X/5tC2/+fRt//hx6j/59G3/wAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAwAA4QMAACw0AAAyOQAAMTcAACw0AAA4NQAAMTkAACw0AAA4NwAAMTkAACw0AAA4OQAAMTkAACw0AAA5MQ==
     * 
     * Example of encrypting an arbitrary message with integrity-protection for the headers
     * 
Content-Length: 1677
Content-Transfer-Encoding: base64
Content-Type: encrypted/java; enclosed="message/rfc822"; alg="AES/OFB8/NoPadding"; key="key1"; digest-alg="SHA-256"

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
BRAxHjG0JcGa2rXZaw2T5+RFCvxF31QX
     * 
     * @throws CryptographyException
     * @throws KeyNotFoundException
     * @throws IOException
     * @throws NoSuchAlgorithmException 
     */
    @Test
    public void testAesMessage() throws CryptographyException, KeyNotFoundException, IOException, NoSuchAlgorithmException {
        // prepare the message to encrypt
        String icon = "AAABAAEAEBAAAAEAIABoBAAAFgAAACgAAAAQAAAAIAAAAAEAIAAAAAAAAAAAABMLAAATCwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA59S8/+fUvP/fxqb/3cOj/93Dov/gxab/4cir/+jWwP/p3Mf/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA3cCd/7uAOf+vcCD/sW8g/7Z8M/+5fjf/sGsc/6xmFP+ycB7/tXYr/8yhbf/k0bb/AAAAAAAAAAAAAAAA3MCe/6hkEP/NpnP/6NbA/+jWwP8AAAAAAAAAAAAAAADo1sD/4ceo/86ldf+yciP/o1gA/+HHqP8AAAAAAAAAALBxJP/dwaL/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAANu7mf8AAAAAAAAAAAAAAAC4fTT/AAAAANe2kf/DkFP/06+D/8+jb//Vs4r/wItQ/8aVW//Illn/tXkt/8+peP/ewaD/xpVb/9Wxh/8AAAAAuX03/+bTvP/JmWL/uX0z/8mfa//CkVX/zaRz/61pF//Us4j/rWgU/8icZv/GlVz/v4tL/9Ougv+1dyv/1rKH/8WWXf/o1sD/zaNv/7yGQv/DklX/uoA5/8+pev+ubRz/2raO/7FwIv/CkE//s3Ek/7yCPP/o1sD/5Myx/7BvH//q2cL/xJZb/9Oziv/Pq3z/1K19/8iYXv/m0LT/snIn/9e1jP/YupL/zaVx/9m7lv++iUj/4smq/wAAAACvbh7/AAAAAOnXv//Op3b/17WO/wAAAAAAAAAAAAAAANq8mf8AAAAAAAAAAAAAAAAAAAAAzad3/9m7lf/m0LT/sHAg/wAAAAAAAAAA6de//+POs//Vs4z/28Gg/+TNsf8AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAADkzbL/tnov/8+ndf8AAAAAAAAAAAAAAAAAAAAA4Miq/9W1jf/SrXz/zaVy/8ufaf/UsIb/1LKH/82kcf/HlVr/wpJX/9Oug//n0bf/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA5M2x/+TNsf/fw6X/5tC2/+fRt//hx6j/59G3/wAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAwAA4QMAACw0AAAyOQAAMTcAACw0AAA4NQAAMTkAACw0AAA4NwAAMTkAACw0AAA4OQAAMTkAACw0AAA5MQ=="; // a base64-encoded favicon.ico file with the intel logo
        Message message = new Message();
        message.getHeaderMap().put("Owner", "alice");
        message.getHeaderMap().put("Owner-Access", "rwx");
        message.getHeaderMap().put("Filename", "favicon.ico");
        message.setContent(icon.getBytes());
        log.debug("Plain message:\n{}", new String(message.toByteArray()));
        // encrypt it with a random key
        HashMapSecretKeyRepository keys = new HashMapSecretKeyRepository();
        keys.put("key1", Aes.generateKey(128)); // throws CryptographyException
        AesMessageWriter writer = new AesMessageWriter();
        writer.setSecretKeyFinder(keys);
        byte[] encrypted = writer.encryptMessage(message, "key1"); // throws CryptographyException, KeyNotFoundException, IOException
        log.debug("Encrypted message:\n{}", new String(encrypted, "UTF-8")); // we know it's printable because AesMessageWriter(String,String) produces a base64-encoded message body
        // decrypt it
        AesMessageReader reader = new AesMessageReader();
        reader.setSecretKeyFinder(keys);
        Message message2 = reader.decryptMessage(encrypted);
        log.debug("Decrypted messge:\n{}", new String(message2.toByteArray()));
        assertArrayEquals(message.toByteArray(), message2.toByteArray());
    }
    
}
