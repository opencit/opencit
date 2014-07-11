/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.data;

import com.intel.mtwilson.audit.handler.AuditEventHandler;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.eclipse.persistence.annotations.Customizer;

/**
 *
 * @author dsmagadx
 */
@Entity
@Customizer(AuditEventHandler.class) 
@Table(name = "mw_ta_log")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "TblTaLog.findAll", query = "SELECT t FROM TblTaLog t"),
    @NamedQuery(name = "TblTaLog.findById", query = "SELECT t FROM TblTaLog t WHERE t.id = :id"),
    @NamedQuery(name = "TblTaLog.findTrustStatusByHostId", query = "SELECT t FROM TblTaLog t WHERE t.hostID = :hostID and t.mleId = 0 order by t.updatedOn desc"),
    @NamedQuery(name = "TblTaLog.findLogsByHostId", query = "SELECT t FROM TblTaLog t WHERE t.hostID = :hostID and t.mleId <> 0 and t.updatedOn = :updatedOn"),
    @NamedQuery(name = "TblTaLog.findLogsByHostId2", query = "SELECT t FROM TblTaLog t WHERE t.hostID = :hostID"),
    @NamedQuery(name = "TblTaLog.findLastStatusTs", query = "SELECT t FROM TblTaLog t WHERE t.hostID = :hostID order by t.updatedOn desc"),
    @NamedQuery(name = "TblTaLog.findByMleId", query = "SELECT t FROM TblTaLog t WHERE t.mleId = :mleId"),
    @NamedQuery(name = "TblTaLog.findByManifestName", query = "SELECT t FROM TblTaLog t WHERE t.manifestName = :manifestName"),
    @NamedQuery(name = "TblTaLog.findByManifestValue", query = "SELECT t FROM TblTaLog t WHERE t.manifestValue = :manifestValue"),
    @NamedQuery(name = "TblTaLog.findByTrustStatus", query = "SELECT t FROM TblTaLog t WHERE t.trustStatus = :trustStatus"),
    @NamedQuery(name = "TblTaLog.findByError", query = "SELECT t FROM TblTaLog t WHERE t.error = :error"),
    @NamedQuery(name = "TblTaLog.findByUpdatedOn", query = "SELECT t FROM TblTaLog t WHERE t.updatedOn = :updatedOn"),
    @NamedQuery(name = "TblTaLog.findByUuid", query = "SELECT t FROM TblTaLog t WHERE t.uuid_hex = :uuid_hex"),
    @NamedQuery(name = "TblTaLog.findLatestTrustStatusByHostUuid", query = "SELECT t FROM TblTaLog t WHERE t.host_uuid_hex = :host_uuid_hex and t.updatedOn > :expiryTs and t.mleId = 0 ORDER BY t.updatedOn DESC"),
    @NamedQuery(name = "TblTaLog.getHostTALogEntryBefore", query = "SELECT t FROM TblTaLog t WHERE t.hostID = :hostId and t.updatedOn > :expiryTs and t.mleId = 0 ORDER BY t.updatedOn DESC")
})

public class TblTaLog implements Serializable {
    @Basic(optional = false)
    @Column(name = "Updated_On")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedOn;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "taLogId")
    private Collection<TblModuleManifestLog> tblModuleManifestLogCollection;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "Host_ID")
    private int hostID;
    @Basic(optional = false)
    @Column(name = "MLE_ID")
    private int mleId;
    @Basic(optional = false)
    @Column(name = "Manifest_Name")
    private String manifestName;
    @Basic(optional = false)
    @Column(name = "Manifest_Value")
    private String manifestValue;
    @Basic(optional = false)
    @Column(name = "Trust_Status")
    private boolean trustStatus;
    @Column(name = "Error")
    private String error;
    @Column(name = "uuid_hex")
    private String uuid_hex;
    @Column(name = "host_uuid_hex")
    private String host_uuid_hex;

    public TblTaLog() {
    }

    public TblTaLog(Integer id) {
        this.id = id;
    }

    public TblTaLog(Integer id, int hostID, int mleId, String manifestName, String manifestValue, boolean trustStatus, Date updatedOn) {
        this.id = id;
        this.hostID = hostID;
        this.mleId = mleId;
        this.manifestName = manifestName;
        this.manifestValue = manifestValue;
        this.trustStatus = trustStatus;
        this.updatedOn = updatedOn;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getHostID() {
        return hostID;
    }

    public void setHostID(int hostID) {
        this.hostID = hostID;
    }

    public int getMleId() {
        return mleId;
    }

    public void setMleId(int mleId) {
        this.mleId = mleId;
    }

    public String getManifestName() {
        return manifestName;
    }

    public void setManifestName(String manifestName) {
        this.manifestName = manifestName;
    }

    public String getManifestValue() {
        return manifestValue;
    }

    public void setManifestValue(String manifestValue) {
        this.manifestValue = manifestValue;
    }

    public boolean getTrustStatus() {
        return trustStatus;
    }

    public void setTrustStatus(boolean trustStatus) {
        this.trustStatus = trustStatus;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Date getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(Date updatedOn) {
        this.updatedOn = updatedOn;
    }

    public String getUuid_hex() {
        return uuid_hex;
    }

    public void setUuid_hex(String uuid_hex) {
        this.uuid_hex = uuid_hex;
    }

    public String getHost_uuid_hex() {
        return host_uuid_hex;
    }

    public void setHost_uuid_hex(String host_uuid_hex) {
        this.host_uuid_hex = host_uuid_hex;
    }

    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof TblTaLog)) {
            return false;
        }
        TblTaLog other = (TblTaLog) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.intel.mountwilson.as.data.TblTaLog[ id=" + id + " ]";
    }


    @XmlTransient
    public Collection<TblModuleManifestLog> getTblModuleManifestLogCollection() {
        return tblModuleManifestLogCollection;
    }

    public void setTblModuleManifestLogCollection(Collection<TblModuleManifestLog> tblModuleManifestLogCollection) {
        this.tblModuleManifestLogCollection = tblModuleManifestLogCollection;
    }
    
}
