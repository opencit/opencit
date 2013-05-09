/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.api;

import com.intel.mountwilson.as.hostmanifestreport.data.HostManifestReportType;
import com.intel.mountwilson.as.hosttrustreport.data.HostsTrustReportType;
import com.intel.mtwilson.datatypes.ApiClientInfo;
import com.intel.mtwilson.datatypes.ApiClientSearchCriteria;
import com.intel.mtwilson.datatypes.ApiClientUpdateRequest;
import com.intel.mtwilson.datatypes.AttestationReport;
import com.intel.mtwilson.datatypes.AuditLogEntry;
import com.intel.mtwilson.datatypes.AuditLogSearchCriteria;
import com.intel.mtwilson.datatypes.BulkHostTrustResponse;
import com.intel.mtwilson.datatypes.CaInfo;
import com.intel.mtwilson.datatypes.HostConfigData;
import com.intel.mtwilson.datatypes.HostConfigDataList;
import com.intel.mtwilson.datatypes.HostConfigResponseList;
import com.intel.mtwilson.datatypes.HostLocation;
import com.intel.mtwilson.datatypes.HostResponse;
import com.intel.mtwilson.datatypes.HostTrustResponse;
import com.intel.mtwilson.datatypes.MLESearchCriteria;
import com.intel.mtwilson.datatypes.MleData;
import com.intel.mtwilson.datatypes.MleSource;
import com.intel.mtwilson.datatypes.ModuleWhiteList;
import com.intel.mtwilson.datatypes.OemData;
import com.intel.mtwilson.datatypes.OpenStackHostTrustLevelReport;
import com.intel.mtwilson.datatypes.OsData;
import com.intel.mtwilson.datatypes.PCRWhiteList;
import com.intel.mtwilson.datatypes.Role;
import com.intel.mtwilson.datatypes.TxtHost;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.datatypes.TxtHostRecordList;
import com.intel.mtwilson.datatypes.xml.HostTrustXmlResponse;
import com.intel.mtwilson.model.Hostname;
import com.intel.mtwilson.model.Sha1Digest;
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
public interface MtWilson {
    // ATTESTATION SERVICE
    
    HostLocation getHostLocation(Hostname hostname) throws IOException, ApiException, SignatureException;
    
    boolean addHostLocation(HostLocation hostLocObj) throws IOException, ApiException, SignatureException;

    HostTrustResponse getHostTrust(Hostname hostname) throws IOException, ApiException, SignatureException;

    HostResponse addHost(TxtHost host) throws IOException, ApiException, SignatureException, MalformedURLException;
    
    HostConfigResponseList addHosts(TxtHostRecordList hostRecords) throws IOException, ApiException, SignatureException;

    HostTrustResponse getHostTrustByAik(Sha1Digest aikSha1) throws IOException, ApiException, SignatureException;
    
    X509Certificate getCurrentTrustCertificateByAik(Sha1Digest aikSha1) throws IOException, ApiException, SignatureException;


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

    List<HostTrustXmlResponse> getSamlForMultipleHosts(Set<Hostname> hostnames, boolean forceVerify) throws IOException, ApiException, SignatureException;

    BulkHostTrustResponse getTrustForMultipleHosts(Set<Hostname> hostnames, boolean forceVerify) throws IOException, ApiException, SignatureException;

    
    String getHostAttestationReport(Hostname hostname) throws IOException, ApiException, SignatureException;
    
    AttestationReport getAttestationFailureReport(Hostname hostname)throws IOException, ApiException, SignatureException;
    
    
    AttestationReport getAttestationReport(Hostname hostname) throws IOException, ApiException, SignatureException;

    X509Certificate getTlsCertificateForTrustedHost(Hostname hostname) throws IOException, ApiException, SignatureException;;

    // WHITELIST SERVICE
    

    boolean addMLE(MleData mle) throws IOException, ApiException, SignatureException;

    boolean updateMLE(MleData mle) throws IOException, ApiException, SignatureException;

    List<MleData> searchMLE(String name) throws IOException, ApiException, SignatureException;

    MleData getMLEManifest(MLESearchCriteria criteria) throws IOException, ApiException, SignatureException;

    boolean deleteMLE(MLESearchCriteria criteria) throws IOException, ApiException, SignatureException;

    List<OemData> listAllOEM() throws IOException, ApiException, SignatureException;

