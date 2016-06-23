/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Tpm2;

import gov.niarl.his.privacyca.Tpm2Algorithm;
import gov.niarl.his.privacyca.Tpm2Credential;
import gov.niarl.his.privacyca.Tpm2Utils;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import org.eclipse.core.runtime.Assert;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author dczech
 */
public class Tpm2MakeCredentialTest {
    static Tpm2Credential expectedCredential;
    static PublicKey publicKey;
    static byte[] objectName;
    public Tpm2MakeCredentialTest() {
    }
    
    @BeforeClass
    public static void setUpClass() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] modulus = Files.readAllBytes(Paths.get("src/test/resources/modulus.out"));
        
        byte[] credentialBlob = new byte[Tpm2Credential.TPM2B_ID_OBJECT_SIZE];
        byte[] encryptedSecret = new byte[Tpm2Credential.TPM2B_ENCRYPTED_SECRET_SIZE];
        
        expectedCredential = new Tpm2Credential(credentialBlob, encryptedSecret);
        BigInteger mod = new BigInteger(1, modulus);
        BigInteger exp = BigInteger.valueOf(65537);
        RSAPublicKeySpec keySpec = new RSAPublicKeySpec(mod, exp);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        publicKey = kf.generatePublic(keySpec);
        
        objectName = Files.readAllBytes(Paths.get("src/test/resources/objectname.out"));
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    public static String bytesToHex(byte[] in) {
        final StringBuilder builder = new StringBuilder();
        for(byte b : in) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }
    
    @Test
    public void testMakeCredential() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, ShortBufferException, IOException {
        Tpm2Credential out = Tpm2Utils.makeCredential(publicKey, Tpm2Algorithm.Symmetric.AES, 128, Tpm2Algorithm.Hash.SHA256, "12345678\n".getBytes(), objectName);
        System.out.println(bytesToHex(out.getCredential()));
        System.out.println(bytesToHex(out.getSecret()));
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
}
