/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.data;

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

/**
 *
 * @author dsmagadx
 */
@Entity
@Table(name = "tbl_request_queue")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "TblRequestQueue.findAll", query = "SELECT t FROM TblRequestQueue t"),
    @NamedQuery(name = "TblRequestQueue.findById", query = "SELECT t FROM TblRequestQueue t WHERE t.id = :id"),
    @NamedQuery(name = "TblRequestQueue.findByHostID", query = "SELECT t FROM TblRequestQueue t WHERE t.hostID = :hostID"),
    @NamedQuery(name = "TblRequestQueue.findByIsProcessed", query = "SELECT t FROM TblRequestQueue t WHERE t.isProcessed = :isProcessed"),
    @NamedQuery(name = "TblRequestQueue.findByTrustStatus", query = "SELECT t FROM TblRequestQueue t WHERE t.trustStatus = :trustStatus"),
    @NamedQuery(name = "TblRequestQueue.findByRQErrorCode", query = "SELECT t FROM TblRequestQueue t WHERE t.rQErrorCode = :rQErrorCode"),
    @NamedQuery(name = "TblRequestQueue.findByRQErrorDescription", query = "SELECT t FROM TblRequestQueue t WHERE t.rQErrorDescription = :rQErrorDescription")})
public class TblRequestQueue implements Serializable {
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
    @Column(name = "Is_Processed")
    private boolean isProcessed;
    @Column(name = "Trust_Status")
    private String trustStatus;
    @Column(name = "RQ_Error_Code")
    private Integer rQErrorCode;
    @Column(name = "RQ_Error_Description")
    private String rQErrorDescription;

    public TblRequestQueue() {
    }

    public TblRequestQueue(Integer id) {
        this.id = id;
    }

    public TblRequestQueue(Integer id, int hostID, boolean isProcessed) {
        this.id = id;
        this.hostID = hostID;
        this.isProcessed = isProcessed;
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

    public boolean getIsProcessed() {
        return isProcessed;
    }

    public void setIsProcessed(boolean isProcessed) {
        this.isProcessed = isProcessed;
    }

    public String getTrustStatus() {
        return trustStatus;
    }

    public void setTrustStatus(String trustStatus) {
        this.trustStatus = trustStatus;
    }

    public Integer getRQErrorCode() {
        return rQErrorCode;
    }

    public void setRQErrorCode(Integer rQErrorCode) {
        this.rQErrorCode = rQErrorCode;
    }

    public String getRQErrorDescription() {
        return rQErrorDescription;
    }

    public void setRQErrorDescription(String rQErrorDescription) {
        this.rQErrorDescription = rQErrorDescription;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof TblRequestQueue)) {
            return false;
        }
        TblRequestQueue other = (TblRequestQueue) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.intel.mountwilson.as.data.TblRequestQueue[ id=" + id + " ]";
    }
    
}
