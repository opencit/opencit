/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.data;

import com.intel.mtwilson.util.DataCipher;
import com.intel.mtwilson.audit.handler.AuditEventHandler;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.io.ByteArrayResource;
import com.intel.dcsg.cpg.io.Resource;
import com.intel.mtwilson.util.ASDataCipher;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import javax.crypto.BadPaddingException;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.eclipse.persistence.annotations.Customizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    @NamedQuery(name = "TblHosts.findByAikSha1", query = "SELECT t FROM TblHosts t WHERE t.aikSha1 = :aikSha1"), // XXX TODO NEED TO ADD A COLUMN TO DATABASE, AND POPULATE IT WITH SHA1(AIK) WHENEVER WE UPDATE THE AIK ITSELF
    @NamedQuery(name = "TblHosts.findByIPAddress", query = "SELECT t FROM TblHosts t WHERE t.iPAddress = :iPAddress"),
    @NamedQuery(name = "TblHosts.findByPort", query = "SELECT t FROM TblHosts t WHERE t.port = :port"),
    @NamedQuery(name = "TblHosts.findByDescription", query = "SELECT t FROM TblHosts t WHERE t.description = :description"),
    @NamedQuery(name = "TblHosts.findByAddOnConnectionInfo", query = "SELECT t FROM TblHosts t WHERE t.addOnConnectionInfo_cipherText = :addOnConnectionInfo"),
    @NamedQuery(name = "TblHosts.findByEmail", query = "SELECT t FROM TblHosts t WHERE t.email = :email"),
    // @since 1.1 we are relying on the audit log for "created on", "created by", etc. type information
//    @NamedQuery(name = "TblHosts.findByCreatedOn", query = "SELECT t FROM TblHosts t WHERE t.createdOn = :createdOn"),
//    @NamedQuery(name = "TblHosts.findByUpdatedOn", query = "SELECT t FROM TblHosts t WHERE t.updatedOn = :updatedOn"),
    @NamedQuery(name = "TblHosts.findByErrorCode", query = "SELECT t FROM TblHosts t WHERE t.errorCode = :errorCode"),
    @NamedQuery(name = "TblHosts.findByErrorDescription", query = "SELECT t FROM TblHosts t WHERE t.errorDescription = :errorDescription"),
    @NamedQuery(name = "TblHosts.findByNameSearchCriteria", query = "SELECT t FROM TblHosts t WHERE t.name like :search")})
public class TblHosts implements Serializable {
    @Transient
    private transient Logger log = LoggerFactory.getLogger(getClass());
    
    // @since 1.1 we are relying on the audit log for "created on", "created by", etc. type information
    /*
    @Basic(optional =     false)
    @Column(name = "Created_On")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn;
    @Basic(optional =     false)
    @Column(name = "Updated_On")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedOn;
    * 
    */
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
    private String addOnConnectionInfo_cipherText;   // must be encrypted 
    
    @Transient
    private transient String addOnConnectionInfo_plainText; // the decrypted version
    
    
    @Lob
    @Column(name = "AIK_Certificate")
    private String aikCertificate;

    @Lob
    @Column(name = "AIK_PublicKey")
    private String aikPublicKey;
    
    @Column(name = "AIK_SHA1")
    private String aikSha1;
    
    @Column(name = "TlsPolicy")
    private String tlsPolicyName;
    
    @Lob
    @Column(name = "TlsKeystore")
    private byte[] tlsKeystore;
    
    @Transient
    private ByteArrayResource tlsKeystoreResource;
    
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

