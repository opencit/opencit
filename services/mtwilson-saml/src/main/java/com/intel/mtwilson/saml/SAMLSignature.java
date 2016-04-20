package com.intel.mtwilson.saml;

import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Collections;
import javax.xml.crypto.MarshalException;
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
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.mtwilson.My;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Will Provost (original author)
 * @author Jonathan Buhacoff
 *
 * Copyright 2009 Will Provost. All rights reserved by Capstone Courseware, LLC.
 * Used with permission.
 *
 * http://capcourse.com/Library/OpenSAML
 */
public class SAMLSignature {

    private XMLSignatureFactory factory;
    private KeyStore keyStore;
    private KeyPair keyPair;
    private KeyInfo keyInfo;

    /**
     * Get a KeyStore object given the keystore filename and password.
     */
    public static KeyStore getKeyStore(InputStream in, String password)
            throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        KeyStore result = KeyStore.getInstance(KeyStore.getDefaultType());
        result.load(in, password.toCharArray());
        return result;
    }

    /**
     * Loads a keystore and builds a stock key-info structure for use by base
     * classes.
     */
    public SAMLSignature(Configuration configuration) throws ClassNotFoundException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableEntryException, IllegalAccessException, InstantiationException, IOException, CertificateException {
        SamlConfiguration saml = new SamlConfiguration(configuration);
        
        String providerName = saml.getJsr105Provider(); //configuration.getString("jsr105Provider", "org.jcp.xml.dsig.internal.dom.XMLDSigRI");
        factory = XMLSignatureFactory.getInstance("DOM", (Provider) Class.forName(providerName).newInstance());

//            URL keystore = getClass().getResource(config.getString ("saml.keystore.file"));
//            System.out.println("keystore url: "+keystore.toString());
//            InputStream keystoreInputStream = keystore.openStream();
        //File keystoreFile = new File(saml.getSamlKeystoreFile());// new File(configuration.getString("saml.keystore.file")); //ResourceFinder.getFile(config.getString("saml.keystore.file"));
        File keystoreFile = My.configuration().getSamlKeystoreFile();
//            InputStream keystoreInputStream = keystoreResource.getInputStream(); // this obtains it from the database (or whatever resource is provided)
//            keyStore = KeyStoreUtil.getKeyStore(SAMLSignature.class.getResourceAsStream(config.getString ("keystore")),config.getString ("storepass"));
        try (FileInputStream keystoreInputStream = new FileInputStream(keystoreFile)) {
            keyStore = getKeyStore(keystoreInputStream, saml.getSamlKeystorePassword()); /*configuration.getString("saml.keystore.password"*//*,System.getenv("SAMLPASSWORD")*/ 
        }
        KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(saml.getSamlKeyAlias(), //  /*configuration.getString("saml.key.alias"),*/
                new KeyStore.PasswordProtection(saml.getSamlKeyPassword().toCharArray()));    //configuration.getString("saml.key.password"/*, System.getenv("SAMLPASSWORD")*/).toCharArray()));
        keyPair = new KeyPair(entry.getCertificate().getPublicKey(),
                entry.getPrivateKey());

        KeyInfoFactory kFactory = factory.getKeyInfoFactory();
        keyInfo = kFactory.newKeyInfo(Collections.singletonList(kFactory.newX509Data(Collections.singletonList(entry.getCertificate()))));
    }

    /**
     * Adds an enveloped signature to the given element. Then moves the
     * signature element so that it is in the correct position according to the
     * SAML assertion and protocol schema: it must immediately follow any Issuer
     * and precede everything else.
     */
    public void signSAMLObject(Element target)
            throws GeneralSecurityException, XMLSignatureException, MarshalException {
        Reference ref = factory.newReference("#" + target.getAttribute("ID"),
                factory.newDigestMethod(DigestMethod.SHA1, null),
                Collections.singletonList(factory.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null)),
                null,
                null);

        SignedInfo signedInfo = factory.newSignedInfo(factory.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE_WITH_COMMENTS,
                (C14NMethodParameterSpec) null),
                factory.newSignatureMethod(SignatureMethod.RSA_SHA1, null),
                Collections.singletonList(ref));

        XMLSignature signature = factory.newXMLSignature(signedInfo, keyInfo);
        DOMSignContext signContext = new DOMSignContext(keyPair.getPrivate(), target);
        signature.sign(signContext);

        // For the result to be schema-valid, we have to move the signature
        // element from its place at the end of the child list to live
        // between Issuer and Subject elements.  So, deep breath, and:
        Node signatureElement = target.getLastChild();

        boolean foundIssuer = false;
        Node elementAfterIssuer = null;
        NodeList children = target.getChildNodes();
        for (int c = 0; c < children.getLength(); ++c) {
            Node child = children.item(c);

            if (foundIssuer) {
                elementAfterIssuer = child;
                break;
            }

            if (child.getNodeType() == Node.ELEMENT_NODE
                    && child.getLocalName().equals("Issuer")) {
                foundIssuer = true;
            }
        }

        // Place after the Issuer, or as first element if no Issuer:
        if (!foundIssuer || elementAfterIssuer != null) {
            target.removeChild(signatureElement);
            target.insertBefore(signatureElement,
                    foundIssuer
                    ? elementAfterIssuer
                    : target.getFirstChild());
        }
    }
}
