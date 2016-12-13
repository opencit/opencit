/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.datatypes;

import com.fasterxml.jackson.annotation.JsonProperty;
//import org.codehaus.jackson.annotate.JsonProperty;

/**
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
    private String pcrBank = "sha1";

    public String getPcrBank() {
        return pcrBank;
    }

    public void setPcrBank(String pcrBank) {
        this.pcrBank = pcrBank;
    }


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
