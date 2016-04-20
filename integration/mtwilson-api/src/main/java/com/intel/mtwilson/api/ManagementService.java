/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.api;

import com.intel.mtwilson.datatypes.*;
import java.io.IOException;
import java.security.SignatureException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Set;

/**
 *
 * @author jbuhacoff
 */
public interface ManagementService {

    List<ApiClientInfo> searchApiClients(ApiClientSearchCriteria criteria) throws IOException, ApiException, SignatureException;
    
    List<AuditLogEntry> searchAuditLog(AuditLogSearchCriteria criteria)  throws IOException, ApiException, SignatureException;

    List<ApiClientInfo> listPendingAccessRequests() throws IOException, ApiException, SignatureException;

    ApiClientInfo getApiClientInfo(byte[] fingerprint) throws IOException, ApiException, SignatureException;
    
    // Adding back this interface for the completeness even though we have the register method in ApiClient.java
    boolean registerApiClient(ApiClientCreateRequest apiClient) throws IOException, ApiException, SignatureException;
    
    boolean updateApiClient(ApiClientUpdateRequest info) throws IOException, ApiException, SignatureException;
    
    boolean deleteApiClient(byte[] fingerprint) throws IOException, ApiException, SignatureException;

    Role[] listAvailableRoles() throws IOException, ApiException, SignatureException;
    
    boolean registerHost(TxtHostRecord hostObj) throws IOException, ApiException, SignatureException;
    
    boolean registerHost(HostConfigData hostConfigObj) throws IOException, ApiException, SignatureException;
    
    boolean configureWhiteList(TxtHostRecord hostObj) throws IOException, ApiException, SignatureException;
    
    boolean configureWhiteList(HostConfigData hostConfigObj) throws IOException, ApiException, SignatureException;

    X509Certificate getSamlCertificate() throws IOException, ApiException, SignatureException;

    Set<X509Certificate> getRootCaCertificates() throws IOException, ApiException, SignatureException;

    Set<X509Certificate> getPrivacyCaCertificates() throws IOException, ApiException, SignatureException;
    
    Set<X509Certificate> getSamlCertificates() throws IOException, ApiException, SignatureException;
    
    Set<X509Certificate> getTlsCertificates() throws IOException, ApiException, SignatureException;

    //CaInfo getCaStatus() throws IOException, ApiException, SignatureException;
    
   // void enableCaWithPassword(String newPassword) throws IOException, ApiException, SignatureException;
    
   // void disableCa() throws IOException, ApiException, SignatureException;
    
    // New functions to support multiple host registration/update
    HostConfigResponseList registerHosts(TxtHostRecordList hostRecords) throws IOException, ApiException, SignatureException;
    
    HostConfigResponseList registerHosts(HostConfigDataList hostRecords) throws IOException, ApiException, SignatureException;
    
    String getLocaleForUser(String username) throws IOException, ApiException, SignatureException;
    
    String setLocaleForUser(PortalUserLocale pul) throws IOException, ApiException, SignatureException;
    
    String[] getLocales() throws IOException, ApiException, SignatureException;
}
