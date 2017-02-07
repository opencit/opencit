/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.rpc;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.crypto.Sha256Digest;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.x509.X509Builder;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.My;
import com.intel.mtwilson.launcher.ws.ext.RPC;
import com.intel.mtwilson.repository.RepositoryCreateException;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import gov.niarl.his.privacyca.TpmUtils;
import java.io.FileInputStream;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.IOUtils;
import static com.intel.mtwilson.as.rest.v2.rpc.CertifyHostBindingKeyRunnable.isAikCertifiedByPrivacyCA;
import static com.intel.mtwilson.as.rest.v2.rpc.CertifyHostBindingKeyRunnable.validatePublicKeyDigest;
import com.intel.mtwilson.util.tpm12.CertifyKey;
import com.intel.mtwilson.util.tpm20.CertifyKey20;
import gov.niarl.his.privacyca.TpmCertifyKey;
import gov.niarl.his.privacyca.TpmCertifyKey20;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.util.Arrays;
import javax.xml.bind.DatatypeConverter;
import org.apache.commons.codec.binary.Hex;
import java.nio.ByteBuffer;

/**
 *
 * @author ssbangal
 */
@RPC("certify-host-signing-key")
@JacksonXmlRootElement(localName = "certify_host_signing_key")
public class CertifyHostSigningKeyRunnable implements Runnable {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CertifyHostSigningKeyRunnable.class);
    
    private byte[] publicKeyModulus;
    private byte[] tpmCertifyKey;
    private byte[] signingKeyDerCertificate;
    private byte[] tpmCertifyKeySignature;
    private byte[] aikDerCertificate;
    private byte[] nameDigest;
    private String tpmVersion;

    public byte[] getNameDigest() {
        return nameDigest;
    }
    public void setNameDigest(byte[] nameDigest) {
        this.nameDigest = nameDigest;
    }

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

    public byte[] getSigningKeyDerCertificate() {
        return signingKeyDerCertificate;
    }

    public void setSigningKeyDerCertificate(byte[] signingKeyDerCertificate) {
        this.signingKeyDerCertificate = signingKeyDerCertificate;
    }

    public byte[] getAikDerCertificate() {
        return aikDerCertificate;
    }

    public void setAikDerCertificate(byte[] aikDerCertificate) {
        this.aikDerCertificate = aikDerCertificate;
    }

    public String getTpmVersion(){
        return tpmVersion;
    }
    public void setTpmVersion(String tpmVersion){
        this.tpmVersion = tpmVersion;
    }

    @Override
    @RequiresPermissions({"host_signing_key_certificates:create"})
    public void run() {
        try {
            if (publicKeyModulus != null && tpmCertifyKey != null && tpmCertifyKeySignature != null && aikDerCertificate != null) {
				// Need to verify nameDigest it only works on 2.0
                if(tpmVersion != null && tpmVersion.equals("2.0") && nameDigest == null)
                     throw new Exception("Invalid input specified or input value missing.");

                log.debug("Starting to verify the Signing key TCG certificate and generate the MTW certified certificate.");

                log.debug("Public key modulus {}, TpmCertifyKey data {} & TpmCertifyKeySignature data {} are specified.",
                        TpmUtils.byteArrayToHexString(publicKeyModulus), TpmUtils.byteArrayToHexString(tpmCertifyKey), TpmUtils.byteArrayToHexString(tpmCertifyKeySignature));

			    
                // Verify the encryption scheme, key flags etc
                // validateCertifyKeyData(tpmCertifyKey, false);       
				TpmCertifyKey tpmCertKey = null;
                if (tpmVersion.equals("1.2")) {
					tpmCertKey = new TpmCertifyKey(tpmCertifyKey);
					if( !CertifyKey.isSigningKey(tpmCertKey)) {
						throw new Exception("Not a valid binding key");
					}
                }
                
                else if(tpmVersion.equals("2.0")) {
                    if( !CertifyKey20.isSigningKey(new TpmCertifyKey20(tpmCertifyKey))) {
                      throw new Exception("Not a valid signing key");
                    }
                }
                else {
                    throw new Exception("Invalid TPM version detected...");
                }
                
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

                boolean validatePublicKeyDigest = false;

                if(tpmVersion == null || tpmVersion.equals("1.2")) {
                if (!CertifyKey.isCertifiedKeySignatureValid(tpmCertifyKey, tpmCertifyKeySignature, decodedAikDerCertificate.getPublicKey())) {
                    throw new CertificateException("The signature specified for the certifiy key does not match.");
                }
                  //In TPM 1.2 we validate TPM Public Key Digest
                  validatePublicKeyDigest = validatePublicKeyDigest(publicKeyModulus, tpmCertifyKey);
                  if (!validatePublicKeyDigest) {
                      throw new Exception("Public key specified does not map to the public key digest in the TCG binding certificate");
                  }
                }
                
                else if(tpmVersion.equals("2.0")) {
                  //validatePublicKeyDigest = validatePublicKeyDigest(publicKeyModulus, tpmCertifyKey);

                if (!CertifyKey20.isCertifiedKeySignatureValid(tpmCertifyKey, tpmCertifyKeySignature, decodedAikDerCertificate.getPublicKey())) {
                      throw new CertificateException("The signature specified for the certifiy key does not match.");
                }


                  //In TPM 2.0 we validate TPM unique name give to each key
                  validatePublicKeyDigest = validateName(nameDigest, tpmCertifyKey);
                  if (!validatePublicKeyDigest) {
                      throw new Exception("TPM Key Name specified does not match name digest in the TCG binding certificate");
                  }
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

                // Load the Privacy CA key
                log.debug("PrivacyCA.p12: {}", My.configuration().getPrivacyCaIdentityP12().getAbsolutePath());
                RSAPrivateKey cakey = TpmUtils.privKeyFromP12(My.configuration().getPrivacyCaIdentityP12().getAbsolutePath(), My.configuration().getPrivacyCaIdentityPassword());
                X509Certificate cacert = TpmUtils.certFromP12(My.configuration().getPrivacyCaIdentityP12().getAbsolutePath(), My.configuration().getPrivacyCaIdentityPassword());
				//Read encryption scheme used in binding key
                ByteBuffer byteBuffer = ByteBuffer.allocate(2);
                TpmCertifyKey20 tpmCertifyKey20 = new TpmCertifyKey20(tpmCertifyKey);
                short hashAlg = tpmCertifyKey20.getTpmuAttest().getTpmsCertifyInfoBlob().getTpmtHa().getHashAlg();
                byteBuffer.putShort(hashAlg);
                
                X509Builder caBuilder = X509Builder.factory();
                // Add encryption scheme
				//ToDo: Add encryption Scheme in certificate attribute
                X509Certificate bkCert = caBuilder
                        .commonName("CN=Signing_Key_Certificate")
                        .subjectPublicKey(pubBk)
                        .expires(RsaUtil.DEFAULT_RSA_KEY_EXPIRES_DAYS, TimeUnit.DAYS)
                        .issuerPrivateKey(cakey)
                        .issuerName(cacert)
                        .keyUsageDigitalSignature()
                        .keyUsageNonRepudiation()
                        .extKeyUsageIsCritical()
                        .randomSerial()
                        .noncriticalExtension(CertifyKey.TCG_STRUCTURE_CERTIFY_INFO_OID, tpmCertifyKey)
                        .noncriticalExtension(CertifyKey.TCG_STRUCTURE_CERTIFY_INFO_SIGNATURE_OID, tpmCertifyKeySignature)
						.noncriticalExtension(CertifyKey.TCG_STRUCTURE_CERTIFY_INFO_ENC_SCHEME_OID, byteBuffer.array())
                        .build();

                if (bkCert != null) {
                    signingKeyDerCertificate = X509Util.encodeDerCertificate(bkCert);
                } else {
                    throw new Exception("Error during creation of the MTW signed signing key certificate");
                }

                log.debug("Successfully created the MTW signed PEM certificate for signing key: {}.", X509Util.encodePemCertificate(bkCert));

            } else {
                throw new Exception("Invalid input specified or input value missing.");
            }            
        } catch (Exception ex) {
            log.error("Error during MTW signed signing key certificate.", ex);
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
     * Validates the name digest in the CertifyKey against the name blob
     * specified.
     *
     * @param nameDigest
     * @param tcgCertificate
     * @return
     * @throws Exception
     */
    protected static boolean validateName(byte[] nameDigest, byte[] tcgCertificate) throws Exception {
        try {
            byte[] padding = Hex.decodeHex("2200000b".toCharArray());
            byte[] endPadding = Hex.decodeHex("00000000000000000000000000000000000000000000000000000000000000000000".toCharArray());
            log.debug("Validating the Name.");
            TpmCertifyKey20 tpmCertifyKey = new TpmCertifyKey20(tcgCertificate);
            byte[] providedName = tpmCertifyKey.getTpmuAttest().getTpmsCertifyInfoBlob().getTpmtHa().getDigest();
            byte[] providedNameWithoutPadding = Arrays.copyOfRange(nameDigest, padding.length, nameDigest.length - endPadding.length);
            log.debug("providedName is {} of size {}\nprovidedNameWithoutPadding is {} of size {}",
                    TpmUtils.byteArrayToHexString(providedName), providedName.length, TpmUtils.byteArrayToHexString(providedNameWithoutPadding), providedNameWithoutPadding.length);

            log.debug("Calculated public key digest is {} -- passed in public key digest is {}",
                    TpmUtils.byteArrayToHexString(providedNameWithoutPadding), TpmUtils.byteArrayToHexString(providedNameWithoutPadding));

            for (int i = 0; i < providedNameWithoutPadding.length; i++) {
                log.debug("Comparing {} with {}", nameDigest[i], providedNameWithoutPadding[i]);
                if (providedNameWithoutPadding[i] != providedNameWithoutPadding[i]) {
                    return false;
                }
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
