/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.datatypes;

import com.fasterxml.jackson.annotation.JsonProperty;
//import org.codehaus.jackson.annotate.JsonProperty;

/**
 * TODO: Once we use the same API DataTypes package in the backend service, we can 
 * use the OS and OEM data objects here instead of declaring them separately again.
 * TODO replace pcrName and pcrDigest with the Pcr object.
 * XXX TODO oemName should be moved out of this class (and corresponding database table)
 * and into a separate table that links oem's to mle's. or create a separate class
 * for Mle(Name,Version,Os,Pcr) and BiosMle(Name,Version,Oem,Pcr) with corresponding
 * separate database tables. "either or" optional fields pervade the entire application
 * complicating logic everywhere, and in most cases a piece of code is only interested
 * in one or the other so a streamlined class is much easier to read at first sight.
 * 
 * @author ssbangal
 */
public class PCRWhiteList {
    
    private String pcrName;
    private String pcrDigest;
    private String mleName;
    private String mleVersion;
    private String osName; 
    private String osVersion; 
    private String oemName;


    /**
     * Constructor for the PCRWhiteList object. Note that based on the MLE type only OS or OEM information
     * need to populated.
     */
    public PCRWhiteList() {
    }

    /**
     * Constructor for the PCRWhiteList object. Note that based on the MLE type only OS or OEM information
     * need to populated.
     * 
     * @param pcrName
     * @param pcrDigest
     * @param mleName
     * @param mleVersion
     * @param osName
     * @param osVersion
     * @param oemName 
     */
    public PCRWhiteList(String pcrName, String pcrDigest, String mleName, String mleVersion, String osName, String osVersion, String oemName) {
        this.pcrName = pcrName;
        this.pcrDigest = pcrDigest;
        this.mleName = mleName;
        this.mleVersion = mleVersion;
        this.osName = osName;
        this.osVersion = osVersion;
        this.oemName = oemName;
    }
    
    @JsonProperty("MLEName")
    public String getMleName() {
        return mleName;
    }

    @JsonProperty("MLEName")
    public void setMleName(String mleName) {
        this.mleName = mleName;
    }

    @JsonProperty("MLEVersion")
    public String getMleVersion() {
        return mleVersion;
    }

    @JsonProperty("MLEVersion")
    public void setMleVersion(String mleVersion) {
        this.mleVersion = mleVersion;
    }

    @JsonProperty("OEMName")
    public String getOemName() {
        return oemName;
    }

    @JsonProperty("OEMName")
    public void setOemName(String oemName) {
        this.oemName = oemName;
    }

    @JsonProperty("OSName")
    public String getOsName() {
        return osName;
    }

    @JsonProperty("OSName")
    public void setOsName(String osName) {
        this.osName = osName;
    }

    @JsonProperty("OSVersion")
    public String getOsVersion() {
        return osVersion;
    }

    @JsonProperty("OSVersion")
    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    @JsonProperty("PCRDigest")
    public String getPcrDigest() {
        return pcrDigest;
    }

    @JsonProperty("PCRDigest")
    public void setPcrDigest(String pcrDigest) {
        this.pcrDigest = pcrDigest;
    }

    @JsonProperty("PCRName")
    public String getPcrName() {
        return pcrName;
    }

    @JsonProperty("PCRName")
    public void setPcrName(String pcrName) {
        this.pcrName = pcrName;
    }

}
