/**
 * 
 */
package com.intel.mountwilson.datamodel;

import java.util.Date;
import java.util.List;

/**
 * @author yuvrajsx
 *
 */
public class ApiClientDetails {
	
    private String name;
    private byte[] certificate;
    private String fingerprint;
    private String issuer;
    private Integer serialNumber;
    private boolean enabled;
    private String status;
    private List<String> requestedRoles;
    private Date expires;
    private String comments;

    public byte[] getCertificate() {
        return certificate;
    }

    public void setCertificate(byte[] certificate) {
        this.certificate = certificate;
    }

    public String getComment() {
        return comments;
    }

    public void setComment(String comment) {
        this.comments = comment;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Date getExpires() {
        return expires;
    }

    public void setExpires(Date expires) {
        this.expires = expires;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getRequestedRoles() {
        return requestedRoles;
    }

    public void setRequestedRoles(List<String> requestedRoles) {
        this.requestedRoles = requestedRoles;
    }

    public Integer getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(Integer serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
	
}
