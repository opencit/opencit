/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mountwilson.datamodel;

/**
 *
 * @author yuvrajsx
 */
public class HostReportTypeVO {
    
    
    private String hostName;
    private String mleInfo;
    private String createdOn;
    private Integer trustStatus;
    private String verifiedOn;

    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public void setMleInfo(String mleInfo) {
        this.mleInfo = mleInfo;
    }

    public void setTrustStatus(Integer trustStatus) {
        this.trustStatus = trustStatus;
    }

    public void setVerifiedOn(String verifiedOn) {
        this.verifiedOn = verifiedOn;
    }

    public String getCreatedOn() {
        return createdOn;
    }

    public String getHostName() {
        return hostName;
    }

    public String getMleInfo() {
        return mleInfo;
    }

    public Integer getTrustStatus() {
        return trustStatus;
    }

    public String getVerifiedOn() {
        return verifiedOn;
    }

    @Override
    public String toString() {
        return "HostReportTypeVO{" + "hostName=" + hostName + ", mleInfo=" + mleInfo + ", createdOn=" + createdOn + ", trustStatus=" + trustStatus + ", verifiedOn=" + verifiedOn + '}';
    }

    
}
