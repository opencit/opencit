/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.measurement;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 *
 * @author rksavino
 *
 */
@XmlRegistry
public class ObjectFactory {
    private final static QName _measurements_QNAME = new QName("", "measurements");

    public ObjectFactory() {
    }
    
    public TcbMeasurement createTcbMeasurementType() {
        return new TcbMeasurement();
    }

    @XmlElementDecl(namespace = "", name = "measurements")
    public JAXBElement<TcbMeasurement> createTcbMeasurement(TcbMeasurement value) {
        return new JAXBElement<TcbMeasurement>(_measurements_QNAME, TcbMeasurement.class, null, value);
    }
}
