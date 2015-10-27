/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.v2.vm.attestation.rpc;

import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.mtwilson.util.xml.dsig.XmlDsigVerify;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import static org.junit.Assert.*;
import org.xml.sax.SAXException;

/**
 *
 * @author jbuhacoff
 */
public class TrustPolicySignatureTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrustPolicySignatureTest.class);
    private static int keySizeInBits = 2048;
    private static int validityInDays = 365;
    private static String xmlpath = "/trustpolicy/trustpolicy_not_signed.xml";
    
    @Test
    public void testSignVerifyTrustPolicy() throws NoSuchAlgorithmException, CryptographyException, IOException, InvalidAlgorithmParameterException, KeyStoreException, UnrecoverableEntryException, CertificateException, ParserConfigurationException, SAXException, MarshalException, XMLSignatureException, TransformerConfigurationException, TransformerException {
        // generate rsa keypair
        KeyPair keypair = RsaUtil.generateRsaKeyPair(keySizeInBits);
        X509Certificate certificate = RsaUtil.generateX509Certificate("TrustDirector", keypair, validityInDays);
        // load & sign xml
        String xml = IOUtils.toString(getClass().getResourceAsStream(xmlpath), Charset.forName("UTF-8"));
        TrustPolicySignature signer = new TrustPolicySignature();
        String signedXml = signer.generateDsig(xml, keypair.getPrivate(), certificate);
        log.debug("signed xml: {}", signedXml);
        // verify xml signature
        boolean isValid = XmlDsigVerify.isValid(signedXml, certificate);
        log.debug("xml signature valid? {}", isValid);
        
    }
}
