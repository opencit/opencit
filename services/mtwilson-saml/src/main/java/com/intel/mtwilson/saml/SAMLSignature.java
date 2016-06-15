package com.intel.mtwilson.saml;

import java.io.*;
import java.security.*;
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
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Will Provost (original author)
 * @author Jonathan Buhacoff (edits)
 *
 * Copyright 2009-2016 Will Provost, Capstone Courseware LLC.
 * http://capcourse.com/Library/OpenSAML
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy 
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights 
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell 
 * copies of the Software, and to permit persons to whom the Software is 
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE. 
 */
public class SAMLSignature {

    private final XMLSignatureFactory factory;
    private final IssuerConfiguration issuerConfiguration;
    private final KeyInfo keyInfo;

    /**
     * Loads a keystore and builds a stock key-info structure for use by base
     * classes.
     * @param issuerConfiguration with the private key, issuer certificate, and JSR105 provider
     * @throws java.lang.ReflectiveOperationException could be ClassNotFoundException, IllegalAccessException, or IntantiationException
     * @throws java.security.GeneralSecurityException could be KeyStoreException, NoSuchAlgorithmException, UnrecoverableEntryException, or CertificateException when accessing the private key and issuer certificate
     * @throws java.io.IOException when reading configuration files from disk
     */
    public SAMLSignature(IssuerConfiguration issuerConfiguration) throws ReflectiveOperationException, GeneralSecurityException, IOException {
        this.issuerConfiguration = issuerConfiguration;
        
        String providerName = issuerConfiguration.getJsr105Provider();
        factory = XMLSignatureFactory.getInstance("DOM", (Provider) Class.forName(providerName).newInstance());


        KeyInfoFactory kFactory = factory.getKeyInfoFactory();
        keyInfo = kFactory.newKeyInfo(Collections.singletonList(kFactory.newX509Data(Collections.singletonList(issuerConfiguration.getCertificate()))));
    }

    /**
     * Adds an enveloped signature to the given element. Then moves the
     * signature element so that it is in the correct position according to the
     * SAML assertion and protocol schema: it must immediately follow any Issuer
     * and precede everything else.
     * @param target the XML element to sign, generally the entire document
     * @throws java.security.GeneralSecurityException on failure to use the private key
     * @throws javax.xml.crypto.dsig.XMLSignatureException for invalid document structure
     * @throws javax.xml.crypto.MarshalException on failure to serialize the signature
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
        DOMSignContext signContext = new DOMSignContext(issuerConfiguration.getPrivateKey(), target);
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
