package com.intel.mtwilson.as.helper.saml;

import com.intel.mtwilson.io.Resource;
import com.intel.mtwilson.util.ResourceFinder;
import java.io.*;
import java.net.URL;

import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.xml.crypto.AlgorithmMethod;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.KeySelectorException;
import javax.xml.crypto.KeySelectorResult;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.configuration.Configuration;

import org.opensaml.xml.XMLObject;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * Original author Will Provost
 * Copyright 2009 Will Provost. All rights reserved by Capstone Courseware, LLC.
 * 
 * http://code.google.com/p/opensaml/source/browse/
 */
public class SAMLSignature
{
    private XMLSignatureFactory factory;
    private KeyStore keyStore;
    private KeyPair keyPair;
    private KeyInfo keyInfo;
    
    /**
    Get a KeyStore object given the keystore filename and password.
    */
    public static KeyStore getKeyStore (InputStream in, String password)
        throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException
    {
        KeyStore result = KeyStore.getInstance (KeyStore.getDefaultType ());
        result.load (in, password.toCharArray ());
        return result;
    }

    /**
    Loads a keystore and builds a stock key-info structure for use by 
    base classes.
    */
    public SAMLSignature (Resource keystoreResource, Configuration config) throws ClassNotFoundException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableEntryException, IllegalAccessException, InstantiationException, IOException, CertificateException
    {
        
            String providerName = config.getString
                ("jsr105Provider", "org.jcp.xml.dsig.internal.dom.XMLDSigRI");
            factory = XMLSignatureFactory.getInstance ("DOM", 
                (Provider) Class.forName (providerName).newInstance ());

//            URL keystore = getClass().getResource(config.getString ("saml.keystore.file"));
//            System.out.println("keystore url: "+keystore.toString());
//            InputStream keystoreInputStream = keystore.openStream();
            //File keystoreFile = ResourceFinder.getFile(config.getString("saml.keystore.file"));
            //FileInputStream keystoreInputStream = new FileInputStream(keystoreFile);
            InputStream keystoreInputStream = keystoreResource.getInputStream();
//            keyStore = KeyStoreUtil.getKeyStore(SAMLSignature.class.getResourceAsStream(config.getString ("keystore")),config.getString ("storepass"));
            try {
            	keyStore = getKeyStore(keystoreInputStream,config.getString ("saml.keystore.password"));
            }
            finally {
            	if( keystoreInputStream != null ) {
            		keystoreInputStream.close();
                }
            }
            KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry)
                keyStore.getEntry (config.getString ("saml.key.alias"), 
                    new KeyStore.PasswordProtection 
                        (config.getString ("saml.key.password").toCharArray ()));
            keyPair = new KeyPair (entry.getCertificate ().getPublicKey (), 
                entry.getPrivateKey ());

            KeyInfoFactory kFactory = factory.getKeyInfoFactory ();
            keyInfo = kFactory.newKeyInfo 
                (Collections.singletonList (kFactory.newX509Data 
                    (Collections.singletonList (entry.getCertificate ()))));
    }
    
    /**
    Adds an enveloped signature to the given element.
    Then moves the signature element so that it is in the correct position
    according to the SAML assertion and protocol schema: it must immediately 
    follow any Issuer and precede everything else.
    */
    public void signSAMLObject (Element target)
        throws GeneralSecurityException, XMLSignatureException, MarshalException 
    {
        Reference ref = factory.newReference
            ("#" + target.getAttribute ("ID"), 
             factory.newDigestMethod (DigestMethod.SHA1, null),
             Collections.singletonList (factory.newTransform
                (Transform.ENVELOPED, (TransformParameterSpec) null)), 
             null, 
             null);

        SignedInfo signedInfo = factory.newSignedInfo 
            (factory.newCanonicalizationMethod
                (CanonicalizationMethod.INCLUSIVE_WITH_COMMENTS, 
                    (C14NMethodParameterSpec) null), 
                 factory.newSignatureMethod (SignatureMethod.RSA_SHA1, null),
                 Collections.singletonList (ref));
             
        XMLSignature signature = factory.newXMLSignature (signedInfo, keyInfo);
        DOMSignContext signContext = new DOMSignContext
            (keyPair.getPrivate (), target);
        signature.sign (signContext);
        
        // For the result to be schema-valid, we have to move the signature
        // element from its place at the end of the child list to live
        // between Issuer and Subject elements.  So, deep breath, and:
        Node signatureElement = target.getLastChild ();

        boolean foundIssuer = false;
        Node elementAfterIssuer = null;
        NodeList children = target.getChildNodes ();
        for (int c = 0; c < children.getLength (); ++c)
        {
            Node child = children.item (c);
            
            if (foundIssuer)
            {
                elementAfterIssuer = child;
                break;
            }
            
            if (child.getNodeType () == Node.ELEMENT_NODE &&
                    child.getLocalName ().equals ("Issuer"))
                foundIssuer = true;
        }
        
        // Place after the Issuer, or as first element if no Issuer:
        if (!foundIssuer || elementAfterIssuer != null)
        {
            target.removeChild (signatureElement);
            target.insertBefore (signatureElement, 
                foundIssuer
                    ? elementAfterIssuer
                    : target.getFirstChild ());
        }
    }
    

}
