/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.client.jaxrs;

import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.crypto.Sha256Digest;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.tag.model.Certificate;
import com.intel.mtwilson.tag.model.CertificateCollection;
import com.intel.mtwilson.tag.model.CertificateFilterCriteria;
import java.security.cert.X509Certificate;
import java.util.Date;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ssbangal
 */
public class CertificateTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CertificateTest.class);

    private static Certificates client = null;
    
    @BeforeClass
    public static void init() throws Exception {
        client = new Certificates(My.configuration().getClientProperties());
    }
    
    @Test
    public void certificateTest() {
        
//        Certificate obj = new Certificate();
//        obj.setCertificate(certificate);
//        obj.setSha1(Sha1Digest.digestOf(certificate).toByteArray());
//        obj.setSha256(Sha256Digest.digestOf(certificate()).toByteArray());
//        obj.setSubject("064866ea-620d-11e0-b1a9-001e671043c4");
//        Date notBefore = new Date();
//        Date notAfter = new Date(notBefore.getTime() + (1000 * 60 * 60 * 24 * 365)); // one year
//        obj.setNotBefore(notBefore);
//        obj.setNotAfter(notAfter);
//        obj.setIssuer("CN=assetTagService");
//        obj = client.createCertificate(obj);
        Certificate retrieveCertificate = client.retrieveCertificate(UUID.valueOf("695e8d32-0dd8-46bb-90d6-d2520ff5e2f0"));
        log.debug(retrieveCertificate.getIssuer());
        
        CertificateFilterCriteria criteria = new CertificateFilterCriteria();
        criteria.subjectEqualTo = "064866ea-620d-11e0-b1a9-001e671043c4";
        CertificateCollection objCollection = client.searchCertificates(criteria);
        for (Certificate cObj : objCollection.getCertificates()) {
            X509Certificate x509Certificate = cObj.getX509Certificate();
            log.debug(cObj.getIssuer() + "::" + cObj.getX509Certificate().getSubjectX500Principal().getName());
        }
        
        Certificate editObj = new Certificate();
        editObj.setId(UUID.valueOf("695e8d32-0dd8-46bb-90d6-d2520ff5e2f0"));
        editObj.setRevoked(false);
        editObj = client.editCertificate(editObj);
        
        
    }
         
}
