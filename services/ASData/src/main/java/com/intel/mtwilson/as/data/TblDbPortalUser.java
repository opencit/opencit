/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.data;

import java.io.Serializable;
import java.util.Collection;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @since 0.5.4 redefined to store portal users with password-protected keystore; password field is deprecated.
 * @author dsmagadx
 */
@Entity
@Table(name = "mw_portal_user")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "TblDbPortalUser.findAll", query = "SELECT t FROM TblDbPortalUser t"),
    @NamedQuery(name = "TblDbPortalUser.findById", query = "SELECT t FROM TblDbPortalUser t WHERE t.id = :id"),
    @NamedQuery(name = "TblDbPortalUser.findByLogin", query = "SELECT t FROM TblDbPortalUser t WHERE t.login = :login"),
    @NamedQuery(name = "TblDbPortalUser.findByFirstName", query = "SELECT t FROM TblDbPortalUser t WHERE t.firstName = :firstName"),
    @NamedQuery(name = "TblDbPortalUser.findByLastName", query = "SELECT t FROM TblDbPortalUser t WHERE t.lastName = :lastName")})
public class TblDbPortalUser implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "Login")
    private String login;
    @Basic(optional = false)
    @Column(name = "Password")
    private String password;
    @Basic(optional = false)
    @Column(name = "First_Name")
    private String firstName;
    @Basic(optional = false)
    @Column(name = "Last_Name")
    private String lastName;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "updatedBy")
    private Collection<TblPcrManifest> tblPcrManifestCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "createdBy")
    private Collection<TblPcrManifest> tblPcrManifestCollection1;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "updatedBy")
    private Collection<TblModuleManifest> tblModuleManifestCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "createdBy")
    private Collection<TblModuleManifest> tblModuleManifestCollection1;

    @Basic(optional = false)
    @Lob
    @Column(name = "keystore")
    private byte[] keystore;
    @Basic(optional = false)
    @Column(name = "enabled")
    private boolean enabled;
    @Basic(optional = false)
    @Column(name = "status")
    private String status;
    @Column(name = "comment")
    private String comment;

    public TblDbPortalUser() {
    }

    public TblDbPortalUser(Integer id) {
        this.id = id;
    }

    public TblDbPortalUser(Integer id, String login, String password, String firstName, String lastName) {
        this.id = id;
        this.login = login;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @XmlTransient
    public Collection<TblPcrManifest> getTblPcrManifestCollection() {
        return tblPcrManifestCollection;
    }

    public void setTblPcrManifestCollection(Collection<TblPcrManifest> tblPcrManifestCollection) {
        this.tblPcrManifestCollection = tblPcrManifestCollection;
    }

    @XmlTransient
    public Collection<TblPcrManifest> getTblPcrManifestCollection1() {
        return tblPcrManifestCollection1;
    }

    public void setTblPcrManifestCollection1(Collection<TblPcrManifest> tblPcrManifestCollection1) {
        this.tblPcrManifestCollection1 = tblPcrManifestCollection1;
    }

    @XmlTransient
    public Collection<TblModuleManifest> getTblModuleManifestCollection() {
        return tblModuleManifestCollection;
    }

    public void setTblModuleManifestCollection(Collection<TblModuleManifest> tblModuleManifestCollection) {
        this.tblModuleManifestCollection = tblModuleManifestCollection;
    }

    @XmlTransient
    public Collection<TblModuleManifest> getTblModuleManifestCollection1() {
        return tblModuleManifestCollection1;
    }

    public void setTblModuleManifestCollection1(Collection<TblModuleManifest> tblModuleManifestCollection1) {
        this.tblModuleManifestCollection1 = tblModuleManifestCollection1;
    }

    public byte[] getKeystore() {
        return keystore;
    }

    public void setKeystore(byte[] keystore) {
        this.keystore = keystore;
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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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
        if (!(object instanceof TblDbPortalUser)) {
            return false;
        }
        TblDbPortalUser other = (TblDbPortalUser) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.intel.mountwilson.as.data.TblDbPortalUser[ id=" + id + " ]";
    }
    
}
