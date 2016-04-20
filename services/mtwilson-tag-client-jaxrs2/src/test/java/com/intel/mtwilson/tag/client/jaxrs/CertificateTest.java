/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.client.jaxrs;

import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.crypto.Sha256Digest;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.x509.X509Builder;
import com.intel.mtwilson.My;
import com.intel.mtwilson.tag.model.Certificate;
import com.intel.mtwilson.tag.model.CertificateCollection;
import com.intel.mtwilson.tag.model.CertificateFilterCriteria;
import com.intel.mtwilson.tag.model.X509AttributeCertificate;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.AttributeCertificate;
import org.bouncycastle.cert.AttributeCertificateHolder;
import org.bouncycastle.cert.AttributeCertificateIssuer;
import org.bouncycastle.cert.X509AttributeCertificateHolder;
import org.bouncycastle.cert.X509v2AttributeCertificateBuilder;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;
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
    public void certificateTest() throws NoSuchAlgorithmException, CertificateEncodingException, IOException, OperatorCreationException { 
        
        KeyPair keyPair = RsaUtil.generateRsaKeyPair(RsaUtil.MINIMUM_RSA_KEY_SIZE);
        AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find("SHA256withRSA");
        AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);
        ContentSigner authority = new BcRSAContentSignerBuilder(sigAlgId, digAlgId).build(PrivateKeyFactory.createKey(keyPair.getPrivate().getEncoded()));
        AttributeCertificateHolder holder = new AttributeCertificateHolder(new X500Name(new RDN[]{})); 
        AttributeCertificateIssuer issuer = new AttributeCertificateIssuer(new X500Name(new RDN[]{}));
        BigInteger serialNumber = new BigInteger(64, new SecureRandom());
        Date notBefore = new Date();
        Date notAfter = new Date(notBefore.getTime() + TimeUnit.MILLISECONDS.convert(365, TimeUnit.DAYS));
        X509v2AttributeCertificateBuilder builder = new X509v2AttributeCertificateBuilder(holder, issuer, serialNumber, notBefore, notAfter);
        X509AttributeCertificateHolder cert = builder.build(authority);
        log.debug("cert: {}", Base64.encodeBase64String(cert.getEncoded())); // MIICGDCCAQACAQEwH6EdpBswGTEXMBUGAWkEEJKnGiKMF0UioYv9PtPQCzmgXzBdpFswWTEQMA4GA1UEAwwHQXR0ciBDQTEMMAoGA1UECwwDQ1BHMQ0wCwYDVQQLDAREQ1NHMQ4wDAYDVQQKDAVJbnRlbDELMAkGA1UECAwCQ0ExCzAJBgNVBAYTAlVTMA0GCSqGSIb3DQEBBQUAAgEBMCIYDzIwMTMwODA4MjIyMTEzWhgPMjAxMzA5MDgyMjIxMTNaMEMwEwYLKwYBBAG9hDcBAQExBAwCVVMwEwYLKwYBBAG9hDgCAgIxBAwCQ0EwFwYLKwYBBAG9hDkDAwMxCAwGRm9sc29tMA0GCSqGSIb3DQEBBQUAA4IBAQCcN8KjjmR2H3LT5aL1SCFS4joy/7vAd3/xdJtkqrb3UAQHMdUUJQHf3frJsMJs22m0So0xs/f1sB15frC1LsQGF5+RYVXsClv0glStWbPYiqEfdM7dc/RDMRtrXKEH3sBlxMT7YS/g5E6qwmKZX9shQ3BYmeZi5A3DTzgHCbA3Cm4/MQbgWGjoamfWZ9EDk4Bww2y0ueRi60PfoLg43rcijr8Wf+JEzCRw040vIaH3DtFdmzvvGRdqE3YlEkrUL3gEIZNY3Po1NL4cb238vT5CHZTt9NyD7xSv0XkwOY4RbSUdYBsxfH3mEcdQ6LtJdfF1BUXfMThKN3TctFcY/dLF
                
        Certificate obj = new Certificate();
        obj.setCertificate(cert.getEncoded());
        obj = client.createCertificate(obj);
        
        Certificate retrieveCertificate = client.retrieveCertificate(UUID.valueOf("76cb8a0b-79b1-437a-9f0f-f8f6ad9b9df3"));
        log.debug(retrieveCertificate.getIssuer());
        
        CertificateFilterCriteria criteria = new CertificateFilterCriteria();
        criteria.subjectEqualTo = "064866ea-620d-11e0-b1a9-001e671043c4";
        CertificateCollection objCollection = client.searchCertificates(criteria);
        for (Certificate cObj : objCollection.getCertificates()) {
            X509AttributeCertificate attrCert = X509AttributeCertificate.valueOf(cObj.getCertificate());
            log.debug(attrCert.getIssuer() + "::" + attrCert.getSubject());
        }
        
        Certificate editObj = new Certificate();
        editObj.setId(UUID.valueOf("695e8d32-0dd8-46bb-90d6-d2520ff5e2f0"));
        editObj.setRevoked(false);
        editObj = client.editCertificate(editObj);
        
        
    }
         
}
