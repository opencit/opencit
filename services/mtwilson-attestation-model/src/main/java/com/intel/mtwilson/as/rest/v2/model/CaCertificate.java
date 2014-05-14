/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.i18n.ErrorCode;
import com.intel.mtwilson.jaxrs2.CertificateDocument;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
@JacksonXmlRootElement(localName="ca_certificate")
public class CaCertificate extends CertificateDocument {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());
    
    private String name;
    private byte[] certificate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    
    public byte[] getCertificate() {
        return certificate;
    }

    public void setCertificate(byte[] certificate) {
        this.certificate = certificate;
    }

    
    @JsonIgnore
    @Override
    public X509Certificate getX509Certificate() {
        if( certificate == null ) { return null; }
        try {
            log.debug("Certificate bytes length {}", certificate.length);
            return X509Util.decodeDerCertificate(certificate);
        }
        catch(CertificateException ce) {
            log.error("Error decoding certificate.", ce);
            throw new ASException(ErrorCode.MS_CERTIFICATE_ENCODING_ERROR, ce.getClass().getSimpleName());
        }
    }

    @JsonIgnore
    @Override
    public void setX509Certificate(X509Certificate certificate) {
        if( certificate == null ) {
            this.certificate = null;
            return;
        }
        try {
            this.certificate = certificate.getEncoded();
        }
        catch(CertificateEncodingException ce) {
            log.error("Error decoding certificate.", ce);
            throw new ASException(ErrorCode.MS_CERTIFICATE_ENCODING_ERROR, ce.getClass().getSimpleName());
        }
    }
    
}
