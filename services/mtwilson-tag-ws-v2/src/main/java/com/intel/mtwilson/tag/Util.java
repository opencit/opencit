/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tag;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.dcsg.cpg.xml.JAXB;
import com.intel.mtwilson.tag.selection.json.TagSelectionModule;
import com.intel.mtwilson.tag.selection.xml.ObjectFactory;
import com.intel.mtwilson.tag.selection.xml.SelectionsType;
import java.io.IOException;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

/**
 * Wraps JAXB and XML exceptions in IOException. to present a uniform
 * interface to callers.
 * 
 * @author jbuhacoff
 */
public class Util {
    private static final ObjectMapper mapper;
    private static final JAXB jaxb;
    private static final ObjectFactory jaxbObjectFactory;
    
    static {
        mapper = new ObjectMapper();
        mapper.registerModule(new TagSelectionModule());
        jaxb = new JAXB();
        jaxbObjectFactory = new ObjectFactory();
    }
    
    public static SelectionsType fromJson(String json) throws IOException {
        return mapper.readValue(json, SelectionsType.class);
    }
    
    public static SelectionsType fromXml(String xml) throws IOException {
        try {
            return jaxb.read(xml, SelectionsType.class);
        }
        catch(JAXBException | XMLStreamException e) {
            throw new IOException(e);
        }
    }
    
    public static String toJson(SelectionsType selections) throws IOException {
        return mapper.writeValueAsString(selections);
    }
    
    public static String toXml(SelectionsType selections) throws IOException {
        try {
//            return jaxb.write(selections);
/*
java.io.IOException: javax.xml.bind.MarshalException
 - with linked exception:
[com.sun.istack.SAXException2: unable to marshal type "com.intel.mtwilson.tag.selection.xml.SelectionsType" as an element because it is missing an @XmlRootElement annotation]
 * 
 */            
            return jaxb.write(jaxbObjectFactory.createSelections(selections));
/*
java.io.IOException: javax.xml.bind.JAXBException: Provider com.sun.xml.bind.v2.ContextFactory could not be instantiated: javax.xml.bind.JAXBException: "javax.xml.bind" doesnt contain ObjectFactory.class or jaxb.index
 - with linked exception:
[javax.xml.bind.JAXBException: "javax.xml.bind" doesnt contain ObjectFactory.class or jaxb.index]
 * 
 */            
        }
        catch(JAXBException e) {
            throw new IOException(e);
        }
    }
}
