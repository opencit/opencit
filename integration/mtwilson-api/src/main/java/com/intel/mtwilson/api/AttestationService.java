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

    HostTrustResponse getHostTrustByAik(Sha256Digest aikSha256) throws IOException, ApiException, SignatureException;
    
    //X509Certificate getCurrentTrustCertificateByAik(Sha1Digest aikSha1) throws IOException, ApiException, SignatureException;


    HostResponse updateHost(TxtHost host) throws IOException, ApiException, SignatureException, MalformedURLException;

    HostConfigResponseList updateHosts(TxtHostRecordList hostRecords) throws IOException, ApiException, SignatureException;
    
    HostResponse deleteHost(Hostname hostname) throws IOException, ApiException, SignatureException;

    List<TxtHostRecord> queryForHosts(String searchCriteria) throws IOException, ApiException, SignatureException;
    
           /**
     * Retrieves the list of hosts matching search criteria. Currently only search on the name is
     * supported. Empty search criteria retrieves all the hosts configured in the system.
     * <p>
     * <i><u>Roles needed:</u></i>Attestation/Report/Security
     * <p>
     * <i><u>Output content type:</u></i>Application/JSON
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i><br>
     * https://192.168.1.101:8181/AttestationService/resources/hosts?searchCriteria=201&includeHardwareUuid=true<br>
     * <p>
     * <i><u>Sample Output:</u></i><br>
     *[{"HostName":"192.168.1.201","IPAddress":"192.168.1.201","Port":9999,"BIOS_Name":"Intel_Corp.","BIOS_Version":"T060","BIOS_Oem":"Intel Corp.","VMM_Name":"Intel_Thurley_Xen","VMM_Version":"11-4.1.0","VMM_OSName":"SUSE_LINUX","VMM_OSVersion":"11","AddOn_Connection_String":"intel:https://192.168.1.201:9999","Description":null,"Email":null,"Location":null,"AIK_Certificate":null,"AIK_PublicKey":null,"AIK_SHA1":null,"Processor_Info":null}]
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
     * List<TxtHostRecord> queryForHosts = apiClientObj.queryForHosts("201");
     * <p>
     * @param searchCriteria search criteria specified by the user. Search criteria applies just for the host name.
     * @parma includeHardwareUuid if set to true, api will include hardware_uuid field in txtHostRecord response, this will break backwards 1.2 compatabiltiy
     * @return List of {@link TxtHostRecord} objects matching the search criteria.
     * @throws IOException
     * @throws ApiException If there are any errors during the execution this exception would be returned to the caller.
     * The caller can use the getErrorCode() and getMessage() functions to retrieve the exception details.
     * @throws SignatureException 
     * @since MTW 1.0 Enterprise/1.2 Opensource
     */
    List<TxtHostRecord> queryForHosts(String searchCriteria,boolean includeHardwareUuid) throws IOException, ApiException, SignatureException;

    /**
     * Returns the host record with new features such as hardware uuid and tls policy.
     * 
     * @param searchCriteria
     * @return
     * @throws IOException
     * @throws ApiException
     * @throws SignatureException 
     * @since MTW 2.0
     */
    List<TxtHostRecord> queryForHosts2(String searchCriteria) throws IOException, ApiException, SignatureException;
    
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
    
    boolean importAssetTagCertificate(AssetTagCertCreateRequest aTagObj) throws IOException, ApiException, SignatureException;
    
    boolean revokeAssetTagCertificate(AssetTagCertRevokeRequest aTagObj) throws IOException, ApiException, SignatureException;
    
    //This method is currently not exposed to the external customers. There is another API, which would call this API.
    // This function has not been added to MtWilson.java since the javadoc would be created from it
    HostResponse registerHostByFindingMLE(TxtHostRecord hostObj) throws IOException, ApiException, SignatureException;
    
    //This method is currently not exposed to the external customers. There is another API, which would call this API.
    // This function has not been added to MtWilson.java since the javadoc would be created from it
    String checkMatchingMLEExists(TxtHostRecord hostObj) throws IOException, ApiException, SignatureException;
}
