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
@JacksonXmlRootElement(localName="tagWriteRequest")
public class TagWriteRequest {
    private byte[] tag; // 20 bytes to write into NVRAM
    private byte[] hardwareUuid; // 16 bytes UUID

    public void setTag(byte[] tag) {
        this.tag = tag;
    }

    public byte[] getTag() {
        return tag;
    }

    public void setHardwareUuid(byte[] hardwareUuid) {
        this.hardwareUuid = hardwareUuid;
    }

    public byte[] getHardwareUuid() {
        return hardwareUuid;
    }

    
    
}
