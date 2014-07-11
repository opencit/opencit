/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.data;

import com.intel.mtwilson.audit.annotations.AuditIgnore;
import com.intel.mtwilson.audit.handler.AuditEventHandler;

import java.io.Serializable;
import java.util.Collection;
import javax.persistence.Basic;
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
import org.eclipse.persistence.annotations.Customizer;

/**
 *
 * @author dsmagadx
 */
@Entity
@Table(name = "mw_os")
@XmlRootElement
@Customizer(AuditEventHandler.class)
@NamedQueries({
    @NamedQuery(name = "TblOs.findAll", query = "SELECT t FROM TblOs t"),
    @NamedQuery(name = "TblOs.findById", query = "SELECT t FROM TblOs t WHERE t.id = :id"),
    @NamedQuery(name = "TblOs.findByName", query = "SELECT t FROM TblOs t WHERE t.name = :name"),
    @NamedQuery(name = "TblOs.findByVersion", query = "SELECT t FROM TblOs t WHERE t.version = :version"),
    @NamedQuery(name = "TblOs.findByDescription", query = "SELECT t FROM TblOs t WHERE t.description = :description"),
    @NamedQuery(name = "TblOs.findByNameLike", query = "SELECT t FROM TblOs t WHERE t.name LIKE :name"), // it's the caller's responsibility to add "%" before and/or after the name value    
    @NamedQuery(name = "TblOs.findByUUID_Hex", query = "SELECT t FROM TblOs t WHERE t.uuid_hex = :uuid_hex"),    
    @NamedQuery(name = "TblOs.findTblOsByNameVersion", query = "SELECT t FROM TblOs t WHERE t.name= :name and t.version = :version")})
//
public class TblOs implements Serializable {
    @OneToMany(mappedBy = "osId")
    private Collection<TblMle> tblMleCollection;

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "NAME")
    private String name;
    @Basic(optional = false)
    @Column(name = "VERSION")
    private String version;
    @Column(name = "DESCRIPTION")
    private String description;
    @Column(name = "uuid_hex")
    private String uuid_hex;

    public TblOs() {
    }

    public TblOs(Integer id) {
        this.id = id;
    }

    public TblOs(Integer id, String name, String version) {
        this.id = id;
        this.name = name;
        this.version = version;
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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUuid_hex() {
        return uuid_hex;
    }

    public void setUuid_hex(String uuid_hex) {
        this.uuid_hex = uuid_hex;
    }

    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof TblOs)) {
            return false;
        }
        TblOs other = (TblOs) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.intel.mountwilson.as.data.TblOs[ id=" + id + " ]";
    }

    @XmlTransient
    public Collection<TblMle> getTblMleCollection() {
        return tblMleCollection;
    }

    public void setTblMleCollection(Collection<TblMle> tblMleCollection) {
        this.tblMleCollection = tblMleCollection;
    }
}
