/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.crypto;

import com.intel.dcsg.cpg.codec.HexUtil;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.crypto.Sha256Digest;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class PublicKeyTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PublicKeyTest.class);

    /**
     * Cannot create a PublicKey object from just the modulus:
     * <pre>
     * com.intel.dcsg.cpg.crypto.CryptographyException: java.security.spec.InvalidKeySpecException: java.security.InvalidKeyException: IOException: DerInputStream.getLength(): lengthTag=41, too big.
     * </pre>
     * @throws CryptographyException 
     */
    @Test(expected=CryptographyException.class)
    public void testDecodePublicKeyFromModulusWrong() throws CryptographyException {
        String publicKeyModulusHex = "97a9ef016a9b9c6fa1089a1bb6e6c08909847f36670bcb50397fd5dae350cb62490c344d02fc19002c23974975a63590d78addf4f2f446c70b30c6c83c8a6f04befeaeedf46e4bc9abebd1c298120361520ed5057c67b00a9fadecc1e340c05eb0d8037550aaddda153b369e245b32366a5936df04fea383363b3b0448c8f9ca0282b06c59ed14b00a2ab7ef59394c9e65643683292c47b29ae714bc602c6e4efb7c9a70ee9480af8b38a085ac1b32c2ee8eb90462183add5dbe26a924c223688b2ea8fc73c985b75b2a73e61f9eef71b33b3821549918c7281a54b57b14aad9569d54f3300e93fdf4f734822dd633c83e6a8f3e3e3aeaf3b37737bae69533d3";
        byte[] publicKeyBytes = HexUtil.toByteArray(publicKeyModulusHex);
        PublicKey publicKey = RsaUtil.decodeDerPublicKey(publicKeyBytes);
        log.debug("public key: {}", publicKey);
    }
    
    @Test
    public void testDecodePublicKeyFromModulusCorrect() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String publicKeyModulusHex = "97a9ef016a9b9c6fa1089a1bb6e6c08909847f36670bcb50397fd5dae350cb62490c344d02fc19002c23974975a63590d78addf4f2f446c70b30c6c83c8a6f04befeaeedf46e4bc9abebd1c298120361520ed5057c67b00a9fadecc1e340c05eb0d8037550aaddda153b369e245b32366a5936df04fea383363b3b0448c8f9ca0282b06c59ed14b00a2ab7ef59394c9e65643683292c47b29ae714bc602c6e4efb7c9a70ee9480af8b38a085ac1b32c2ee8eb90462183add5dbe26a924c223688b2ea8fc73c985b75b2a73e61f9eef71b33b3821549918c7281a54b57b14aad9569d54f3300e93fdf4f734822dd633c83e6a8f3e3e3aeaf3b37737bae69533d3";
        BigInteger modulus = new BigInteger(publicKeyModulusHex, 16);
        BigInteger exponent = new BigInteger("65537", 10); // 65537 = 2^16 + 1 = public key exponent required by TCG Spec for TPM RSA keys
        RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
        KeyFactory factory = KeyFactory.getInstance("RSA"); // throws NoSuchAlgorithmException
        PublicKey publicKey = factory.generatePublic(spec); // throws InvalidKeySpecException
        log.debug("public key: {}", publicKey);

    }
    
    @Test
    public void testSha256OfPublicKeyModulus() {
        String publicKeyModulusHex = "97a9ef016a9b9c6fa1089a1bb6e6c08909847f36670bcb50397fd5dae350cb62490c344d02fc19002c23974975a63590d78addf4f2f446c70b30c6c83c8a6f04befeaeedf46e4bc9abebd1c298120361520ed5057c67b00a9fadecc1e340c05eb0d8037550aaddda153b369e245b32366a5936df04fea383363b3b0448c8f9ca0282b06c59ed14b00a2ab7ef59394c9e65643683292c47b29ae714bc602c6e4efb7c9a70ee9480af8b38a085ac1b32c2ee8eb90462183add5dbe26a924c223688b2ea8fc73c985b75b2a73e61f9eef71b33b3821549918c7281a54b57b14aad9569d54f3300e93fdf4f734822dd633c83e6a8f3e3e3aeaf3b37737bae69533d3";
        byte[] publicKeyModulus = HexUtil.toByteArray(publicKeyModulusHex);
        log.debug("sha256 of ek modulus: {}", Sha256Digest.digestOf(publicKeyModulus).toHexString());
    }
}
