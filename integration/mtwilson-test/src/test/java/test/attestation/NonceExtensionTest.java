/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.attestation;

import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.io.ByteArray;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.net.IPv4Address;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import org.apache.commons.codec.binary.Hex;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.xml.crypto.KeySelector;
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
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;


/**
 *
 * @author jbuhacoff
 */
public class NonceExtensionTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NonceExtensionTest.class);

    @Test
    public void testIdentityAwareChallenger() {
        byte[] nonce = RandomUtil.randomByteArray(16);
        assertEquals(16, nonce.length);
        log.debug("nonce: {}", Hex.encodeHexString(nonce));
        byte[] ipv4 = new IPv4Address("192.168.1.100").toByteArray();
        assertEquals(4, ipv4.length);
        log.debug("ipv4: {}", Hex.encodeHexString(ipv4));
        byte[] extended = Sha1Digest.digestOf(nonce).extend(ipv4).toByteArray();
        assertEquals(20, extended.length);
        log.debug("extended nonce: {}", Hex.encodeHexString(extended));
    }
    
    @Test
    public void testPrivacyGuardedChallenger() {
        byte[] challengerNonce = RandomUtil.randomByteArray(16);
        assertEquals(16, challengerNonce.length);
        log.debug("challenger nonce: {}", Hex.encodeHexString(challengerNonce));
        
        // now challenger submits the nonce to mtwilson and mtwilson will calculate the extended nonce and provide it to the challenger
        byte[] nonce = RandomUtil.randomByteArray(16);
        assertEquals(16, nonce.length);
        log.debug("nonce: {}", Hex.encodeHexString(nonce));
        byte[] ipv4 = new IPv4Address("192.168.1.100").toByteArray();
        assertEquals(4, ipv4.length);
        log.debug("ipv4: {}", Hex.encodeHexString(ipv4));
        byte[] extended = Sha1Digest.digestOf(nonce).extend(ipv4).extend(challengerNonce).toByteArray();
        assertEquals(20, extended.length);
        log.debug("extended nonce: {}", Hex.encodeHexString(extended));
        
        // mtwilson reports to the challenger only the last intermediate nonce:
        byte[] attestationServiceNonce = Sha1Digest.digestOf(nonce).extend(ipv4).toByteArray();
        log.debug("attestation service nonce: {}", Hex.encodeHexString(attestationServiceNonce));
        // challenger can verify the attestation service nonce combined with challenger's own nonce is what the host used when signing:
        byte[] verifyExtended = Sha1Digest.valueOf(attestationServiceNonce).extend(challengerNonce).toByteArray();
        assertArrayEquals(extended, verifyExtended);
        log.debug("verify extended nonce: {}", Hex.encodeHexString(verifyExtended));
        byte[] verifyExtended2 = Sha1Digest.digestOf(ByteArray.concat(attestationServiceNonce,challengerNonce)).toByteArray();
        log.debug("verify extended nonce 2 : {}", Hex.encodeHexString(verifyExtended2));
    }
    
    @Test
    public void testGetPrivacyGuardedHostId() {
        UUID hostId = new UUID();
        log.debug("host id (not hardware uuid or ip address): {}", hostId.toString());
    }
    
   @Test
    public void testVirtualPcrExtension() { 
        Sha1Digest pcr = Sha1Digest.ZERO;
        // repeat for each measurement
        pcr = pcr.extend(Sha1Digest.valueOfHex("31f9e039d77aa9e220828d439c8254d51748bc77"));
        // final value:
        log.debug("pcr value = {}", pcr.toHexString());
    }
