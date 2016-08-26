/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tag.common;

import com.intel.mtwilson.tag.model.OID;
import com.intel.mtwilson.tag.model.x509.*;
import com.intel.dcsg.cpg.crypto.RsaCredentialX509;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.validation.BuilderModel;
import java.io.IOException;
import org.bouncycastle.asn1.ASN1Encodable;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import javax.security.auth.x500.X500Principal;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This convenience class implements the Builder pattern in order to simplify creation of X509 Attribute Certificates.
 * Certificates created by this builder do NOT have a public key. They only have attributes about the subject/holder.
 *
 * NOTE: this is different than X509 Public Key Certificates which can be built using cpg-crypto X509Builder.
 *
 * You should create a new instance of this class for every certificate.
 *
 * This class requires Bouncy Castle
 * 
 * @since 0.1
 * @author jbuhacoff
 */
public class X509AttrBuilder extends BuilderModel {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private X500Name issuerName = null;
    private PrivateKey issuerPrivateKey = null;
    private BigInteger serialNumber = null;
    private X500Name subjectName = null;
    private Date notBefore = null;
    private Date notAfter = null;
    private ArrayList<Attribute> attributes = new ArrayList<>();

    public static class Attribute {
        public ASN1ObjectIdentifier oid;
        public ASN1Encodable value;
        public Attribute(ASN1ObjectIdentifier oid, ASN1Encodable value) {
            this.oid = oid;
            this.value = value;
        }
    }
    
    public X509AttrBuilder() {
    }

    /**
     * Supports fluent writing: X509AttrBuilder x509 = X509AttrBuilder.factory().subjectName(...).alternativeName(...);
     * if( x509.isValid() ) { X509Certificate cert = x509.build(); } // check for isValid() is optional, but you will
     * get null result from build if it's not valid
     *
     * @return
     */
    public static X509AttrBuilder factory() {
        return new X509AttrBuilder();
    }

    public X509AttrBuilder expires(long expiration, TimeUnit units) {
        notBefore = new Date();
        notAfter = new Date(notBefore.getTime() + TimeUnit.MILLISECONDS.convert(expiration, units));
        return this;
    }

    public X509AttrBuilder valid(Date from, Date to) {
        notBefore = from;
        notAfter = to;
        return this;
    }

    public X509AttrBuilder serialNumber(BigInteger number) {
        serialNumber = number;
        return this;
    }

    public X509AttrBuilder randomSerial() {
        serialNumber = new BigInteger(64, new SecureRandom());
        return this;
    }
    
    public X509AttrBuilder dateSerial() {
        serialNumber = new BigInteger( String.valueOf(Calendar.getInstance().getTimeInMillis()) );
        return this;
    }

    /*
     public X509AttrBuilder subjectName(sun.security.x509.X500Name subjectName) {
     return subjectName(subjectName.getRFC2253Name());
     }
     */
    public X509AttrBuilder subjectUuid(UUID uuid) {
        DEROctetString uuidText = new DEROctetString(uuid.toByteArray().getBytes());
        ASN1ObjectIdentifier oid = new ASN1ObjectIdentifier(OID.HOST_UUID);
        AttributeTypeAndValue attr = new AttributeTypeAndValue(oid, uuidText);
        RDN rdn = new RDN(attr);
        subjectName = new X500Name(new RDN[]{rdn});
        return this;
    }

    /**
     *
     * @param dn like "CN=Dave, OU=JavaSoft, O=Sun Microsystems, C=US"
     * @return
     */
    /*
     public X509AttrBuilder subjectName(String dn) {
     try {
     }
     catch(Exception e) {
     fault(e, "subjectName(%s)", dn);
     }
     return this;        
     }
     */
    public X509AttrBuilder issuerName(X500Principal principal) {
        issuerName = new X500Name(principal.getName()); // principal.getName() produces RFC 2253 output which we hope is compatible wtih X500Name directory name input
        return this;
    }

    public X509AttrBuilder issuerName(X500Name issuerName) {
        this.issuerName = issuerName;
        return this;
    }

    public X509AttrBuilder issuerName(X509Certificate issuerCertificate) {
        return issuerName(issuerCertificate.getSubjectX500Principal());
    }

    /**
     * Sets the issuerPrivateKey and issuerName using the provided credential.
     *
     * @param issuerCredential
     * @return
     */
    public X509AttrBuilder issuer(RsaCredentialX509 issuerCredential) {
        try {
            issuerPrivateKey(issuerCredential.getPrivateKey());
            issuerName(issuerCredential.getCertificate());
        } catch (Exception e) {
            fault(e, "issuer(%s)", issuerCredential == null ? "null" : issuerCredential.getCertificate().getIssuerX500Principal().getName());
        }
        return this;
    }

