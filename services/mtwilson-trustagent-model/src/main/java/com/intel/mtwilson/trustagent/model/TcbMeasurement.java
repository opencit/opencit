/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.model;

import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author rksavino
 */
@XmlRootElement(name = "measurements")
public class TcbMeasurement {
    private String digestAlg;
    private List<ITcbMeasurementData> measurements = null;

    @XmlAttribute
    public String getDigestAlg() {
        return digestAlg;
    }
    
    public void setDigestAlg(String digestAlg) {
        this.digestAlg = digestAlg;
    }
    
    @XmlElementRefs({
        @XmlElementRef(name = "file", type = TcbMeasurementFile.class),
        @XmlElementRef(name = "dir", type = TcbMeasurementDirectory.class)
    })
    public List<ITcbMeasurementData> getMeasurements() {
        return measurements;
    }
    
    public void setMeasurements(List<ITcbMeasurementData> measurements) {
        this.measurements = measurements;
    }
}