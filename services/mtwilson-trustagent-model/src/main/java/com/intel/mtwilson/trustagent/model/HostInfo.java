/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 *
 * @author jbuhacoff
 */
@JacksonXmlRootElement(localName="host_info")
public class HostInfo {
    public long timestamp; // was timeStamp
    public String errorCode;
    public String errorMessage;
    public String osName;
    public String osVersion;
    public String biosOem;
    public String biosVersion;
    public String vmmName;
    public String vmmVersion;
    public String processorInfo;
    public String hardwareUuid; // was hostUUID
    public String tpmVersion;
    public String pcrBanks;

    /*
    "<host_info>"
    + "<timeStamp>" + new Date(System.currentTimeMillis()).toString() + "</timeStamp>"
    + "<clientIp>" + CommandUtil.getHostIpAddress() + "</clientIp>"
    + "<errorCode>" + context.getErrorCode().getErrorCode() + "</errorCode>"
    + "<errorMessage>" + context.getErrorCode().getMessage() + "</errorMessage>"
    + "<osName>" + context.getOsName() + "</osName>"
    + "<osVersion> " + context.getOsVersion() + "</osVersion>"
    + "<biosOem>" + context.getBiosOem() + "</biosOem>"
    + "<biosVersion> " + context.getBiosVersion()+ "</biosVersion>"
    + "<vmmName>" + context.getVmmName() + "</vmmName>"
    + "<vmmVersion>" + context.getVmmVersion() + "</vmmVersion>"
    + "<processorInfo>" + context.getProcessorInfo() + "</processorInfo>"
    +"<hostUUID>" + context.getHostUUID() + "</hostUUID>"
    + "</host_info>";
     */
    public String getBiosOem() {
        return biosOem;
    }

    public String getBiosVersion() {
        return biosVersion;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getHardwareUuid() {
        return hardwareUuid;
    }

    public String getOsName() {
        return osName;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public String getProcessorInfo() {
        return processorInfo;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getVmmName() {
        return vmmName;
    }

    public String getVmmVersion() {
        return vmmVersion;
    }
    
    public String getTpmVersion() {
        return tpmVersion;
    }
    
    public String getPcrBanks() {
        return pcrBanks;
    }
}