    public X509AttrBuilder issuerPrivateKey(PrivateKey privateKey) {
        this.issuerPrivateKey = privateKey;
        return this;
    }

    public X509AttrBuilder attribute(String name, String textValue) {
        attributes.add(new Attribute(new ASN1ObjectIdentifier(UTF8NameValueSequence.OID), new UTF8NameValueSequence(name, textValue)));
        return this;
    }
    
    public X509AttrBuilder attribute(String name, String... textValues) {
        attributes.add(new Attribute(new ASN1ObjectIdentifier(UTF8NameValueSequence.OID), new UTF8NameValueSequence(name, textValues)));
        return this;
    }

    public X509AttrBuilder attribute(ASN1ObjectIdentifier oid, ASN1Encodable value) {
        attributes.add(new Attribute(oid, value));
        return this;
    }
    
    public X509AttrBuilder attribute(Attribute attribute) {
        attributes.add(attribute);
        return this;
    }

    public byte[] build() {
        if (notBefore == null || notAfter == null) {
            expires(1, TimeUnit.DAYS); // 1 day default
        }
        if (serialNumber == null) {
            dateSerial();
        }
        if (subjectName == null) {
            fault("Subject name is missing");
        }
        if (issuerName == null) {
            fault("Issuer name is missing");
        }
        if (issuerPrivateKey == null) {
            fault("Issuer private key is missing");
        }
        if (attributes.isEmpty()) {
            fault("No attributes selected");
        }
        try {
            if (getFaults().isEmpty()) {
                AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find("SHA256withRSA");
                AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);
                if (issuerPrivateKey == null) {
                    return null;
                }
                ContentSigner authority = new BcRSAContentSignerBuilder(sigAlgId, digAlgId).build(PrivateKeyFactory.createKey(issuerPrivateKey.getEncoded())); // create a bouncy castle content signer convert using our existing private key
                // second, prepare the attribute certificate
                AttributeCertificateHolder holder = new AttributeCertificateHolder(subjectName); // which is expected to be a UUID  like this: 33766a63-5c55-4461-8a84-5936577df450
                AttributeCertificateIssuer issuer = new AttributeCertificateIssuer(issuerName);
                X509v2AttributeCertificateBuilder builder = new X509v2AttributeCertificateBuilder(holder, issuer, serialNumber, notBefore, notAfter);
                for (Attribute attribute : attributes) {
                    builder.addAttribute(attribute.oid, attribute.value);
                }
                // third, add extensions - information regarding the certificate itself which is not an attribute of the subject
//                builder.addExtension(oid, /*critical*/true, /*asn1encodable*/)
                // fourth, sign the attribute certificate
                X509AttributeCertificateHolder cert = builder.build(authority);
                log.debug("cert: {}", Base64.encodeBase64String(cert.getEncoded())); // MIICGDCCAQACAQEwH6EdpBswGTEXMBUGAWkEEJKnGiKMF0UioYv9PtPQCzmgXzBdpFswWTEQMA4GA1UEAwwHQXR0ciBDQTEMMAoGA1UECwwDQ1BHMQ0wCwYDVQQLDAREQ1NHMQ4wDAYDVQQKDAVJbnRlbDELMAkGA1UECAwCQ0ExCzAJBgNVBAYTAlVTMA0GCSqGSIb3DQEBBQUAAgEBMCIYDzIwMTMwODA4MjIyMTEzWhgPMjAxMzA5MDgyMjIxMTNaMEMwEwYLKwYBBAG9hDcBAQExBAwCVVMwEwYLKwYBBAG9hDgCAgIxBAwCQ0EwFwYLKwYBBAG9hDkDAwMxCAwGRm9sc29tMA0GCSqGSIb3DQEBBQUAA4IBAQCcN8KjjmR2H3LT5aL1SCFS4joy/7vAd3/xdJtkqrb3UAQHMdUUJQHf3frJsMJs22m0So0xs/f1sB15frC1LsQGF5+RYVXsClv0glStWbPYiqEfdM7dc/RDMRtrXKEH3sBlxMT7YS/g5E6qwmKZX9shQ3BYmeZi5A3DTzgHCbA3Cm4/MQbgWGjoamfWZ9EDk4Bww2y0ueRi60PfoLg43rcijr8Wf+JEzCRw040vIaH3DtFdmzvvGRdqE3YlEkrUL3gEIZNY3Po1NL4cb238vT5CHZTt9NyD7xSv0XkwOY4RbSUdYBsxfH3mEcdQ6LtJdfF1BUXfMThKN3TctFcY/dLF

                return cert.getEncoded(); //X509AttributeCertificate.valueOf(cert.getEncoded());            
            }
            return null;
        } catch (IOException | OperatorCreationException e) {
            fault(e, "cannot sign certificate");
            return null;
        } finally {
            done();
        }
    }
}
