/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package test.privacyca;

import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.util.ResourceFinder;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class AikValidityTest {
    @Test
    public void testPrivacyCASelfSignedCertificate() throws CertificateException, IOException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
        // read privacy ca certificate
        InputStream privacyCaIn = new FileInputStream(new File("src/test/resources/PrivacyCA.2.crt")); // XXX TODO currently we only support one privacy CA cert... in the future we should read a PEM format file with possibly multiple trusted privacy ca certs
        X509Certificate privacyCaCert = X509Util.decodeDerCertificate(IOUtils.toByteArray(privacyCaIn));
        IOUtils.closeQuietly(privacyCaIn);
        privacyCaCert.checkValidity();
        // verify the trusted privacy ca signed this aik cert
        privacyCaCert.verify(privacyCaCert.getPublicKey()); // NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException
        
    }

    
    @Test
    public void testAikCertificateIsValid() throws CertificateException, IOException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
        InputStream hostAikIn = new FileInputStream(new File("src/test/resources/aikcert.167.crt.pem"));
        X509Certificate hostAikCert = X509Util.decodePemCertificate(IOUtils.toString(hostAikIn));
        IOUtils.closeQuietly(hostAikIn);
        hostAikCert.checkValidity();
        // read privacy ca certificate
        InputStream privacyCaIn = new FileInputStream(new File("src/test/resources/PrivacyCA.88-167.crt")); // XXX TODO currently we only support one privacy CA cert... in the future we should read a PEM format file with possibly multiple trusted privacy ca certs
        X509Certificate privacyCaCert = X509Util.decodeDerCertificate(IOUtils.toByteArray(privacyCaIn));
        IOUtils.closeQuietly(privacyCaIn);
        privacyCaCert.checkValidity();
        // verify the trusted privacy ca signed this aik cert
        hostAikCert.verify(privacyCaCert.getPublicKey()); // NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException        
    }
}
