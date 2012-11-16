/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mountwilson.td.data;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author ssbangal
 */
@Entity
@Table(name = "tbl_hostvmmapping")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "TblHostvmmapping.findAll", query = "SELECT t FROM TblHostvmmapping t"),
    @NamedQuery(name = "TblHostvmmapping.findById", query = "SELECT t FROM TblHostvmmapping t WHERE t.id = :id"),
    @NamedQuery(name = "TblHostvmmapping.findByHostId", query = "SELECT t FROM TblHostvmmapping t WHERE t.hostId = :hostId"),
    @NamedQuery(name = "TblHostvmmapping.findByVMName", query = "SELECT t FROM TblHostvmmapping t WHERE t.vMName = :vMName"),
    @NamedQuery(name = "TblHostvmmapping.findByVMStatus", query = "SELECT t FROM TblHostvmmapping t WHERE t.vMStatus = :vMStatus"),
    @NamedQuery(name = "TblHostvmmapping.findByTrustedHostPolicy", query = "SELECT t FROM TblHostvmmapping t WHERE t.trustedHostPolicy = :trustedHostPolicy"),
    @NamedQuery(name = "TblHostvmmapping.findByLocationPolicy", query = "SELECT t FROM TblHostvmmapping t WHERE t.locationPolicy = :locationPolicy"),
    @NamedQuery(name = "TblHostvmmapping.findByCreatedBy", query = "SELECT t FROM TblHostvmmapping t WHERE t.createdBy = :createdBy"),
    @NamedQuery(name = "TblHostvmmapping.findByCreatedDate", query = "SELECT t FROM TblHostvmmapping t WHERE t.createdDate = :createdDate"),
    @NamedQuery(name = "TblHostvmmapping.findByModifiedBy", query = "SELECT t FROM TblHostvmmapping t WHERE t.modifiedBy = :modifiedBy"),
    @NamedQuery(name = "TblHostvmmapping.findByModifiedDate", query = "SELECT t FROM TblHostvmmapping t WHERE t.modifiedDate = :modifiedDate")})
public class TblHostvmmapping implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID")
    private Integer id;
    @Column(name = "Host_Id")
    private Integer hostId;
    @Column(name = "VM_Name")
    private String vMName;
    @Basic(optional = false)
    @Column(name = "VM_Status")
    private short vMStatus;
    @Basic(optional = false)
    @Column(name = "Trusted_Host_Policy")
    private short trustedHostPolicy;
    @Basic(optional = false)
    @Column(name = "Location_Policy")
    private short locationPolicy;
    @Column(name = "Created_By")
    private String createdBy;
    @Column(name = "Created_Date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;
    @Column(name = "Modified_By")
    private String modifiedBy;
    @Column(name = "Modified_Date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modifiedDate;

    public TblHostvmmapping() {
    }

    public TblHostvmmapping(Integer id) {
        this.id = id;
    }

    public TblHostvmmapping(Integer id, short vMStatus, short trustedHostPolicy, short locationPolicy) {
        this.id = id;
        this.vMStatus = vMStatus;
        this.trustedHostPolicy = trustedHostPolicy;
        this.locationPolicy = locationPolicy;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getHostId() {
        return hostId;
    }

    public void setHostId(Integer hostId) {
        this.hostId = hostId;
    }

    public String getVMName() {
        return vMName;
    }

    public void setVMName(String vMName) {
        this.vMName = vMName;
    }

    public short getVMStatus() {
        return vMStatus;
    }

    public void setVMStatus(short vMStatus) {
        this.vMStatus = vMStatus;
    }

    public short getTrustedHostPolicy() {
        return trustedHostPolicy;
    }

    public void setTrustedHostPolicy(short trustedHostPolicy) {
        this.trustedHostPolicy = trustedHostPolicy;
    }

    public short getLocationPolicy() {
        return locationPolicy;
    }

    public void setLocationPolicy(short locationPolicy) {
        this.locationPolicy = locationPolicy;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TblHostvmmapping)) {
            return false;
        }
        TblHostvmmapping other = (TblHostvmmapping) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.intel.mountwilson.td.data.TblHostvmmapping[ id=" + id + " ]";
    }
    
}
