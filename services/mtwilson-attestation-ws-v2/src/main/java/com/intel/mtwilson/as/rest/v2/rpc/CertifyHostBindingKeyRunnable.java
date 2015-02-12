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
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
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
    // This OID is used for storing the TCG standard certificate as an attr within the x.509 cert.
    // We are using this OID as we could not find any specific OID for the certifyKey structure.
    // 2.23.133.6 is the OID for joint-isu-itu-t(2) international-organizations(23) tcg(133) tcg-ce(6) 
    // and we are adding to it certify-key-validation-data(434188) to be specific because there is
    // no official TCG OID for this in the TCG Credential Profiles v1.2 specification (level2 revision8).
    protected static final String tcgCertExtOid = "2.23.133.6.434188"; 
    
    private byte[] publicKeyModulus;
    private byte[] tpmCertifyKey;
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

    @Override
    @RequiresPermissions({"host_signing_key_certificates:create"})
    public void run() {
        try {
            if (publicKeyModulus != null && tpmCertifyKey != null) {

                log.debug("Starting to verify the Binding key TCG certificate and generate the MTW certified certificate.");

                log.debug("Public key modulus {} and TpmCertifyKey data {} are specified.",
                        TpmUtils.byteArrayToHexString(publicKeyModulus), TpmUtils.byteArrayToHexString(tpmCertifyKey));

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
                        .keyUsageKeyEncipherment()
                        .extKeyUsageIsCritical()
                        .randomSerial()
                        .noncriticalExtension(tcgCertExtOid, tpmCertifyKey)
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
}
