/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tag.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.intel.dcsg.cpg.crypto.Sha256Digest;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.tag.model.x509.UTF8NameValueMicroformat;
import com.intel.mtwilson.tag.model.x509.UTF8NameValueSequence;
import java.io.IOException;
import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.Attribute;
import org.bouncycastle.cert.X509AttributeCertificateHolder;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.operator.ContentVerifierProvider;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.bc.BcRSAContentVerifierProviderBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convenience class that wraps the Bouncy Castle API to walk the ASN1 tree and extract the information that we are
 * interested in.
 * 
 * @author jbuhacoff
 */
public class X509AttributeCertificate {

    private static Logger log = LoggerFactory.getLogger(X509AttributeCertificate.class);
    private final byte[] encoded;
    private String issuer;
    private BigInteger serialNumber = null;
    private String subject = null;
//    private UUID subjectUuid = null;
    private Date notBefore;
    private Date notAfter;
    private ArrayList<Attribute> attributes = new ArrayList<>();
    private ArrayList<UTF8NameValueMicroformat> tags1 = new ArrayList<>();
    private ArrayList<UTF8NameValueSequence> tags2 = new ArrayList<>();
    private ArrayList<ASN1Encodable> tagsOther = new ArrayList<>();
    
    private X509AttributeCertificate(byte[] encoded) {
        this.encoded = encoded;
    }
    
    public byte[] getEncoded() {
        return encoded;
    }
    
    public byte[] getFingerprintSha256() {
        return Sha256Digest.digestOf(encoded).toByteArray();
    }
    
    public String getIssuer() {
        return issuer;
    }
    
    public BigInteger getSerialNumber() {
        return serialNumber;
    }
    
    public String getSubject() {
        return subject;
    }
    
    public Date getNotBefore() {
        return notBefore;
    }
    
    public Date getNotAfter() {
        return notAfter;
    }
    
    public List<Attribute> getAttribute() {
        return attributes;
    }
    public <T extends ASN1Encodable> List<T> getAttributes(Class<T> clazz) {
        if( clazz.equals(UTF8NameValueMicroformat.class) ) {
            return (List<T>)tags1;
        }
        if( clazz.equals(UTF8NameValueSequence.class)) {
            return (List<T>)tags2;
        }
        return (List<T>)tagsOther;
    }
    
    @Override
    public String toString() {
        return Base64.encodeBase64String(encoded);
    }

