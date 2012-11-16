/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.crypto;

import java.security.*;
import java.security.cert.X509Certificate;
import java.util.List;
import javax.xml.crypto.*;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Instantiate this class with a SAML signing public key and then you can 
 * use it to verify multiple documents signed with the corresponding private
 * key.
 * 
 * See also http://ws.apache.org/wss4j/config.html 
 * 
 * @author jbuhacoff
 */
public class SamlUtil {
    private XMLSignatureFactory factory;
    private Logger log = LoggerFactory.getLogger(getClass());
    
    
    // keystore must already be unlocked with its password
    public SamlUtil() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
            String providerName = System.getProperty
                ("jsr105Provider", "org.jcp.xml.dsig.internal.dom.XMLDSigRI");
            factory = XMLSignatureFactory.getInstance ("DOM", 
                (Provider) Class.forName (providerName).newInstance ()); //ClassNotFoundException, InstantiationException, IllegalAccessException
    }
    
    /**
    Seeks out the signature element in the given tree, and validates it.
    Searches the configured keystore (asking it to function also as a
    truststore) for a certificate with a matching fingerprint.
    * 
    * Certificates trusted for SAML-signing must be marked with the
    * tag "(saml)" or "(SAML)" in their alias
    * 
    
    @return true if the signature validates and we know the signer; 
            false otherwise
    */
    public boolean verifySAMLSignature(Element target, X509Certificate[] trustedSigners) throws MarshalException, XMLSignatureException, KeyStoreException
    {
        // Validate the signature -- i.e. SAML object is pristine:
        NodeList nl = 
            target.getElementsByTagNameNS (XMLSignature.XMLNS, "Signature");
        if (nl.getLength () == 0) {
            throw new IllegalArgumentException ("Cannot find Signature element");
        }

        DOMValidateContext context = new DOMValidateContext
            (new KeyValueKeySelector (), nl.item (0));

        XMLSignature signature = factory.unmarshalXMLSignature (context); // MarshalException
        if (!signature.validate (context)) { // XMLSignatureException
            log.debug("XML signature is not valid");
            return false;
        }
        
        // Find a trusted cert -- i.e. the signer is actually someone we trust:
        for (Object keyInfoItem : signature.getKeyInfo().getContent ()) {
          if (keyInfoItem instanceof X509Data) {
            for (Object X509Item : ((X509Data) keyInfoItem).getContent ()) {
              if (X509Item instanceof X509Certificate) {
                X509Certificate theirCert = (X509Certificate) X509Item;
                log.debug("Found X509 certificate in XML: {}", theirCert.getSubjectX500Principal().getName());
                for(X509Certificate ourCert : trustedSigners) {
                    if (ourCert.equals(theirCert)) {
                        return true;
                    }
                }
              }
            }
          }
        }
        
        log.debug("Signature was valid, but signer was not known.");
        return false;
    }

    /**
    KeySelector that can handle KeyValue and X509Data info.
    */
    private static class KeyValueKeySelector 
        extends KeySelector 
    {
        @Override
        public KeySelectorResult select(KeyInfo keyInfo, 
            KeySelector.Purpose purpose, AlgorithmMethod method,
                XMLCryptoContext context)
            throws KeySelectorException 
        {
            if (keyInfo == null) {
                throw new KeySelectorException ("Null KeyInfo object!");
            }
            SignatureMethod sm = (SignatureMethod) method;
            List list = keyInfo.getContent ();

            for (int i = 0; i < list.size(); i++) 
            {
                XMLStructure xmlStructure = (XMLStructure) list.get(i);
                PublicKey pk = null;
                try 
                {
                    if (xmlStructure instanceof KeyValue) {
                        pk = ((KeyValue) xmlStructure).getPublicKey();
                    }
                    else if (xmlStructure instanceof X509Data) {
                        for (Object data : ((X509Data) xmlStructure).getContent()) {
                            if (data instanceof X509Certificate) {
                                pk = ((X509Certificate) data).getPublicKey ();
                            }
                        }
                    }
                    if (pk!=null && algEquals(sm.getAlgorithm(), pk.getAlgorithm())) {
                        return new SimpleKeySelectorResult (pk);
                    }
                }
                catch (KeyException ke) {
                    throw new KeySelectorException(ke);
                }
                    
            }
            
            throw new KeySelectorException ("No KeyValue element found!");
        }
    }

    /**
    Test that a formal URI expresses the same algorithm as a conventional
    short name such as "DSA" or "RSA".
    */
    static boolean algEquals (String algURI, String algName) 
    {
        return (algName.equalsIgnoreCase ("DSA") &&
                algURI.equalsIgnoreCase (SignatureMethod.DSA_SHA1)) 
            ||
               (algName.equalsIgnoreCase ("RSA") &&
                algURI.equalsIgnoreCase(SignatureMethod.RSA_SHA1));
    }

    /**
    Data structure returned by the key selector to the validation context.
    */
    private static class SimpleKeySelectorResult implements KeySelectorResult {
        private PublicKey pk;
        
        SimpleKeySelectorResult (PublicKey pk) {
            this.pk = pk;
        }

        @Override
        public Key getKey() { 
            return pk; 
        }
    }
    
}
