/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.data;

import java.io.Serializable;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author dsmagadx
 */
@Entity
@Table(name = "mw_module_manifest_log")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "TblModuleManifestLog.findAll", query = "SELECT t FROM TblModuleManifestLog t"),
    @NamedQuery(name = "TblModuleManifestLog.findById", query = "SELECT t FROM TblModuleManifestLog t WHERE t.id = :id"),
    @NamedQuery(name = "TblModuleManifestLog.findByName", query = "SELECT t FROM TblModuleManifestLog t WHERE t.name = :name"),
    @NamedQuery(name = "TblModuleManifestLog.findByValue", query = "SELECT t FROM TblModuleManifestLog t WHERE t.value = :value"),
    @NamedQuery(name = "TblModuleManifestLog.findByWhitelistValue", query = "SELECT t FROM TblModuleManifestLog t WHERE t.whitelistValue = :whitelistValue"),
    @NamedQuery(name = "TblModuleManifestLog.findByTaLogIdAndName", query = "SELECT t FROM TblModuleManifestLog t WHERE t.taLogId=:taLogId AND t.name = :name"),})
public class TblModuleManifestLog implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID")
    private Integer id;
    @Column(name = "name")
    private String name;
    @Column(name = "value")
    private String value;
    @Column(name = "whitelist_value")
    private String whitelistValue;
    @JoinColumn(name = "ta_log_id", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private TblTaLog taLogId;

    public TblModuleManifestLog() {
    }

    public TblModuleManifestLog(Integer id) {
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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getWhitelistValue() {
        return whitelistValue;
    }

    public void setWhitelistValue(String whitelistValue) {
        this.whitelistValue = whitelistValue;
    }

    public TblTaLog getTaLogId() {
        return taLogId;
    }

    public void setTaLogId(TblTaLog taLogId) {
        this.taLogId = taLogId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof TblModuleManifestLog)) {
            return false;
        }
        TblModuleManifestLog other = (TblModuleManifestLog) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.intel.mountwilson.as.data.TblModuleManifestLog[ id=" + id + " ]";
    }
    
}
