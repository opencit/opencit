/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.measurement;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author rksavino
 */
@XmlRootElement(name = "file")
public class FileMeasurementEntry extends MeasurementEntry {
    private String path;
    
    @XmlAttribute
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
}
