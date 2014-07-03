/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.ms.data;

import com.intel.mtwilson.ms.converter.ByteArrayToBase64Converter;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.persistence.Convert;

/**
            @QueryParam("expiresAfter") String expiresAfter,
            @QueryParam("expiresBefore") String expiresBefore,
            @QueryParam("fingerprintEqualTo") String fingerprintEqualTo,
            @QueryParam("issuerEqualTo") String issuerEqualTo,
            @QueryParam("nameContains") String nameContains,
            @QueryParam("nameEqualTo") String nameEqualTo,
            @QueryParam("serialNumberEqualTo") String serialNumberEqualTo,
            @QueryParam("statusEqualTo") String statusEqualTo

 */
/**
 *
 * @author dsmagadx
 */
@Entity
@Table(name = "mw_api_client_x509")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ApiClientX509.findAll", query = "SELECT a FROM ApiClientX509 a"),
    @NamedQuery(name = "ApiClientX509.findById", query = "SELECT a FROM ApiClientX509 a WHERE a.id = :id"),
    @NamedQuery(name = "ApiClientX509.findByName", query = "SELECT a FROM ApiClientX509 a WHERE a.name = :name"),
    @NamedQuery(name = "ApiClientX509.findByNameLike", query = "SELECT a FROM ApiClientX509 a WHERE a.name LIKE :name"), // it's the caller's responsibility to add "%" before and/or after the name value
    @NamedQuery(name = "ApiClientX509.findByFingerprint", query = "SELECT a FROM ApiClientX509 a WHERE a.fingerprint = :fingerprint"), // added to facilitate authentication filter -jabuhacx 20120621
    @NamedQuery(name = "ApiClientX509.findByFingerprintEnabled", query = "SELECT a FROM ApiClientX509 a WHERE a.fingerprint = :fingerprint AND a.enabled = :enabled"), 
    @NamedQuery(name = "ApiClientX509.findByIssuer", query = "SELECT a FROM ApiClientX509 a WHERE a.issuer = :issuer"),
    @NamedQuery(name = "ApiClientX509.findBySerialNumber", query = "SELECT a FROM ApiClientX509 a WHERE a.serialNumber = :serialNumber"),
    @NamedQuery(name = "ApiClientX509.findByCommentLike", query = "SELECT a FROM ApiClientX509 a WHERE a.comment LIKE :comment"), // it's the caller's responsibility to add "%" before and/or after the name value
    @NamedQuery(name = "ApiClientX509.findByExpires", query = "SELECT a FROM ApiClientX509 a WHERE a.expires = :expires"),
    @NamedQuery(name = "ApiClientX509.findByExpiresAfter", query = "SELECT a FROM ApiClientX509 a WHERE a.expires > :expires"),
    @NamedQuery(name = "ApiClientX509.findByExpiresBefore", query = "SELECT a FROM ApiClientX509 a WHERE a.expires < :expires"),
    @NamedQuery(name = "ApiClientX509.findByEnabled", query = "SELECT a FROM ApiClientX509 a WHERE a.enabled = :enabled"),
    @NamedQuery(name = "ApiClientX509.findByStatus", query = "SELECT a FROM ApiClientX509 a WHERE a.status = :status"),
    @NamedQuery(name = "ApiClientX509.findByEnabledStatus", query = "SELECT a FROM ApiClientX509 a WHERE a.enabled = :enabled AND a.status = :status"),
    @NamedQuery(name = "ApiClientX509.findByUuid", query = "SELECT a FROM ApiClientX509 a WHERE a.uuid_hex = :uuid_hex"),
    @NamedQuery(name = "ApiClientX509.findByUserUuid", query = "SELECT a FROM ApiClientX509 a WHERE a.user_uuid_hex = :user_uuid_hex")})

public class ApiClientX509 implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "name")
    private String name;
    @Basic(optional = false)
    @Lob
    @Column(name = "certificate")
//    @Convert(converter=ByteArrayToBase64Converter.class)
    private byte[] certificate;
    @Basic(optional = false)
    @Lob
    @Column(name = "fingerprint")
    private byte[] fingerprint;
    @Column(name = "issuer")
    private String issuer;
    @Column(name = "serial_number")
    private Integer serialNumber;
    @Column(name = "expires")
    @Temporal(TemporalType.TIMESTAMP)
    private Date expires;
    @Basic(optional = false)
    @Column(name = "enabled")
    private boolean enabled;
    @Basic(optional = false)
    @Column(name = "status")
    private String status;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "apiClientX509")
    private Collection<ApiRoleX509> apiRoleX509Collection;
    @Basic(optional = true)
    @Column(name = "comment")
    private String comment;
    @Basic(optional = true)
    @Column(name = "locale")
    private String locale;
    @Basic(optional = false)
    @Column(name = "uuid_hex")
    private String uuid_hex;
    @Basic(optional = false)
    @Column(name = "user_uuid_hex")
    private String user_uuid_hex;

    public ApiClientX509() {
    }

    public ApiClientX509(Integer id) {
        this.id = id;
    }

    public ApiClientX509(Integer id, String name, byte[] certificate, byte[] fingerprint, boolean enabled, String status) {
        this.id = id;
        this.name = name;
        this.certificate = certificate;
        this.fingerprint = fingerprint;
        this.enabled = enabled;
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

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

    public byte[] getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(byte[] fingerprint) {
        this.fingerprint = fingerprint;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public Integer getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(Integer serialNumber) {
        this.serialNumber = serialNumber;
    }

    public Date getExpires() {
        return expires;
    }

    public void setExpires(Date expires) {
        this.expires = expires;
    }

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @XmlTransient
    public Collection<ApiRoleX509> getApiRoleX509Collection() {
        return apiRoleX509Collection;
    }

    public void setApiRoleX509Collection(Collection<ApiRoleX509> apiRoleX509Collection) {
        this.apiRoleX509Collection = apiRoleX509Collection;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
    
    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getUuid_hex() {
        return uuid_hex;
    }

    public void setUuid_hex(String uuid_hex) {
        this.uuid_hex = uuid_hex;
    }

    public String getUser_uuid_hex() {
        return user_uuid_hex;
    }

    public void setUser_uuid_hex(String user_uuid_hex) {
        this.user_uuid_hex = user_uuid_hex;
    }

    public String getUserNameFromName() {
        String x509UserName = name;
        String[] parts = x509UserName.split(",");
        String[] subParts = parts[0].split("=");
        String userName = subParts[1];
        return userName;
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ApiClientX509)) {
            return false;
        }
        ApiClientX509 other = (ApiClientX509) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.intel.mountwilson.ms.data.ApiClientX509[ id=" + id + " ]";
    }
    
}
