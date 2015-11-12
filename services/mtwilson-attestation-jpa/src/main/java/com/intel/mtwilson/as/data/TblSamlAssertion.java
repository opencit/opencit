/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.data;

import com.intel.mtwilson.audit.handler.AuditEventHandler;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import org.eclipse.persistence.annotations.Customizer;

/**
 *
 * @author dsmagadX
 */
@Entity
@Customizer(AuditEventHandler.class) 
@Table(name = "mw_saml_assertion")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "TblSamlAssertion.findAll", query = "SELECT t FROM TblSamlAssertion t"),
    @NamedQuery(name = "TblSamlAssertion.findById", query = "SELECT t FROM TblSamlAssertion t WHERE t.id = :id"),
    @NamedQuery(name = "TblSamlAssertion.findByExpiryTs", query = "SELECT t FROM TblSamlAssertion t WHERE t.expiryTs = :expiryTs"),
    @NamedQuery(name = "TblSamlAssertion.findByBiosTrust", query = "SELECT t FROM TblSamlAssertion t WHERE t.biosTrust = :biosTrust"),
    @NamedQuery(name = "TblSamlAssertion.findByVmmTrust", query = "SELECT t FROM TblSamlAssertion t WHERE t.vmmTrust = :vmmTrust"),
    @NamedQuery(name = "TblSamlAssertion.findByErrorCode", query = "SELECT t FROM TblSamlAssertion t WHERE t.errorCode = :errorCode"),
    @NamedQuery(name = "TblSamlAssertion.findByErrorMessage", query = "SELECT t FROM TblSamlAssertion t WHERE t.errorMessage = :errorMessage"),
    @NamedQuery(name = "TblSamlAssertion.findByCreatedTs", query = "SELECT t FROM TblSamlAssertion t WHERE t.createdTs = :createdTs"),
    @NamedQuery(name = "TblSamlAssertion.findByRangeOfCreatedTs", query = "SELECT t FROM TblSamlAssertion t WHERE t.hostId.name = :hostName and t.createdTs >= :fromCreatedTs and t.createdTs < :toCreatedTs ORDER BY t.createdTs ASC"),
    @NamedQuery(name = "TblSamlAssertion.findByHostAndExpiry", query = "SELECT t FROM TblSamlAssertion t WHERE t.expiryTs > :now and t.hostId.name = :hostName ORDER BY t.expiryTs DESC"),
    @NamedQuery(name = "TblSamlAssertion.findByHostID", query = "SELECT t FROM TblSamlAssertion t WHERE t.hostId = :hostId"),
    @NamedQuery(name = "TblSamlAssertion.findByAssertionUuid", query = "SELECT t FROM TblSamlAssertion t WHERE t.assertionUuid = :assertionUuid ORDER BY t.expiryTs DESC")})
public class TblSamlAssertion implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "uuid_hex")
    private String assertionUuid;
    @Lob
    @Column(name = "saml")
    private String saml;
    @Basic(optional = false)
    @Column(name = "trust_report")
    private String trustReport;
    @Basic(optional = false)
    @Column(name = "expiry_ts")
    @Temporal(TemporalType.TIMESTAMP)
    private Date expiryTs;
    @Basic(optional = false)
    @Column(name = "bios_trust")
    private boolean biosTrust;
    @Basic(optional = false)
    @Column(name = "vmm_trust")
    private boolean vmmTrust;
    @Column(name = "error_code")
    private String errorCode;
    @Column(name = "error_message")
    private String errorMessage;
    @Column(name = "created_ts")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdTs;
    @JoinColumn(name = "host_id", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private TblHosts hostId;

    public TblSamlAssertion() {
    }

    public TblSamlAssertion(Integer id) {
        this.id = id;
    }

    public TblSamlAssertion(Integer id, Date expiryTs, boolean biosTrust, boolean vmmTrust) {
        this.id = id;
        this.expiryTs = expiryTs;
        this.biosTrust = biosTrust;
        this.vmmTrust = vmmTrust;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAssertionUuid() {
        return assertionUuid;
    }

    public void setAssertionUuid(String assertionUuid) {
        this.assertionUuid = assertionUuid;
    }
    
    public String getSaml() {
        return saml;
    }

    public void setSaml(String saml) {
        this.saml = saml;
    }
    
    public String getTrustReport() {
        return trustReport;
    }
    
    public void setTrustReport(String trustReport) {
        this.trustReport = trustReport;
    }

    public Date getExpiryTs() {
        return expiryTs;
    }

    public void setExpiryTs(Date expiryTs) {
        this.expiryTs = expiryTs;
    }

    public boolean getBiosTrust() {
        return biosTrust;
    }

    public void setBiosTrust(boolean biosTrust) {
        this.biosTrust = biosTrust;
    }

    public boolean getVmmTrust() {
        return vmmTrust;
    }

    public void setVmmTrust(boolean vmmTrust) {
        this.vmmTrust = vmmTrust;
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
        if (!(object instanceof TblSamlAssertion)) {
            return false;
        }
        TblSamlAssertion other = (TblSamlAssertion) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.intel.mountwilson.as.data.TblSamlAssertion[ id=" + id + " ]";
    }
    
}
