/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tag;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.dcsg.cpg.xml.JAXB;
import com.intel.mtwilson.tag.common.X509AttrBuilder;
import com.intel.mtwilson.tag.model.x509.UTF8NameValueMicroformat;
import com.intel.mtwilson.tag.selection.json.TagSelectionModule;
import com.intel.mtwilson.tag.selection.xml.AttributeType;
import com.intel.mtwilson.tag.selection.xml.ObjectFactory;
import com.intel.mtwilson.tag.selection.xml.SelectionsType;
import java.io.IOException;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;

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
            return jaxb.write(selections);
        }
        catch(JAXBException e) {
            throw new IOException(e);
        }
    }
    
    
    /**
     * Converts the xml attribute element to a pair of OID and ASN1 encoding
     * of the attribute value.
     * @param attribute
     * @return the same attribute in a representation that can be used with X509AttrBuilder
     * @throws IOException if the attribute value is der-encoded but cannot be parsed into an ASN1Object
     * @throws UnsupportedOperationException if the attribute OID is not supported or a text format was given for which a parser is not available
     */
    public static X509AttrBuilder.Attribute toAttributeOidValue(AttributeType attribute) throws IOException {
        if (attribute.getDer() != null) {
            ASN1Object asn1 = ASN1Object.fromByteArray(attribute.getDer().getValue()); // throws IOException
            return new X509AttrBuilder.Attribute(new ASN1ObjectIdentifier(attribute.getOid()), asn1);
        } else if (attribute.getOid().equals("2.5.4.789.1") && attribute.getText() != null) {
            String[] parts = attribute.getText().getValue().split("=");  // name=value
            return new X509AttrBuilder.Attribute(new ASN1ObjectIdentifier(UTF8NameValueMicroformat.OID), new UTF8NameValueMicroformat(parts[0], parts[1]));
        } else if (attribute.getOid().equals("2.5.4.789.2") && attribute.getText() != null) {
            throw new UnsupportedOperationException("text format not implemented yet for 2.5.4.789.2"); // typically 2.5.4.789.2 would use der format anyway...
        } else {
            throw new UnsupportedOperationException("text format not implemented yet for " + attribute.getOid());
        }
        
    }
    
    
}
