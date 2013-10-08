/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup;

import com.intel.mtwilson.crypto.Aes128;
import com.intel.mtwilson.crypto.RsaCredentialX509;
import com.intel.mtwilson.crypto.SimpleKeystore;
import com.intel.mtwilson.datatypes.Hostname;
import com.intel.mtwilson.datatypes.InternetAddress;
import com.intel.mtwilson.datatypes.TLSPolicy;
import com.intel.mtwilson.setup.model.*;
import java.net.URL;
import java.security.KeyPair;
import java.security.KeyStore;
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
    public String samlCertificateFile; // XXX TODO should not be configurable. 
    public String samlIssuer; // XXX TODO:  this should not be configurable, it should be the SUBJECT from the server's SAML certificate !!!!
    public String samlKeystoreFile; // XXX TODO should not be configurable.  decide on a filename in the mt wilson config dir and that's what it has to be. if the user wants to use a different file, they can copy one to replace it, or link it at filesystem level.
    public String samlKeystorePassword; 
    public SimpleKeystore samlKeystore;
    public String samlKeyAlias;
    public String samlKeyPassword;
    public Integer samlValidityPeriodInSeconds;
    
    // XXX TODO: maybe for consistency we should generate the privacy ca and install it... instead of the niarl code ??
//    public KeyPair pcaKeypair;
//    public X509Certificate pcaCertificate;
    public PrivacyCA privacyCA; // what is used to download Mt Wilson EK Signing Key (aka Privacy CA EK Signing Key) 
    
    public String aikqverifyHome; // path to aikqverify data directory & bin directory... XXX TODO first, those two directories should not be combined. second, attestation service already has data and bin directories, so those should be used automatically.  this configuration item should be deprecated
    public String aikqverifyCommand; // path or command name of the aikqverify command (used by attestation service to verify tpm quotes);  usually aikqverify on linux and aikqverify.exe on windows... this should just be "hardcoded" in the app. , it's not something a user needs to configure.  determine a suitable location fo this program on linux and windows, make sure th einstaller puts it there, and rely on that. 
    public String opensslCommand; // XXX TODO  same issue as aikqverify Command ... needs to be at a fixed place or auto-detected on server, not configurable.
    
    public Timeout trustAgentTimeout;
    
    public String dataEncryptionKeyBase64; 
    
    // MANAGEMENT SERVICE
    public URL serverUrl; // typically https://serverAddress:serverPort 
    public Database managementServiceDatabase; // could be same database but different login credentials than the other services
    
    // XXX TODO the "management servcie automation key" is used to connect o auttestation service and execute command on behalf of the user who is using the managemnent console automation scripts "configure whitelist" and "register host";  problem is it 's not a good idea since using a separate key means all the audit logging will lose the true identity of the caller. what really needs to happen is the automation features need to move into attestation service & whitelist service directly instead of being aggregated by a middleman, OR move into a client-side program such as a desktop app or the management console itself (which uses the logged-in user's identity key)
    public String automationKeyAlias;
    public String automationKeyPassword;
    public String automationKeystoreFile;
    public TLSPolicy automationTlsPolicy;
    
    public WebServiceSecurityPolicy securityPolicy;
    
    public String managementServiceKeystoreDir; // XXX TODO will not be needed when we get rid of the "automation user" defined above
    public String biosPCRs; //  semicolon separated list like  0;1     XXX TODO this should not eb configurable here, it should be either cocded into a trust verification class or in the whitelist database, or if it's just for the UI it should be n a UI database table
    public String vmmPCRs; // semicolon separatetd list like 18;19;20    XXX TODO this should not eb configurable here, it should be either cocded into a trust verification class or in the whitelist database, or if it's just for the UI it should be n a UI database table

    // AUDIT HANDLER
    public Database auditDatabase; // could be same database but different login credentials than the other services
    public boolean auditEnabled;
    public boolean auditLogUnchangedColumns;
    public boolean auditAsync; // XXX TODO: probably should get rid of this. if auditing performance is an issue, then it needs to be reimplemented in something other than java, for example at each native database  using its own audit system ;  for example mysql has a "binary log" capability, but also tehre are SQL scripts to automatically add auditing to every table in a schema using triggers etc.
    
    
    // PORTALS
    public String portalUserKeystoreDir;
    public Timeout portalSessionTimeout;
    public String portalHostTypeList;
    public Timeout portalApiKeyExpirationNotice; // typically configured as number of months
    
    
    // OTHER
    public WebContainerType webContainerType;
}
