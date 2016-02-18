/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson;

//import com.intel.mountwilson.as.common.Aes128DataCipher;
import com.intel.mtwilson.as.controller.*;
import com.intel.mtwilson.audit.controller.*;
import com.intel.dcsg.cpg.crypto.CryptographyException;
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
    private final MyPersistenceManager pm;
//    private final String dekBase64;
    TblApiClientJpaController mwApiClientHmac;
    MwApiClientHttpBasicJpaController mwApiClientHttpBasic;
    ApiClientX509JpaController mwApiClientX509;
    ApiRoleX509JpaController mwApiRoleX509;
    AuditLogEntryJpaController mwAuditLogEntry;
    MwCertificateX509JpaController mwCertificateX509;
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
    MwProcessorMappingJpaController mwProcessorMapping;
    MwAssetTagCertificateJpaController mwAssetTagCertificate;
    MwVmAttestationReportJpaController mwVmAttestationReportJpaController;
    MwHostPreRegistrationDetailsJpaController mwHostPreRegistrationDetailsJpaController;
//    public MyJpa(MyPersistenceManager pm) { this.pm = pm; }
    
    public MyJpa(MyPersistenceManager pm) { 
        this.pm = pm; 
//        this.dekBase64 = dekBase64; 
//        initDataEncryptionKey(dekBase64);
    }
    
    public TblApiClientJpaController mwApiClientHmac() throws IOException {
        return new TblApiClientJpaController(pm.getMSData());
//		if( mwApiClientHmac == null ) { mwApiClientHmac = new TblApiClientJpaController(pm.getMSData()); }
//        return mwApiClientHmac;
	}
    public MwApiClientHttpBasicJpaController mwApiClientHttpBasic() throws IOException {
        return new MwApiClientHttpBasicJpaController(pm.getASData());
//		if( mwApiClientHttpBasic == null ) { mwApiClientHttpBasic = new MwApiClientHttpBasicJpaController(pm.getASData()); }
//		return mwApiClientHttpBasic;
	}
    public ApiClientX509JpaController mwApiClientX509() throws IOException {
        return new ApiClientX509JpaController(pm.getMSData());
//		if( mwApiClientX509 == null ) { mwApiClientX509 = new ApiClientX509JpaController(pm.getMSData()); }
//		return mwApiClientX509;
	}
    public ApiRoleX509JpaController mwApiRoleX509() throws IOException {
        return new ApiRoleX509JpaController(pm.getMSData());
//		if( mwApiRoleX509 == null ) { mwApiRoleX509 = new ApiRoleX509JpaController(pm.getMSData()); }
//		return mwApiRoleX509;
	}
    public AuditLogEntryJpaController mwAuditLogEntry() throws IOException {
        return new AuditLogEntryJpaController(pm.getAuditData());
//		if( mwAuditLogEntry == null ) { mwAuditLogEntry = new AuditLogEntryJpaController(pm.getAuditData()); }
//		return mwAuditLogEntry;
	}
    public MwCertificateX509JpaController mwCertificateX509() throws IOException {
        return new MwCertificateX509JpaController(pm.getASData());
//		if( mwCertificateX509 == null ) { mwCertificateX509 = new MwCertificateX509JpaController(pm.getASData()); }
//		return mwCertificateX509;
	}
    public MwConfigurationJpaController mwConfiguration() throws IOException {
        return new MwConfigurationJpaController(pm.getMSData());
//		if( mwConfiguration == null ) { mwConfiguration = new MwConfigurationJpaController(pm.getMSData()); }
//		return mwConfiguration;
	}
    public TblEventTypeJpaController mwEventType() throws IOException {
        return new TblEventTypeJpaController(pm.getASData());
//		if( mwEventType == null ) { mwEventType = new TblEventTypeJpaController(pm.getASData()); }
//		return mwEventType;
	}
    public TblHostSpecificManifestJpaController mwHostSpecificManifest() throws IOException {
        return new TblHostSpecificManifestJpaController(pm.getASData());
//		if( mwHostSpecificManifest == null ) { mwHostSpecificManifest = new TblHostSpecificManifestJpaController(pm.getASData()); }
//		return mwHostSpecificManifest;
	}
    public TblHostsJpaController mwHosts() throws IOException, CryptographyException {
        return new TblHostsJpaController(pm.getASData());
//		if( mwHosts == null ) { mwHosts = new TblHostsJpaController(pm.getASData()); }
//		return mwHosts;
	}
    public MwKeystoreJpaController mwKeystore() throws IOException {
        return new MwKeystoreJpaController(pm.getASData());
//		if( mwKeystore == null ) { mwKeystore = new MwKeystoreJpaController(pm.getASData()); }
//		return mwKeystore;
	}
    public TblLocationPcrJpaController mwLocationPcr() throws IOException {
        return new TblLocationPcrJpaController(pm.getASData());
//		if( mwLocationPcr == null ) { mwLocationPcr = new TblLocationPcrJpaController(pm.getASData()); }
//		return mwLocationPcr;
	}
    public TblMleJpaController mwMle() throws IOException {
        return new TblMleJpaController(pm.getASData());
//		if( mwMle == null ) { mwMle = new TblMleJpaController(pm.getASData()); }
//		return mwMle;
	}
    public MwMleSourceJpaController mwMleSource() throws IOException {
        return new MwMleSourceJpaController(pm.getASData());
//		if( mwMleSource == null ) { mwMleSource = new MwMleSourceJpaController(pm.getASData()); }
//		return mwMleSource;
	}
    public TblModuleManifestJpaController mwModuleManifest() throws IOException {
        return new TblModuleManifestJpaController(pm.getASData());
//		if( mwModuleManifest == null ) { mwModuleManifest = new TblModuleManifestJpaController(pm.getASData()); }
//		return mwModuleManifest;
	}
    public TblModuleManifestLogJpaController mwModuleManifestLog() throws IOException {
        return new TblModuleManifestLogJpaController(pm.getASData());
//		if( mwModuleManifestLog == null ) { mwModuleManifestLog = new TblModuleManifestLogJpaController(pm.getASData()); }
//		return mwModuleManifestLog;
	}
    public TblOemJpaController mwOem() throws IOException {
        return new TblOemJpaController(pm.getASData());
//		if( mwOem == null ) { mwOem = new TblOemJpaController(pm.getASData()); }
//		return mwOem;
	}
    public TblOsJpaController mwOs() throws IOException {
        return new TblOsJpaController(pm.getASData());
//		if( mwOs == null ) { mwOs = new TblOsJpaController(pm.getASData()); }
//		return mwOs;
	}
    public TblPackageNamespaceJpaController mwPackageNamespace() throws IOException {
        return new TblPackageNamespaceJpaController(pm.getASData());
//		if( mwPackageNamespace == null ) { mwPackageNamespace = new TblPackageNamespaceJpaController(pm.getASData()); }
//		return mwPackageNamespace;
	}
    public TblPcrManifestJpaController mwPcrManifest() throws IOException {
        return new TblPcrManifestJpaController(pm.getASData());
//		if( mwPcrManifest == null ) { mwPcrManifest = new TblPcrManifestJpaController(pm.getASData()); }
//		return mwPcrManifest;
	}
    public MwPortalUserJpaController mwPortalUser() throws IOException {
        return new MwPortalUserJpaController(pm.getMSData());
//		if( mwPortalUser == null ) { mwPortalUser = new MwPortalUserJpaController(pm.getMSData()); }
//		return mwPortalUser;
	}
    public MwRequestLogJpaController mwRequestLog() throws IOException {
        return new MwRequestLogJpaController(pm.getASData()); 
//		if( mwRequestLog == null ) { mwRequestLog = new MwRequestLogJpaController(pm.getASData()); }
//		return mwRequestLog;
	}
    public TblRequestQueueJpaController mwRequestQueue() throws IOException {
        return new TblRequestQueueJpaController(pm.getASData());
//		if( mwRequestQueue == null ) { mwRequestQueue = new TblRequestQueueJpaController(pm.getASData()); }
//		return mwRequestQueue;
	}
    public TblSamlAssertionJpaController mwSamlAssertion() throws IOException {
        return new TblSamlAssertionJpaController(pm.getASData()); 
//		if( mwSamlAssertion == null ) { mwSamlAssertion = new TblSamlAssertionJpaController(pm.getASData()); }
//		return mwSamlAssertion;
	}
    public TblTaLogJpaController mwTaLog() throws IOException {
        return new TblTaLogJpaController(pm.getASData());
//		if( mwTaLog == null ) { mwTaLog = new TblTaLogJpaController(pm.getASData()); }
//		return mwTaLog;
	}
    public MwProcessorMappingJpaController mwProcessorMapping() throws IOException {
        return new MwProcessorMappingJpaController(pm.getASData());
//		if( mwProcessorMapping == null ) { mwProcessorMapping = new MwProcessorMappingJpaController(pm.getASData()); }
//		return mwProcessorMapping;
	}

    public MwAssetTagCertificateJpaController mwAssetTagCertificate() throws IOException {
        return new MwAssetTagCertificateJpaController(pm.getASData());
    }
    
    public MwMeasurementXmlJpaController mwMeasurementXml() throws IOException {
        return new MwMeasurementXmlJpaController(pm.getASData());
    }
    
    public MwVmAttestationReportJpaController mwVmAttestationReport() throws IOException {
        return new MwVmAttestationReportJpaController(pm.getASData());
    }
    
    public MwHostPreRegistrationDetailsJpaController mwHostPreRegistrationDetails() throws IOException {
        return new MwHostPreRegistrationDetailsJpaController(pm.getASData());
    }
    
}
