/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.rpc;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.x509.X509Builder;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.My;
import com.intel.mtwilson.launcher.ws.ext.RPC;
import com.intel.mtwilson.repository.RepositoryCreateException;
import gov.niarl.his.privacyca.TpmCertifyKey;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import gov.niarl.his.privacyca.TpmUtils;
import java.io.FileInputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author ssbangal
 */
@RPC("certify-host-binding-key")
@JacksonXmlRootElement(localName = "certify_host_binding_key")
public class CertifyHostBindingKeyRunnable implements Runnable {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CertifyHostBindingKeyRunnable.class);
    // This OID is used for storing the TCG standard certificate as an attr within the x.509 cert.
    // We are using this OID as we could not find any specific OID for the certifyKey structure.
    protected static final String tcgCertExtOid = "2.23.133.6"; 
    protected static final String tcgCertSignatureOid = "1.2.3.4";
    
    private byte[] publicKeyModulus;
    private byte[] tpmCertifyKey;
    private byte[] tpmCertifyKeySignature;
    private String aikPemCertificate;
    private String bindingKeyPemCertificate;

    public byte[] getPublicKeyModulus() {
        return publicKeyModulus;
    }

    public void setPublicKeyModulus(byte[] publicKeyModulus) {
        this.publicKeyModulus = publicKeyModulus;
    }

    public byte[] getTpmCertifyKey() {
        return tpmCertifyKey;
    }

    public void setTpmCertifyKey(byte[] tpmCertifyKey) {
        this.tpmCertifyKey = tpmCertifyKey;
    }

    public String getBindingKeyPemCertificate() {
        return bindingKeyPemCertificate;
    }

    public void setBindingKeyPemCertificate(String bindingKeyPemCertificate) {
        this.bindingKeyPemCertificate = bindingKeyPemCertificate;
    }

    public byte[] getTpmCertifyKeySignature() {
        return tpmCertifyKeySignature;
    }

    public void setTpmCertifyKeySignature(byte[] tpmCertifyKeySignature) {
        this.tpmCertifyKeySignature = tpmCertifyKeySignature;
    }

    public String getAikPemCertificate() {
        return aikPemCertificate;
    }

    public void setAikPemCertificate(String aikPemCertificate) {
        this.aikPemCertificate = aikPemCertificate;
    }

    
    @Override
    @RequiresPermissions({"host_signing_key_certificates:create"})
    public void run() {
        try {
            if (publicKeyModulus != null && tpmCertifyKey != null && tpmCertifyKeySignature != null && aikPemCertificate != null) {

                log.debug("Starting to verify the Binding key TCG certificate and generate the MTW certified certificate.");

                // Verify the encryption scheme, key flags etc
                validateCertifyKeyData(tpmCertifyKey, true);
                
               log.debug("Public key modulus {}, TpmCertifyKey data {}  & TpmCertifyKeySignature data {} are specified.",
                        TpmUtils.byteArrayToHexString(publicKeyModulus), TpmUtils.byteArrayToHexString(tpmCertifyKey), TpmUtils.byteArrayToHexString(tpmCertifyKeySignature));
                
                X509Certificate decodedAikPemCertificate = X509Util.decodePemCertificate(aikPemCertificate);
                log.debug("AIK Certificate {}", decodedAikPemCertificate.getIssuerX500Principal().getName());
                
                // Need to verify if the AIK is signed by the trusted Privacy CA, which would also ensure that the EK is verified.
                byte[] privacyCAPemBytes;
                try (FileInputStream privacyCAPemFile = new FileInputStream(My.configuration().getPrivacyCaIdentityCacertsFile())) {
                    privacyCAPemBytes = IOUtils.toByteArray(privacyCAPemFile);
                }
                
                X509Certificate privacyCACert = X509Util.decodePemCertificate(new String(privacyCAPemBytes));
                log.debug("Privacy CA Certificate {}", privacyCACert.getIssuerX500Principal().getName());
                
                if (!isAikCertifiedByPrivacyCA(decodedAikPemCertificate, privacyCACert)) {
                    throw new CertificateException("The specified AIK certificate is not trusted.");
                }
                
                if (!isCertifiedKeySignatureValid(tpmCertifyKey, tpmCertifyKeySignature, decodedAikPemCertificate)) {
                    throw new CertificateException("The signature specified for the certifiy key does not match.");
                }
                
                boolean validatePublicKeyDigest = validatePublicKeyDigest(publicKeyModulus, tpmCertifyKey);
                if (!validatePublicKeyDigest) {
                    throw new Exception("Public key specified does not map to the public key digest in the TCG binding certificate");
                }

                // Generate the TCG standard exponent to create the RSApublic key from the modulus specified.
                byte[] pubExp = new byte[3];
                pubExp[0] = (byte) (0x01 & 0xff);
                pubExp[1] = (byte) (0x00);
                pubExp[2] = (byte) (0x01 & 0xff);
                RSAPublicKey pubBk = TpmUtils.makePubKey(publicKeyModulus, pubExp);

                if (pubBk != null) {
                    log.debug("Successfully created the public key from the modulus specified");
                } else {
                    throw new Exception("Error during the creation of the public key from the modulus and exponent");
                }

                // first load the ca key
                byte[] combinedPrivateKeyAndCertPemBytes;
                try (FileInputStream cakeyIn = new FileInputStream(My.configuration().getCaKeystoreFile())) {
                    combinedPrivateKeyAndCertPemBytes = IOUtils.toByteArray(cakeyIn);
                }
                
                PrivateKey cakey = RsaUtil.decodePemPrivateKey(new String(combinedPrivateKeyAndCertPemBytes));
                X509Certificate cacert = X509Util.decodePemCertificate(new String(combinedPrivateKeyAndCertPemBytes));
                X509Builder caBuilder = X509Builder.factory();
                X509Certificate bkCert = caBuilder
                        .commonName("CN=Binding_Key_Certificate")
                        .subjectPublicKey(pubBk)
                        .expires(RsaUtil.DEFAULT_RSA_KEY_EXPIRES_DAYS, TimeUnit.DAYS)
                        .issuerPrivateKey(cakey)
                        .issuerName(cacert)
                        .keyUsageDigitalSignature()
                        .keyUsageNonRepudiation()
                        .keyUsageKeyEncipherment()
                        .extKeyUsageIsCritical()
                        .randomSerial()
                        .noncriticalExtension(tcgCertExtOid, tpmCertifyKey)
                        .noncriticalExtension(tcgCertSignatureOid, tpmCertifyKeySignature)
                        .build();

                if (bkCert != null) {
                    bindingKeyPemCertificate = X509Util.encodePemCertificate(bkCert);
                } else {
                    throw new Exception("Error during creation of the MTW signed binding key certificate");
                }

                log.debug("Successfully created the MTW signed PEM certificate for binding key: {}.", X509Util.encodePemCertificate(bkCert));

            } else {
                throw new Exception("Invalid input specified or input value missing.");
            }
        } catch (Exception ex) {
            log.error("Error during MTW signed binding key certificate.", ex);
            throw new RepositoryCreateException();
        }
    }
    
    
    /**
     * Verifies the encryption scheme, key type and the key flags used for the creation of the keys.
     * @param tcgCertificate
     * @param isBindingKey
     * @throws Exception 
     */
    protected static void validateCertifyKeyData(byte[] tcgCertificate, boolean isBindingKey) 
            throws Exception {
        int TPM_ES_RSAESOAEP_SHA1_MGF1 = 0x0003;
        int TPM_VOLATILE = 0x00000004;
        int TPM_KEY_SIGNING = 0x0010; // This SHALL indicate a signing key.
        int TPM_KEY_BIND = 0x0014; // This SHALL indicate a key that can be used for TPM_Bind and TPM_UnBind operations only
        
        try {
            
            TpmCertifyKey certifiedKey = new TpmCertifyKey(tcgCertificate);
            log.debug("Certified key info:");
            log.debug("@certifyKey@ *version info: {}", TpmUtils.byteArrayToHexString(certifiedKey.getStructVer()));                           
            log.debug("@certifyKey@ *key usage: {}", certifiedKey.getTpmKeyUsage());
            log.debug("@certifyKey@ *key flags: {}", certifiedKey.getTpmKeyFlags());
            log.debug("@certifyKey@ *auth data usage: {}", certifiedKey.getTpmAuthDataUsage());
            log.debug("@certifyKey@ *Alg params:: ");
            log.debug("@certifyKey@ *Alg id:  {}, enc scheme: {}, sig scheme: {}; parm size: {}",
                    certifiedKey.getKeyParms().getAlgorithmId(), certifiedKey.getKeyParms().getEncScheme(), 
                    certifiedKey.getKeyParms().getSigScheme(), TpmUtils.byteArrayToHexString(certifiedKey.getKeyParms().getSubParams().toByteArray()));      
            
            if ( certifiedKey.getKeyParms().getEncScheme() != TPM_ES_RSAESOAEP_SHA1_MGF1) {
                log.error("Invalid encryption scheme used. Using {} scheme instead of RSA.", certifiedKey.getKeyParms().getEncScheme());
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
    
    /**
     * Validates the public key digest in the CertifyKey against the public key specified.
     * @param publicKeyModulus
     * @param tcgCertificate
     * @return
     * @throws Exception 
     */
    protected static boolean validatePublicKeyDigest(byte[] publicKeyModulus, byte[] tcgCertificate) throws Exception {
        try {
            
            log.debug("Validating the public key.");
            byte[] calculatedModulusDigest = Sha1Digest.digestOf(publicKeyModulus).toByteArray();
            
            TpmCertifyKey certifiedKey = new TpmCertifyKey(tcgCertificate);
            byte[] providedDigest = certifiedKey.getPublicKeyDigest();
            
            log.debug("Calculated public key digest is {} -- passed in public key digest is {}", 
                    TpmUtils.byteArrayToHexString(calculatedModulusDigest), TpmUtils.byteArrayToHexString(certifiedKey.getPublicKeyDigest()));
            
            for (int i = 0; i < calculatedModulusDigest.length; i++) {
                if(calculatedModulusDigest[i] != providedDigest[i])
                    return false;
            }
            
            return true;
        } catch (TpmUtils.TpmBytestreamResouceException | TpmUtils.TpmUnsignedConversionException ex) {
            throw ex;
        }        
    }
    
    /**
     * Verifies if the specified AIK certificate is issued by Privacy CA and is valid.
     * @param aikCert
     * @param privacyCACert
     * @return
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws NoSuchProviderException
     * @throws SignatureException 
     */
    protected static boolean isAikCertifiedByPrivacyCA(X509Certificate aikCert, X509Certificate privacyCACert) 
            throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
        try {
            
            log.debug("Verifying the AIK cert with public key {} with the Privacy CA {}.", TpmUtils.byteArrayToHexString(aikCert.getPublicKey().getEncoded()),
                    TpmUtils.byteArrayToHexString(privacyCACert.getPublicKey().getEncoded()));
            
            if (aikCert != null && privacyCACert != null) {
                aikCert.verify(privacyCACert.getPublicKey());
                log.debug("Successfully verified the AIK signature against the Privacy CA");
                return true;
            }
            log.debug("Error verifying the AIK signature against the Privacy CA");
            return false;
            
        } catch (CertificateException | NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException | SignatureException ex) {
            log.error("Error during signature verification. {}", ex.getMessage());
            throw ex;
        }
    }
    
    /**
     * This function validates the certify key against the specified signature using the AIK certificate that was used during the key certification.
     * @param certifyKeyDataBlob
     * @param certifyKeySignatureBlob
     * @param aikCertificate
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException 
     */
    protected static boolean isCertifiedKeySignatureValid(byte[] certifyKeyDataBlob, byte[] certifyKeySignatureBlob, X509Certificate aikCertificate) 
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        byte[] oidPadding = TpmUtils.hexStringToByteArray("3021300906052B0E03021A05000414");
        try {
            
            log.debug("Verifying the certify key signature against the AIK cert which signed it.");
            PublicKey aikPublicKey = aikCertificate.getPublicKey();
            
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, aikPublicKey);
            byte[] signedDigest = cipher.doFinal(certifyKeySignatureBlob);
            byte[] signedDigestWithoutOidPadding = Arrays.copyOfRange(signedDigest, oidPadding.length, signedDigest.length);
            byte[] computedDigest = Sha1Digest.digestOf(certifyKeyDataBlob).toByteArray();
            
            log.debug("Verifying the signed digest {} against the computed digest {}", TpmUtils.byteArrayToHexString(signedDigestWithoutOidPadding), 
                    TpmUtils.byteArrayToHexString(computedDigest));
            
            boolean result = Arrays.equals( signedDigestWithoutOidPadding, computedDigest );
            
            log.debug("Result of signature verification is {}", result);
            
            return result;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
            log.error("Error during signature verification. {}", ex.getMessage());
            throw ex;
        }
        
    }
}