    /*
    public TblHosts(Integer id, String name, String iPAddress, int port, Date createdOn, Date updatedOn) {
        this.id = id;
        this.name = name;
        this.iPAddress = iPAddress;
        this.port = port;
    }
    */
        // @since 1.1 we are relying on the audit log for "created on", "created by", etc. type information
        /*
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
        * 
        */

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
     * with Trust Agent that format might be "intel:https://hostname:9999"
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
        if( addOnConnectionInfo_plainText == null && addOnConnectionInfo_cipherText != null ) {
            try {
                //log.info("XXX TblHosts ASDataCipher ref = {}", ASDataCipher.cipher.hashCode());
                addOnConnectionInfo_plainText = ASDataCipher.cipher.decryptString(addOnConnectionInfo_cipherText);
                //log.info("XXX TblHosts ASDataCipher plainText = {}", addOnConnectionInfo_plainText);
                //log.info("XXX TblHosts ASDataCipher cipherText = {}", addOnConnectionInfo_cipherText);
            }
            catch(Exception e) {
                log.error("Cannot decrypt host connection credentials", e);
                // this will happen if the data is being decrypted with the wrong key (which will happen if someone reinstalled mt wilson and kept the data but didn't save the data encryption key)
                // it may also happen if the data wasn't encrypted in the first place
                if( addOnConnectionInfo_cipherText.startsWith("http") ) {
                    return addOnConnectionInfo_plainText; // data was not encrypted
                }
                else {
                    throw new IllegalArgumentException("Cannot decrypt host connection credentials; check the key or delete and re-register the host");
                }
            }
        }
        return addOnConnectionInfo_plainText;
    }

    public void setAddOnConnectionInfo(String addOnConnectionInfo) {
        this.addOnConnectionInfo_plainText = addOnConnectionInfo;
        // TODO  encrypt it and set addOnConnectionInfo_cipherText
        if( addOnConnectionInfo == null ) { addOnConnectionInfo_cipherText = null; }
        else {
             addOnConnectionInfo_cipherText = ASDataCipher.cipher.encryptString(addOnConnectionInfo_plainText);
        }
    }

    public String getAIKCertificate() {
        return aikCertificate;
    }

    public void setAIKCertificate(String aikCertificate) {
        this.aikCertificate = aikCertificate;
    }

    public String getAikPublicKey() {
        return aikPublicKey;
    }

    /**
     * You should set this anytime you set the AIK Certificate
     * @param aikPublicKey 
     */
    public void setAikPublicKey(String aikPublicKey) {
        this.aikPublicKey = aikPublicKey;
    }
    
    /**
     * The AIK SHA1 hash is ALWAYS a hash of the Public Key, NOT the Certificate
     * @return 
     */
    public String getAikSha1() {
        return aikSha1;
    }
    
    /**
     * You should set this anytime you set the AIK Public Key.
     * The value should be hex-encoded sha1 of the DER-ENCODED (binary) AIK Public Key
     * Even if someone has signed the AIK and created an AIK CERTIFICATE, this value
     * should remain as the AIK PUBLIC KEY SHA1 so that it is unambiguous. 
     * It is trivial to extract the AIK PUBLIC KEY from the AIK CERTIFICATE in order
     * to compute the SHA1.
     * @param aikSha1 
     */
    public void setAikSha1(String aikSha1) {
        this.aikSha1 = aikSha1;
    }
    
    public String getTlsPolicyName() { return tlsPolicyName; }
    public void setTlsPolicyName(String sslPolicy) { 
        this.tlsPolicyName = sslPolicy; 
    }

    
    public byte[] getTlsKeystore() { 
        log.debug("getTlsKeystore called on TblHosts for hostname: {}", name);
        return tlsKeystore; 
    }
    public void setTlsKeystore(byte[] tlsKeystoreBytes) {        
        tlsKeystore = tlsKeystoreBytes;
        tlsKeystoreResource = null;
    }

    public Resource getTlsKeystoreResource() { 
        if( tlsKeystoreResource == null ) {
            tlsKeystoreResource = new ByteArrayResource(tlsKeystore) {
                @Override
                protected void onClose() {
                    tlsKeystore = array; // array is a protected member of ByteArrayResource
                }
            };
        }
        return tlsKeystoreResource; 
    }
    
    
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // @since 1.1 we are relying on the audit log for "created on", "created by", etc. type information
    /*
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
    * 
    */

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
