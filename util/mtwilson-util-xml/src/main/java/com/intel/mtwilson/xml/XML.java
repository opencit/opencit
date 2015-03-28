/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 *
 * @author jbuhacoff
 */
public class XML {

    private String schemaPackageName;
    private HashSet<String> schemaLocations;

    public XML() {
        this.schemaPackageName = null;
        this.schemaLocations = new HashSet<>();
    }

    public void setSchemaPackageName(String schemaPackageName) {
        this.schemaPackageName = schemaPackageName;
    }
    
    public void addSchemaLocation(String href) {
        this.schemaLocations.add(href);
    }

    /**
     * Example schema locations:
     * http://docs.oasis-open.org/security/saml/v2.0/saml-schema-protocol-2.0.xsd
     * http://docs.oasis-open.org/security/saml/v2.0/saml-schema-assertion-2.0.xsd
     * http://www.w3.org/TR/2002/REC-xmlenc-core-20021210/xenc-schema.xsd
     * http://www.w3.org/TR/2002/REC-xmldsig-core-20020212/xmldsig-core-schema.xsd
     *
     * @param xml
     * @return
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public Element parseDocumentElement(String xml) throws ParserConfigurationException, SAXException, IOException {
        ClasspathResourceResolver resolver = new ClasspathResourceResolver();
        resolver.setResourcePackage(schemaPackageName);
        
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        schemaFactory.setResourceResolver(resolver);

        Source[] schemaSources = new Source[schemaLocations.size()];
        int i = 0;
        for (String schemaLocation : schemaLocations) {
            InputStream in = resolver.findResource(schemaLocation);
            schemaSources[i] = new StreamSource(in);
            i++;
        }
        Schema schema = schemaFactory.newSchema(schemaSources);
        
//        Validator validator = schema.newValidator();
//        validator.validate(new StreamSource(new ByteArrayInputStream(xml.getBytes())));
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setExpandEntityReferences(false); // bug #1038 prevent XXE
        factory.setXIncludeAware(false); // bug #1038 prevent XXE
        factory.setSchema(schema); // fix for javax.xml.crypto.dsig.XMLSignatureException: javax.xml.crypto.URIReferenceException: com.sun.org.apache.xml.internal.security.utils.resolver.ResourceResolverException: Cannot resolve element with ID HostTrustAssertion
        DocumentBuilder builder = factory.newDocumentBuilder(); // ParserConfigurationException
        try (ByteArrayInputStream in = new ByteArrayInputStream(xml.getBytes())) {
            Element document = builder.parse(in).getDocumentElement(); // SAXException, IOException
            return document;
        }
    }
    
}
