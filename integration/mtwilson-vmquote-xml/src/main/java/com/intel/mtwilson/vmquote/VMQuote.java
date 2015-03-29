/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.vmquote;

import com.intel.dcsg.cpg.xml.JAXB;
import com.intel.mtwilson.xml.XML;
import java.io.IOException;
import java.security.KeyStoreException;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.parsers.ParserConfigurationException;
import org.opensaml.xml.io.UnmarshallingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * This class extracts trust information from a SAML assertion.
 *
 * Before using the assertions contained within a TrustAssertion object, you
 * must call isValid() to find out if the provided assertion is valid. If it is,
 * you can call getSubject(), getIssuer(), getAttributeNames(), and
 * getStringAttribute(), etc. If isValid() returns false, you can call error()
 * to get the Exception object that describes the validation error.
 *
 * See also http://ws.apache.org/wss4j/config.html
 *
 * @author jbuhacoff
 */
public class VMQuote {

    private final Logger log = LoggerFactory.getLogger(getClass());
//    private HashMap<String,String> assertionMap;
    private boolean isValid;
    private Exception error;
    private Element documentElement;
    private com.intel.mtwilson.vmquote.xml.VMQuoteResponse vmQuoteResponse;
    private JAXB jaxb = new JAXB();

    /**
     * Trusted SAML-signing certificates in the keystore must be marked for this
     * trusted purpose with the tag "(saml)" or "(SAML)" at the end of their
     * alias.
     *
     * @param trustedSigners keystore with at least one trusted certificate with
     * the "(saml)" tag in its alias
     * @param xml returned from attestation service
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws UnmarshallingException
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws MarshalException
     * @throws XMLSignatureException
     * @throws KeyStoreException
     */
    public VMQuote(String xml) {
        try {
            documentElement = readXml(xml);
            vmQuoteResponse = jaxb.convert(documentElement, com.intel.mtwilson.vmquote.xml.VMQuoteResponse.class);
            
            // TODO: validate signatures
            
        } catch (Exception e) {
            log.error("Cannot verify trust assertion", e);
            isValid = false;
            error = e;
//            assertionMap = null;
        }
    }

    public boolean isValid() {
        return isValid;
    }

    /**
     *
     * @return null if assertion is valid, otherwise an exception object
     * describing the error
     */
    public Exception error() {
        return error;
    }


    /**
     * See also {@code XML.parseDocumentElement} in mtwilson-util-xml
     */
    private Element readXml(String xmlDocument) throws ParserConfigurationException, SAXException, IOException {
        XML xml = new XML();
        xml.setSchemaPackageName("xsd");
        xml.addSchemaLocation("http://www.w3.org/2001/XMLSchema/XMLSchema.xsd");
        xml.addSchemaLocation("http://www.w3.org/TR/2002/REC-xmldsig-core-20020212/xmldsig-core-schema.xsd");
        xml.addSchemaLocation("mtwilson-trustpolicy.xsd");
        xml.addSchemaLocation("mtwilson-measurement.xsd");
        xml.addSchemaLocation("mtwilson-vmquote.xsd");
        return xml.parseDocumentElement(xmlDocument);
    }

}
