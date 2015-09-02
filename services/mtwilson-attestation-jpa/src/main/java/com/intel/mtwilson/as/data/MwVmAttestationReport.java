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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
    @NamedQuery(name = "MwVmAttestationReport.findByVmSaml", query = "SELECT m FROM MwVmAttestationReport m WHERE m.vmSaml = :vmSaml"),
    @NamedQuery(name = "MwVmAttestationReport.findByVmTrustReport", query = "SELECT m FROM MwVmAttestationReport m WHERE m.vmTrustReport = :vmTrustReport"),
    @NamedQuery(name = "MwVmAttestationReport.findByErrorCode", query = "SELECT m FROM MwVmAttestationReport m WHERE m.errorCode = :errorCode"),
    @NamedQuery(name = "MwVmAttestationReport.findByErrorMessage", query = "SELECT m FROM MwVmAttestationReport m WHERE m.errorMessage = :errorMessage"),
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
    @Column(name = "vm_saml")
    private String vmSaml;
    @Column(name = "vm_trust_report")
    private String vmTrustReport;
    @Column(name = "error_code")
    private String errorCode;
    @Column(name = "error_message")
    private String errorMessage;
    @Column(name = "created_ts")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdTs;
    @JoinColumn(name = "host_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private TblHosts hostId;

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

    public TblHosts getHostId() {
        return hostId;
    }

    public void setHostId(TblHosts hostId) {
        this.hostId = hostId;
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
