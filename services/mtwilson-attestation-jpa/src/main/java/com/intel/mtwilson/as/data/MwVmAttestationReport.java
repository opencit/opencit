/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.as.data;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author ssbangal
 */
@Entity
@Table(name = "mw_vm_attestation_report")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "MwVmAttestationReport.findAll", query = "SELECT m FROM MwVmAttestationReport m"),
    @NamedQuery(name = "MwVmAttestationReport.findById", query = "SELECT m FROM MwVmAttestationReport m WHERE m.id = :id"),
    @NamedQuery(name = "MwVmAttestationReport.findByVmInstanceId", query = "SELECT m FROM MwVmAttestationReport m WHERE m.vmInstanceId = :vmInstanceId"),
    @NamedQuery(name = "MwVmAttestationReport.findByHostName", query = "SELECT m FROM MwVmAttestationReport m WHERE m.hostName = :hostName"),
    @NamedQuery(name = "MwVmAttestationReport.findByVmSaml", query = "SELECT m FROM MwVmAttestationReport m WHERE m.vmSaml = :vmSaml"),
    @NamedQuery(name = "MwVmAttestationReport.findByVmTrustReport", query = "SELECT m FROM MwVmAttestationReport m WHERE m.vmTrustReport = :vmTrustReport"),
    @NamedQuery(name = "MwVmAttestationReport.findByErrorCode", query = "SELECT m FROM MwVmAttestationReport m WHERE m.errorCode = :errorCode"),
    @NamedQuery(name = "MwVmAttestationReport.findByErrorMessage", query = "SELECT m FROM MwVmAttestationReport m WHERE m.errorMessage = :errorMessage"),
    @NamedQuery(name = "MwVmAttestationReport.findByVMAndExpiry", query = "SELECT m FROM MwVmAttestationReport m WHERE m.expiryTs > :now and m.vmInstanceId = :vmInstanceId ORDER BY m.expiryTs DESC"),    
    @NamedQuery(name = "MwVmAttestationReport.findByHostAndExpiry", query = "SELECT m FROM MwVmAttestationReport m WHERE m.expiryTs > :now and m.hostName = :hostName ORDER BY m.expiryTs DESC"),    
    @NamedQuery(name = "MwVmAttestationReport.findByHostAndRangeOfCreatedTs", query = "SELECT m FROM MwVmAttestationReport m WHERE m.hostName = :hostName and m.createdTs >= :fromCreatedTs and m.createdTs < :toCreatedTs ORDER BY m.createdTs ASC"),
    @NamedQuery(name = "MwVmAttestationReport.findByVMAndRangeOfCreatedTs", query = "SELECT m FROM MwVmAttestationReport m WHERE m.vmInstanceId = :vmInstanceId and m.createdTs >= :fromCreatedTs and m.createdTs < :toCreatedTs ORDER BY m.createdTs ASC"),
    @NamedQuery(name = "MwVmAttestationReport.findByCreatedTs", query = "SELECT m FROM MwVmAttestationReport m WHERE m.createdTs = :createdTs")})
public class MwVmAttestationReport implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id")
    private String id;
    @Basic(optional = false)
    @Column(name = "vm_instance_id")
    private String vmInstanceId;
    @Basic(optional = false)
    @Column(name = "vm_trust_status")
    private boolean vmTrustStatus;    
    @Column(name = "vm_saml")
    private String vmSaml;
    @Column(name = "vm_trust_report")
    private String vmTrustReport;
    @Column(name = "host_attestation_report")
    private String hostAttestationReport;
    @Column(name = "error_code")
    private String errorCode;
    @Column(name = "error_message")
    private String errorMessage;
    @Column(name = "created_ts")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdTs;
    @Column(name = "expiry_ts")
    @Temporal(TemporalType.TIMESTAMP)
    private Date expiryTs;
    @Basic(optional = false)
    @Column(name = "host_name")
    private String hostName;

    public MwVmAttestationReport() {
    }

    public MwVmAttestationReport(String id) {
        this.id = id;
    }

    public MwVmAttestationReport(String id, String vmInstanceId) {
        this.id = id;
        this.vmInstanceId = vmInstanceId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVmInstanceId() {
        return vmInstanceId;
    }

    public void setVmInstanceId(String vmInstanceId) {
        this.vmInstanceId = vmInstanceId;
    }

    public boolean isVmTrustStatus() {
        return vmTrustStatus;
    }

    public void setVmTrustStatus(boolean vmTrustStatus) {
        this.vmTrustStatus = vmTrustStatus;
    }
    
    public String getVmSaml() {
        return vmSaml;
    }

    public void setVmSaml(String vmSaml) {
        this.vmSaml = vmSaml;
    }

    public String getVmTrustReport() {
        return vmTrustReport;
    }

    public void setVmTrustReport(String vmTrustReport) {
        this.vmTrustReport = vmTrustReport;
    }

    public String getHostAttestationReport() {
        return hostAttestationReport;
    }

    public void setHostAttestationReport(String hostAttestationReport) {
        this.hostAttestationReport = hostAttestationReport;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Date getCreatedTs() {
        return createdTs;
    }

    public void setCreatedTs(Date createdTs) {
        this.createdTs = createdTs;
    }

    public Date getExpiryTs() {
        return expiryTs;
    }

    public void setExpiryTs(Date expiryTs) {
        this.expiryTs = expiryTs;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
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
        if (!(object instanceof MwVmAttestationReport)) {
            return false;
        }
        MwVmAttestationReport other = (MwVmAttestationReport) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.intel.mtwilson.as.data.MwVmAttestationReport[ id=" + id + " ]";
    }
    
}
