/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.tasks;

import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.dcsg.cpg.crypto.RsaCredentialX509;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.mtwilson.datatypes.Role;
import com.intel.dcsg.cpg.io.ByteArrayResource;
import com.intel.dcsg.cpg.io.Resource;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.validation.Fault;
import com.intel.dcsg.cpg.x509.X509Builder;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.My;
import com.intel.mtwilson.ms.data.ApiClientX509;
import com.intel.mtwilson.ms.data.ApiRoleX509;
import com.intel.mtwilson.ms.data.MwPortalUser;
import com.intel.mtwilson.setup.DatabaseSetupTask;
import com.intel.mtwilson.shiro.authc.password.PasswordCredentialsMatcher;
import com.intel.mtwilson.shiro.jdbi.LoginDAO;
import com.intel.mtwilson.shiro.jdbi.MyJdbi;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.KeyPair;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.IOUtils;

/**
 * This task is only needed until we transition the mtwilson-portal to shiro.
 *
 * This class is a partial replacement to BoostrapUser in mtwilson-console but
 * it focuses only on creating the administrator user during application setup.
 * The bootstrap-user command line tool should be kept separately as it is to
 * create any user from the command line, not just an admin user.
 *
 * Pre-requisites: CreateCertificateAuthorityKey
 *
 * @author jbuhacoff
 */
