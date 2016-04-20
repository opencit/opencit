/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup;

import com.intel.dcsg.cpg.crypto.RsaCredentialX509;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
//import com.intel.mtwilson.datatypes.TLSPolicy;
import com.intel.mtwilson.model.*;
import com.intel.mtwilson.setup.model.*;
import java.net.URL;
import java.security.KeyPair;
import java.security.cert.X509Certificate;

/**
 *
 * @author jbuhacoff
 */
public class SetupContext {
    public SetupTarget target;
    
    // ATTESTATION SERVICE 
    
    public InternetAddress serverAddress; // do we really need this if we're going to keep the URL ???  yes because we need server address and also an external url to hand out to clients, which could be load balancer url
//    public Integer serverPort; // do we really need this if we're going to keep the URL ??? 
    
    public Database attestationServiceDatabase; // could be same database but different login credentials than the other services
     
    public AdminUser admin; // first admin user that can then approve all other accounts and maange settings
    
    public DistinguishedName dn; // distinguished name defaults for all certificates (usually the common name part is set automatically according to the certificate type, so don't need use input for it)
    
    public RsaCredentialX509 rootCa;
    
    public KeyPair tlsKeypair;
    public String tlsCertificateFile;
    public X509Certificate tlsCertificate;
    public String tlsKeystoreFile;
    public String tlsKeystorePassword;
    public SimpleKeystore tlsKeystore; // only applies to Tomcat, Glassfish, or other Java container... for apache and nginx it's different.
    public String tlsKeyAlias;
    public String tlsKeyPassword;
    
    public KeyPair samlKeypair;
    public X509Certificate samlCertificate;
    public String samlCertificateFile; 
    public String samlIssuer; 
    public String samlKeystoreFile; 
    public String samlKeystorePassword; 
    public SimpleKeystore samlKeystore;
    public String samlKeyAlias;
    public String samlKeyPassword;
    public Integer samlValidityPeriodInSeconds;
    
    
//    public KeyPair pcaKeypair;
//    public X509Certificate pcaCertificate;
    public PrivacyCA privacyCA; // what is used to download Mt Wilson EK Signing Key (aka Privacy CA EK Signing Key) 
    
    public String aikqverifyHome; // path to aikqverify data directory & bin directory... 
    public String aikqverifyCommand; // path or command name of the aikqverify command (used by attestation service to verify tpm quotes);  usually aikqverify on linux and aikqverify.exe on windows... this should just be "hardcoded" in the app. , it's not something a user needs to configure.  determine a suitable location fo this program on linux and windows, make sure th einstaller puts it there, and rely on that. 
    public String opensslCommand; 
    
    public Timeout trustAgentTimeout;
    
    public String dataEncryptionKeyBase64; 
    
    // MANAGEMENT SERVICE
    public URL serverUrl; // typically https://serverAddress:serverPort 
    public Database managementServiceDatabase; // could be same database but different login credentials than the other services
    
    public String automationKeyAlias;
    public String automationKeyPassword;
    public String automationKeystoreFile;
//    public TLSPolicy automationTlsPolicy;
    
    public WebServiceSecurityPolicy securityPolicy;
    
    public String managementServiceKeystoreDir; 
    public String biosPCRs; //  semicolon separated list like  0;1    
    public String vmmPCRs; // semicolon separatetd list like 18;19;20  

    // AUDIT HANDLER
    public Database auditDatabase; // could be same database but different login credentials than the other services
    public boolean auditEnabled;
    public boolean auditLogUnchangedColumns;
    public boolean auditAsync; 
    
    
    // PORTALS
    public String portalUserKeystoreDir;
    public Timeout portalSessionTimeout;
    public String portalHostTypeList;
    public Timeout portalApiKeyExpirationNotice; // typically configured as number of months
    
    
    // OTHER
    public WebContainerType webContainerType;
}