    boolean addOEM(OemData oem) throws IOException, ApiException, SignatureException;

    boolean updateOEM(OemData oem) throws IOException, ApiException, SignatureException;

    boolean deleteOEM(String name) throws IOException, ApiException, SignatureException;

    List<OsData> listAllOS() throws IOException, ApiException, SignatureException;

    boolean updateOS(OsData os) throws IOException, ApiException, SignatureException;

    boolean addOS(OsData os) throws IOException, ApiException, SignatureException;

    boolean deleteOS(OsData os) throws IOException, ApiException, SignatureException;

    boolean addPCRWhiteList(PCRWhiteList pcrObj) throws IOException, ApiException, SignatureException;
    
    boolean updatePCRWhiteList(PCRWhiteList pcrObj) throws IOException, ApiException, SignatureException;
    
    boolean deletePCRWhiteList(PCRWhiteList pcrObj) throws IOException, ApiException, SignatureException;
    
    boolean addModuleWhiteList(ModuleWhiteList moduleObj) throws IOException, ApiException, SignatureException;
    
    boolean updateModuleWhiteList(ModuleWhiteList moduleObj) throws IOException, ApiException, SignatureException;
    
    boolean deleteModuleWhiteList(ModuleWhiteList moduleObj) throws IOException, ApiException, SignatureException;    
    
    List<ModuleWhiteList> listModuleWhiteListForMLE(String mleName, String mleVersion, 
            String osName, String osVersion, String oemName) throws IOException, ApiException, SignatureException;
    
    boolean addMleSource(MleSource mleSourceObj) throws IOException, ApiException, SignatureException;
    
    boolean updateMleSource(MleSource mleSourceObj) throws IOException, ApiException, SignatureException;
    
    boolean deleteMleSource(MleData mleDataObj) throws IOException, ApiException, SignatureException;
    
    String getMleSource(MleData mleDataObj) throws IOException, ApiException, SignatureException;    
    
    // MANAGEMENT SERVICE
    
    List<ApiClientInfo> searchApiClients(ApiClientSearchCriteria criteria) throws IOException, ApiException, SignatureException;
    
    List<AuditLogEntry> searchAuditLog(AuditLogSearchCriteria criteria)  throws IOException, ApiException, SignatureException;

    List<ApiClientInfo> listPendingAccessRequests() throws IOException, ApiException, SignatureException;

    ApiClientInfo getApiClientInfo(byte[] fingerprint) throws IOException, ApiException, SignatureException;
    
    boolean updateApiClient(ApiClientUpdateRequest info) throws IOException, ApiException, SignatureException;
    
    boolean deleteApiClient(byte[] fingerprint) throws IOException, ApiException, SignatureException;

    Role[] listAvailableRoles() throws IOException, ApiException, SignatureException;
    
    boolean registerHost(TxtHostRecord hostObj) throws IOException, ApiException, SignatureException;
    
    boolean registerHost(HostConfigData hostConfigObj) throws IOException, ApiException, SignatureException;
    
    boolean configureWhiteList(TxtHostRecord hostObj) throws IOException, ApiException, SignatureException;
    
    boolean configureWhiteList(HostConfigData hostConfigObj) throws IOException, ApiException, SignatureException;

    // TODO: deprecate in next release in favor of getSamlCertificates()
    X509Certificate getSamlCertificate() throws IOException, ApiException, SignatureException;

    Set<X509Certificate> getRootCaCertificates() throws IOException, ApiException, SignatureException;

    Set<X509Certificate> getPrivacyCaCertificates() throws IOException, ApiException, SignatureException;
    
    Set<X509Certificate> getSamlCertificates() throws IOException, ApiException, SignatureException;
    
    Set<X509Certificate> getTlsCertificates() throws IOException, ApiException, SignatureException;

    CaInfo getCaStatus() throws IOException, ApiException, SignatureException;
    
    void enableCaWithPassword(String newPassword) throws IOException, ApiException, SignatureException;
    
    void disableCa() throws IOException, ApiException, SignatureException;
    
    // New functions to support multiple host registration/update
    HostConfigResponseList registerHosts(TxtHostRecordList hostRecords) throws IOException, ApiException, SignatureException;
    
    HostConfigResponseList registerHosts(HostConfigDataList hostRecords) throws IOException, ApiException, SignatureException;
    
}
