/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.x509.repository;

import com.intel.mtwilson.codec.ByteArrayCodec;
import com.intel.dcsg.cpg.x509.X509Util;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Lightweight implementation of MutableCertificateRepository backed by HashSet of
 * String with automatic encoding and decoding using the specified codec, for
 * example a HexCodec or Base64Codec.
 *
 * The getCertificates method returns an immutable list in accordance with the
 * CertificateRepository contract. The mutable in MutableCertificateRepository is
 * implemented via the addCertificate method.
 *
 * @author jbuhacoff
 */
public class EncodingMutableCertificateRepository implements MutableCertificateRepository {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EncodingMutableCertificateRepository.class);
    private Collection<String> data;
    private ByteArrayCodec codec;

    public EncodingMutableCertificateRepository(Collection<String> store, ByteArrayCodec codec) {
        this.data = store;
        this.codec = codec;
    }

    @Override
    public void addCertificate(X509Certificate certificate) {
        try {
            String encoded = codec.encode(certificate.getEncoded());
            log.debug("Encoded certificate: {}", encoded);
            data.add(encoded);
        }
        catch(Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public List<X509Certificate> getCertificates() {
        ArrayList<X509Certificate> certificates = new ArrayList<>();
        for (String item : data) {
            try {
                X509Certificate certificate = X509Util.decodeDerCertificate(codec.decode(item));
                certificates.add(certificate);
            } catch (Exception e) {
                log.warn("Cannot decode certificate: {}", item, e);
            }
        }
        return Collections.unmodifiableList(certificates);
    }
}
