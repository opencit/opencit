/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.api;

import com.intel.mountwilson.as.hostmanifestreport.data.HostManifestReportType;
import com.intel.mountwilson.as.hosttrustreport.data.HostsTrustReportType;
import com.intel.mtwilson.datatypes.*;
import com.intel.mtwilson.model.*;
import com.intel.mtwilson.datatypes.xml.HostTrustXmlResponse;
import java.io.IOException;
import java.net.MalformedURLException;
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

    HostResponse addHost(TxtHost host) throws IOException, ApiException, SignatureException, MalformedURLException;
    
    HostConfigResponseList addHosts(TxtHostRecordList hostRecords) throws IOException, ApiException, SignatureException;

    HostTrustResponse getHostTrustByAik(Sha1Digest aikSha1) throws IOException, ApiException, SignatureException;
    
    //X509Certificate getCurrentTrustCertificateByAik(Sha1Digest aikSha1) throws IOException, ApiException, SignatureException;


    HostResponse updateHost(TxtHost host) throws IOException, ApiException, SignatureException, MalformedURLException;

    HostConfigResponseList updateHosts(TxtHostRecordList hostRecords) throws IOException, ApiException, SignatureException;
    
    HostResponse deleteHost(Hostname hostname) throws IOException, ApiException, SignatureException;

    List<TxtHostRecord> queryForHosts(String searchCriteria) throws IOException, ApiException, SignatureException;

    OpenStackHostTrustLevelReport pollHosts(List<Hostname> hostnames) throws IOException, ApiException, SignatureException;


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
    
    String getSamlForHost(Hostname hostname, boolean forceVerify) throws IOException, ApiException, SignatureException;

    List<HostTrustXmlResponse> getSamlForMultipleHosts(Set<Hostname> hostnames, boolean forceVerify) throws IOException, ApiException, SignatureException;

    BulkHostTrustResponse getTrustForMultipleHosts(Set<Hostname> hostnames, boolean forceVerify) throws IOException, ApiException, SignatureException;

    // this method is used only by OpenSourceVMMHelper which is being replaced by IntelHostAgent; also the service implementation of this method only supports hosts with trust agents (even though vmware hosts also have their own attestation report)
    //String getHostAttestationReport(Hostname hostname) throws IOException, ApiException, SignatureException; 
    
    AttestationReport getAttestationFailureReport(Hostname hostname)throws IOException, ApiException, SignatureException;
    
    
    AttestationReport getAttestationReport(Hostname hostname) throws IOException, ApiException, SignatureException;

    X509Certificate getTlsCertificateForTrustedHost(Hostname hostname) throws IOException, ApiException, SignatureException;
    
    //This method is currently not exposed to the external customers. There is another API, which would call this API.
    // This function has not been added to Mtwilson.java since the javadoc would be created from it
    HostResponse registerHostByFindingMLE(TxtHostRecord hostObj) throws IOException, ApiException, SignatureException;
    
    //This method is currently not exposed to the external customers. There is another API, which would call this API.
    // This function has not been added to Mtwilson.java since the javadoc would be created from it
    String checkMatchingMLEExists(TxtHostRecord hostObj) throws IOException, ApiException, SignatureException;
}
