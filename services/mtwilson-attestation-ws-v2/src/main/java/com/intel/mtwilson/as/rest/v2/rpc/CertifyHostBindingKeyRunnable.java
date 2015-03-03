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
import com.intel.mtwilson.util.tpm12.CertifyKey;
import gov.niarl.his.privacyca.TpmCertifyKey;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import gov.niarl.his.privacyca.TpmUtils;
import java.io.FileInputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author ssbangal
 */
@RPC("certify-host-binding-key")
@JacksonXmlRootElement(localName = "certify_host_binding_key")
public class CertifyHostBindingKeyRunnable implements Runnable {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CertifyHostBindingKeyRunnable.class);
    
    private byte[] publicKeyModulus;
    private byte[] tpmCertifyKey;
    private byte[] tpmCertifyKeySignature;
    private byte[] aikDerCertificate;
    private byte[] bindingKeyDerCertificate;

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

    public byte[] getTpmCertifyKeySignature() {
        return tpmCertifyKeySignature;
    }

    public void setTpmCertifyKeySignature(byte[] tpmCertifyKeySignature) {
        this.tpmCertifyKeySignature = tpmCertifyKeySignature;
    }

    public byte[] getAikDerCertificate() {
        return aikDerCertificate;
    }

    public void setAikDerCertificate(byte[] aikDerCertificate) {
        this.aikDerCertificate = aikDerCertificate;
    }

    public byte[] getBindingKeyDerCertificate() {
        return bindingKeyDerCertificate;
    }

    public void setBindingKeyDerCertificate(byte[] bindingKeyDerCertificate) {
        this.bindingKeyDerCertificate = bindingKeyDerCertificate;
    }
    
    @Override
    @RequiresPermissions({"host_signing_key_certificates:create"})
    public void run() {
        try {
            if (publicKeyModulus != null && tpmCertifyKey != null && tpmCertifyKeySignature != null && aikDerCertificate != null) {

                log.debug("Starting to verify the Binding key TCG certificate and generate the MTW certified certificate.");

                // Verify the encryption scheme, key flags etc
                if( !CertifyKey.isBindingKey(new TpmCertifyKey(tpmCertifyKey))) {
                    throw new Exception("Not a valid binding key");
                }
                
               log.debug("Public key modulus {}, TpmCertifyKey data {}  & TpmCertifyKeySignature data {} are specified.",
                        TpmUtils.byteArrayToHexString(publicKeyModulus), TpmUtils.byteArrayToHexString(tpmCertifyKey), TpmUtils.byteArrayToHexString(tpmCertifyKeySignature));
                
                X509Certificate decodedAikDerCertificate = X509Util.decodeDerCertificate(aikDerCertificate);
                log.debug("AIK Certificate {}", decodedAikDerCertificate.getIssuerX500Principal().getName());
                
                // Need to verify if the AIK is signed by the trusted Privacy CA, which would also ensure that the EK is verified.
                byte[] privacyCAPemBytes;
                try (FileInputStream privacyCAPemFile = new FileInputStream(My.configuration().getPrivacyCaIdentityCacertsFile())) {
                    privacyCAPemBytes = IOUtils.toByteArray(privacyCAPemFile);
                }
                
                X509Certificate privacyCACert = X509Util.decodePemCertificate(new String(privacyCAPemBytes));
                log.debug("Privacy CA Certificate {}", privacyCACert.getIssuerX500Principal().getName());
                
                if (!isAikCertifiedByPrivacyCA(decodedAikDerCertificate, privacyCACert)) {
                    throw new CertificateException("The specified AIK certificate is not trusted.");
                }
                
                if (!CertifyKey.isCertifiedKeySignatureValid(tpmCertifyKey, tpmCertifyKeySignature, decodedAikDerCertificate.getPublicKey())) {
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

                // first load the privacy ca key                
                log.debug("PrivacyCA.p12: {}", My.configuration().getPrivacyCaIdentityP12().getAbsolutePath());
                RSAPrivateKey cakey = TpmUtils.privKeyFromP12(My.configuration().getPrivacyCaIdentityP12().getAbsolutePath(), My.configuration().getPrivacyCaIdentityPassword());
                X509Certificate cacert = TpmUtils.certFromP12(My.configuration().getPrivacyCaIdentityP12().getAbsolutePath(), My.configuration().getPrivacyCaIdentityPassword());
                
                X509Builder caBuilder = X509Builder.factory();
                X509Certificate bkCert = caBuilder
                        .commonName("CN=Binding_Key_Certificate")
                        .subjectPublicKey(pubBk)
                        .expires(RsaUtil.DEFAULT_RSA_KEY_EXPIRES_DAYS, TimeUnit.DAYS)
                        .issuerPrivateKey(cakey)
                        .issuerName(cacert)
                        .keyUsageKeyEncipherment()
//                        .keyUsageDataEncipherment()
                        .extKeyUsageIsCritical()
                        .randomSerial()
                        .noncriticalExtension(CertifyKey.TCG_STRUCTURE_CERTIFY_INFO_OID, tpmCertifyKey)
                        .noncriticalExtension(CertifyKey.TCG_STRUCTURE_CERTIFY_INFO_SIGNATURE_OID, tpmCertifyKeySignature)
                        .build();

                if (bkCert != null) {
                    bindingKeyDerCertificate = X509Util.encodeDerCertificate(bkCert);
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
    

}
