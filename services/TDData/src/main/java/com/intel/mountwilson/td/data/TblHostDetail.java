/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mountwilson.td.data;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author ssbangal
 */
@Entity
@Table(name = "host_detail")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "TblHostDetail.findAll", query = "SELECT t FROM TblHostDetail t"),
    @NamedQuery(name = "TblHostDetail.findByHostDetailID", query = "SELECT t FROM TblHostDetail t WHERE t.hostDetailID = :hostDetailID"),
    @NamedQuery(name = "TblHostDetail.findByHostName", query = "SELECT t FROM TblHostDetail t WHERE t.hostName = :hostName"),
    @NamedQuery(name = "TblHostDetail.findByHostIPAddress", query = "SELECT t FROM TblHostDetail t WHERE t.hostIPAddress = :hostIPAddress"),
    @NamedQuery(name = "TblHostDetail.findByHostPort", query = "SELECT t FROM TblHostDetail t WHERE t.hostPort = :hostPort"),
    @NamedQuery(name = "TblHostDetail.findByHostDescription", query = "SELECT t FROM TblHostDetail t WHERE t.hostDescription = :hostDescription"),
    @NamedQuery(name = "TblHostDetail.findByBIOSName", query = "SELECT t FROM TblHostDetail t WHERE t.bIOSName = :bIOSName"),
    @NamedQuery(name = "TblHostDetail.findByBIOSBuildNo", query = "SELECT t FROM TblHostDetail t WHERE t.bIOSBuildNo = :bIOSBuildNo"),
    @NamedQuery(name = "TblHostDetail.findByVMMName", query = "SELECT t FROM TblHostDetail t WHERE t.vMMName = :vMMName"),
    @NamedQuery(name = "TblHostDetail.findByVMMBuildNo", query = "SELECT t FROM TblHostDetail t WHERE t.vMMBuildNo = :vMMBuildNo"),
    @NamedQuery(name = "TblHostDetail.findByCreatedOn", query = "SELECT t FROM TblHostDetail t WHERE t.createdOn = :createdOn"),
    @NamedQuery(name = "TblHostDetail.findByCreatedBy", query = "SELECT t FROM TblHostDetail t WHERE t.createdBy = :createdBy"),
    @NamedQuery(name = "TblHostDetail.findByUpdatedOn", query = "SELECT t FROM TblHostDetail t WHERE t.updatedOn = :updatedOn"),
    @NamedQuery(name = "TblHostDetail.findByUpdatedBy", query = "SELECT t FROM TblHostDetail t WHERE t.updatedBy = :updatedBy"),
    @NamedQuery(name = "TblHostDetail.findByIsProcessed", query = "SELECT t FROM TblHostDetail t WHERE t.isProcessed = :isProcessed"),
    @NamedQuery(name = "TblHostDetail.findByEmailAddress", query = "SELECT t FROM TblHostDetail t WHERE t.emailAddress = :emailAddress"),
    @NamedQuery(name = "TblHostDetail.findByVCenterDetails", query = "SELECT t FROM TblHostDetail t WHERE t.vCenterDetails = :vCenterDetails"),
    @NamedQuery(name = "TblHostDetail.findByLocation", query = "SELECT t FROM TblHostDetail t WHERE t.location = :location"),
    @NamedQuery(name = "TblHostDetail.findByOem", query = "SELECT t FROM TblHostDetail t WHERE t.oem = :oem")})
public class TblHostDetail implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "Host_Detail_ID")
    private Integer hostDetailID;
    @Column(name = "Host_Name")
    private String hostName;
    @Column(name = "Host_IP_Address")
    private String hostIPAddress;
    @Column(name = "Host_Port")
    private String hostPort;
    @Column(name = "Host_Description")
    private String hostDescription;
    @Column(name = "BIOS_Name")
    private String bIOSName;
    @Column(name = "BIOS_Build_No")
    private String bIOSBuildNo;
    @Column(name = "VMM_Name")
    private String vMMName;
    @Column(name = "VMM_Build_No")
    private String vMMBuildNo;
    @Basic(optional = false)
    @Column(name = "Created_On")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn;
    @Column(name = "Created_By")
    private String createdBy;
    @Basic(optional = false)
    @Column(name = "Updated_On")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedOn;
    @Column(name = "Updated_By")
    private String updatedBy;
    @Column(name = "Is_Processed")
    private Character isProcessed;
    @Column(name = "Email_Address")
    private String emailAddress;
    @Column(name = "VCenter_Details")
    private String vCenterDetails;
    @Column(name = "Location")
    private String location;
    @Column(name = "OEM")
    private String oem;

    public TblHostDetail() {
    }

    public TblHostDetail(Integer hostDetailID) {
        this.hostDetailID = hostDetailID;
    }

    public TblHostDetail(Integer hostDetailID, Date createdOn, Date updatedOn) {
        this.hostDetailID = hostDetailID;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
    }

    public Integer getHostDetailID() {
        return hostDetailID;
    }

    public void setHostDetailID(Integer hostDetailID) {
        this.hostDetailID = hostDetailID;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getHostIPAddress() {
        return hostIPAddress;
    }

    public void setHostIPAddress(String hostIPAddress) {
        this.hostIPAddress = hostIPAddress;
    }

    public String getHostPort() {
        return hostPort;
    }

    public void setHostPort(String hostPort) {
        this.hostPort = hostPort;
    }

    public String getHostDescription() {
        return hostDescription;
    }

    public void setHostDescription(String hostDescription) {
        this.hostDescription = hostDescription;
    }

    public String getBIOSName() {
        return bIOSName;
    }

    public void setBIOSName(String bIOSName) {
        this.bIOSName = bIOSName;
    }

    public String getBIOSBuildNo() {
        return bIOSBuildNo;
    }

    public void setBIOSBuildNo(String bIOSBuildNo) {
        this.bIOSBuildNo = bIOSBuildNo;
    }

    public String getVMMName() {
        return vMMName;
    }

    public void setVMMName(String vMMName) {
        this.vMMName = vMMName;
    }

    public String getVMMBuildNo() {
        return vMMBuildNo;
    }

    public void setVMMBuildNo(String vMMBuildNo) {
        this.vMMBuildNo = vMMBuildNo;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(Date updatedOn) {
        this.updatedOn = updatedOn;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Character getIsProcessed() {
        return isProcessed;
    }

    public void setIsProcessed(Character isProcessed) {
        this.isProcessed = isProcessed;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getVCenterDetails() {
        return vCenterDetails;
    }

    public void setVCenterDetails(String vCenterDetails) {
        this.vCenterDetails = vCenterDetails;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getOem() {
        return oem;
    }

    public void setOem(String oem) {
        this.oem = oem;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (hostDetailID != null ? hostDetailID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TblHostDetail)) {
            return false;
        }
        TblHostDetail other = (TblHostDetail) object;
        if ((this.hostDetailID == null && other.hostDetailID != null) || (this.hostDetailID != null && !this.hostDetailID.equals(other.hostDetailID))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.intel.mountwilson.td.data.TblHostDetail[ hostDetailID=" + hostDetailID + " ]";
    }
    
}
