/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson;

import com.intel.mtwilson.as.controller.*;
import com.intel.mtwilson.audit.controller.*;
import com.intel.mtwilson.crypto.CryptographyException;
import com.intel.mtwilson.ms.controller.*;
import java.io.IOException;

/**
 * Convenience class to instantiate JPA controllers for the purpose of writing JUnit tests...
 * Using this class allows you to eliminate a lot of boilerplate from your tests.
 * 
 * Instead of writing this:
 * 
 * TblHostsJpaController hosts = new TblHostsJpaController(ASPersistenceManager.createEntityManagerFactory("ASDataPU", ASConfig.getJpaProperties()));
 * (and that only works when it executes on a server with /etc/intel/cloudsecurity/attestation-service.properties)
 * 
 * You write this:
 * 
 * TblHostsJpaController hosts = My.jpa().mwHosts();
 * 
 * The naming convention is that given a table name like mw_api_client_x509, the method name is
 * chosen by removing underscores, and capitalizing the first letter of 
 * every word after the first one. So the method to get the corresponding JPA controller
 * in this example would be mwApiClientX509().
 * 
 * 
 * @author jbuhacoff
 */
public class MyJpa {
    TblApiClientJpaController mwApiClientHmac;
    MwApiClientHttpBasicJpaController mwApiClientHttpBasic;
    ApiClientX509JpaController mwApiClientX509;
    ApiRoleX509JpaController mwApiRoleX509;
    AuditLogEntryJpaController mwAuditLogEntry;
    MwCertificateX509JpaController mwCertificateX509;
    // XXX TODO we don't have a controller for the schema changelog   mw_changelog !!!
    MwConfigurationJpaController mwConfiguration;
    TblEventTypeJpaController mwEventType;
    TblHostSpecificManifestJpaController mwHostSpecificManifest;
    TblHostsJpaController mwHosts;
    MwKeystoreJpaController mwKeystore;
    TblLocationPcrJpaController mwLocationPcr;
    TblMleJpaController mwMle;
    MwMleSourceJpaController mwMleSource;
    TblModuleManifestJpaController mwModuleManifest;
    TblModuleManifestLogJpaController mwModuleManifestLog;
    TblOemJpaController mwOem;
    TblOsJpaController mwOs;
    TblPackageNamespaceJpaController mwPackageNamespace;
    TblPcrManifestJpaController mwPcrManifest;
    MwPortalUserJpaController mwPortalUser;
    MwRequestLogJpaController mwRequestLog;
    TblRequestQueueJpaController mwRequestQueue;
    TblSamlAssertionJpaController mwSamlAssertion;
    TblTaLogJpaController mwTaLog;

