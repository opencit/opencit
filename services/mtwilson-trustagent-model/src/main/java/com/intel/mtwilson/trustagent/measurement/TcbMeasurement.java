/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.measurement;

import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author rksavino
 */
@XmlRootElement(name = "Measurements")
public class TcbMeasurement {
    private String digestAlg;
    private List<MeasurementEntry> measurements = null;

    @XmlAttribute
    public String getDigestAlg() {
        return digestAlg;
    }
    
    public void setDigestAlg(String digestAlg) {
        this.digestAlg = digestAlg;
    }
    
    @XmlElementRefs({
        @XmlElementRef(name = "File", type = FileMeasurementEntry.class),
        @XmlElementRef(name = "Dir", type = DirectoryMeasurementEntry.class)
    })
    public List<MeasurementEntry> getMeasurements() {
        return measurements;
    }
    
    public void setMeasurements(List<MeasurementEntry> measurements) {
        this.measurements = measurements;
    }
}