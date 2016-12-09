/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.data;

import com.intel.mtwilson.audit.handler.AuditEventHandler;
import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import org.eclipse.persistence.annotations.Customizer;

/**
 *
 * @author dsmagadx
 */
@Entity
@Customizer(AuditEventHandler.class)
@Table(name = "mw_host_specific_manifest")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "TblHostSpecificManifest.findAll", query = "SELECT t FROM TblHostSpecificManifest t"),
    @NamedQuery(name = "TblHostSpecificManifest.findById", query = "SELECT t FROM TblHostSpecificManifest t WHERE t.id = :id"),
    @NamedQuery(name = "TblHostSpecificManifest.findByHostID", query = "SELECT t FROM TblHostSpecificManifest t WHERE t.hostID = :hostID"),
    @NamedQuery(name = "TblHostSpecificManifest.findByModuleAndHostID", query = "SELECT t FROM TblHostSpecificManifest t WHERE t.hostID = :hostID AND t.moduleManifestID.id = :Module_Manifest_ID"),
    @NamedQuery(name = "TblHostSpecificManifest.findByDigestValue", query = "SELECT t FROM TblHostSpecificManifest t WHERE t.digestValue = :digestValue")})
    @NamedQuery(name = "TblHostSpecificManifest.findByModuleIdHostIdPcrBank", query = "SELECT t FROM TblHostSpecificManifest t where t.hostID = :hostID AND t.moduleManifestID.id = :Module_Manifest_ID AND t.pcrBank = :pcrBank")
public class TblHostSpecificManifest implements Serializable {
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
    @Column(name = "DigestValue")
    private String digestValue;
    @Column(name = "pcr_bank")
    private String pcrBank = "SHA1";

    public String getPcrBank() {
        return pcrBank;
    }

    public void setPcrBank(String pcrBank) {
        this.pcrBank = pcrBank;
    }
    @JoinColumn(name = "Module_Manifest_ID", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private TblModuleManifest moduleManifestID;

    public TblHostSpecificManifest() {
    }

    public TblHostSpecificManifest(Integer id) {
        this.id = id;
    }

    public TblHostSpecificManifest(Integer id, int hostID, String digestValue) {
        this.id = id;
        this.hostID = hostID;
        this.digestValue = digestValue;
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

    public String getDigestValue() {
        return digestValue;
    }

    public void setDigestValue(String digestValue) {
        this.digestValue = digestValue;
    }

    public TblModuleManifest getModuleManifestID() {
        return moduleManifestID;
    }

    public void setModuleManifestID(TblModuleManifest moduleManifestID) {
        this.moduleManifestID = moduleManifestID;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof TblHostSpecificManifest)) {
            return false;
        }
        TblHostSpecificManifest other = (TblHostSpecificManifest) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.intel.mountwilson.as.data.TblHostSpecificManifest[ id=" + id + " ]";
    }
    
}
