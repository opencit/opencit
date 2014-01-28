/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.as.rest;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author jbuhacoff
 */
public class SAMLReadXPathTest {

    public SAMLReadXPathTest() {
        
    }
    
    @Test
    public void testInterpretSAMLWithXPath() throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        String filename = "/host-10-1-70-126.saml";
        
        /*
        DocumentBuilderFactory factory =  DocumentBuilderFactory.newInstance ();
        factory.setNamespaceAware (true);
        DocumentBuilder builder = factory.newDocumentBuilder ();
*/
        Document xmlDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File("src/test/saml/"+filename));
//        Document xmlDocument = builder.parse(new File("src/test/saml/"+filename));
        XPath xPath = XPathFactory.newInstance().newXPath();
        
//        XPathExpression issuerNameExpr = xPath.compile("/saml2:Assertion/saml2:Issuer/text()");
          XPathExpression issuerNameExpr = xPath.compile("/Assertion/Issuer/text()");
                String issuer = (String) issuerNameExpr.evaluate(xmlDocument, XPathConstants.STRING);
                System.out.println("Issuer: "+issuer);
                  
         System.out.println("Subject: "+(String)xPath.compile("/Assertion/Subject/NameID/text()").evaluate(xmlDocument, XPathConstants.STRING));

         System.out.println("Trusted: "+(String)xPath.compile("/Assertion/AttributeStatement/Attribute[@Name='Trusted']/AttributeValue/text()").evaluate(xmlDocument, XPathConstants.STRING));
         System.out.println("BIOS_Name: "+(String)xPath.compile("/Assertion/AttributeStatement/Attribute[@Name='BIOS_Name']/AttributeValue/text()").evaluate(xmlDocument, XPathConstants.STRING));
    }
}
