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
 * @author ssbangal
 */
@Entity
@Table(name = "mw_mle_source")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "MwMleSource.findAll", query = "SELECT t FROM MwMleSource t"),
    @NamedQuery(name = "MwMleSource.findById", query = "SELECT t FROM MwMleSource t WHERE t.id = :id"),
    @NamedQuery(name = "MwMleSource.findByHostName", query = "SELECT t FROM MwMleSource t WHERE t.hostName = :hostName"),
    @NamedQuery(name = "MwMleSource.findByUUID_Hex", query = "SELECT t FROM MwMleSource t WHERE t.uuid_hex = :uuid_hex"),
    @NamedQuery(name = "MwMleSource.findByMleUuidHex", query = "SELECT t FROM MwMleSource t WHERE t.mle_uuid_hex = :mle_uuid_hex"),
    @NamedQuery(name = "MwMleSource.findByHostNameLike", query = "SELECT t FROM MwMleSource t WHERE t.hostName LIKE :hostName"),    
    @NamedQuery(name = "MwMleSource.findByMleID", query = "SELECT t FROM MwMleSource t WHERE t.mleId.id =:mleId")})

public class MwMleSource implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID")
    private Integer id;
    @Column(name = "Host_Name")
    private String hostName;
    @JoinColumn(name = "MLE_ID", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private TblMle mleId;
    @Column(name = "uuid_hex")
    private String uuid_hex;
    @Column(name = "mle_uuid_hex")
    private String mle_uuid_hex;

    public MwMleSource() {
    }

    public MwMleSource(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public TblMle getMleId() {
        return mleId;
    }

    public void setMleId(TblMle mleId) {
        this.mleId = mleId;
    }

    public String getUuid_hex() {
        return uuid_hex;
    }

    public void setUuid_hex(String uuid_hex) {
        this.uuid_hex = uuid_hex;
    }

    public String getMle_uuid_hex() {
        return mle_uuid_hex;
    }

    public void setMle_uuid_hex(String mle_uuid_hex) {
        this.mle_uuid_hex = mle_uuid_hex;
    }

    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof MwMleSource)) {
            return false;
        }
        MwMleSource other = (MwMleSource) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.intel.mountwilson.as.data.TblMleSource[ id=" + id + " ]";
    }
    
}
