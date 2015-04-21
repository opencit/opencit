/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 *
 * @author boskisha, rksavino
 */
package com.intel.mtwilson.util.xml.dsig;

import javax.xml.crypto.*;
import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.*;
import java.io.IOException;
import java.io.StringReader;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This is a simple example of validating an XML Signature using the JSR 105
 * API. It assumes the key needed to validate the signature is contained in a
 * KeyValue KeyInfo.
 */
public class XmlDsigVerify {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(XmlDsigVerify.class);
    
    public static boolean isValid(String xmlData, X509Certificate trustedCert) throws ParserConfigurationException, SAXException, IOException, MarshalException, XMLSignatureException {

        // Instantiate the document to be validated
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document doc = dbf.newDocumentBuilder().parse(new InputSource(new StringReader(xmlData)));
        
        // Find Signature element
        NodeList nl = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
        if (nl.getLength() == 0) {
            log.error("Cannot find Signature element in the data to be validated.");
            throw new IOException("Cannot find Signature element in the data to be validated.");
        }

        // Create a DOM XMLSignatureFactory that will be used to unmarshal the document containing the XMLSignature
        XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
        // Create a DOMValidateContext and specify a KeyValue KeySelector and document context
        DOMValidateContext valContext = new DOMValidateContext(new TrustedKeyValueKeySelector(trustedCert), nl.item(0));

        // unmarshal the XMLSignature
        XMLSignature signature = fac.unmarshalXMLSignature(valContext);

        // ValidateSignature the XMLSignature (generated above)
        boolean coreValidity = signature.validate(valContext);
 
        // Check core validation status
        if (coreValidity == false) {
            log.error("Signature failed core validation");
            boolean sv = signature.getSignatureValue().validate(valContext);
            log.error("signature validation status: " + sv);
            // check the validation status of each Reference
            Iterator i = signature.getSignedInfo().getReferences().iterator();
            for (int j = 0; i.hasNext(); j++) {
                boolean refValid = ((Reference) i.next()).validate(valContext);
                log.error("ref[" + j + "] validity status: " + refValid);
            }
            return false;
        } else {
            log.info("Signature passed core validation");
            return true;
        }
    }
    
    /**
     * KeySelector which retrieves the public key out of the KeyValue element
     * and returns it. NOTE: If the key algorithm doesn't match signature
     * algorithm, then the public key will be ignored.
     */
    private static class TrustedKeyValueKeySelector extends KeySelector {
        private X509Certificate trustedCert;
        
        TrustedKeyValueKeySelector(X509Certificate trustedCert) {
            this.trustedCert = trustedCert;
        }
        
        @Override
        public KeySelectorResult select(KeyInfo keyInfo,KeySelector.Purpose purpose,AlgorithmMethod method,XMLCryptoContext context)throws KeySelectorException {
            
            Iterator ki = keyInfo.getContent().iterator();
            while (ki.hasNext()) {
                
                XMLStructure info = (XMLStructure) ki.next();
                if (!(info instanceof X509Data)) { continue; }
                
                X509Data x509Data = (X509Data) info;
                Iterator xi = x509Data.getContent().iterator();
                while (xi.hasNext()) {
                    Object o = xi.next();
                    if (!(o instanceof X509Certificate)) { continue; }

                    if(!isTrustedCertificate((X509Certificate) o)){ continue; }
                    
                    final PublicKey key = ((X509Certificate) o).getPublicKey();
                    // Make sure the algorithm is compatible with the method.
                    if (algEquals(method.getAlgorithm(), key.getAlgorithm())) {
                        return new KeySelectorResult() {
                            @Override
                            public Key getKey() { return key; }
                        };
                    }
                }
            }
            throw new KeySelectorException("No key found!");
        }


        /*
         * Verifies the certificate that was used to sign the XML document against the MTW SAML certificate.
         */
        private boolean isTrustedCertificate(X509Certificate xmlCert) {
            boolean trusted = false;

            try {
                log.debug("Validating the certificate that signed the XML data. {}", xmlCert.getIssuerX500Principal().getName());
                xmlCert.verify(trustedCert.getPublicKey());
                trusted = true;

            } catch (CertificateException | NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException | SignatureException ex) {
                log.error("Error during verification of the certificate that signed the data. {}", ex.getMessage());
            }

            return trusted;
        }
    }

    /**
     *  Verifies if given two algorithms are same
    */
    static boolean algEquals(String algURI, String algName) {
        if ((algName.equalsIgnoreCase("DSA") && algURI.equalsIgnoreCase(SignatureMethod.DSA_SHA1))
                || (algName.equalsIgnoreCase("RSA") && algURI.equalsIgnoreCase(SignatureMethod.RSA_SHA1))) {
            return true;
        } else {
            return false;
        }
    }
}
