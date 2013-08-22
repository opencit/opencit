/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.x509;

import com.intel.mtwilson.atag.OID;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.x509.X509Builder;
import com.intel.dcsg.cpg.x509.X509Util;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import javax.security.auth.x500.X500Principal;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.Attribute;
import org.bouncycastle.asn1.x509.DisplayText;
import org.bouncycastle.cert.AttributeCertificateHolder;
import org.bouncycastle.cert.AttributeCertificateIssuer;
import org.bouncycastle.cert.X509AttributeCertificateHolder;
import org.bouncycastle.jce.provider.BouncyCastleProvider; // from bcprov
import org.bouncycastle.cert.X509v2AttributeCertificateBuilder; // from bcmail, not bcprov!
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * See also: RFC 3281
 *
 * @author jbuhacoff
 */
public class AttrCertificateTest {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final ObjectMapper mapper = new ObjectMapper(); // or from  org.fasterxml.jackson.databind.ObjectMapper
    // the Intel root OID 2.16.840.1.113741  is joint-iso-itu-t(2) country(16), US(840), organization(1), intel(113741)  see also http://oid-info.com/get/2.16.840.1.113741
    // under this we are INVENTING dcsg(xx) cpg(xx) asset tagging (xx) 
    // and under this we are INVENTING the following attributes:
    public final static String OID_INTEL = "2.16.840.1.113741";
    public final static String OID_DCSG_CPG = OID_INTEL + ".72833.2555";
    public final static String OID_ASSET_TAG_SOLUTION = OID_DCSG_CPG + ".3";  // with mt wilson = 1 and mystery hill = 2
    public final static String OID_ASSET_TAG_HOST_UUID = OID.HOST_UUID; // same as "2.25"; // see http://oid-info.com/get/2.25  for standard OID for UUIDs // OID_ASSET_TAG_SOLUTION + ".1";
    public final static String OID_CUSTOMER_ROOT = "1.3.6.1.4.1.99999"; // instead of OID_ASSET_TAG_SOLUTION + ".9"; // http://oid-info.com/get/1.3.6.1.4.1  is private organizations on internet, 999999 is INVENTED value that is not curently registered , for use in our demonstrations

