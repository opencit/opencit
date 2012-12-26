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
@Table(name = "mw_hosts")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "TblHosts.findAll", query = "SELECT t FROM TblHosts t"),
    @NamedQuery(name = "TblHosts.findById", query = "SELECT t FROM TblHosts t WHERE t.id = :id"),
    @NamedQuery(name = "TblHosts.findByName", query = "SELECT t FROM TblHosts t WHERE t.name = :name"),
    @NamedQuery(name = "TblHosts.findByIPAddress", query = "SELECT t FROM TblHosts t WHERE t.iPAddress = :iPAddress"),
    @NamedQuery(name = "TblHosts.findByPort", query = "SELECT t FROM TblHosts t WHERE t.port = :port"),
    @NamedQuery(name = "TblHosts.findByDescription", query = "SELECT t FROM TblHosts t WHERE t.description = :description"),
    @NamedQuery(name = "TblHosts.findByAddOnConnectionInfo", query = "SELECT t FROM TblHosts t WHERE t.addOnConnectionInfo = :addOnConnectionInfo"),
    @NamedQuery(name = "TblHosts.findByEmail", query = "SELECT t FROM TblHosts t WHERE t.email = :email"),
    @NamedQuery(name = "TblHosts.findByCreatedOn", query = "SELECT t FROM TblHosts t WHERE t.createdOn = :createdOn"),
    @NamedQuery(name = "TblHosts.findByUpdatedOn", query = "SELECT t FROM TblHosts t WHERE t.updatedOn = :updatedOn"),
    @NamedQuery(name = "TblHosts.findByErrorCode", query = "SELECT t FROM TblHosts t WHERE t.errorCode = :errorCode"),
    @NamedQuery(name = "TblHosts.findByErrorDescription", query = "SELECT t FROM TblHosts t WHERE t.errorDescription = :errorDescription"),
    @NamedQuery(name = "TblHosts.findByNameSearchCriteria", query = "SELECT t FROM TblHosts t WHERE t.name like :search")})
public class TblHosts implements Serializable {
    @Basic(optional =     false)
    @Column(name = "Created_On")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn;
    @Basic(optional =     false)
    @Column(name = "Updated_On")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedOn;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "hostId")
    private Collection<TblSamlAssertion> tblSamlAssertionCollection;
    @Column(name = "Location")
    private String location;
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
    @Column(name = "IPAddress")
    private String iPAddress;
    @Basic(optional = false)
    @Column(name = "Port")
    private int port;
    @Column(name = "Description")
    private String description;
    @Column(name = "AddOn_Connection_Info")
    private String addOnConnectionInfo;
    @Lob
    @Column(name = "AIK_Certificate")
    private String aIKCertificate;
    
    @Lob
    @Column(name = "SSL_Certificate")
    private byte[] sslCertificate;
    
    @Column(name = "SSL_Policy")
    private String sslPolicy;
    
    
    @Column(name = "Email")
    private String email;
    @Column(name = "Error_Code")
    private Integer errorCode;
    @Column(name = "Error_Description")
    private String errorDescription;
    @JoinColumn(name = "VMM_MLE_ID", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private TblMle vmmMleId;
    @JoinColumn(name = "BIOS_MLE_ID", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private TblMle biosMleId;

    public TblHosts() {
    }

    public TblHosts(Integer id) {
        this.id = id;
    }

    public TblHosts(Integer id, String name, String iPAddress, int port, Date createdOn, Date updatedOn) {
        this.id = id;
        this.name = name;
        this.iPAddress = iPAddress;
        this.port = port;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
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

    public String getIPAddress() {
        return iPAddress;
    }

    public void setIPAddress(String iPAddress) {
        this.iPAddress = iPAddress;
    }

    /**
     * XXX TODO the port field is only used for Linux hosts running Trust Agent
     * and needs to be removed;  all agent connection information should be
     * stored in the "AddOn_Connection_String" in a URI format. For Linux hosts
     * with Trust Agent that format might be "linux:https://hostname:9999"
     * @return 
     */
    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddOnConnectionInfo() {
        return addOnConnectionInfo;
    }

    public void setAddOnConnectionInfo(String addOnConnectionInfo) {
        this.addOnConnectionInfo = addOnConnectionInfo;
    }

    public String getAIKCertificate() {
        return aIKCertificate;
    }

    public void setAIKCertificate(String aIKCertificate) {
        this.aIKCertificate = aIKCertificate;
    }
    
    public byte[] getSSLCertificate() { return sslCertificate; }
    public void setSSLCertificate(byte[] encodedSslCertificate) {
        sslCertificate = encodedSslCertificate;
    }
    
    public String getSSLPolicy() { return sslPolicy; }
    public void setSSLPolicy(String sslPolicy) { 
        this.sslPolicy = sslPolicy; 
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public Date getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(Date updatedOn) {
        this.updatedOn = updatedOn;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    public TblMle getVmmMleId() {
        return vmmMleId;
    }

    public void setVmmMleId(TblMle vmmMleId) {
        this.vmmMleId = vmmMleId;
    }

    public TblMle getBiosMleId() {
        return biosMleId;
    }

    public void setBiosMleId(TblMle biosMleId) {
        this.biosMleId = biosMleId;
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
        if (!(object instanceof TblHosts)) {
            return false;
        }
        TblHosts other = (TblHosts) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.intel.mountwilson.as.data.TblHosts[ id=" + id + " ]";
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @XmlTransient
    public Collection<TblSamlAssertion> getTblSamlAssertionCollection() {
        return tblSamlAssertionCollection;
    }

    public void setTblSamlAssertionCollection(Collection<TblSamlAssertion> tblSamlAssertionCollection) {
        this.tblSamlAssertionCollection = tblSamlAssertionCollection;
    }

 
    
}
