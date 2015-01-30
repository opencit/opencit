/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.rpc;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.x509.X509Builder;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.My;
import com.intel.mtwilson.launcher.ws.ext.RPC;
import com.intel.mtwilson.repository.RepositoryCreateException;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import gov.niarl.his.privacyca.TpmCertifyKey;
import gov.niarl.his.privacyca.TpmUtils;
import java.io.FileInputStream;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author ssbangal
 */
@RPC("certify-host-binding-key")
@JacksonXmlRootElement(localName="certify_host_binding_key")
public class CertifyHostBindingKeyRunnable implements Runnable{

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CertifyHostBindingKeyRunnable.class);

    private String tcgCertificateAsHex;
    private String pemCertificate;

    public String getTcgCertificateAsHex() {
        return tcgCertificateAsHex;
    }

    public void setTcgCertificateAsHex(String tcgCertificateAsHex) {
        this.tcgCertificateAsHex = tcgCertificateAsHex;
    }

    public String getPemCertificate() {
        return pemCertificate;
    }

    public void setPemCertificate(String pemCertificate) {
        this.pemCertificate = pemCertificate;
    }

    
    @Override
    @RequiresPermissions({"hosts:create","hosts:store"})
    public void run() {
        try {
            if (tcgCertificateAsHex != null) {
                log.debug("Starting to verify the TCG certificate and generate the MTW certified certificate: {}.", tcgCertificateAsHex);
                
                // first load the ca key
                byte[] combinedPrivateKeyAndCertPemBytes;
                try (FileInputStream cakeyIn = new FileInputStream(My.configuration().getCaKeystoreFile())) {
                    combinedPrivateKeyAndCertPemBytes = IOUtils.toByteArray(cakeyIn);
                }
                PrivateKey cakey = RsaUtil.decodePemPrivateKey(new String(combinedPrivateKeyAndCertPemBytes));
                X509Certificate cacert = X509Util.decodePemCertificate(new String(combinedPrivateKeyAndCertPemBytes));
                
                TpmCertifyKey tpmCertifyKey = new TpmCertifyKey(TpmUtils.hexStringToByteArray(tcgCertificateAsHex));
                X509Builder caBuilder = X509Builder.factory();
                X509Certificate bkCert = caBuilder
                    .commonName("CN=Binding_Key_Certificate")
                    .subjectPublicKey(tpmCertifyKey.getKey())
                    .expires(RsaUtil.DEFAULT_RSA_KEY_EXPIRES_DAYS, TimeUnit.DAYS)
                    .issuerPrivateKey(cakey)
                    .issuerName(cacert)
                    .keyUsageDigitalSignature()
                    .keyUsageNonRepudiation()
                    .keyUsageKeyEncipherment()
                    .extKeyUsageIsCritical()
                    .randomSerial()
                    .build();
                
                if (bkCert != null) {
                    pemCertificate = X509Util.encodePemCertificate(bkCert);
                } else {
                    throw new Exception("Error during creation of the MTW signed binding key certificate");
                }
                log.debug("Successfully created the MTW signed PEM certificate for binding key: {}.", pemCertificate);
            }
        } catch (Exception ex) {
            log.error("Error during MTW signed binding key certificate.", ex);
            throw new RepositoryCreateException();
        }
    }
    
}
