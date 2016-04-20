/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.data;

import java.io.Serializable;
import java.util.Collection;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author dsmagadx
 */
@Entity
@Table(name = "mw_package_namespace")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "TblPackageNamespace.findAll", query = "SELECT t FROM TblPackageNamespace t"),
    @NamedQuery(name = "TblPackageNamespace.findById", query = "SELECT t FROM TblPackageNamespace t WHERE t.id = :id"),
    @NamedQuery(name = "TblPackageNamespace.findByName", query = "SELECT t FROM TblPackageNamespace t WHERE t.name = :name"),
    @NamedQuery(name = "TblPackageNamespace.findByVendorName", query = "SELECT t FROM TblPackageNamespace t WHERE t.vendorName = :vendorName")})
public class TblPackageNamespace implements Serializable {
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
    @Column(name = "VendorName")
    private String vendorName;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "nameSpaceID")
    private Collection<TblModuleManifest> tblModuleManifestCollection;

    public TblPackageNamespace() {
    }

    public TblPackageNamespace(Integer id) {
        this.id = id;
    }

    public TblPackageNamespace(Integer id, String name, String vendorName) {
        this.id = id;
        this.name = name;
        this.vendorName = vendorName;
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

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    @XmlTransient
    public Collection<TblModuleManifest> getTblModuleManifestCollection() {
        return tblModuleManifestCollection;
    }

    public void setTblModuleManifestCollection(Collection<TblModuleManifest> tblModuleManifestCollection) {
        this.tblModuleManifestCollection = tblModuleManifestCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof TblPackageNamespace)) {
            return false;
        }
        TblPackageNamespace other = (TblPackageNamespace) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.intel.mountwilson.as.data.TblPackageNamespace[ id=" + id + " ]";
    }
    
}
