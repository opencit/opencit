/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson;

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
    
    boolean updateApiClient(ApiClientUpdateRequest info) throws IOException, ApiException, SignatureException;
    
    boolean deleteApiClient(byte[] fingerprint) throws IOException, ApiException, SignatureException;

    Role[] listAvailableRoles() throws IOException, ApiException, SignatureException;
    
    boolean registerHost(TxtHostRecord hostObj) throws IOException, ApiException, SignatureException;
    
    boolean registerHost(HostConfigData hostConfigObj) throws IOException, ApiException, SignatureException;
    
    boolean configureWhiteList(TxtHostRecord hostObj) throws IOException, ApiException, SignatureException;
    
    boolean configureWhiteList(HostConfigData hostConfigObj) throws IOException, ApiException, SignatureException;

    X509Certificate getSamlCertificate() throws IOException, ApiException, SignatureException;

    Set<X509Certificate> getCaCertificates() throws IOException, ApiException, SignatureException;
    
    CaInfo getCaStatus() throws IOException, ApiException, SignatureException;
    
    void enableCaWithPassword(String newPassword) throws IOException, ApiException, SignatureException;
    
    void disableCa() throws IOException, ApiException, SignatureException;
}
