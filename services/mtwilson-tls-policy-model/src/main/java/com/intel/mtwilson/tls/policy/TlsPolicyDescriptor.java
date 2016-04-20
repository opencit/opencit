/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls.policy;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.dcsg.cpg.validation.Regex;
import com.intel.dcsg.cpg.validation.Unchecked;
import java.util.Collection;
import java.util.Map;

/**
 *
 * @author jbuhacoff
 */
@JacksonXmlRootElement(localName="tlsPolicyDescriptor")
public class TlsPolicyDescriptor {
    private String policyType; // INSECURE, TRUST_FIRST_CERTIFICATE, public-key, public-key-digest, certificate, certificate-digest
    private String ciphers; // comma-separated AES128-SHA, EDH-DSS-CBC-SHA, NULL-MD5 etc.  with ! prefix to mean exclude  for example !NULL  means exclude null ciphers,  !DES  means exclude ciphers with DES,  !MD5 means exclude ciphers with MD5 hashing, etc.  and + means "at least" so +AES128  means "AES128 or greater" and no prefix means exact match so "AES128" would not match AES256 but "AES" would match both AES128 and AES256, similarly "SHA" matches SHA-1 and any SHA-2 algorithm
    private String protocols; // comma-separated ssl, ssl2, ssl3, tls, tls1.2, tls1.3,  with ! to mean exclude for example !SSLv2 means don't allow ssl2, and "ssl" matches any ssl and "tls" matches any tls version;  -ssl  means don't include ssl but if one is added later it's ok,  whereas !ssl would not allow any to be added (-ssl,ssl3 means ssl3 but not others, whereas !ssl,ssl3 means no ssl at all)
    private TlsProtection protection; // encryption, integrity, authentication, forwardSecrecy
    private Map<String,String> metadata; // digestAlgorithm (MD5, SHA-1, SHA-256, etc), encoding (base64 or hex)
    @JacksonXmlElementWrapper(localName="data")
    @JacksonXmlProperty(localName="item")
    private Collection<String> data; // certificates, certificate digests, public keys, or public key digests

    @Regex("(?:[a-zA-Z0-9_-]+)")
    public String getPolicyType() {
        return policyType;
    }

    public void setPolicyType(String name) {
        this.policyType = name;
    }

    @Regex("(?:[a-zA-Z0-9,!_+-]+)")
    public String getCiphers() {
        return ciphers;
    }

    public void setCiphers(String ciphers) {
        this.ciphers = ciphers;
    }

    @Regex("(?:[a-zA-Z0-9,.!_+-]+)")
    public String getProtocols() {
        return protocols;
    }

    public void setProtocols(String protocols) {
        this.protocols = protocols;
    }

    public TlsProtection getProtection() {
        return protection;
    }

    public void setProtection(TlsProtection protection) {
        this.protection = protection;
    }

    @Unchecked
    public Collection<String> getData() {
        return data;
    }

    public void setData(Collection<String> data) {
        this.data = data;
    }

    @Unchecked
    public Map<String, String> getMeta() {
        return metadata;
    }

    public void setMeta(Map<String, String> meta) {
        this.metadata = meta;
    }
    
    
}
