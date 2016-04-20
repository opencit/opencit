/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.as.rest;

import java.io.File;
import java.io.FileInputStream;
import org.junit.Test;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.io.IOUtils;
import org.opensaml.saml2.core.*;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.parse.ParserPool;
import org.opensaml.xml.schema.XSAny;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
/**
 *
 * @author jbuhacoff
 */
public class SAMLReadOpenSAMLTest {

    public SAMLReadOpenSAMLTest() {
        
    }
    
    
    @Test
    public void testInterpretSAMLWithOpenSAML() throws IOException, UnmarshallingException, SAXException, ParserConfigurationException {
        String filename = "/host-10-1-70-126.saml";
        
        
        DocumentBuilderFactory factory =  DocumentBuilderFactory.newInstance ();
        factory.setNamespaceAware (true);
        DocumentBuilder builder = factory.newDocumentBuilder ();

        InputStream in = new FileInputStream(new File("src/test/saml/"+filename)); //getClass().getResourceAsStream(filename);
        //System.out.println(IOUtils.toString(in)); // works
        Element document = builder.parse(in).getDocumentElement ();
        in.close();
        
         Assertion assertion = (Assertion) Configuration.getUnmarshallerFactory().getUnmarshaller(document).unmarshall(document);    
        NameID nameID = assertion.getSubject ().getNameID ();
        
        System.out.println ("Assertion issued by " +
            assertion.getIssuer ().getValue ());
        System.out.println ("Subject name: " + nameID.getValue ());
        System.out.println ("  (Format " + nameID.getFormat () + ")");
        
        System.out.println ("Attributes found:");
        for (Statement statement : assertion.getStatements ())
            if (statement instanceof AttributeStatement)
                for (Attribute attribute : 
                        ((AttributeStatement) statement).getAttributes ())
                {
                    System.out.print ("  " + attribute.getName () + ": ");
                    for (XMLObject value : attribute.getAttributeValues ())
                        if (value instanceof XSAny)
                            System.out.print 
                                (((XSAny) value).getTextContent () + " ");
                    System.out.println ();
                }
        
    }
}
