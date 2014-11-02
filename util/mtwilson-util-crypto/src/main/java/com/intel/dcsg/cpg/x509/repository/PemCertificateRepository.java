/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.x509.repository;

import com.intel.dcsg.cpg.io.Resource;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A read-only repository of X509Certificates that is saved in a PEM-format file.
 * @since 0.1
 * @author jbuhacoff
 */
public class PemCertificateRepository implements CertificateRepository {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final PemMutableCertificateRepository repository;
    private transient Integer hashCode = null;

    public PemCertificateRepository(Resource resource) throws IOException, CertificateException {
        this.repository = new PemMutableCertificateRepository(resource);
    }

    public Resource getResource() {
        return repository.getResource();
    }

    @Override
    public List<X509Certificate> getCertificates() {
        return Collections.unmodifiableList(repository.getCertificates());
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
        for (X509Certificate cert : repository.getCertificates()) {
            try {
                builder.append(cert.getEncoded());
            } catch (Exception e) {
                builder.append(e.toString());
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
        PemCertificateRepository rhs = (PemCertificateRepository) other;
        return new EqualsBuilder().append(hashCode(), rhs.hashCode()).isEquals();
    }

}