    /**
     *
     * @param encodedCertificate
     * @return
     */
    @JsonCreator
    public static X509AttributeCertificate valueOf(@JsonProperty("encoded") byte[] encodedCertificate) {
        X509AttributeCertificate result = new X509AttributeCertificate(encodedCertificate);
        X509AttributeCertificateHolder cert;
        try {
            cert = new X509AttributeCertificateHolder(encodedCertificate);
        }
        catch(IOException e) {
            throw new IllegalArgumentException(e);
        }
        log.debug("issuer: {}", StringUtils.join(cert.getIssuer().getNames(), "; "));  // calls toString() on each X500Name so we get the default representation; we can do it ourselves for custom display;  output example: CN=Attr CA,OU=CPG,OU=DCSG,O=Intel,ST=CA,C=US
        result.issuer = StringUtils.join(cert.getIssuer().getNames(), "; "); // but expected to be only one
        log.debug("serial number: {}", cert.getSerialNumber().toString()); // output example:   1
        result.serialNumber = cert.getSerialNumber();
        log.debug("holder: {}", StringUtils.join(cert.getHolder().getEntityNames(), ", ")); // output example:  2.25=#041092a71a228c174522a18bfd3ed3d00b39
        // now let's get the UUID specifically out of this
        log.debug("holder has {} entity names", cert.getHolder().getEntityNames().length);
        for (X500Name entityName : cert.getHolder().getEntityNames()) {
            log.debug("holder entity name has {} rdns", entityName.getRDNs().length);
            for (RDN rdn : entityName.getRDNs()) {
                log.debug("entity rdn is multivalued? {}", rdn.isMultiValued());
                AttributeTypeAndValue attr = rdn.getFirst();
                if (attr.getType().toString().equals(OID.HOST_UUID)) {
                    UUID uuid = UUID.valueOf(DEROctetString.getInstance(attr.getValue()).getOctets());
                    log.debug("holder uuid: {}", uuid);
                    result.subject = uuid.toString();// example: 33766a63-5c55-4461-8a84-5936577df450
                }
            }
        }
        // if we ddin't identify the UUID,  just display the subject same way we did the issuer... concat all the entity names. example: 2.25=#041033766a635c5544618a845936577df450  (notice that in the value, there's a #0410 prepended to the uuid 33766a635c5544618a845936577df450)
        if (result.subject == null) {
            result.subject = StringUtils.join(cert.getHolder().getEntityNames(), "; ");
        }
        log.debug("not before: {}", cert.getNotBefore()); // output example: Thu Aug 08 15:21:13 PDT 2013
        log.debug("not after: {}", cert.getNotAfter()); // output example: Sun Sep 08 15:21:13 PDT 2013
        result.notBefore = cert.getNotBefore();
        result.notAfter = cert.getNotAfter();
        Attribute[] attributes = cert.getAttributes();
        result.tags1 = new ArrayList<>();
        result.tags2 = new ArrayList<>();
        result.tagsOther = new ArrayList<>();
        for (Attribute attr : attributes) {
            log.debug("attr {} is {}", attr.hashCode(), attr.toString());
            result.attributes.add(attr);
            for (ASN1Encodable value : attr.getAttributeValues()) {
//                log.trace("encoded value: {}", Base64.encodeBase64String(value.getEncoded())); // throws IOException
//                log.debug("attribute: {} is {}", attr.getAttrType().toString(), DERUTF8String.getInstance(value).getString()); // our values are just UTF-8 strings  but if you use new String(value.getEncoded())  you will get two extra spaces at the beginning of the string
//                result.tags.add(new AttributeOidAndValue(attr.getAttrType().toString(), DERUTF8String.getInstance(value).getString()));
                if( attr.getAttrType().toString().equals(UTF8NameValueMicroformat.OID)) {
                    log.debug("name-value microformat attribute: {}",  DERUTF8String.getInstance(value).getString()); // our values are just UTF-8 strings  but if you use new String(value.getEncoded())  you will get two extra spaces at the beginning of the string                    
                    UTF8NameValueMicroformat microformat = new UTF8NameValueMicroformat(DERUTF8String.getInstance(value));
                    log.debug("name-value microformat attribute (2)  name {} value {}", microformat.getName(), microformat.getValue());
                    result.tags1.add(microformat);
                }
                else if( attr.getAttrType().toString().equals(UTF8NameValueSequence.OID)) {
                    UTF8NameValueSequence sequence = new UTF8NameValueSequence(ASN1Sequence.getInstance(value));
                    String name = sequence.getName();
                    List<String> values = sequence.getValues();
                    log.debug("name-values asn.1 attribute {} values {}",  name, values); 
                    result.tags2.add(sequence);
                }
                else {
                    log.debug("unrecognzied attribute type {}", attr.getAttrType().toString());
                    result.tagsOther.add(value);
                }
                /*
                 * output examples:
                 * attribute: 1.3.6.1.4.1.99999.1.1.1.1 is US
                 * attribute: 1.3.6.1.4.1.99999.2.2.2.2 is CA
                 * attribute: 1.3.6.1.4.1.99999.3.3.3.3 is Folsom
                 */
            }
        }
        log.debug("valueOf ok");
        return result;
    }
    
    /**
     * This checks the certificate's notBefore and notAfter dates against the current time.
     * This does NOT check the signature. Do that separately with isTrusted().
     * @return true if the certificate is valid now
     */
    public boolean isValid(X509Certificate issuer) {
        return isValid(issuer, new Date());
    }
    
    /**
     * This checks the certificate's notBefore and notAfter dates against the current time.
     * This does NOT check the signature. Do that separately with isTrusted().
     * 
     * @param date to check against the certificate's validity period
     * @return true if the certificate is valid on the given date
     */
    public boolean isValid(X509Certificate issuer, Date date) {
        try {
            X509AttributeCertificateHolder holder = new X509AttributeCertificateHolder(encoded);
            ContentVerifierProvider verifierProvider = new BcRSAContentVerifierProviderBuilder(new DefaultDigestAlgorithmIdentifierFinder()).build(new X509CertificateHolder(issuer.getEncoded()));
            if( !holder.isSignatureValid(verifierProvider) ) {
                log.debug("Certificate signature cannot be validated with certificate: {}", issuer.getIssuerX500Principal().getName());
                return false;
            }
            return date.compareTo(notBefore) > -1 && date.compareTo(notAfter) < 1;
        }
        catch(Exception e) {
            log.error("Cannot initialize certificate verifier", e);
            return false;
        }
    }
    
}
