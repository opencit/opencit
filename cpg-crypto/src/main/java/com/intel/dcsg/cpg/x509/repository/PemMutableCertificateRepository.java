/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.x509.repository;

import com.intel.dcsg.cpg.io.Resource;
import com.intel.dcsg.cpg.x509.X509Util;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyManagementException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A writable repository of X509Certificates that is saved in a PEM-format file.
 * @author jbuhacoff
 */
public class PemMutableCertificateRepository implements MutableCertificateRepository {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final Resource resource;
    private ArrayList<X509Certificate> keystore = new ArrayList<X509Certificate>();
    private transient Integer hashCode = null;

    public PemMutableCertificateRepository(Resource resource) throws IOException, CertificateException {
        this.resource = resource;
        load();
    }

    public Resource getResource() {
        return resource;
    }

    private void load() throws IOException, CertificateException {
        InputStream in = resource.getInputStream();
        if( in != null ) { // will be null if the resource is known to be empty
            String pem = IOUtils.toString(in);
            keystore = new ArrayList<X509Certificate>(X509Util.decodePemCertificates(pem));
            in.close();
        }
    }

    private void save() throws IOException, CertificateEncodingException {
        OutputStream out = resource.getOutputStream();
        if( out != null ) {
            for (X509Certificate certificate : keystore) {
                String pem = X509Util.encodePemCertificate(certificate);
                out.write(pem.getBytes());
            }
            out.close();
        }
    }

    @Override
    public List<X509Certificate> getCertificates() {
        ArrayList<X509Certificate> allCerts = new ArrayList<X509Certificate>();
        allCerts.addAll(keystore);
        return allCerts;
    }

    /**
     * Calculates the hash code based on the order and contents of the
     * certificates in the repository. Two Array Certficate Repository objects
     * are considered equal if they have the same certificates in the same
     * order. We might relax the order requirement in the future. The hash code
     * is only calculated once, after that it is cached and reused. This assumes
     * the repository will not be modified outside of this object, and since
     * it's presented as a read-only repository that is not likely to happen.
     *
     * @return
     */
    @Override
    public int hashCode() {
        if (hashCode != null) {
            return hashCode;
        } // use cached value when possible
        HashCodeBuilder builder = new HashCodeBuilder(11, 31);
        if (keystore != null) {
            for (X509Certificate cert : keystore) {
                try {
                    builder.append(cert.getEncoded());
                } catch (Exception e) {
                    builder.append(e.toString());
                }
            }
        }
        hashCode = builder.toHashCode();
        return hashCode;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (other == this) {
            return true;
        }
        if (other.getClass() != this.getClass()) {
            return false;
        }
        PemMutableCertificateRepository rhs = (PemMutableCertificateRepository) other;
        return new EqualsBuilder().append(hashCode(), rhs.hashCode()).isEquals();
    }

    public void addCertificate(X509Certificate certificate) throws KeyManagementException {
        keystore.add(certificate);
        try {
            save();
        } catch (IOException e) {
            throw new KeyManagementException(e);
        } catch (CertificateEncodingException e) {
            throw new KeyManagementException(e);
        }
    }
}