    @BeforeClass
    public static void addBouncyCastleProvider() {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * References: http://www.docjar.org/html/api/org/bouncycastle/x509/examples/AttrCertExample.java.html
     */
    @Test
    public void testCreateAttrCert() throws Exception {
        // first, create the CA key pair and certificate
        KeyPair cakey = RsaUtil.generateRsaKeyPair(2048);
        X509Certificate cacert = X509Builder.factory().selfSigned("CN=Attr CA,OU=CPG,OU=DCSG,O=Intel,ST=CA,C=US", cakey).build();
        log.debug("ca: {}", X509Util.encodePemCertificate(cacert));
        AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find("SHA256withRSA"); // works with SHA1withRSA and  SHA256withRSA
        AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);
        ContentSigner authority = new BcRSAContentSignerBuilder(sigAlgId, digAlgId).build(PrivateKeyFactory.createKey(cakey.getPrivate().getEncoded())); // create a bouncy castle content signer convert using our existing private key
        // second, prepare the attribute certificate
        UUID uuid = new UUID();
        log.debug("host uuid: {}", uuid); // example: 33766a63-5c55-4461-8a84-5936577df450
        AttributeCertificateHolder holder = createHolder(uuid);
        AttributeCertificateIssuer issuer = createIssuer(cacert.getSubjectX500Principal());
        BigInteger serialNumber = new BigInteger("1");
        Calendar notBefore = Calendar.getInstance();
        Calendar notAfter = Calendar.getInstance();
        notAfter.add(Calendar.MONTH, 1);
        X509v2AttributeCertificateBuilder builder = new X509v2AttributeCertificateBuilder(holder, issuer, serialNumber, notBefore.getTime(), notAfter.getTime());
        // example of customer-defined location tags,  
        builder.addAttribute(new ASN1ObjectIdentifier(OID_CUSTOMER_ROOT + ".1.1.1.1"), new DERUTF8String("US")); // a country tag
        builder.addAttribute(new ASN1ObjectIdentifier(OID_CUSTOMER_ROOT + ".2.2.2.2"), new DERUTF8String("CA")); // a state tag
        builder.addAttribute(new ASN1ObjectIdentifier(OID_CUSTOMER_ROOT + ".3.3.3.3"), new DERUTF8String("Folsom")); // a city tag
        // third, sign the attribute certificate
        X509AttributeCertificateHolder cert = builder.build(authority);
        log.debug("cert: {}", Base64.encodeBase64String(cert.getEncoded())); // MIICGDCCAQACAQEwH6EdpBswGTEXMBUGAWkEEJKnGiKMF0UioYv9PtPQCzmgXzBdpFswWTEQMA4GA1UEAwwHQXR0ciBDQTEMMAoGA1UECwwDQ1BHMQ0wCwYDVQQLDAREQ1NHMQ4wDAYDVQQKDAVJbnRlbDELMAkGA1UECAwCQ0ExCzAJBgNVBAYTAlVTMA0GCSqGSIb3DQEBBQUAAgEBMCIYDzIwMTMwODA4MjIyMTEzWhgPMjAxMzA5MDgyMjIxMTNaMEMwEwYLKwYBBAG9hDcBAQExBAwCVVMwEwYLKwYBBAG9hDgCAgIxBAwCQ0EwFwYLKwYBBAG9hDkDAwMxCAwGRm9sc29tMA0GCSqGSIb3DQEBBQUAA4IBAQCcN8KjjmR2H3LT5aL1SCFS4joy/7vAd3/xdJtkqrb3UAQHMdUUJQHf3frJsMJs22m0So0xs/f1sB15frC1LsQGF5+RYVXsClv0glStWbPYiqEfdM7dc/RDMRtrXKEH3sBlxMT7YS/g5E6qwmKZX9shQ3BYmeZi5A3DTzgHCbA3Cm4/MQbgWGjoamfWZ9EDk4Bww2y0ueRi60PfoLg43rcijr8Wf+JEzCRw040vIaH3DtFdmzvvGRdqE3YlEkrUL3gEIZNY3Po1NL4cb238vT5CHZTt9NyD7xSv0XkwOY4RbSUdYBsxfH3mEcdQ6LtJdfF1BUXfMThKN3TctFcY/dLF
    }

