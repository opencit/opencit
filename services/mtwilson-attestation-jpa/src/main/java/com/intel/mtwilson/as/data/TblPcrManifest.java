/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.data;

import com.intel.mtwilson.audit.annotations.AuditIgnore;
import com.intel.mtwilson.audit.handler.AuditEventHandler;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;
import org.eclipse.persistence.annotations.Customizer;

/**
 *
 * @author dsmagadx
 */
@Entity
@Customizer(AuditEventHandler.class)
@Table(name = "mw_pcr_manifest")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "TblPcrManifest.findAll", query = "SELECT t FROM TblPcrManifest t"),
    @NamedQuery(name = "TblPcrManifest.findById", query = "SELECT t FROM TblPcrManifest t WHERE t.id = :id"),
    @NamedQuery(name = "TblPcrManifest.findByName", query = "SELECT t FROM TblPcrManifest t WHERE t.name = :name"),
    @NamedQuery(name = "TblPcrManifest.findByValue", query = "SELECT t FROM TblPcrManifest t WHERE t.value = :value"),
//    @NamedQuery(name = "TblPcrManifest.findByCreatedOn", query = "SELECT t FROM TblPcrManifest t WHERE t.createdOn = :createdOn"),
//    @NamedQuery(name = "TblPcrManifest.findByUpdatedOn", query = "SELECT t FROM TblPcrManifest t WHERE t.updatedOn = :updatedOn"),
    @NamedQuery(name = "TblPcrManifest.findByUuidHex", query = "SELECT t FROM TblPcrManifest t WHERE t.uuid_hex = :uuid_hex"),
    @NamedQuery(name = "TblPcrManifest.findByMleUuidHex", query = "SELECT t FROM TblPcrManifest t WHERE t.mle_uuid_hex = :mle_uuid_hex"),    
    @NamedQuery(name = "TblPcrManifest.findByPCRDescription", query = "SELECT t FROM TblPcrManifest t WHERE t.pCRDescription = :pCRDescription"),
    @NamedQuery(name = "TblPcrManifest.findByMleIdName", query = "SELECT t FROM TblPcrManifest t WHERE t.mleId.id = :mleId and t.name = :name")})
    @NamedQuery(name = "TblPcrManifest.findByMleIdNamePcrBank", query = "SELECT t FROM TblPcrManifest t WHERE t.mleId.id = :mleId and t.name = :name and t.pcrBank = :pcrBank")

public class TblPcrManifest implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "Name")
    private String name;
    @Basic(optional = false)
    @Column(name = "Value")
    private String value;    
    @Column(name = "pcr_bank")
    private String pcrBank;

    public String getPcrBank() {
        return pcrBank;
    }

    public void setPcrBank(String pcrBank) {
        this.pcrBank = pcrBank;
    }
    // @since 1.1 we are relying on the audit log for "created on", "created by", etc. type information
    /*
    @Basic(optional = false)
    @Column(name = "Created_On")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn;
    @Basic(optional = false)
    @Column(name = "Updated_On")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedOn;
    */
    @Column(name = "PCR_Description")
    private String pCRDescription;
    
    // @since 1.1 we are relying on the audit log for "created on", "created by", etc. type information
    /*
    @JoinColumn(name = "Updated_By", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private TblDbPortalUser updatedBy;
    @JoinColumn(name = "Created_By", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private TblDbPortalUser createdBy;
    */
    @JoinColumn(name = "MLE_ID", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private TblMle mleId;
    @Column(name = "uuid_hex")
    private String uuid_hex;
    @Column(name = "mle_uuid_hex")
    private String mle_uuid_hex;

    public TblPcrManifest() {
    }

    public TblPcrManifest(Integer id) {
        this.id = id;
    }

    public TblPcrManifest(Integer id, String name, String value, Date createdOn, Date updatedOn) {
        this.id = id;
        this.name = name;
        this.value = value;
        // @since 1.1 we are relying on the audit log for "created on", "created by", etc. type information
        /*
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
        */
    }

    public TblPcrManifest(Integer id, String name, String value) {
        this.id = id;
        this.name = name;
        this.value = value;
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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    // @since 1.1 we are relying on the audit log for "created on", "created by", etc. type information
    /*
    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public Date getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(Date updatedOn) {
        this.updatedOn = updatedOn;
    }
    */
    public String getPCRDescription() {
        return pCRDescription;
    }

    public void setPCRDescription(String pCRDescription) {
        this.pCRDescription = pCRDescription;
    }

    // @since 1.1 we are relying on the audit log for "created on", "created by", etc. type information
    /*
    public TblDbPortalUser getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(TblDbPortalUser updatedBy) {
        this.updatedBy = updatedBy;
    }

    public TblDbPortalUser getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(TblDbPortalUser createdBy) {
        this.createdBy = createdBy;
    }*/

    public TblMle getMleId() {
        return mleId;
    }

    public void setMleId(TblMle mleId) {
        this.mleId = mleId;
    }

    public String getUuid_hex() {
        return uuid_hex;
    }

    public void setUuid_hex(String uuid_hex) {
        this.uuid_hex = uuid_hex;
    }

    public String getMle_uuid_hex() {
        return mle_uuid_hex;
    }

    public void setMle_uuid_hex(String mle_uuid_hex) {
        this.mle_uuid_hex = mle_uuid_hex;
    }

    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof TblPcrManifest)) {
            return false;
        }
        TblPcrManifest other = (TblPcrManifest) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.intel.mountwilson.as.data.TblPcrManifest[ id=" + id + " ]";
    }
    
}
