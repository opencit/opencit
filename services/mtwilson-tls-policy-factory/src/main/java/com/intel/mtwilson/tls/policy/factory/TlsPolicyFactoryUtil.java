/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls.policy.factory;

import com.intel.dcsg.cpg.codec.Base64Codec;
import com.intel.dcsg.cpg.codec.Base64Util;
import com.intel.dcsg.cpg.codec.ByteArrayCodec;
import com.intel.dcsg.cpg.codec.HexCodec;
import com.intel.dcsg.cpg.codec.HexUtil;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.io.Resource;
import com.intel.mtwilson.My;
import com.intel.mtwilson.tls.policy.TlsPolicyDescriptor;
import com.intel.mtwilson.tls.policy.TlsProtection;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author jbuhacoff
 */
public class TlsPolicyFactoryUtil {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TlsPolicyFactoryUtil.class);

    /**
     * Utility function to detect if the sample is base64-encoded or hex-encoded
     * and return a new instance of the appropriate codec. If the sample
     * encoding cannot be detected, this method will return null.
     *
     * @param sample of data either base64-encoded or hex-encoded
     * @return a new codec instance or null if the encoding is not recognized
     */
    public static String guessEncodingForData(String sample) {
        log.debug("guessEncodingForData: {}", sample);
        if( sample == null ) { return null; }
//        String printable = sample.replaceAll("[^\\p{Print}]", "");
        String printable = sample.replaceAll("["+HexUtil.NON_HEX+"&&"+Base64Util.NON_BASE64+"]", "");
        log.debug("guessEncodingForData printable: {}", printable);
        String hex = HexUtil.trim(printable);
        if (HexUtil.isHex(hex)) {
            log.debug("guessEncodingForData hex: {}", hex);
            return "hex";
        }
        String base64 = Base64Util.trim(printable);
        if (Base64Util.isBase64(base64)) {
            log.debug("guessEncodingForData base64: {}", base64);
            return "base64";
        }
        log.debug("guessEncodingForData failed");
        return null;
    }

    /**
     * Utility function to instantiate a codec by name
     *
     * @param encoding "base64" or "hex" or null
     * @return new codec instance or null if the encoding name is null or is not
     * recognized
     */
    public static ByteArrayCodec getCodecByName(String encoding) {
        if (encoding == null) {
            return null;
        }
        switch (encoding) {
            case "base64": {
                Base64Codec codec = new Base64Codec();
                codec.setNormalizeInput(true);
                return codec;
            }
            case "hex": {
                HexCodec codec = new HexCodec();
                codec.setNormalizeInput(true);
                return codec;
            }
            default:
                return null;
        }
    }

    public static String guessAlgorithmForDigest(byte[] hash) {
        if (hash.length == 16) {
            return "MD5";
        }
        if (hash.length == 20) {
            return "SHA-1";
        }
        if (hash.length == 32) {
            return "SHA-256";
        }
        if (hash.length == 48) {
            return "SHA-384";
        }
        if (hash.length == 64) {
            return "SHA-512";
        }
        return null;
    }

    /**
     * Utility function to get a sample item from a collection
     *
     * @param collection
     * @return the first item from the collection, or null if the collection is
     * empty
     */
    public static String getFirst(Collection<String> collection) {
        Iterator<String> it = collection.iterator();
        if (it.hasNext()) {
            return it.next();
        }
        return null;
    }

    public static TlsPolicyDescriptor getTlsPolicyDescriptorFromResource(Resource resource) {
        return getTlsPolicyDescriptorFromResource(resource, My.configuration().getTlsKeystorePassword());
    }

    public static TlsPolicyDescriptor getTlsPolicyDescriptorFromResource(Resource resource, String password) {
        try {
            SimpleKeystore tlsKeystore = new SimpleKeystore(resource, password);
            TlsPolicyDescriptor tlsPolicyDescriptor = createTlsPolicyDescriptorFromKeystore(tlsKeystore);
            return tlsPolicyDescriptor;
        } catch (KeyManagementException e) {
            log.warn("Cannot load tls policy descriptor", e);
            return null;
        }
    }

    public static TlsPolicyDescriptor createTlsPolicyDescriptorFromKeystore(SimpleKeystore tlsKeystore) {
        TlsPolicyDescriptor tlsPolicyDescriptor = new TlsPolicyDescriptor();
        tlsPolicyDescriptor.setPolicyType("certificate");
        tlsPolicyDescriptor.setProtection(getAllTlsProtection());
        ArrayList<String> encodedCertificates = new ArrayList<>();
        tlsPolicyDescriptor.setData(encodedCertificates);
        ArrayList<X509Certificate> certificates = new ArrayList<>();
        certificates.addAll(V1TlsPolicyFactory.getMtWilsonTrustedTlsCertificates());
        certificates.addAll(getTrustedTlsCertificatesFromSimpleKeystore(tlsKeystore));
        for (X509Certificate cert : certificates) {
            log.debug("Adding trusted TLS certs and cacerts: {}", cert.getSubjectX500Principal().getName());
            try {
                encodedCertificates.add(Base64.encodeBase64String(cert.getEncoded()));
            } catch (CertificateEncodingException e) {
                throw new IllegalArgumentException("Invalid certificate", e);
            }
        }
        return tlsPolicyDescriptor;
    }

    public static TlsProtection getAllTlsProtection() {
        TlsProtection tlsProtection = new TlsProtection();
        tlsProtection.integrity = true;
        tlsProtection.encryption = true;
        tlsProtection.authentication = true;
        tlsProtection.forwardSecrecy = true;
        return tlsProtection;
    }

    public static List<X509Certificate> getTrustedTlsCertificatesFromSimpleKeystore(SimpleKeystore tlsKeystore) {
        ArrayList<X509Certificate> list = new ArrayList<>();
        if (tlsKeystore != null) {
            try {
                X509Certificate[] cacerts = tlsKeystore.getTrustedCertificates(SimpleKeystore.CA);
                list.addAll(Arrays.asList(cacerts));
                X509Certificate[] sslcerts = tlsKeystore.getTrustedCertificates(SimpleKeystore.SSL);
                list.addAll(Arrays.asList(sslcerts));
            } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableEntryException | CertificateEncodingException e) {
                log.warn("Cannot load trusted TLS certificates from Mt Wilson 1.x keystore", e);
            }
        }
        return list;
    }
}