    /**
     * Example output:
2013-08-08 15:46:03,908 DEBUG [main] t.x.AttrCertificateTest [AttrCertificateTest.java:108] issuer: CN=Attr CA,OU=CPG,OU=DCSG,O=Intel,ST=CA,C=US
2013-08-08 15:46:03,922 DEBUG [main] t.x.AttrCertificateTest [AttrCertificateTest.java:109] serial number: 1
2013-08-08 15:46:03,924 DEBUG [main] t.x.AttrCertificateTest [AttrCertificateTest.java:110] holder: 2.25=#041033766a635c5544618a845936577df450
2013-08-08 15:46:03,925 DEBUG [main] t.x.AttrCertificateTest [AttrCertificateTest.java:112] holder has 1 entity names
2013-08-08 15:46:03,925 DEBUG [main] t.x.AttrCertificateTest [AttrCertificateTest.java:114] holder entity name has 1 rdns
2013-08-08 15:46:03,926 DEBUG [main] t.x.AttrCertificateTest [AttrCertificateTest.java:116] entity rdn is multivalued? false
2013-08-08 15:46:03,930 DEBUG [main] t.x.AttrCertificateTest [AttrCertificateTest.java:120] holder uuid: 33766a63-5c55-4461-8a84-5936577df450
2013-08-08 15:46:03,948 DEBUG [main] t.x.AttrCertificateTest [AttrCertificateTest.java:124] not before: Thu Aug 08 15:44:27 PDT 2013
2013-08-08 15:46:03,950 DEBUG [main] t.x.AttrCertificateTest [AttrCertificateTest.java:125] not after: Sun Sep 08 15:44:27 PDT 2013
2013-08-08 15:46:03,953 DEBUG [main] t.x.AttrCertificateTest [AttrCertificateTest.java:130] attribute: 1.3.6.1.4.1.99999.1.1.1.1 is US
2013-08-08 15:46:03,954 DEBUG [main] t.x.AttrCertificateTest [AttrCertificateTest.java:130] attribute: 1.3.6.1.4.1.99999.2.2.2.2 is CA
2013-08-08 15:46:03,955 DEBUG [main] t.x.AttrCertificateTest [AttrCertificateTest.java:130] attribute: 1.3.6.1.4.1.99999.3.3.3.3 is Folsom
     * 
     * @throws IOException 
     */
    @Test
    public void readAttrCertificate() throws IOException {
        String input = "MIICGzCCAQMCAQEwH6EdpBswGTEXMBUGAWkEEDN2amNcVURhioRZNld99FCgXzBdpFswWTEQMA4GA1UEAwwHQXR0ciBDQTEMMAoGA1UECwwDQ1BHMQ0wCwYDVQQLDAREQ1NHMQ4wDAYDVQQKDAVJbnRlbDELMAkGA1UECAwCQ0ExCzAJBgNVBAYTAlVTMA0GCSqGSIb3DQEBBQUAAgEBMCIYDzIwMTMwODA4MjI0NDI3WhgPMjAxMzA5MDgyMjQ0MjdaMEYwFAYMKwYBBAGGjR8BAQEBMQQMAlVTMBQGDCsGAQQBho0fAgICAjEEDAJDQTAYBgwrBgEEAYaNHwMDAwMxCAwGRm9sc29tMA0GCSqGSIb3DQEBBQUAA4IBAQAIPESDSy8TLMzlhO3kpOSUU2Y063qS4iaSHEaX4CDG1hMD/VMcu0MKIaZnr83RzHIv1Z+01s8HDbW5IMwU3rOE99sR1e4DmP0a4hh3GLL38Rta6FkxSt8vL2ie7irK4BWCgWZd3Oc1xeCyLZ7uK6jerw+Qt6zzMRy74z6+5k2jLsveF1XqJvdTQZZYeyeSLFBYc74akWGGYJ29eB6y8dKp/UWJ5VU21NldfpW5hBap2v1wQpUih7+CcRIZ7fvZaZbONEBU+UyYbT8OASJkvxmLB5eKLTXftz5gQkCPyR8oKacc5n0alU/DMWkKOOQUc2VzIAkJMR7DLwD1fnb+msnx";
        X509AttributeCertificateHolder cert = new X509AttributeCertificateHolder(Base64.decodeBase64(input));
        log.debug("issuer: {}", StringUtils.join(cert.getIssuer().getNames(), ", "));  // calls toString() on each X500Name so we get the default representation; we can do it ourselves for custom display;  output example: CN=Attr CA,OU=CPG,OU=DCSG,O=Intel,ST=CA,C=US
        log.debug("serial number: {}", cert.getSerialNumber().toString()); // output example:   1
        log.debug("holder: {}", StringUtils.join(cert.getHolder().getEntityNames(), ", ")); // output example:  2.25=#041092a71a228c174522a18bfd3ed3d00b39
        // now let's get the UUID specifically out of this
        log.debug("holder has {} entity names", cert.getHolder().getEntityNames().length);
        for(X500Name entityName : cert.getHolder().getEntityNames()) {
            log.debug("holder entity name has {} rdns", entityName.getRDNs().length);
            for(RDN rdn : entityName.getRDNs()) {
                log.debug("entity rdn is multivalued? {}", rdn.isMultiValued());
                AttributeTypeAndValue attr = rdn.getFirst();
                if( attr.getType().toString().equals(OID_ASSET_TAG_HOST_UUID) ) {
                    UUID uuid = UUID.valueOf(DEROctetString.getInstance(attr.getValue()).getOctets());
                    log.debug("holder uuid: {}", uuid);
                }
            }
        }
        log.debug("not before: {}", cert.getNotBefore()); // output example: Thu Aug 08 15:21:13 PDT 2013
        log.debug("not after: {}", cert.getNotAfter()); // output example: Sun Sep 08 15:21:13 PDT 2013
        Attribute[] attributes = cert.getAttributes();
        for (Attribute attr : attributes) {
            for (ASN1Encodable value : attr.getAttributeValues()) {
                log.trace("encoded value: {}", Base64.encodeBase64String(value.getEncoded()));
                log.debug("attribute: {} is {}", attr.getAttrType().toString(), DERUTF8String.getInstance(value).getString()); // our values are just UTF-8 strings  but if you use new String(value.getEncoded())  you will get two extra spaces at the beginning of the string
                /*
                 * output examples:
                 * attribute: 1.3.6.1.4.1.99999.1.1.1.1 is US
                 * attribute: 1.3.6.1.4.1.99999.2.2.2.2 is CA
                 * attribute: 1.3.6.1.4.1.99999.3.3.3.3 is Folsom
                 */
            }
        }
        
        // now show the signature algorithm
//        AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find("SHA256withRSA");
//        cert.getSignatureAlgorithm().getAlgorithm().
        log.debug("signature alg is sha1+rsa? {}", Arrays.equals(new DefaultSignatureAlgorithmIdentifierFinder().find("SHA1withRSA").getEncoded(), cert.getSignatureAlgorithm().getEncoded()));
        log.debug("signature alg is sha256+rsa? {}", Arrays.equals(new DefaultSignatureAlgorithmIdentifierFinder().find("SHA256withRSA").getEncoded(), cert.getSignatureAlgorithm().getEncoded()));
        log.debug("cert version: {}", cert.getVersion());
    }

