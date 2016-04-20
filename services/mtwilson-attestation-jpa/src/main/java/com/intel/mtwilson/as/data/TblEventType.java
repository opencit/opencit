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
@Table(name = "mw_event_type")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "TblEventType.findAll", query = "SELECT t FROM TblEventType t"),
    @NamedQuery(name = "TblEventType.findById", query = "SELECT t FROM TblEventType t WHERE t.id = :id"),
    @NamedQuery(name = "TblEventType.findByName", query = "SELECT t FROM TblEventType t WHERE t.name = :name"),
    @NamedQuery(name = "TblEventType.findByFieldName", query = "SELECT t FROM TblEventType t WHERE t.fieldName = :fieldName")})
public class TblEventType implements Serializable {
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
    @Column(name = "FieldName")
    private String fieldName;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "eventID")
    private Collection<TblModuleManifest> tblModuleManifestCollection;

    public TblEventType() {
    }

    public TblEventType(Integer id) {
        this.id = id;
    }

    public TblEventType(Integer id, String name, String fieldName) {
        this.id = id;
        this.name = name;
        this.fieldName = fieldName;
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

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
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
        if (!(object instanceof TblEventType)) {
            return false;
        }
        TblEventType other = (TblEventType) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.intel.mountwilson.as.data.TblEventType[ id=" + id + " ]";
    }
    
}
