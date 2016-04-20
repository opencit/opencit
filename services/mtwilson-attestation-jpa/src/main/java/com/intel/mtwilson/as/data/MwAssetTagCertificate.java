/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.data;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;
import sun.security.util.BigInt;

/**
 *
 * @author ssbangal
 */
@Entity
@Table(name = "mw_asset_tag_certificate")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "MwAssetTagCertificate.findAll", query = "SELECT m FROM MwAssetTagCertificate m"),
    @NamedQuery(name = "MwAssetTagCertificate.findById", query = "SELECT m FROM MwAssetTagCertificate m WHERE m.id = :id"),
    @NamedQuery(name = "MwAssetTagCertificate.findByHostID", query = "SELECT m FROM MwAssetTagCertificate m WHERE m.hostID = :hostID ORDER BY m.create_time DESC"),
    @NamedQuery(name = "MwAssetTagCertificate.findByUuidHex", query = "SELECT m FROM MwAssetTagCertificate m WHERE m.uuid_hex = :uuid_hex"),
    @NamedQuery(name = "MwAssetTagCertificate.findByUuid", query = "SELECT m FROM MwAssetTagCertificate m WHERE m.uuid = :uuid ORDER BY m.create_time DESC"),
    @NamedQuery(name = "MwAssetTagCertificate.findByRevoked", query = "SELECT m FROM MwAssetTagCertificate m WHERE m.revoked = :revoked"),
    @NamedQuery(name = "MwAssetTagCertificate.findByNotBefore", query = "SELECT m FROM MwAssetTagCertificate m WHERE m.notBefore = :notBefore"),
    @NamedQuery(name = "MwAssetTagCertificate.findByNotAfter", query = "SELECT m FROM MwAssetTagCertificate m WHERE m.notAfter = :notAfter"),
    @NamedQuery(name = "MwAssetTagCertificate.findBySha1Hash", query = "SELECT m FROM MwAssetTagCertificate m WHERE m.sHA1Hash = :sHA1Hash")
    //@NamedQuery(name = "MwAssetTagCertificate.findBySha256Hash", query = "SELECT m FROM MwAssetTagCertificate m WHERE m.sHA256Hash = :sHA256Hash")
    })
public class MwAssetTagCertificate implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID")
    private Integer id;
    @Column(name = "Host_ID")
    private Integer hostID;
    @Column(name = "UUID")
    private String uuid;
    @Basic(optional = false)
    @Lob
    @Column(name = "Certificate")
    private byte[] certificate;
    @Lob
    @Column(name = "SHA1_Hash")
    private byte[] sHA1Hash;
    @Column(name = "uuid_hex")
    private String uuid_hex;
    @Column(name = "create_time")
    private BigInteger create_time;    
    /*
    @Lob
    @Column(name = "SHA256_Hash")
    private byte[] sHA256Hash;
    */
    @Lob
    @Column(name = "PCREvent")
    private byte[] pCREvent;
    @Column(name = "Revoked")
    private Boolean revoked;
    @Column(name = "NotBefore")
    @Temporal(TemporalType.TIMESTAMP)
    private Date notBefore;
    @Column(name = "NotAfter")
    @Temporal(TemporalType.TIMESTAMP)
    private Date notAfter;

    public MwAssetTagCertificate() {
    }

    public MwAssetTagCertificate(Integer id) {
        this.id = id;
    }

    public MwAssetTagCertificate(Integer id, byte[] certificate) {
        this.id = id;
        this.certificate = certificate;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getHostID() {
        return hostID;
    }

    public void setHostID(Integer hostID) {
        this.hostID = hostID;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public byte[] getCertificate() {
        return certificate;
    }

    public void setCertificate(byte[] certificate) {
        this.certificate = certificate;
    }

    public byte[] getSHA1Hash() {
        return sHA1Hash;
    }

    public void setSHA1Hash(byte[] sHA1Hash) {
        this.sHA1Hash = sHA1Hash;
    }

    /*
    public byte[] getSHA256Hash() {
        return sHA256Hash;
    }

    public void setSHA256Hash(byte[] sHA256Hash) {
        this.sHA256Hash = sHA256Hash;
    }
    */
    public byte[] getPCREvent() {
        return pCREvent;
    }

    public void setPCREvent(byte[] pCREvent) {
        this.pCREvent = pCREvent;
    }

    public Boolean getRevoked() {
        return revoked;
    }

    public void setRevoked(Boolean revoked) {
        this.revoked = revoked;
    }

    public Date getNotBefore() {
        return notBefore;
    }

    public void setNotBefore(Date notBefore) {
        this.notBefore = notBefore;
    }

    public Date getNotAfter() {
        return notAfter;
    }

    public void setNotAfter(Date notAfter) {
        this.notAfter = notAfter;
    }

    public String getUuid_hex() {
        return uuid_hex;
    }

    public void setUuid_hex(String uuid_hex) {
        this.uuid_hex = uuid_hex;
    }

    public BigInteger getCreate_time() {
        return create_time;
    }

    public void setCreate_time(BigInteger create_time) {
        this.create_time = create_time;
    }

    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof MwAssetTagCertificate)) {
            return false;
        }
        MwAssetTagCertificate other = (MwAssetTagCertificate) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.intel.mtwilson.as.data.MwAssetTagCertificate[ id=" + id + " ]";
    }
    
}
