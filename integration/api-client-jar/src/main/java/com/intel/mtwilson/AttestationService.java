/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson;

import com.intel.mountwilson.as.hostmanifestreport.data.HostManifestReportType;
import com.intel.mountwilson.as.hosttrustreport.data.HostsTrustReportType;
import com.intel.mtwilson.datatypes.*;
import com.intel.mtwilson.datatypes.xml.HostTrustXmlResponse;

import java.io.IOException;
import java.security.SignatureException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Set;
import javax.xml.bind.JAXBException;

/**
 *
 * @author jbuhacoff
 */
public interface AttestationService {
    
    HostLocation getHostLocation(Hostname hostname) throws IOException, ApiException, SignatureException;
    
    boolean addHostLocation(HostLocation hostLocObj) throws IOException, ApiException, SignatureException;

    HostTrustResponse getHostTrust(Hostname hostname) throws IOException, ApiException, SignatureException;

    HostResponse addHost(TxtHost host) throws IOException, ApiException, SignatureException;

    HostResponse updateHost(TxtHost host) throws IOException, ApiException, SignatureException;

    HostResponse deleteHost(Hostname hostname) throws IOException, ApiException, SignatureException;

    List<TxtHostRecord> queryForHosts(String searchCriteria) throws IOException, ApiException, SignatureException;

    PollHostsOutput pollHosts(List<Hostname> hostnames) throws IOException, ApiException, SignatureException;


    HostsTrustReportType getHostTrustReport (List<Hostname> hostnames) throws IOException, ApiException, SignatureException, JAXBException;

    HostManifestReportType getHostManifestReport (Hostname hostname) throws IOException, ApiException, SignatureException, JAXBException;


    
    /**
     * Returns an XML document (SAML) describing the trust attributes of the host
     * @param hostname
     * @return
     * @throws IOException
     * @throws ApiException
     * @throws SignatureException 
     */
    String getSamlForHost(Hostname hostname) throws IOException, ApiException, SignatureException;

    List<HostTrustXmlResponse> getSamlForMultipleHosts(Set<Hostname> hostnames, boolean forceVerify) throws IOException, ApiException, SignatureException;

    BulkHostTrustResponse getTrustForMultipleHosts(Set<Hostname> hostnames, boolean forceVerify) throws IOException, ApiException, SignatureException;

    
    String getHostAttestationReport(Hostname hostname) throws IOException, ApiException, SignatureException;
    
    AttestationReport getAttestationFailureReport(Hostname hostname)throws IOException, ApiException, SignatureException;
    
    
    AttestationReport getAttestationReport(Hostname hostname) throws IOException, ApiException, SignatureException;

    X509Certificate getTlsCertificateForTrustedHost(Hostname hostname) throws IOException, ApiException, SignatureException;;
}