    private AttributeCertificateHolder createHolder(UUID uuid) {
//        ASN1OctetString uuidText = new ASN1OctetString(uuid.toByteArray().getBytes());  // ASN1Encodable
        DEROctetString uuidText = new DEROctetString(uuid.toByteArray().getBytes());
        ASN1ObjectIdentifier oid = new ASN1ObjectIdentifier(OID_ASSET_TAG_HOST_UUID);
        AttributeTypeAndValue attr = new AttributeTypeAndValue(oid, uuidText);
        RDN rdn = new RDN(attr);
        X500Name name = new X500Name(new RDN[]{rdn});
        AttributeCertificateHolder holder = new AttributeCertificateHolder(name);
        return holder;
    }

    private AttributeCertificateIssuer createIssuer(X500Principal principal) {
        X500Name name = new X500Name(principal.getName()); // principal.getName() produces RFC 2253 output which we hope is compatible wtih X500Name directory name input
        AttributeCertificateIssuer issuer = new AttributeCertificateIssuer(name);
        return issuer;
    }

    @Test
    public void testInventOids() {
        // DCSG  = 72833
        long dcsg1 = /* D */ (4 * 26 * 26 * 26) + /* C */ (3 * 26 * 26) + /* S */ (19 * 26) + /* G */ (7);
        log.debug("DCSG OID: {}", dcsg1);
        // CPG = 2555
        long cpg1 = /* C */ (3 * 26 * 27) + /* P */ (17 * 26) + /* G */ (7);
        log.debug("CPG OID: {}", cpg1);
        // mtwilson=1, mystery hill=2, asset-tagging=3
        long at1 = 3;
        log.debug("Asset-tagging OID: {}", at1);

    }
}
