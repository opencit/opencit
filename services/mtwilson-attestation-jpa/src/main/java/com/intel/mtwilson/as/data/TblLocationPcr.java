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
//@Customizer(AuditEventHandler.class)
@Table(name = "mw_location_pcr")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "TblLocationPcr.findAll", query = "SELECT t FROM TblLocationPcr t"),
    @NamedQuery(name = "TblLocationPcr.findById", query = "SELECT t FROM TblLocationPcr t WHERE t.id = :id"),
    @NamedQuery(name = "TblLocationPcr.findByLocation", query = "SELECT t FROM TblLocationPcr t WHERE t.location = :location"),
    @NamedQuery(name = "TblLocationPcr.findByPcrValue", query = "SELECT t FROM TblLocationPcr t WHERE t.pcrValue = :pcrValue")})
public class TblLocationPcr implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "location")
    private String location;
    @Basic(optional = false)
    @Column(name = "pcr_value")
    private String pcrValue;

    public TblLocationPcr() {
    }

    public TblLocationPcr(Integer id) {
        this.id = id;
    }

    public TblLocationPcr(Integer id, String location, String pcrValue) {
        this.id = id;
        this.location = location;
        this.pcrValue = pcrValue;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPcrValue() {
        return pcrValue;
    }

    public void setPcrValue(String pcrValue) {
        this.pcrValue = pcrValue;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof TblLocationPcr)) {
            return false;
        }
        TblLocationPcr other = (TblLocationPcr) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.intel.mountwilson.as.data.TblLocationPcr[ id=" + id + " ]";
    }
    
}
