/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.x509;

import com.intel.mtwilson.atag.model.OID;
import com.intel.mtwilson.atag.model.x509.*;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.validation.Fault;
import com.intel.dcsg.cpg.x509.X509Builder;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.atag.X509AttrBuilder;
import com.intel.mtwilson.atag.model.X509AttributeCertificate;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
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
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.jce.provider.BouncyCastleProvider; // from bcprov
import org.bouncycastle.cert.X509v2AttributeCertificateBuilder; // from bcmail, not bcprov!
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.operator.ContentVerifierProvider;
import org.bouncycastle.operator.bc.BcRSAContentVerifierProviderBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Enumeration;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
//import org.codehaus.jackson.map.ObjectMapper;
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
    public final static String OID_NAMEVALUE_UTF8 = "2.5.4.789.1";
    public final static String OID_NAMEVALUE_LIST_UTF8 = "2.5.4.789.2";
    
    @BeforeClass
    public static void addBouncyCastleProvider() {
        Security.addProvider(new BouncyCastleProvider());
    }

    ////////////////////////// THE FIRST SET OF TESTS IS FOR THE BUONCY CASTLE LIBRARY, FOR SAMPLE CODE LOOK TO SECOND SET OF TESTS  ///////////////////////
    
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
        // example of customer-defined location tags,  in the text microformat
        builder.addAttribute(new ASN1ObjectIdentifier(OID_NAMEVALUE_UTF8), new DERUTF8String("Country=US")); // a country tag
        builder.addAttribute(new ASN1ObjectIdentifier(OID_NAMEVALUE_UTF8), new DERUTF8String("State=CA")); // a state tag
        builder.addAttribute(new ASN1ObjectIdentifier(OID_NAMEVALUE_UTF8), new DERUTF8String("City=Folsom")); // a city tag
        // example of customer defined location tags in the ASN.1  format
        builder.addAttribute(new ASN1ObjectIdentifier(OID_NAMEVALUE_LIST_UTF8), new UTF8NameValueSequence("Country", "US"));
        builder.addAttribute(new ASN1ObjectIdentifier(OID_NAMEVALUE_LIST_UTF8), new UTF8NameValueSequence("State", "CA", "TX"));
        builder.addAttribute(new ASN1ObjectIdentifier(OID_NAMEVALUE_LIST_UTF8), new UTF8NameValueSequence("City", "Folsom", "El Paso"));
        
        // third, sign the attribute certificate
        X509AttributeCertificateHolder cert = builder.build(authority);
        log.debug("cert: {}", Base64.encodeBase64String(cert.getEncoded())); // MIICGDCCAQACAQEwH6EdpBswGTEXMBUGAWkEEJKnGiKMF0UioYv9PtPQCzmgXzBdpFswWTEQMA4GA1UEAwwHQXR0ciBDQTEMMAoGA1UECwwDQ1BHMQ0wCwYDVQQLDAREQ1NHMQ4wDAYDVQQKDAVJbnRlbDELMAkGA1UECAwCQ0ExCzAJBgNVBAYTAlVTMA0GCSqGSIb3DQEBBQUAAgEBMCIYDzIwMTMwODA4MjIyMTEzWhgPMjAxMzA5MDgyMjIxMTNaMEMwEwYLKwYBBAG9hDcBAQExBAwCVVMwEwYLKwYBBAG9hDgCAgIxBAwCQ0EwFwYLKwYBBAG9hDkDAwMxCAwGRm9sc29tMA0GCSqGSIb3DQEBBQUAA4IBAQCcN8KjjmR2H3LT5aL1SCFS4joy/7vAd3/xdJtkqrb3UAQHMdUUJQHf3frJsMJs22m0So0xs/f1sB15frC1LsQGF5+RYVXsClv0glStWbPYiqEfdM7dc/RDMRtrXKEH3sBlxMT7YS/g5E6qwmKZX9shQ3BYmeZi5A3DTzgHCbA3Cm4/MQbgWGjoamfWZ9EDk4Bww2y0ueRi60PfoLg43rcijr8Wf+JEzCRw040vIaH3DtFdmzvvGRdqE3YlEkrUL3gEIZNY3Po1NL4cb238vT5CHZTt9NyD7xSv0XkwOY4RbSUdYBsxfH3mEcdQ6LtJdfF1BUXfMThKN3TctFcY/dLF
        
        // fourth verify the signature
        ContentVerifierProvider verifierProvider = new BcRSAContentVerifierProviderBuilder(new DefaultDigestAlgorithmIdentifierFinder()).build(new X509CertificateHolder(cacert.getEncoded()));
        assertTrue("certificate signature must be valid", cert.isSignatureValid(verifierProvider));
        
        // negative test case for verifying certificate: cannot verify signature using wrong certificate:
        KeyPair cakeyOther = RsaUtil.generateRsaKeyPair(2048);
        X509Certificate cacertOther = X509Builder.factory().selfSigned("CN=Other Attr CA,OU=CPG,OU=DCSG,O=Intel,ST=CA,C=US", cakeyOther).build();
        ContentVerifierProvider verifierProviderFail = new BcRSAContentVerifierProviderBuilder(new DefaultDigestAlgorithmIdentifierFinder()).build(new X509CertificateHolder(cacertOther.getEncoded()));
        assertTrue("certificate signature must be valid", !cert.isSignatureValid(verifierProviderFail));
    }

    @Test
    public void testDER() throws IOException {
        // encode
        UTF8NameValueSequence a0 = new UTF8NameValueSequence("name", "value1");
        log.debug("der encoded: {}", Base64.encodeBase64String(a0.getEncoded())); // MBAMBG5hbWUwCAwGdmFsdWUx
        
        // decode
//        ASN1Object asn = ASN1Object.fromByteArray(Base64.decodeBase64("MBAMBG5hbWUwCAwGdmFsdWUx")); // works fine
//        ASN1Object asn = ASN1Object.fromByteArray(Base64.decodeBase64("MAgMADAEDAJDQQ=="));  // missing the name
        ASN1Object asn = ASN1Object.fromByteArray(Base64.decodeBase64("MBEMBVN0YXRlMAgMAkNBDAJUWA=="));  // missing the name
        ASN1Sequence seq = (ASN1Sequence)asn;
        UTF8NameValueSequence a1 = new UTF8NameValueSequence(seq);
        log.debug("name {}", a1.getName());
        log.debug("values {}", a1.getValues());
//        UTFNameValueSequence a1 = UTF8NameValueSequence.
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
//        String input = "MIICEzCB/AIBATAfoR2kGzAZMRcwFQYBaQQQNLzh4PDBQfi3MpF7mSIT/qBfMF2kWzBZMRAwDgYDVQQDDAdBdHRyIENBMQwwCgYDVQQLDANDUEcxDTALBgNVBAsMBERDU0cxDjAMBgNVBAoMBUludGVsMQswCQYDVQQIDAJDQTELMAkGA1UEBhMCVVMwDQYJKoZIhvcNAQELBQACBgFEt/32JjAiGA8yMDE0MDMxMjIwMzExOFoYDzIwMTQwMzEyMjEzMTE4WjA6MBoGBVUEhhUCMREwDwwHQ291bnRyeTAEDAJVUzAcBgVVBIYVAjETMBEMBVN0YXRlMAgMAkNBDAJUWDANBgkqhkiG9w0BAQsFAAOCAQEAchfMCn4/7FeMU00+Jm2HHSanqYobH5oQ1pND/eEcCH/8k2HQ/qtp54Mge4mNhiGBlC/5MLmNXGDfFWgxbIbyYxJjSHMWJf/Kzv5L/9aituYg1dwHJF5GcL5Ue0WOEbbW7sRQSEMfbLePxaxs/T0pXHphyPKaeFunu8YSx57tvhth6jWDw2/Wk2IQ03FV4W1Lb0tYSLtoGA5CsVvVPXqcIrtvKybKhiGeyeMEyS+9nZwCzmK3i5Y1Zof4HfQJb4MltlbQarAcrwfHKe4ZvYWom64awl5FiPz2IxvtXbx6VhGyso6+E4wo54EiDghLvYvX8AyYE3Z8oTJkSi2FTlKDeQ==";
//        String input = "MIICGTCCAQECAQEwH6EdpBswGTEXMBUGAWkEENP3IYWWPU+wgLZwdbugFQygXzBdpFswWTEQMA4GA1UEAwwHQXR0ciBDQTEMMAoGA1UECwwDQ1BHMQ0wCwYDVQQLDAREQ1NHMQ4wDAYDVQQKDAVJbnRlbDELMAkGA1UECAwCQ0ExCzAJBgNVBAYTAlVTMA0GCSqGSIb3DQEBCwUAAgEBMCIYDzIwMTMxMTEyMjE0MDAwWhgPMjAxMzEyMTIyMTQwMDBaMEQwFQYFVQSGFQExDAwKQ291bnRyeT1VUzATBgVVBIYVATEKDAhTdGF0ZT1DQTAWBgVVBIYVATENDAtDaXR5PUZvbHNvbTANBgkqhkiG9w0BAQsFAAOCAQEAY8PvE89R+qxymMSnyH6Tg19x0v6A7BqeliIEingP/NIKTp07McubBo1kJ8UsogmeSZ4UOtxA+0GIjfec6DPNyH/J9u+dLfEFi6EV8Qrigi56SJRGa3VuK4ElZSmyk5lwhORtAW2oISGp/z5prOiItN1JEv4X/FTrJ62OBqK0+nzduRo0fjYizPc1+bI0zPsevBRvvPjtSwKnjq4DLjs7i6/SQMT4Tkq7QF7JM4TJthCbepUimXWXb1QrH9VTs4fzFGSn2JVVCWw/g/aPyQaLCo+Z1VVf4odDCOkVd9sHEAL1cXGIhGmWKuLTPZbz+XYJlHb5qUdi9rBTqVqJ6LQAqQ==";
//        String input = "MIICPTCCASUCAQEwH6EdpBswGTEXMBUGAWkEENJtsZYGpEnmp+ShAWjjyMugOzA5pDcwNTERMA8GA1UEAwwIQXNzZXQgQ0ExEzARBgNVBAsMCkRhdGFjZW50ZXIxCzAJBgNVBAYTAlVTMA0GCSqGSIb3DQEBCwUAAggPnqFOJ5mWHDAiGA8yMDEzMTExMjIxNTAxN1oYDzIwMTMxMTE5MjE1MDE3WjCBhDARBgkrBgEEAYaNHwExBAwCVVMwEQYJKwYBBAGGjR8CMQQMAkNBMBUGCSsGAQQBho0fAzEIDAZGb2xzb20wGgYJKwYBBAGGjR8DMQ0MC1NhbnRhIENsYXJhMBMGCSsGAQQBho0fBDEGDARDb2tlMBQGCSsGAQQBho0fBDEHDAVQZXBzaTANBgkqhkiG9w0BAQsFAAOCAQEAuSRvK3Vd1HiNwWKqtuYRPPhS4g8GqIW/10aLYS+Rj/FnPS3VHYIOp79orvECxCOPILUvtsYWFFfq/Gl+YSOp6xKrZxBsjA6pIHLH7x5J5x+33rgbu/2o2sn/DAjfrS87WdPE26Bo0woSjDRJkw948XHRA82kG6SFecK3W0Hq9DEqm8JPUFBu+53t2xh8JvSXKWxAwmVjuAMWytxkAfMS5xTQBe0BliUAwGR/7SOZnWQOxLUHXkIkKiXirU4UJKJ2wih2xmX5aNLsC9aqseAivK0OKobOX72WGpmCQkYjO2SDvMmJH2pzxie6X+BSxqhaGEcgpNllToEtU93GPSGdrA==";
//        String input = "MIICejCCAWICAQEwH6EdpBswGTEXMBUGAWkEEJtmbrHLIUmviWSB09GTAACgXzBdpFswWTEQMA4GA1UEAwwHQXR0ciBDQTEMMAoGA1UECwwDQ1BHMQ0wCwYDVQQLDAREQ1NHMQ4wDAYDVQQKDAVJbnRlbDELMAkGA1UECAwCQ0ExCzAJBgNVBAYTAlVTMA0GCSqGSIb3DQEBCwUAAgEBMCIYDzIwMTQwMzA2MjExMTQ2WhgPMjAxNDA0MDYyMDExNDZaMIGkMBUGBVUEhhUBMQwMCkNvdW50cnk9VVMwEwYFVQSGFQExCgwIU3RhdGU9Q0EwFgYFVQSGFQExDQwLQ2l0eT1Gb2xzb20wGgYFVQSGFQIxETAPDAdDb3VudHJ5MAQMAlVTMBwGBVUEhhUCMRMwEQwFU3RhdGUwCAwCQ0EMAlRYMCQGBVUEhhUCMRswGQwEQ2l0eTARDAZGb2xzb20MB0VsIFBhc28wDQYJKoZIhvcNAQELBQADggEBAJmaQ99TykWSutAXNUwQ+oB+nZ+oYMFehuwbQ/fFDD/Z2wUKqdZflHVo94swOlm69s5xl/x+3hhiCdv8H63AOmjb626nmtOvp4hRb+YBx56F+uOGzb5zTbJwKc5LCEjAFXpHL71uxdQFf20+6rC7IhuKJZwLY89hb8Tk94vg0++9cecthRMWoSmp56rPZ44ZWyClNYHs2H20uO/8GR6t2FZX7L9aLg8xyXipdsCy/NmtxXflaHhTaNVWwGeYBoKcjKB4jsIKp/2cwLA0krGobBfGPUVyVc+On3sow17jFStAHqHQzJmNeFXy8NdTAqvAp6ZEfMkTdz3ccNiygXQxG90=";
//        String input = "MIICejCCAWICAQEwH6EdpBswGTEXMBUGAWkEECnguFrn9ktxowQgi70EeWegXzBdpFswWTEQMA4GA1UEAwwHQXR0ciBDQTEMMAoGA1UECwwDQ1BHMQ0wCwYDVQQLDAREQ1NHMQ4wDAYDVQQKDAVJbnRlbDELMAkGA1UECAwCQ0ExCzAJBgNVBAYTAlVTMA0GCSqGSIb3DQEBCwUAAgEBMCIYDzIwMTQwMzA2MjIyOTE4WhgPMjAxNDA0MDYyMTI5MThaMIGkMBUGBVUEhhUBMQwMCkNvdW50cnk9VVMwEwYFVQSGFQExCgwIU3RhdGU9Q0EwFgYFVQSGFQExDQwLQ2l0eT1Gb2xzb20wGgYFVQSGFQIxETAPDAdDb3VudHJ5MAQMAlVTMBwGBVUEhhUCMRMwEQwFU3RhdGUwCAwCQ0EMAlRYMCQGBVUEhhUCMRswGQwEQ2l0eTARDAZGb2xzb20MB0VsIFBhc28wDQYJKoZIhvcNAQELBQADggEBACeZdTYQdnimdsMfoN5Bk+qCNA2mYdFEpEKekyDhPP0B7+h0on1UHlcsTsNI/P4c4Co8r5lln3hVuWsYvcb2HFseSuREv6O3fNCY8a77OmQWRkH2AVMDXIDDgpEMdAq7ada4V/Fxeh/TILr4VMQf4TuFdn6QBTW7Hqgk9cP55njPv5BG0ukv677L3vODqY/u9KOkMNarx/qgHVcywefwi/zcC6wuG7BQ/6kOX933g31rTZSfI/+dFVS/x6CoWUkU+TbSLUE+RY2fAVkGNeWz5OLkRhKrCrJ91XDDQuXnJzRafKkk7MJqYtG+qk6U8qvVtUf9EjN0g/u3V0aqu1ddB1g=";
//        String input = "MIICNjCCAR4CAQEwH6EdpBswGTEXMBUGAWkEEHOlGZWNdeERvR0AHmc+dWOgIDAepBwwGjEYMBYGA1UEAwwPYXNzZXRUYWdTZXJ2aWNlMA0GCSqGSIb3DQEBCwUAAgYBRLfX2PQwIhgPMjAxNDAzMTIxOTQ5NDBaGA8yMDE1MDMxMjE5NDk0MFowgZowHAYFVQSGFQIxEzARDAAwDQwLLTEyMS4xNjM2MTkwHQYFVQSGFQExFAwSTGF0aXR1ZGU9MzguNjQ0Nzg1MBgGBVUEhhUCMQ8wDQwAMAkMB0ZpbmFuY2UwFAYFVQSGFQIxCzAJDAAwBQwDREMxMBMGBVUEhhUCMQowCAwAMAQMAkNBMBYGBVUEhhUBMQ0MC0NvdW50cnk9VVNBMA0GCSqGSIb3DQEBCwUAA4IBAQCx/KPjCqWewCz+7vn2Scm4IFRzcJSCTzSiYy9P+AjwAFBsdxfiq9t7hlIWVXcrvB83EZk7xFYG1tUsKRPq+cWSr74+sXYp1dtaURGWlohNpFW0rBuTdIyO+Fi4DMaYpDmN9Dznq3v8c303rYGVCyoUEBG1piA7DIEIuBjLAJ0IKJ31k9ymPvNlxl0/hWzCOtsL2WIqWHpEf1c4YE6r8oR3hwRMPz6Zu1cMUy7CnRaDctJjPBQUpDyNJfYC/EXPSAfHsa4SovsdrrvMbfZB63JwPEN0t6wSremUrdEjFWvlN9Z1ZsVBA0MflbeSTnDbH1QSL8KHHngF7DQF37whxTxD";
//        String input = "MIIB1DCBvQIBATAfoR2kGzAZMRcwFQYBaQQQc6UZlY114RG9HQAeZz51Y6AgMB6kHDAaMRgwFgYDVQQDDA9hc3NldFRhZ1NlcnZpY2UwDQYJKoZIhvcNAQELBQACBgFEuA7VWTAiGA8yMDE0MDMxMjIwNDk0M1oYDzIwMTUwMzEyMjA0OTQzWjA6MBkGBVUEhhUBMRAMDkNvdW50cnk9Q2FuYWRhMB0GBVUEhhUBMRQMEkxhdGl0dWRlPTU2LjU0MDY1MjANBgkqhkiG9w0BAQsFAAOCAQEActZ//ZkXtTDQc72i4AW3BqIa9n0vUzcC72lXqO0eFrawDUoMOSwf80F4T8YrGZr1q61QMTWv4GqlSsbKwuGyLcHWal/s9cZzr9oiE5sU60Mvg5eux939MKKr/bVXO3CkrsY7c1iaf+66zCXLNQ9pkAO67djo19sj7KWeDRR7uuuz1Fl+/oHpJEOEyhc6Uz7cTBckg/qL6zd7kavkKNSZyPLLPDNRN7lRdm75Gn8qsrIiqi1dmYXJCouMJSATZ+CuS7Vsop2ulS6Iso4cSlwWPH4f1pCmkxQDZx+YYtOREHIWhSOQWIibyffsgNs10kARYalYeFJxc7G+mPgrp0gIMw==";
        String input = "MIICLzCCARcCAQEwH6EdpBswGTEXMBUGAWkEEHOlGZWNdeERvR0AHmc+dWOgIDAepBwwGjEYMBYGA1UEAwwPYXNzZXRUYWdTZXJ2aWNlMA0GCSqGSIb3DQEBCwUAAgYBRLgdCXgwIhgPMjAxNDAzMTIyMTA1MTRaGA8yMDE1MDMxMjIxMDUxNFowgZMwGQYFVQSGFQExEAwOQ291bnRyeT1DYW5hZGEwGQYFVQSGFQExEAwORGF0YWNlbnRlcj1EQzUwGgYFVQSGFQExEQwPUmVnaW9uPUJlbmVmaXRzMB0GBVUEhhUBMRQMEkxhdGl0dWRlPTU2LjU0MDY1MjAgBgVVBIYVATEXDBVMb25naXR1ZGU9LTEyNi45NTExNTMwDQYJKoZIhvcNAQELBQADggEBAI/NocY0z4ibw4hYjr1Oxk2PBflor82pcRIJCkMWiooWrx9MXPCvvZEPk6I3YkwmZfsbBgcrCuhi4ZL5ie4SUQbjzT9PU7j1TWGgjjaRQBeb507BWIhKK1lDJt1PLgKo8ZBbTagdAebz5r6VZ3bstbSaIxgUfOo/jnGpRPq0ysaePBrICfb/8O6/LInrhMzOlkqwplzqJmveO+/BLaueg2RGEJ5Gco1eA2zDEPNcaiTEf7HLI2pLIcd517N8tWay9c5Nu3U8N+qZ/qNXGSWlqKTTJqydeR56mzQZ/rXlaJwABRqo9WkpfE+j+W8j3LaCvHaEcw/60ELLurxIxKeb9vY=";
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
//                log.trace("encoded value: {}", Base64.encodeBase64String(value.getEncoded()));
                if( attr.getAttrType().toString().equals(OID_NAMEVALUE_UTF8)) {
                    log.debug("name-value microformat attribute: {}",  DERUTF8String.getInstance(value).getString()); // our values are just UTF-8 strings  but if you use new String(value.getEncoded())  you will get two extra spaces at the beginning of the string                    
                    UTF8NameValueMicroformat microformat = new UTF8NameValueMicroformat(DERUTF8String.getInstance(value));
                    log.debug("name-value microformat attribute (3)  name {} value {}", microformat.getName(), microformat.getValue());
                }
                else if( attr.getAttrType().toString().equals(OID_NAMEVALUE_LIST_UTF8)) {
                    log.debug(".2  der = {}", Base64.encodeBase64String(value.getEncoded()));
//                    UTF8NameValueSequence sequence = new UTF8NameValueSequence(ASN1Sequence.getInstance(value));
                    UTF8NameValueSequence sequence = new UTF8NameValueSequence((ASN1Sequence)value);
                    String name = sequence.getName();
                    List<String> values = sequence.getValues();
                    log.debug("name-values asn.1 attribute {} values {}",  name, values); 
                }
                else {
                    log.debug("unrecognzied attribute type {}", attr.getAttrType().toString());
                }
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
    
    ////////////////////////// THE SECOND SET OF TESTS IS  SAMPLE CODE   ///////////////////////
    
    @Test
    public void createAttributeCertificate() throws NoSuchAlgorithmException {
        // first, create the CA key pair and certificate
        KeyPair cakey = RsaUtil.generateRsaKeyPair(2048);
        X509Certificate cacert = X509Builder.factory().selfSigned("CN=Attr CA,OU=CPG,OU=DCSG,O=Intel,ST=CA,C=US", cakey).build();
        // second, create the attribute certificate
        X509AttrBuilder builder = X509AttrBuilder.factory();
        byte[] attrCertBytes = builder
                .issuerName(cacert).issuerPrivateKey(cakey.getPrivate())
                .expires(1, TimeUnit.HOURS)
                .subjectUuid(new UUID())
                .attribute("Country", "US")
                .attribute("State", "CA", "TX")
                .build();
        if( attrCertBytes == null ) {
            List<Fault> faults = builder.getFaults();
            for(Fault fault : faults) {
                log.debug("Error: {}", fault.toString());
                if( fault.getCause() != null ) {
                    log.debug("Caused by:", fault.getCause());
                }
            }
        }
        else {
            X509AttributeCertificate attrCert = X509AttributeCertificate.valueOf(attrCertBytes);
            if( attrCert.isValid(cacert) ) {
                log.debug("Certificate is valid now");
            }
            Calendar future = Calendar.getInstance();
            future.add(Calendar.MINUTE, 5);
            if( attrCert.isValid(cacert, future.getTime()) ) {
                log.debug("Certificate is valid in 5 minutes");
            }        
            log.debug("cert serial: {}", attrCert.getSerialNumber().longValue() );
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(attrCert.getSerialNumber().longValue());
            log.debug("cert serial date: {}", c.getTime().toString());
        }
    }
    
}