//SignedInfo si;
//    private XMLSignatureFactory createXmlSigFac() throws Exception{
//        XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
//        Reference ref = fac.newReference
//         ("", fac.newDigestMethod(DigestMethod.SHA1, null),
//          Collections.singletonList
//           (fac.newTransform
//            (Transform.ENVELOPED, (TransformParameterSpec) null)),
//             null, null);
//
//        // Create the SignedInfo.
//         si = fac.newSignedInfo
//         (fac.newCanonicalizationMethod
//          (CanonicalizationMethod.INCLUSIVE,
//           (C14NMethodParameterSpec) null),
//            fac.newSignatureMethod(SignatureMethod.RSA_SHA1, null),
//             Collections.singletonList(ref));
//        
//        return fac;
//    }
    @Test
    public void generateDsig() throws Exception, NoSuchAlgorithmException, InvalidAlgorithmParameterException, KeyStoreException, UnrecoverableEntryException, IOException, CertificateException{
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
        
//        XMLSignatureFactory fac = createXmlSigFac();
        
        //2. Load the KeyStore and get the signing key and certificate.
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream("C:\\Users\\boskisha\\Downloads\\SAML.jks"), "xEhsm6BXDJ42GRLG".toCharArray());
        KeyStore.PrivateKeyEntry keyEntry =
            (KeyStore.PrivateKeyEntry) ks.getEntry
                ("samlkey1", new KeyStore.PasswordProtection("xEhsm6BXDJ42GRLG".toCharArray()));
        X509Certificate cert = (X509Certificate) keyEntry.getCertificate();

        // Create the KeyInfo containing the X509Data.
        KeyInfoFactory kif = fac.getKeyInfoFactory();
        List x509Content = new ArrayList();
        x509Content.add(cert.getSubjectX500Principal().getName());
        x509Content.add(cert);
        X509Data xd = kif.newX509Data(x509Content);
        KeyInfo ki = kif.newKeyInfo(Collections.singletonList(xd));
        
        //3. Instantiate the document to be signed.
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document doc = dbf.newDocumentBuilder().parse(new InputSource( new StringReader("<test>hi</test>")));

        // Create a DOMSignContext and specify the RSA PrivateKey and
        // location of the resulting XMLSignature's parent element.
        DOMSignContext dsc = new DOMSignContext
            (keyEntry.getPrivateKey(), doc.getDocumentElement());

        // Create the XMLSignature, but don't sign it yet.
        XMLSignature signature = fac.newXMLSignature(si, ki);

        // Marshal, generate, and sign the enveloped signature.
        signature.sign(dsc);
        
        //4. Output the resulting document.
        StringWriter writter = new StringWriter();
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer trans = tf.newTransformer();
        trans.transform(new DOMSource(doc), new StreamResult(writter));
        System.out.println(writter.toString());
        
    
    
//    @Test
//    public void validateDsig(){
//        try{
//        XMLSignatureFactory fac = createXmlSigFac();
//        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//        Document doc = dbf.newDocumentBuilder().parse(new FileInputStream("C:\\Users\\boskisha\\Downloads\\po.xml"));
        
            
        //5. Find Signature element.
//            System.out.println("*********************************"+doc.toString());
//        NodeList nl =
//            doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
//        if (nl.getLength() == 0) {
//            throw new Exception("Cannot find Signature element");
//        }
//
//        // Create a DOMValidateContext and specify a KeySelector
//        // and document context.
//        DOMValidateContext valContext = new DOMValidateContext
//            (new X509KeySelector(), nl.item(0));
//
//        // Unmarshal the XMLSignature.
//        signature = fac.unmarshalXMLSignature(valContext);
//
//        // Validate the XMLSignature.
//        boolean coreValidity = signature.validate(valContext);
//        
//        
//        //6. Check core validation status.
//        if (coreValidity == false) {
//            System.err.println("Signature failed core validation");
//            boolean sv = signature.getSignatureValue().validate(valContext);
//            System.out.println("signature validation status: " + sv);
//            if (sv == false) {
//                // Check the validation status of each Reference.
//                Iterator i = signature.getSignedInfo().getReferences().iterator();
//                for (int j=0; i.hasNext(); j++) {
//                    boolean refValid = ((Reference) i.next()).validate(valContext);
//                    System.out.println("ref["+j+"] validity status: " + refValid);
//                }
//            }
//        } else {
//            System.out.println("Signature passed core validation");
//        }
//        
        
//        valContext.setProperty("javax.xml.crypto.dsig.cacheReference", Boolean.TRUE);
//        // Unmarshal the XMLSignature.
//        XMLSignature signature = fac.unmarshalXMLSignature(valContext);
//        // Validate the XMLSignature.
//        boolean coreValidity = signature.validate(valContext);
//
//        Iterator i = signature.getSignedInfo().getReferences().iterator();
//        for (int j=0; i.hasNext(); j++) {
//            InputStream is = ((Reference) i.next()).getDigestInputStream();
//            // Display the data.
//        }
//    }
//    catch(Exception e){
//
//    }
    }
}

