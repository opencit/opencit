/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.tpm12;

import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.mtwilson.util.tpm12.x509.TpmCertifyKeyInfo;
import com.intel.mtwilson.util.tpm12.x509.TpmCertifyKeySignature;
import gov.niarl.his.privacyca.TpmCertifyKey;
import gov.niarl.his.privacyca.TpmUtils;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERUTF8String;

/**
 *
 * @author ssbangal and jbuhacoff
 */
public class CertifyKey {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CertifyKey.class);
    // This OID is used for storing the TCG standard certificate as an attr within the x.509 cert.
    // We are using this OID as we could not find any specific OID for the certifyKey structure.
    public static final String TCG_STRUCTURE_CERTIFY_INFO_OID = "2.5.4.133.3.2.41"; 
    public static final String TCG_STRUCTURE_CERTIFY_INFO_SIGNATURE_OID = "2.5.4.133.3.2.41.1";

    public static boolean verifyTpmBindingKeyCertificate(X509Certificate keyCertificate, PublicKey aikPublicKey) {
        /*
         * First check that the given AIK public key can verify the signature
         * on the TPM binding public key.
         */
        TpmCertifyKeyInfo tpmCertifyKeyInfo;
        TpmCertifyKeySignature tpmCertifyKeySignature;
        
        try {
            tpmCertifyKeyInfo = TpmCertifyKeyInfo.valueOf(keyCertificate.getExtensionValue(TpmCertifyKeyInfo.OID));
            tpmCertifyKeySignature = TpmCertifyKeySignature.valueOf(keyCertificate.getExtensionValue(TpmCertifyKeySignature.OID));
        }
        catch(IOException e) {
            log.debug("Cannot parse X509 extensions TpmCertifyKeyInfo and TpmCertifyKeySignature", e);
            return false;
        }
        
        try {
        if( !isCertifiedKeySignatureValid(tpmCertifyKeyInfo.getBytes(), tpmCertifyKeySignature.getBytes(), aikPublicKey) ) {
            log.debug("TPM Binding Public Key cannot be verified by the given AIK public key");
            return false;
        }
        }
        catch(GeneralSecurityException | DecoderException e) {
            log.debug("Cannot verify TPM Binding Public Key signature", e);
            return false;
        }
        
        /*
         * Second, check that the certified key information indicates a binding key
         */
        try {
        if( !isBindingKey(new TpmCertifyKey(tpmCertifyKeyInfo.getBytes()))) {
            log.debug("TPM Binding Key has incorrect attributes");
            return false;
        }
        }
        catch(TpmUtils.TpmBytestreamResouceException | TpmUtils.TpmUnsignedConversionException e) {
            log.debug("Cannot verify TPM Binding Public Key attributes", e);
            return false;
        }
        return true;

    }

    public static boolean verifyTpmSigningKeyCertificate(X509Certificate keyCertificate, PublicKey aikPublicKey) {
        /*
         * First check that the given AIK public key can verify the signature
         * on the TPM binding public key.
         */
        TpmCertifyKeyInfo tpmCertifyKeyInfo;
        TpmCertifyKeySignature tpmCertifyKeySignature;
        
        try {
            tpmCertifyKeyInfo = TpmCertifyKeyInfo.valueOf(keyCertificate.getExtensionValue(TpmCertifyKeyInfo.OID));
            tpmCertifyKeySignature = TpmCertifyKeySignature.valueOf(keyCertificate.getExtensionValue(TpmCertifyKeySignature.OID));
        }
        catch(IOException e) {
            log.debug("Cannot parse X509 extensions TpmCertifyKeyInfo and TpmCertifyKeySignature", e);
            return false;
        }
        
        try {
        if( !isCertifiedKeySignatureValid(tpmCertifyKeyInfo.getBytes(), tpmCertifyKeySignature.getBytes(), aikPublicKey) ) {
            log.debug("TPM Binding Signing Key cannot be verified by the given AIK public key");
            return false;
        }
        }
        catch(GeneralSecurityException | DecoderException e) {
            log.debug("Cannot verify TPM Signing Public Key signature", e);
            return false;
        }
        
        /*
         * Second, check that the certified key information indicates a binding key
         */
        try {
        if( !isSigningKey(new TpmCertifyKey(tpmCertifyKeyInfo.getBytes()))) {
            log.debug("TPM Signing Key has incorrect attributes");
            return false;
        }
        }
        catch(TpmUtils.TpmBytestreamResouceException | TpmUtils.TpmUnsignedConversionException e) {
            log.debug("Cannot verify TPM Signing Public Key attributes", e);
            return false;
        }
        return true;
    }
    
    /**
     * If you have an X.509 key certificate signed by Mt Wilson for a TPM binding key
     * or signing key, use the {@code verifyTpmKeyCertificate()} function instead.
     * 
     * This function validates the certify key against the specified signature using the AIK certificate that was used during the key certification.
     * @param certifyKeyDataBlob
     * @param certifyKeySignatureBlob
     * @param aikPublicKey
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException 
     */
    public static boolean isCertifiedKeySignatureValid(byte[] certifyKeyDataBlob, byte[] certifyKeySignatureBlob, PublicKey aikPublicKey) 
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, DecoderException {
        byte[] oidPadding = Hex.decodeHex("3021300906052B0E03021A05000414".toCharArray()); //TpmUtils.hexStringToByteArray("3021300906052B0E03021A05000414");
        try {
            
            log.debug("Verifying the certify key signature against the AIK cert which signed it.");
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, aikPublicKey);
            byte[] signedDigest = cipher.doFinal(certifyKeySignatureBlob);
            byte[] signedDigestWithoutOidPadding = Arrays.copyOfRange(signedDigest, oidPadding.length, signedDigest.length);
            byte[] computedDigest = Sha1Digest.digestOf(certifyKeyDataBlob).toByteArray();
            
            log.debug("Verifying the signed digest {} against the computed digest {}", 
                    Hex.encodeHexString(signedDigestWithoutOidPadding), //TpmUtils.byteArrayToHexString(signedDigestWithoutOidPadding), 
                    Hex.encodeHexString(computedDigest)); //TpmUtils.byteArrayToHexString(computedDigest));
            
            boolean result = Arrays.equals( signedDigestWithoutOidPadding, computedDigest );
            
            log.debug("Result of signature verification is {}", result);
            
            return result;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
            log.error("Error during signature verification. {}", ex.getMessage());
            throw ex;
        }
        
    }
    
    public static boolean isBindingKey(TpmCertifyKey certifiedKey) {
        int TPM_KEY_BIND = 0x0014; // This SHALL indicate a key that can be used for TPM_Bind and TPM_UnBind operations only
        int TPM_ES_RSAESOAEP_SHA1_MGF1 = 0x0003;
        int TPM_VOLATILE = 0x00000004;
        if( certifiedKey.getTpmKeyUsage() != TPM_KEY_BIND ) {
            log.debug("Invalid key type specified during creation. Using {} instead of {}.", certifiedKey.getTpmKeyUsage(), TPM_KEY_BIND);
            return false;
        }
        if( certifiedKey.getKeyParms().getEncScheme() != TPM_ES_RSAESOAEP_SHA1_MGF1 ) {
            log.debug("Invalid encryption scheme used. Using {} scheme instead of RSA.", certifiedKey.getKeyParms().getEncScheme());
            return false;
        }
        if( certifiedKey.getTpmKeyFlags() != TPM_VOLATILE ) {
            log.debug("Invalid flag specified during the key creation. Using {} instead of {}.", certifiedKey.getTpmKeyFlags(), TPM_VOLATILE);
            return false;
        }
        return true;
    }

    public static boolean isSigningKey(TpmCertifyKey certifiedKey) {
        int TPM_KEY_SIGNING = 0x0010; // This SHALL indicate a signing key.
        int TPM_ES_NONE = 0x0001;
        int TPM_VOLATILE = 0x00000004;
        if( certifiedKey.getTpmKeyUsage() != TPM_KEY_SIGNING ) {
            log.debug("Invalid key type specified during creation. Using {} instead of {}.", certifiedKey.getTpmKeyUsage(), TPM_KEY_SIGNING);
            return false;
        }
        if( certifiedKey.getKeyParms().getEncScheme() != TPM_ES_NONE ) {
            log.debug("Invalid encryption scheme used. Using {} scheme instead of No scheme.", certifiedKey.getKeyParms().getEncScheme());
            return false;
        }
        if( certifiedKey.getTpmKeyFlags() != TPM_VOLATILE ) {
            log.debug("Invalid flag specified during the key creation. Using {} instead of {}.", certifiedKey.getTpmKeyFlags(), TPM_VOLATILE);
            return false;
        }
        return true;
    }
    
    /**
     * Verifies the encryption scheme, key type and the key flags used for the creation of the keys.
     * @param tcgCertificate
     * @param isBindingKey
     * @throws Exception 
     */
    @Deprecated
    private static void validateCertifyKeyData(byte[] tcgCertificate, boolean isBindingKey) 
            throws Exception {
        int TPM_ES_RSAESOAEP_SHA1_MGF1 = 0x0003;
        int TPM_ES_NONE = 0x0001;
        int TPM_VOLATILE = 0x00000004;
        int TPM_KEY_SIGNING = 0x0010; // This SHALL indicate a signing key.
        int TPM_KEY_BIND = 0x0014; // This SHALL indicate a key that can be used for TPM_Bind and TPM_UnBind operations only
        
        try {
            
            TpmCertifyKey certifiedKey = new TpmCertifyKey(tcgCertificate);
            log.debug("Certified key info:");
            log.debug("@certifyKey@ *version info: {}", Hex.encodeHexString(certifiedKey.getStructVer())); //TpmUtils.byteArrayToHexString(certifiedKey.getStructVer()));                           
            log.debug("@certifyKey@ *key usage: {}", certifiedKey.getTpmKeyUsage());
            log.debug("@certifyKey@ *key flags: {}", certifiedKey.getTpmKeyFlags());
            log.debug("@certifyKey@ *auth data usage: {}", certifiedKey.getTpmAuthDataUsage());
            log.debug("@certifyKey@ *Alg params:: ");
            log.debug("@certifyKey@ *Alg id:  {}, enc scheme: {}, sig scheme: {}; parm size: {}",
                    certifiedKey.getKeyParms().getAlgorithmId(), certifiedKey.getKeyParms().getEncScheme(), 
                    certifiedKey.getKeyParms().getSigScheme(), 
                    Hex.encodeHexString(certifiedKey.getKeyParms().getSubParams().toByteArray())); //TpmUtils.byteArrayToHexString(certifiedKey.getKeyParms().getSubParams().toByteArray()));      
            
            if ( isBindingKey && certifiedKey.getKeyParms().getEncScheme() != TPM_ES_RSAESOAEP_SHA1_MGF1) {
                log.error("Invalid encryption scheme used. Using {} scheme instead of RSA.", certifiedKey.getKeyParms().getEncScheme());
                throw new Exception ("Invalid encryption scheme used for creating the key.");
            } 
            
            if ( !isBindingKey && certifiedKey.getKeyParms().getEncScheme() != TPM_ES_NONE) {
                log.error("Invalid encryption scheme used. Using {} scheme instead of No scheme.", certifiedKey.getKeyParms().getEncScheme());
                throw new Exception ("Invalid encryption scheme used for creating the key.");
            } 

            if (certifiedKey.getTpmKeyFlags() != TPM_VOLATILE) {
                log.error("Invalid flag specified during the key creation. Using {} instead of {}.", certifiedKey.getTpmKeyFlags(), TPM_VOLATILE);
                throw new Exception ("Invalid flag specified during the key creation.");
            }
            
            if (isBindingKey && certifiedKey.getTpmKeyUsage() != TPM_KEY_BIND ) {
                log.error("Invalid key type specified during creation. Using {} instead of {}.", certifiedKey.getTpmKeyUsage(), TPM_KEY_BIND);
                throw new Exception ("Invalid flag specified during the binding key creation.");                
            }
            
            if (!isBindingKey && certifiedKey.getTpmKeyUsage() != TPM_KEY_SIGNING ) {
                log.error("Invalid flag specified during the key creation. Using {} instead of {}.", certifiedKey.getTpmKeyUsage(), TPM_KEY_SIGNING);
                throw new Exception ("Invalid flag specified during the signing key creation.");                
            }
            
            
        } catch (Exception ex) {
            log.error("Error during signature verification. {}", ex.getMessage());
            throw ex;
        }
        
    }
}
