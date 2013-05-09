/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.datatypes;

/**
 * Definition of policies for Transport Layer Security throughout the
 * Mt Wilson solution. Network clients using SSL/TLS should accept 
 * @author jbuhacoff
 */
public class TLSPolicy {
    /**
     * The target server must present an X509 certificate signed by a trusted CA
     * and the hostname on the certificate must match the server's hostname.
     * This policy is only possible when there is a list of trusted CA's.
     * This policy is typically implemented in web browsers.
     */
    public static final TLSPolicy TRUST_CA_VERIFY_HOSTNAME = new TLSPolicy("TRUST_CA_VERIFY_HOSTNAME");
    
    /**
     * The target server must present an X509 certificate that matches what we
     * already have saved in our trusted certificates repository for the server.
     * This policy enables self-signed certificates and certificates without
     * the alternative name extension, but if the extension is present it must
     * match the hostname.
     * It's important that the trusted certificates repository be per server
     * because if it is global then a single compromised server can be used
     * to stage a MITM attack against attestation of non-compromised servers.
     */
    public static final TLSPolicy TRUST_KNOWN_CERTIFICATE = new TLSPolicy("TRUST_KNOWN_CERTIFICATE");
    
    /**
     * Same as TRUST_KNOWN_CERTIFICATE, but if the repository does not have a certificate
     * already saved for that server, the server's current certificate is the first
     * known certificate and is automatically saved in the repository. 
     * This policy enables self-signed certificates and certificates without
     * the alternative name extension, but if the extension is present it must
     * match the hostname.
     * This option requires a second-channel comparison of the certificate fingerprints in order
     * to be secure. In practice this means an administrator should get a list of all servers
     * and their SSL certificates obtained using this policy, and compare the fingerprints of 
     * these certificates to the fingerprints of the known SSL certificates on the servers. 
     * Any mismatch indicates a possible MITM attack has occurred. 
     */
    public static final TLSPolicy TRUST_FIRST_CERTIFICATE = new TLSPolicy("TRUST_FIRST_CERTIFICATE");
    
    /**
     * Accepts any certificate and does not check the hostname against the
     * alternativeAddress extension.
     */
    public static final TLSPolicy INSECURE = new TLSPolicy("INSECURE");
    
    private String policyName;
    
    public TLSPolicy(String policyName) {
        this.policyName = policyName;
    }
    
    @Override
    public String toString() { return policyName; }
    
    
    /**
     * 
     * @param policyName
     * @return null if input is null, matching TLSPolicy otherwise
     * @throws IllegalArgumentException if an unknown policy name is provided
     */
    public static TLSPolicy valueOf(String policyName) {
        if( policyName == null ) { return null; }
        if( policyName.equals(TRUST_CA_VERIFY_HOSTNAME.toString()) ) { return TRUST_CA_VERIFY_HOSTNAME; }
        if( policyName.equals(TRUST_KNOWN_CERTIFICATE.toString()) ) { return TRUST_KNOWN_CERTIFICATE; }
        if( policyName.equals(TRUST_FIRST_CERTIFICATE.toString()) ) { return TRUST_FIRST_CERTIFICATE; }
        if( policyName.equals(INSECURE.toString()) ) { return INSECURE; }
        throw new IllegalArgumentException("Unknown TLSPolicy: "+policyName);
    }
}
