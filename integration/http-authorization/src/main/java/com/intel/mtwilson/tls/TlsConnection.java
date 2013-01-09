/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 *
 * @author jbuhacoff
 */
public class TlsConnection {
    private final String connectionString;
    private final TlsPolicy tlsPolicy;
    private transient Integer hashCode = null;
    public TlsConnection(String connectionString, TlsPolicy tlsPolicy) {
        this.connectionString = connectionString;
        this.tlsPolicy = tlsPolicy;
    }
    public String getConnectionString() { return connectionString; }
    public TlsPolicy getTlsPolicy() { return tlsPolicy; }
    
    /**
     * The following elements are considered when calculating hashCode:
     * 1. connection string
     * 2. tls policy name (obtained from the implementation class name)
     * 3. contents of certificate repository 
     * 
     * @return 
     */
    @Override
    public int hashCode() {
        if( hashCode != null ) { return hashCode; }
        return new HashCodeBuilder(19,47).append(connectionString).append(tlsPolicy.getClass().getName()).append(tlsPolicy.getCertificateRepository()).toHashCode(); 
    }
    
    @Override
    public boolean equals(Object other) {
        if( other == null ) { return false; }
        if( other == this ) { return true; }
        if( other.getClass() != this.getClass() ) { return false; }
        TlsConnection rhs = (TlsConnection)other;
        return new EqualsBuilder().append(connectionString, rhs.connectionString).append(tlsPolicy.getClass().getName(), rhs.tlsPolicy.getClass().getName()).append(tlsPolicy.getCertificateRepository(), rhs.tlsPolicy.getCertificateRepository()).isEquals(); // XXX TODO see note in hashCode about comparing the tls policy
    }
    
}
