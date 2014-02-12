/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.datatypes.ErrorCode;
import com.intel.mtwilson.jersey.CertificateDocument;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
@JacksonXmlRootElement(localName="host_aik_certificate")
public class HostAikCertificate extends CertificateDocument {

    Logger log = LoggerFactory.getLogger(getClass().getName());
    
    private String hostUuid;
    private String aikSha1;
    private byte[] certificate;

    public String getHostUuid() {
        return hostUuid;
    }

    public void setHostUuid(String hostUuid) {
        this.hostUuid = hostUuid;
    }

    public String getAikSha1() {
        return aikSha1;
    }

    public void setAikSha1(String aikSha1) {
        this.aikSha1 = aikSha1;
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
