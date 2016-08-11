/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.data;

import com.intel.mtwilson.audit.handler.AuditEventHandler;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.eclipse.persistence.annotations.Customizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dsmagadx
 */
@Entity
@Customizer(AuditEventHandler.class)
@Table(name = "mw_module_manifest")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "TblModuleManifest.findAll", query = "SELECT t FROM TblModuleManifest t"),
    @NamedQuery(name = "TblModuleManifest.findById", query = "SELECT t FROM TblModuleManifest t WHERE t.id = :id"),
    @NamedQuery(name = "TblModuleManifest.findByDescription", query = "SELECT t FROM TblModuleManifest t WHERE t.description = :description"),
//    @NamedQuery(name = "TblModuleManifest.findByCreatedOn", query = "SELECT t FROM TblModuleManifest t WHERE t.createdOn = :createdOn"),
    @NamedQuery(name = "TblModuleManifest.findByMleId", query = "SELECT t FROM TblModuleManifest t WHERE t.mleId.id = :mleId"),
    @NamedQuery(name = "TblModuleManifest.findByUuidHex", query = "SELECT t FROM TblModuleManifest t WHERE t.uuid_hex = :uuid_hex"),
    @NamedQuery(name = "TblModuleManifest.findByMleUuidHex", query = "SELECT t FROM TblModuleManifest t WHERE t.mle_uuid_hex = :mle_uuid_hex"),    
    @NamedQuery(name = "TblModuleManifest.findByComponentNameLike", query = "SELECT t FROM TblModuleManifest t WHERE t.componentName LIKE :name"),    
    @NamedQuery(name = "TblModuleManifest.findByMleNameEventName", query = "SELECT t FROM TblModuleManifest t WHERE t.mleId.id = :mleId and t.componentName= :name and t.eventID.name = :eventName"),
    @NamedQuery(name = "TblModuleManifest.findByMleNameEventNamePcrBank", query = "SELECT t FROM TblModuleManifest t WHERE t.mleId.id = :mleId and t.componentName = :name and t.eventID.name = :eventName and t.pcrBank = :pcrBank"),
    @NamedQuery(name = "TblModuleManifest.findByModuleValue", query = "SELECT t FROM TblModuleManifest t WHERE t.digestValue = :digestValue"),
    @NamedQuery(name = "TblModuleManifest.findByMleIDEventIDPcrBank", query = "SELECT t FROM TblModuleManifest t WHERE t.mleId.id = :mleId and t.eventID.id = :eventId and t.componentName= :name and t.pcrBank = :pcrBank")
    })
//    @NamedQuery(name = "TblModuleManifest.findByUpdatedOn", query = "SELECT t FROM TblModuleManifest t WHERE t.updatedOn = :updatedOn")


public class TblModuleManifest implements Serializable {
    @Transient
    private Logger log = LoggerFactory.getLogger(getClass().getName());
        
    @Basic(optional = false)
    @Column(name = "ComponentName")
    private String componentName;
    @Basic(optional = false)
    @Column(name = "DigestValue")
    private String digestValue;
    @Column(name = "pcr_bank")
    private String pcrBank;

    public String getPcrBank() {
        return pcrBank;
    }

    public void setPcrBank(String pcrBank) {
        this.pcrBank = pcrBank;
    }
    @Column(name = "ExtendedToPCR")
    private String extendedToPCR;
    @Column(name = "PackageName")
    private String packageName;
    @Column(name = "PackageVendor")
    private String packageVendor;
    @Column(name = "PackageVersion")
    private String packageVersion;
    @Column(name = "UseHostSpecificDigestValue")
    private Boolean useHostSpecificDigestValue;
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
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "moduleManifestID")
    private Collection<TblHostSpecificManifest> tblHostSpecificManifestCollection;
    @JoinColumn(name = "Event_ID", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private TblEventType eventID;
    @JoinColumn(name = "NameSpace_ID", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private TblPackageNamespace nameSpaceID;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID")
    private Integer id;
    @Column(name = "Description")
    private String description;
    
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

    public TblModuleManifest() {
    }

    public TblModuleManifest(Integer id) {
        this.id = id;
    }

    public TblModuleManifest(Integer id, Date createdOn, Date updatedOn) {
        this.id = id;
        // @since 1.1 we are relying on the audit log for "created on", "created by", etc. type information
        /*
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
        */
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }



    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
    }
    */
    
    public TblMle getMleId() {
        return mleId;
    }

    public void setMleId(TblMle mleId) {
        this.mleId = mleId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof TblModuleManifest)) {
            return false;
        }
        TblModuleManifest other = (TblModuleManifest) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.intel.mountwilson.as.data.TblModuleManifest[ id=" + id + " ]";
    }

    public String getComponentName() {
        // fix for Bug #730 that affected postgres only because postgres does not automatically trim spaces on queries but mysql automatically trims
       
        if( this.componentName != null ) {
            //log.debug("trimming componentName");
            this.componentName = this.componentName.trim(); 
        }
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
        
        // fix for bug 2013-02-04 that affected postgres only because postgres does not automatically trim spaces on queries but mysql automatically trims
       
        if( this.componentName != null ) {
            //log.debug("trimming componentName");
            this.componentName = this.componentName.trim(); 
        }
    }

    public String getDigestValue() {
        return digestValue;
    }

    public void setDigestValue(String digestValue) {
        this.digestValue = digestValue;
    }

    public String getExtendedToPCR() {
        return extendedToPCR;
    }

    public void setExtendedToPCR(String extendedToPCR) {
        this.extendedToPCR = extendedToPCR;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageVendor() {
        return packageVendor;
    }

    public void setPackageVendor(String packageVendor) {
        this.packageVendor = packageVendor;
    }

    public String getPackageVersion() {
        return packageVersion;
    }

    public void setPackageVersion(String packageVersion) {
        this.packageVersion = packageVersion;
    }

    public Boolean getUseHostSpecificDigestValue() {
        return useHostSpecificDigestValue;
    }

    public void setUseHostSpecificDigestValue(Boolean useHostSpecificDigestValue) {
        this.useHostSpecificDigestValue = useHostSpecificDigestValue;
    }


    @XmlTransient
    public Collection<TblHostSpecificManifest> getTblHostSpecificManifestCollection() {
        return tblHostSpecificManifestCollection;
    }

    public void setTblHostSpecificManifestCollection(Collection<TblHostSpecificManifest> tblHostSpecificManifestCollection) {
        this.tblHostSpecificManifestCollection = tblHostSpecificManifestCollection;
    }

    public TblEventType getEventID() {
        return eventID;
    }

    public void setEventID(TblEventType eventID) {
        this.eventID = eventID;
    }

    public TblPackageNamespace getNameSpaceID() {
        return nameSpaceID;
    }

    public void setNameSpaceID(TblPackageNamespace nameSpaceID) {
        this.nameSpaceID = nameSpaceID;
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
    
    
}