    public TblApiClientJpaController mwApiClientHmac() throws IOException {
		if( mwApiClientHmac == null ) { mwApiClientHmac = new TblApiClientJpaController(My.persistenceManager().getMSData()); }
        return mwApiClientHmac;
	}
    public MwApiClientHttpBasicJpaController mwApiClientHttpBasic() throws IOException {
		if( mwApiClientHttpBasic == null ) { mwApiClientHttpBasic = new MwApiClientHttpBasicJpaController(My.persistenceManager().getASData()); }
		return mwApiClientHttpBasic;
	}
    public ApiClientX509JpaController mwApiClientX509() throws IOException {
		if( mwApiClientX509 == null ) { mwApiClientX509 = new ApiClientX509JpaController(My.persistenceManager().getMSData()); }
		return mwApiClientX509;
	}
    public ApiRoleX509JpaController mwApiRoleX509() throws IOException {
		if( mwApiRoleX509 == null ) { mwApiRoleX509 = new ApiRoleX509JpaController(My.persistenceManager().getMSData()); }
		return mwApiRoleX509;
	}
    public AuditLogEntryJpaController mwAuditLogEntry() throws IOException {
		if( mwAuditLogEntry == null ) { mwAuditLogEntry = new AuditLogEntryJpaController(My.persistenceManager().getAuditData()); }
		return mwAuditLogEntry;
	}
    public MwCertificateX509JpaController mwCertificateX509() throws IOException {
		if( mwCertificateX509 == null ) { mwCertificateX509 = new MwCertificateX509JpaController(My.persistenceManager().getASData()); }
		return mwCertificateX509;
	}
    // XXX TODO we don't have a controller for the schema changelog   mw_changelog !!!
    public MwConfigurationJpaController mwConfiguration() throws IOException {
		if( mwConfiguration == null ) { mwConfiguration = new MwConfigurationJpaController(My.persistenceManager().getMSData()); }
		return mwConfiguration;
	}
    public TblEventTypeJpaController mwEventType() throws IOException {
		if( mwEventType == null ) { mwEventType = new TblEventTypeJpaController(My.persistenceManager().getASData()); }
		return mwEventType;
	}
    public TblHostSpecificManifestJpaController mwHostSpecificManifest() throws IOException {
		if( mwHostSpecificManifest == null ) { mwHostSpecificManifest = new TblHostSpecificManifestJpaController(My.persistenceManager().getASData()); }
		return mwHostSpecificManifest;
	}
    public TblHostsJpaController mwHosts() throws IOException, CryptographyException {
		if( mwHosts == null ) { mwHosts = new TblHostsJpaController(My.persistenceManager().getASData()); }
		return mwHosts;
	}
    public MwKeystoreJpaController mwKeystore() throws IOException {
		if( mwKeystore == null ) { mwKeystore = new MwKeystoreJpaController(My.persistenceManager().getASData()); }
		return mwKeystore;
	}
    public TblLocationPcrJpaController mwLocationPcr() throws IOException {
		if( mwLocationPcr == null ) { mwLocationPcr = new TblLocationPcrJpaController(My.persistenceManager().getASData()); }
		return mwLocationPcr;
	}
    public TblMleJpaController mwMle() throws IOException {
		if( mwMle == null ) { mwMle = new TblMleJpaController(My.persistenceManager().getASData()); }
		return mwMle;
	}
    public MwMleSourceJpaController mwMleSource() throws IOException {
		if( mwMleSource == null ) { mwMleSource = new MwMleSourceJpaController(My.persistenceManager().getASData()); }
		return mwMleSource;
	}
    public TblModuleManifestJpaController mwModuleManifest() throws IOException {
		if( mwModuleManifest == null ) { mwModuleManifest = new TblModuleManifestJpaController(My.persistenceManager().getASData()); }
		return mwModuleManifest;
	}
    public TblModuleManifestLogJpaController mwModuleManifestLog() throws IOException {
		if( mwModuleManifestLog == null ) { mwModuleManifestLog = new TblModuleManifestLogJpaController(My.persistenceManager().getASData()); }
		return mwModuleManifestLog;
	}
    public TblOemJpaController mwOem() throws IOException {
		if( mwOem == null ) { mwOem = new TblOemJpaController(My.persistenceManager().getASData()); }
		return mwOem;
	}
    public TblOsJpaController mwOs() throws IOException {
		if( mwOs == null ) { mwOs = new TblOsJpaController(My.persistenceManager().getASData()); }
		return mwOs;
	}
    public TblPackageNamespaceJpaController mwPackageNamespace() throws IOException {
		if( mwPackageNamespace == null ) { mwPackageNamespace = new TblPackageNamespaceJpaController(My.persistenceManager().getASData()); }
		return mwPackageNamespace;
	}
    public TblPcrManifestJpaController mwPcrManifest() throws IOException {
		if( mwPcrManifest == null ) { mwPcrManifest = new TblPcrManifestJpaController(My.persistenceManager().getASData()); }
		return mwPcrManifest;
	}
    public MwPortalUserJpaController mwPortalUser() throws IOException {
		if( mwPortalUser == null ) { mwPortalUser = new MwPortalUserJpaController(My.persistenceManager().getMSData()); }
		return mwPortalUser;
	}
    public MwRequestLogJpaController mwRequestLog() throws IOException {
		if( mwRequestLog == null ) { mwRequestLog = new MwRequestLogJpaController(My.persistenceManager().getASData()); }
		return mwRequestLog;
	}
    public TblRequestQueueJpaController mwRequestQueue() throws IOException {
		if( mwRequestQueue == null ) { mwRequestQueue = new TblRequestQueueJpaController(My.persistenceManager().getASData()); }
		return mwRequestQueue;
	}
    public TblSamlAssertionJpaController mwSamlAssertion() throws IOException {
		if( mwSamlAssertion == null ) { mwSamlAssertion = new TblSamlAssertionJpaController(My.persistenceManager().getASData()); }
		return mwSamlAssertion;
	}
    public TblTaLogJpaController mwTaLog() throws IOException {
		if( mwTaLog == null ) { mwTaLog = new TblTaLogJpaController(My.persistenceManager().getASData()); }
		return mwTaLog;
	}

}
