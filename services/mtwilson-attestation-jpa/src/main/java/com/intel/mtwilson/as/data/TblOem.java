/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.data;

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
@Customizer(AuditEventHandler.class)
@Table(name = "mw_oem")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "TblOem.findAll", query = "SELECT t FROM TblOem t"),
    @NamedQuery(name = "TblOem.findById", query = "SELECT t FROM TblOem t WHERE t.id = :id"),
    @NamedQuery(name = "TblOem.findByName", query = "SELECT t FROM TblOem t WHERE t.name = :name"),
    @NamedQuery(name = "TblOem.findByNameLike", query = "SELECT t FROM TblOem t WHERE t.name LIKE :name"), // it's the caller's responsibility to add "%" before and/or after the name value    
    @NamedQuery(name = "TblOem.findByUUID_Hex", query = "SELECT t FROM TblOem t WHERE t.uuid_hex = :uuid_hex"),
    @NamedQuery(name = "TblOem.findByDescription", query = "SELECT t FROM TblOem t WHERE t.description = :description")})
public class TblOem implements Serializable {
    @OneToMany(mappedBy = "oemId")
    private Collection<TblMle> tblMleCollection;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID")
    private Integer id;
    @Column(name = "NAME")
    private String name;
    @Column(name = "DESCRIPTION")
    private String description;
    @Column(name = "uuid_hex")
    private String uuid_hex;

    public TblOem() {
    }

    public TblOem(Integer id) {
        this.id = id;
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
        if (!(object instanceof TblOem)) {
            return false;
        }
        TblOem other = (TblOem) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.intel.mountwilson.as.data.TblOem[ id=" + id + " ]";
    }

    @XmlTransient
    public Collection<TblMle> getTblMleCollection() {
        return tblMleCollection;
    }

    public void setTblMleCollection(Collection<TblMle> tblMleCollection) {
        this.tblMleCollection = tblMleCollection;
    }
    
}
