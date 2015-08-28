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
@Table(name = "mw_vm_saml_assertion")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "MwVmSamlAssertion.findAll", query = "SELECT m FROM MwVmSamlAssertion m"),
    @NamedQuery(name = "MwVmSamlAssertion.findById", query = "SELECT m FROM MwVmSamlAssertion m WHERE m.id = :id"),
    @NamedQuery(name = "MwVmSamlAssertion.findByVmInstanceId", query = "SELECT m FROM MwVmSamlAssertion m WHERE m.vmInstanceId = :vmInstanceId"),
    @NamedQuery(name = "MwVmSamlAssertion.findByVmSaml", query = "SELECT m FROM MwVmSamlAssertion m WHERE m.vmSaml = :vmSaml"),
    @NamedQuery(name = "MwVmSamlAssertion.findByVmTrustReport", query = "SELECT m FROM MwVmSamlAssertion m WHERE m.vmTrustReport = :vmTrustReport"),
    @NamedQuery(name = "MwVmSamlAssertion.findByCreatedTs", query = "SELECT m FROM MwVmSamlAssertion m WHERE m.createdTs = :createdTs"),
    @NamedQuery(name = "MwVmSamlAssertion.findByErrorCode", query = "SELECT m FROM MwVmSamlAssertion m WHERE m.errorCode = :errorCode"),
    @NamedQuery(name = "MwVmSamlAssertion.findByErrorMessage", query = "SELECT m FROM MwVmSamlAssertion m WHERE m.errorMessage = :errorMessage")})
public class MwVmSamlAssertion implements Serializable {
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
    @Column(name = "created_ts")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdTs;
    @Column(name = "error_code")
    private String errorCode;
    @Column(name = "error_message")
    private String errorMessage;
    @JoinColumn(name = "host_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private TblHosts hostId;

    public MwVmSamlAssertion() {
    }

    public MwVmSamlAssertion(String id) {
        this.id = id;
    }

    public MwVmSamlAssertion(String id, String vmInstanceId) {
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

    public Date getCreatedTs() {
        return createdTs;
    }

    public void setCreatedTs(Date createdTs) {
        this.createdTs = createdTs;
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
        if (!(object instanceof MwVmSamlAssertion)) {
            return false;
        }
        MwVmSamlAssertion other = (MwVmSamlAssertion) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.intel.mtwilson.as.data.MwVmSamlAssertion[ id=" + id + " ]";
    }
    
}