public class CreatePortalAdministrator extends DatabaseSetupTask {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CreatePortalAdministrator.class);
    private String adminUsername;
    private String adminPassword;

    public String getAdminUsername() {
        return adminUsername;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    @Override
    protected void configure() throws Exception {
        if (adminUsername == null) {
            adminUsername = "admin";
            log.info("Creating administrator user with default username 'admin'");
        }
        if (adminPassword == null) {
            adminPassword = RandomUtil.randomBase64String(8).replace("/", "!").replace("=", "-"); // TODO:  use more special characters ; use a string generation function instead of a base64 function
            log.info("Generated random password for administrator user"); // but do not print it to the log, that's permanent -- the setup application needs to grab it from the getAdminPassword() and display to the user
        }
        // check for the required database tables to be present
        try (Connection c = My.jdbc().connection()) {
            requireTable(c, "mw_portal_user");
            requireTable(c, "mw_api_client_x509");
            requireTable(c, "mw_api_role_x509");
        }
        // check that the mtwilson CA key is present
        if (!My.configuration().getCaKeystoreFile().exists()) {
            configuration("Mt Wilson CA is required to create the portal administrator");
        }
        // we do not store the admin username or password in configuration - it is only to display to the administrator
    }

    @Override
    protected void validate() throws Exception {
        List<MwPortalUser> users = My.jpa().mwPortalUser().findMwPortalUserByUsernameEnabled(adminUsername);
        for (MwPortalUser user : users) {
            log.debug("Found user {} status {} enabled {} uuid {}", user.getUsername(), user.getStatus(), user.getEnabled(), user.getUuid_hex());
        }
        if (users.isEmpty()) {
            validation("Portal administrator user does not exist");
        }
    }

    // also, instead of using the deprecated RsaUtil.createX509CertificateWithIssuer,
    // this method uses the new X509Builder class.
    private SimpleKeystore createUserKeystore(String username, String password) throws Exception {
        // load the mtwilson CA to use it as an issuer
        // XXX TODO  this code is duplicated from CreateCertificateAuthorityKey  validate()  -  needs to be refactored to a CA repository layer so they can both just say getCAPrivateKey() etc
        byte[] combinedPrivateKeyAndCertPemBytes;
        try (FileInputStream cakeyIn = new FileInputStream(My.configuration().getCaKeystoreFile())) {
            combinedPrivateKeyAndCertPemBytes = IOUtils.toByteArray(cakeyIn);
        }
        PrivateKey cakey = RsaUtil.decodePemPrivateKey(new String(combinedPrivateKeyAndCertPemBytes));
        X509Certificate cacert = X509Util.decodePemCertificate(new String(combinedPrivateKeyAndCertPemBytes));

        // create the keystore and a new credential
        SimpleKeystore keystore = new SimpleKeystore(new ByteArrayResource(), password); // KeyManagementException
        KeyPair keypair = RsaUtil.generateRsaKeyPair(RsaUtil.MINIMUM_RSA_KEY_SIZE); // NoSuchAlgorithmException
        X509Builder builder = X509Builder.factory();
        X509Certificate certificate = builder
                .commonName(username)
                .subjectPublicKey(keypair.getPublic())
                .expires(RsaUtil.DEFAULT_RSA_KEY_EXPIRES_DAYS, TimeUnit.DAYS)
                .issuerPrivateKey(cakey)
                .issuerName(cacert)
                .keyUsageDigitalSignature()
                .keyUsageNonRepudiation()
                .keyUsageKeyEncipherment()
                .extKeyUsageIsCritical()
                .randomSerial()
                .build();
        if( certificate == null ) {
//            validation("Failed to create certificate");
            List<Fault> faults = builder.getFaults();
            for(Fault fault : faults) {
                log.error(fault.toString());
                validation(fault);
            }
        }
//             = RsaUtil.generateX509Certificate("CN="+username, keypair, RsaUtil.DEFAULT_RSA_KEY_EXPIRES_DAYS); // GeneralSecurityException
        keystore.addKeyPairX509(keypair.getPrivate(), certificate, username, password, cacert); // KeyManagementException
//            keystore.save(); // KeyStoreException, IOException, CertificateException    

        // add the CA cert itself to the keystore
        keystore.addTrustedCaCertificate(cacert, "mtwilson-cacert");
        return keystore;

    }

    // TODO:  duplicated code from setup task CreateSamlCertificate , should be 
    // refactored to a SAML repository or business layer
    private X509Certificate getSamlCertificate() throws Exception {
        SimpleKeystore samlKeystore = new SimpleKeystore(My.configuration().getSamlKeystoreFile(), My.configuration().getSamlKeystorePassword());
        for (String alias : samlKeystore.aliases()) {
            log.debug("SAML Keystore alias: {}", alias);
            // make sure it has a SAML private key and certificate inside
            try {
                RsaCredentialX509 credential = samlKeystore.getRsaCredentialX509(alias, My.configuration().getSamlKeystorePassword());
                log.debug("SAML certificate: {}", credential.getCertificate().getSubjectX500Principal().getName());
                return credential.getCertificate();
            } catch (IOException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableEntryException | CertificateEncodingException | CryptographyException e) {
                log.debug("Cannot read SAML key from keystore", e);
//                validation("Cannot read SAML key from keystore"); // we are assuming the keystore only has one private key entry ... 
            }
        }
        return null;
    }
    // TODO:  duplicated code from setup task CreateTlsCertificate , should be 
    // refactored to a SAML repository or business layer

    private X509Certificate getTlsCertificate() throws Exception {
        SimpleKeystore tlsKeystore = new SimpleKeystore(My.configuration().getTlsKeystoreFile(), My.configuration().getTlsKeystorePassword());
        for (String alias : tlsKeystore.aliases()) {
            log.debug("TLS Keystore alias: {}", alias);
            // make sure it has a SAML private key and certificate inside
            try {
                RsaCredentialX509 credential = tlsKeystore.getRsaCredentialX509(alias, My.configuration().getTlsKeystorePassword());
                log.debug("TLS certificate: {}", credential.getCertificate().getSubjectX500Principal().getName());
                return credential.getCertificate();
            } catch (IOException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableEntryException | CertificateEncodingException | CryptographyException e) {
                log.debug("Cannot read TLS key from keystore", e);
//                validation("Cannot read SAML key from keystore"); // we are assuming the keystore only has one private key entry ... 
            }
        }
        return null;
    }

    @Override
    protected void execute() throws Exception {
        // create the admin user's keystore.  instead of using KeystoreUtil.createUserInResource
        // as bootstrap user does, we take advantage of the fact that setup tasks can only be
        // run on the mtwilson server itself so instead of using the APIs to configure the
        // keystore with trusted certs, we can load those trusted certs directly and add them.
        // this approach also resolves the tls policy UX issue because avoiding API calls
        // means we don't need to ask the user to confirm the tls certificate.

        SimpleKeystore keystore = createUserKeystore(adminUsername, adminPassword); // already saves the keystore to resource
        // add the mtwilson  saml and tls certs to the keystore
        X509Certificate saml = getSamlCertificate();
        if (saml != null) {
            keystore.addTrustedSamlCertificate(saml, "mtwilson-saml");
        }
        X509Certificate tls = getTlsCertificate();
        if (tls != null) {
            keystore.addTrustedSslCertificate(tls, "mtwilson-tls");
        }

        keystore.save();

        MwPortalUser portalUser = new MwPortalUser();
        portalUser.setComment("Automatically generated");
        portalUser.setEnabled(true);
        portalUser.setKeystore(((ByteArrayResource) keystore.getResource()).toByteArray()); // we know it's bytearrayresource because that's what we used in createUserKeystore above
        portalUser.setStatus("APPROVED"); // TODO:  should use a constant
        portalUser.setUsername(adminUsername);
        portalUser.setUuid_hex(new UUID().toString());
        My.jpa().mwPortalUser().create(portalUser);

        // create a corresponding api client x509 record and roles - XXX duplicate code from APIClientBO in mtwilson-management
        X509Certificate certificate = keystore.getX509Certificate(adminUsername);
        ApiClientX509 apiClient = new ApiClientX509();
        apiClient.setCertificate(certificate.getEncoded());
        apiClient.setComment("Automatically generated");
        apiClient.setEnabled(true);
        apiClient.setExpires(certificate.getNotAfter());
        apiClient.setFingerprint(Sha1Digest.digestOf(certificate.getEncoded()).toByteArray());
        apiClient.setIssuer(certificate.getIssuerX500Principal().getName());
        apiClient.setName(certificate.getSubjectX500Principal().getName());
        apiClient.setSerialNumber(certificate.getSerialNumber().intValue());
        apiClient.setStatus("APPROVED"); // TODO:  should use a constant
        apiClient.setUser_uuid_hex(portalUser.getUuid_hex());
        apiClient.setUuid_hex(new UUID().toString());
        My.jpa().mwApiClientX509().create(apiClient); // populates the id attribute, which we'll need for roles below

        ArrayList<ApiRoleX509> apiClientRoles = new ArrayList<>();
        apiClientRoles.add(new ApiRoleX509(apiClient.getId(), Role.Attestation.getName()));
        apiClientRoles.add(new ApiRoleX509(apiClient.getId(), Role.Whitelist.getName()));
        apiClientRoles.add(new ApiRoleX509(apiClient.getId(), Role.Security.getName()));
        apiClientRoles.add(new ApiRoleX509(apiClient.getId(), Role.AssetTagManagement.getName()));
        apiClientRoles.add(new ApiRoleX509(apiClient.getId(), Role.Report.getName()));
        for (ApiRoleX509 apiClientRole : apiClientRoles) {
            apiClientRole.setApiClientX509(apiClient);
            My.jpa().mwApiRoleX509().create(apiClientRole);
        }
        apiClient.setApiRoleX509Collection(apiClientRoles);

    }
}
