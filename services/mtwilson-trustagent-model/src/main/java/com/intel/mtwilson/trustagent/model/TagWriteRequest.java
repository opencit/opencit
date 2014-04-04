/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.dcsg.cpg.io.UUID;

/**
 * Example input:  { tag: "YTBiMWMyZDNlNGY1ZzZoN2k4ajk=", hardware_uuid: "7a569dad-2d82-49e4-9156-069b0065b262" }
 * 
 * @author jbuhacoff
 */
@JacksonXmlRootElement(localName="tag_write_request")
public class TagWriteRequest {
    private byte[] tag; // 20 bytes to write into NVRAM
    private UUID hardwareUuid; // 16 bytes UUID

    public void setTag(byte[] tag) {
        this.tag = tag;
    }

    public byte[] getTag() {
        return tag;
    }

    public void setHardwareUuid(UUID hardwareUuid) {
        this.hardwareUuid = hardwareUuid;
    }

    public UUID getHardwareUuid() {
        return hardwareUuid;
    }

    
    
}
