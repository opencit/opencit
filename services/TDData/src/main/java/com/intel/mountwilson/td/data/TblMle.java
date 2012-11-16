/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mountwilson.td.data;

import java.io.Serializable;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author ssbangal
 */
@Entity
@Table(name = "tbl_mle")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "TblMle.findAll", query = "SELECT t FROM TblMle t"),
    @NamedQuery(name = "TblMle.findById", query = "SELECT t FROM TblMle t WHERE t.id = :id"),
    @NamedQuery(name = "TblMle.findByName", query = "SELECT t FROM TblMle t WHERE t.name = :name"),
    @NamedQuery(name = "TblMle.findByVersion", query = "SELECT t FROM TblMle t WHERE t.version = :version"),
    @NamedQuery(name = "TblMle.findByAttestationType", query = "SELECT t FROM TblMle t WHERE t.attestationType = :attestationType"),
    @NamedQuery(name = "TblMle.findByMLEType", query = "SELECT t FROM TblMle t WHERE t.mLEType = :mLEType"),
    @NamedQuery(name = "TblMle.findByRequiredManifestList", query = "SELECT t FROM TblMle t WHERE t.requiredManifestList = :requiredManifestList"),
    @NamedQuery(name = "TblMle.findByDescription", query = "SELECT t FROM TblMle t WHERE t.description = :description"),
    @NamedQuery(name = "TblMle.findByOsName", query = "SELECT t FROM TblMle t WHERE t.osName = :osName"),
    @NamedQuery(name = "TblMle.findByOsVersion", query = "SELECT t FROM TblMle t WHERE t.osVersion = :osVersion"),
    @NamedQuery(name = "TblMle.findByOemName", query = "SELECT t FROM TblMle t WHERE t.oemName = :oemName")})
public class TblMle implements Serializable {
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
    @Column(name = "Version")
    private String version;
    @Basic(optional = false)
    @Column(name = "Attestation_Type")
    private String attestationType;
    @Basic(optional = false)
    @Column(name = "MLE_Type")
    private String mLEType;
    @Column(name = "Required_Manifest_List")
    private String requiredManifestList;
    @Column(name = "Description")
    private String description;
    @Column(name = "OsName")
    private String osName;
    @Column(name = "OsVersion")
    private String osVersion;
    @Column(name = "OemName")
    private String oemName;

    public TblMle() {
    }

    public TblMle(Integer id) {
        this.id = id;
    }

    public TblMle(Integer id, String name, String version, String attestationType, String mLEType) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.attestationType = attestationType;
        this.mLEType = mLEType;
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

    public String getAttestationType() {
        return attestationType;
    }

    public void setAttestationType(String attestationType) {
        this.attestationType = attestationType;
    }

    public String getMLEType() {
        return mLEType;
    }

    public void setMLEType(String mLEType) {
        this.mLEType = mLEType;
    }

    public String getRequiredManifestList() {
        return requiredManifestList;
    }

    public void setRequiredManifestList(String requiredManifestList) {
        this.requiredManifestList = requiredManifestList;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getOemName() {
        return oemName;
    }

    public void setOemName(String oemName) {
        this.oemName = oemName;
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
        if (!(object instanceof TblMle)) {
            return false;
        }
        TblMle other = (TblMle) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.intel.mountwilson.td.data.TblMle[ id=" + id + " ]";
    }
    
}
