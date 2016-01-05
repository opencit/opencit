/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.v2.vm.attestation.rpc;

import com.intel.mtwilson.launcher.ws.ext.V2;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import com.intel.mtwilson.My;
import javax.ws.rs.Consumes;
import com.intel.dcsg.cpg.xml.JAXB;
import com.intel.mtwilson.trustpolicy.xml.TrustPolicy;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBException;
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
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author jbuhacoff
 */
@V2
@Path("/trustpolicy-signature")
public class TrustPolicySignature {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrustPolicySignature.class);
    
    @POST
    @Consumes({MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_XML})
    @RequiresPermissions("trust_policies:certify")
    public String signImageTrustPolicy(String xml) {
        try {
        JAXB jaxb = new JAXB();
        String validatedXml = jaxb.write(jaxb.read(xml, TrustPolicy.class));
        String signedXml = generateDsig(validatedXml);
        return signedXml;
        }
        catch(IOException | JAXBException | XMLStreamException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | KeyStoreException | UnrecoverableEntryException | CertificateException | ParserConfigurationException | SAXException | MarshalException | XMLSignatureException | TransformerException e) {
            log.error("Cannot sign trust policy", e);
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }
    }

   public String generateDsig(String xml, PrivateKey privateKey, X509Certificate certificate) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, KeyStoreException, UnrecoverableEntryException, IOException, CertificateException, ParserConfigurationException, SAXException, MarshalException, XMLSignatureException, TransformerConfigurationException, TransformerException{
        StringWriter result;
        // Create a DOM XMLSignatureFactory that will be used to
        // generate the enveloped signature.
        XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
        Reference ref = fac.newReference
         ("", fac.newDigestMethod(DigestMethod.SHA1, null),
          Collections.singletonList
           (fac.newTransform
            (Transform.ENVELOPED, (TransformParameterSpec) null)),
             null, null);

        // Create the SignedInfo.
        SignedInfo si = fac.newSignedInfo
         (fac.newCanonicalizationMethod
          (CanonicalizationMethod.INCLUSIVE,
           (C14NMethodParameterSpec) null),
            fac.newSignatureMethod(SignatureMethod.RSA_SHA1, null),
             Collections.singletonList(ref));
       
           // Create the KeyInfo containing the X509Data.
           KeyInfoFactory kif = fac.getKeyInfoFactory();
           List x509Content = new ArrayList();
           x509Content.add(certificate.getSubjectX500Principal().getName());
           x509Content.add(certificate);
           X509Data xd = kif.newX509Data(x509Content);
           KeyInfo ki = kif.newKeyInfo(Collections.singletonList(xd));

           //3. Instantiate the document to be signed.
           DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
           dbf.setNamespaceAware(true);
           Document doc = dbf.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));

        // Create a DOMSignContext and specify the RSA PrivateKey and
           // location of the resulting XMLSignature's parent element.
           DOMSignContext dsc = new DOMSignContext(privateKey, doc.getDocumentElement());

           // Create the XMLSignature, but don't sign it yet.
           XMLSignature signature = fac.newXMLSignature(si, ki);

           // Marshal, generate, and sign the enveloped signature.
           signature.sign(dsc);

           //4. Output the resulting document.
           result = new StringWriter();
           TransformerFactory tf = TransformerFactory.newInstance();
           Transformer trans = tf.newTransformer();
           trans.transform(new DOMSource(doc), new StreamResult(result));
           
           return result.toString();
   }
   
   public String generateDsig(String xml) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, KeyStoreException, UnrecoverableEntryException, IOException, CertificateException, ParserConfigurationException, SAXException, MarshalException, XMLSignatureException, TransformerConfigurationException, TransformerException{
                
        //2. Load the KeyStore and get the signing key and certificate.
        //TODO saml key and keystore password are same
        KeyStore ks = KeyStore.getInstance("JKS");
        try (FileInputStream fin = new FileInputStream(My.configuration().getSamlKeystoreFile())) {
           ks.load(fin, My.configuration().getSamlKeystorePassword().toCharArray());
           KeyStore.PrivateKeyEntry keyEntry
                   = (KeyStore.PrivateKeyEntry) ks.getEntry(My.configuration().getSamlKeyAlias(), new KeyStore.PasswordProtection(My.configuration().getSamlKeystorePassword().toCharArray()));
           X509Certificate cert = (X509Certificate) keyEntry.getCertificate();

           String signedXml = generateDsig(xml, keyEntry.getPrivateKey(), cert);
            log.debug("Signed TP is: {}", signedXml);
            return signedXml;
        }
   }
}
