/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.setup;

import com.intel.mtwilson.user.management.rest.v2.model.RolePermission;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginCertificate;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginPassword;
import com.intel.mtwilson.user.management.rest.v2.model.User;
import com.intel.mtwilson.user.management.rest.v2.model.Status;
import com.intel.mtwilson.user.management.rest.v2.model.Role;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.dcsg.cpg.crypto.RsaCredentialX509;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.crypto.Sha256Digest;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.io.ByteArrayResource;
import com.intel.dcsg.cpg.io.Platform;
import com.intel.mtwilson.crypto.password.PasswordUtil;
import com.intel.mtwilson.setup.DatabaseSetupTask;
import com.intel.mtwilson.shiro.jdbi.LoginDAO;
import com.intel.mtwilson.shiro.jdbi.MyJdbi;
import java.io.File;
import java.util.List;
import org.apache.commons.io.FileUtils;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.validation.Fault;
import com.intel.dcsg.cpg.x509.X509Builder;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.Folders;
import com.intel.mtwilson.My;
import com.intel.mtwilson.ms.controller.exceptions.MSDataException;
import com.intel.mtwilson.ms.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.ms.data.MwPortalUser;
import java.io.IOException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.sql.Connection;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;

/**
 * we do not store the admin username or password in configuration - the application
 * must display them to the administrator
 * 
 * You can execute this task from command line with something like this:
 * mtwilson setup setup-manager create-admin-user
 * 
 * @author jbuhacoff
 */
public class CreateAdminUser extends DatabaseSetupTask {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CreateAdminUser.class);
    public static final String ADMINISTRATOR_ROLE = "administrator"; 
    public static final String APPROVED = "APPROVED";
    private String username;
    private String password;
    private List<Fault> certificateBuilderFaults = null; // only populated if during execution the certificate builder fails; then if application calls validation afterwards we can copy these faults to our validation faults to inform the user; if not, the faults get logged to the error log anyway

    private String getUsername() {
        if (System.getenv("MC_FIRST_USERNAME") != null) {
            return System.getenv("MC_FIRST_USERNAME");
        }
        return "admin";
    }

    private String getPassword() {
        if (System.getenv("MC_FIRST_PASSWORD") != null) {
            return System.getenv("MC_FIRST_PASSWORD");
        }
        return RandomStringUtils.randomAscii(16);
    }

    @Override
    protected void configure() throws Exception {
        username = getUsername();
        password = getPassword();
        // check that the mtwilson CA key is present
        if (!My.configuration().getCaKeystoreFile().exists()) {
            configuration("Mt Wilson CA is required to create the portal administrator");
        }        
        // check for the required database tables to be present
        try (Connection c = My.jdbc().connection()) {
            requireTable(c, "mw_role");
            requireTable(c, "mw_role_permission");
            requireTable(c, "mw_user");
            requireTable(c, "mw_user_login_password");
            requireTable(c, "mw_user_login_password_role");
            requireTable(c, "mw_user_login_certificate");
            requireTable(c, "mw_user_login_certificate_role");
            requireTable(c, "mw_portal_user");
        }
        
    }

    Set<UUID> getRoleUuidList(List<Role> roles) {
        HashSet<UUID> uuids = new HashSet<>();
        for (Role role : roles) {
            uuids.add(role.getId());
        }
        return uuids;
    }
    
    // see also LoginPassword command
    private Set<String> toStrings(Set<UUID> uuids) {
        HashSet<String> set = new HashSet<>();
        for(UUID uuid : uuids) {
            set.add(uuid.toString());
        }
        return set;
    }

    @Override
    protected void validate() throws Exception {
        // ensure we have an admin user created with permissions assigned - at least one assigned login method must have them
        boolean isAdminPermissionAssigned = false;
        try (LoginDAO loginDAO = MyJdbi.authz()) {
            UserLoginPassword userLoginPassword = loginDAO.findUserLoginPasswordByUsername(username);
            if (userLoginPassword == null) {
                validation("User does not exist or does not have a password: %s", username);
            }
            else {
                List<Role> passwordRoles = loginDAO.findRolesByUserLoginPasswordId(userLoginPassword.getId());
                Set<UUID> passwordRoleIds = getRoleUuidList(passwordRoles);
                List<RolePermission> passwordRolePermissions = loginDAO.findRolePermissionsByPasswordRoleIds(toStrings(passwordRoleIds));
                if (passwordRolePermissions == null || passwordRolePermissions.isEmpty()) {
                    validation("User does not have password permissions assigned: %s", username);
                }
                else {
                    for (RolePermission rolePermission : passwordRolePermissions) {
                        if (rolePermission.getPermitDomain().equals("*") && rolePermission.getPermitAction().equals("*") && rolePermission.getPermitSelection().equals("*")) {
                            isAdminPermissionAssigned = true;
                        }
                    }
                }
            }
            // now check for the admin user's x509 login
            UserLoginCertificate userLoginCertificate = loginDAO.findUserLoginCertificateByUsername(username);
            if (userLoginCertificate == null) {
                validation("User does not exist or does not have a certificate");
            }
            else {
                List<Role> certificateRoles = loginDAO.findRolesByUserLoginCertificateId(userLoginCertificate.getId());
                Set<UUID> certificateRoleIds = getRoleUuidList(certificateRoles);
                List<RolePermission> certificateRolePermissions = loginDAO.findRolePermissionsByCertificateRoleIds(toStrings(certificateRoleIds));
                if (certificateRolePermissions == null || certificateRolePermissions.isEmpty()) {
                    validation("User does not have certificate permissions assigned: %s", username);
                }
                else {
                    for (RolePermission rolePermission : certificateRolePermissions) {
                        if (rolePermission.getPermitDomain().equals("*") && rolePermission.getPermitAction().equals("*") && rolePermission.getPermitSelection().equals("*")) {
                            isAdminPermissionAssigned = true;
                        }
                    }
                }
            }
            if (!isAdminPermissionAssigned) {
                validation("User does not have admin permissions assigned: %s", username); // eithe rpassword or certificate
            }
            // now check the portal user exists and that we can unlock the keystore with the new password
            MwPortalUser portalUser = My.jpa().mwPortalUser().findMwPortalUserByUserName(username);
            if( portalUser == null ) {
                validation("Portal User was not created");
            }
            else {
                if( !portalUser.getEnabled() ) {
                    validation("Portal User is not enabled");
                }
                if( !APPROVED.equalsIgnoreCase(portalUser.getStatus()) ) {
                    validation("Portal User status is not approved");
                }
                byte[] keystoreBytes =  portalUser.getKeystore();
                if( keystoreBytes == null || keystoreBytes.length == 0 ) {
                    validation("Portal User keystore is missing");
                }
                else {
                    try {
                        SimpleKeystore keystore = new SimpleKeystore(new ByteArrayResource(keystoreBytes), password);
                        X509Certificate certificate = keystore.getX509Certificate(username);
                        if( certificate == null ) {
                            validation("Portal User keystore does not contain user certificate");
                        }
                    }
                    catch(KeyManagementException | NoSuchAlgorithmException | UnrecoverableEntryException | KeyStoreException | CertificateEncodingException e) {
                        log.error("Cannot open user keystore: {}", e.getMessage());
                        validation("Portal User keystore cannot be opened");
                    }
                }
            }
        }
        // now if the validation is happening immediately after execution and there was a problem building the certificate, we will have some faults here to share with the user
        if( certificateBuilderFaults != null ) {
            for(Fault fault : certificateBuilderFaults) {
                validation(fault);
            }            
        }
    }

    private User createUser(LoginDAO loginDAO) {
        User user = loginDAO.findUserByName(username);
        if (user == null) {
            user = new User();
            user.setId(new UUID());
            user.setComment("");
//            user.setEnabled(true);
//            user.setStatus(Status.APPROVED);
            user.setUsername(username);
            loginDAO.insertUser(user.getId(), user.getUsername(), null, user.getComment()); // setting the default locale to null
        }
        return user;
    }

    private Role createAdminRole(LoginDAO loginDAO) {
        Role adminRole = loginDAO.findRoleByName(ADMINISTRATOR_ROLE);
        if (adminRole == null) {
            adminRole = new Role();
            adminRole.setId(new UUID());
            adminRole.setRoleName(ADMINISTRATOR_ROLE);
            adminRole.setDescription("the only role required to exist; the administrator may create and edit all other roles and permissions");
            loginDAO.insertRole(adminRole.getId(), adminRole.getRoleName(), adminRole.getDescription());
        }
        return adminRole;
    }

    private UserLoginPassword createUserLoginPassword(LoginDAO loginDAO, User user) throws IOException {
        UserLoginPassword userLoginPassword = loginDAO.findUserLoginPasswordByUsername(username);
        if (userLoginPassword == null) {
            userLoginPassword = new UserLoginPassword();
            userLoginPassword.setId(new UUID());
            userLoginPassword.setUserId(user.getId());
            userLoginPassword.setAlgorithm("SHA256");
            userLoginPassword.setIterations(1);
            userLoginPassword.setSalt(RandomUtil.randomByteArray(8));
            userLoginPassword.setPasswordHash(PasswordUtil.hash(password.getBytes(), userLoginPassword));
            userLoginPassword.setEnabled(true);
            userLoginPassword.setStatus(Status.APPROVED);
//            userLoginPassword.setComment("automatically created by setup");  // now it needs to be a list of roles           
            loginDAO.insertUserLoginPassword(userLoginPassword.getId(), userLoginPassword.getUserId(), userLoginPassword.getPasswordHash(), 
                    userLoginPassword.getSalt(), userLoginPassword.getIterations(), userLoginPassword.getAlgorithm(), userLoginPassword.getExpires(), 
                    userLoginPassword.isEnabled(), userLoginPassword.getStatus(), userLoginPassword.getComment());
            // now we have to store the password somewhere so the admin can read it and know it; but it doesn't belong in the server configuration so we save it in a file which the admin can read and delete (or the setup application can automatically delete it)
            storeAdminPassword();
        }
        return userLoginPassword;
    }

    private List<RolePermission> createAdminUserPasswordPermissions(LoginDAO loginDAO, Role adminRole, UserLoginPassword userLoginPassword) {
        List<Role> adminUserPasswordRoles = loginDAO.findRolesByUserLoginPasswordId(userLoginPassword.getId());
        HashSet<UUID> adminUserPasswordRoleIdList = new HashSet<>(); // it's a list but it will only be populated with ONE role id -- the administrator role id
        if (adminUserPasswordRoles.isEmpty()) {
            loginDAO.insertUserLoginPasswordRole(userLoginPassword.getId(), adminRole.getId());
            adminUserPasswordRoleIdList.add(adminRole.getId());
        } else {
            // admin user already has some roles, check if one of them is the admin role -- if the admin user is missing the admin role we will automatically add it
            for (Role role : adminUserPasswordRoles) {
                if (role.getRoleName().equalsIgnoreCase(ADMINISTRATOR_ROLE) && role.getId().equals(adminRole.getId())) {
                    adminUserPasswordRoleIdList.add(role.getId());
                }
            }
            if (adminUserPasswordRoleIdList.isEmpty()) {
                loginDAO.insertUserLoginPasswordRole(userLoginPassword.getId(), adminRole.getId());
                adminUserPasswordRoleIdList.add(adminRole.getId());
            }
        }
        List<RolePermission> adminPasswordRolePermissions = loginDAO.findRolePermissionsByPasswordRoleIds(toStrings(adminUserPasswordRoleIdList));
        if (adminPasswordRolePermissions.isEmpty()) {
            RolePermission adminPermission = new RolePermission();
            adminPermission.setRoleId(adminRole.getId());
            adminPermission.setPermitDomain("*");
            adminPermission.setPermitAction("*");
            adminPermission.setPermitSelection("*");
            loginDAO.insertRolePermission(adminPermission.getRoleId(), adminPermission.getPermitDomain(), adminPermission.getPermitAction(), adminPermission.getPermitSelection());
        }
        return adminPasswordRolePermissions;
    }

    private MwPortalUser createMwPortalUser(PrivateKey privateKey, X509Certificate certificate, X509Certificate... cacerts) 
            throws IOException, KeyManagementException, KeyStoreException, NoSuchAlgorithmException, CertificateException, NonexistentEntityException, MSDataException  {
        MwPortalUser portalUser = My.jpa().mwPortalUser().findMwPortalUserByUserName(username);
        
        if (portalUser == null) {
            log.debug("Creating new mw_portal_user entry with keystore");
            ByteArrayResource resource = new ByteArrayResource();
            SimpleKeystore keystore = new SimpleKeystore(resource, password);
            keystore.addKeyPairX509(privateKey, certificate, username, password, cacerts);
            // add the CA cert itself to the keystore
            for(X509Certificate cacert : cacerts) {
                keystore.addTrustedCaCertificate(cacert, "mtwilson-cacert");
            }
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
            portalUser = new MwPortalUser();
            portalUser.setComment("created automatically by setup");
            portalUser.setUsername(username);
            portalUser.setUuid_hex(new UUID().toString());
            portalUser.setStatus(APPROVED);
            portalUser.setEnabled(true);
            portalUser.setKeystore(resource.toByteArray());
            log.debug("Created keystore size {} bytes", portalUser.getKeystore().length);
            My.jpa().mwPortalUser().create(portalUser);
        } else {
            log.debug("Updating existing mw_portal_user entry");
            SimpleKeystore keystore = new SimpleKeystore(portalUser.getKeystoreResource(), password);
            try {
                // ensure the "username" alias is not alraedy in the keystore because we will add the private key
                // there later
                keystore.delete(username);
            } catch (Exception e) {
                log.debug("Existing keystore did not contain private key for {}: {}", username, e.getMessage());
            }
            keystore.addKeyPairX509(privateKey, certificate, username, password);
            keystore.save(); // saves into portalUser keystore field
            portalUser.setComment("updated automatically by setup");
            portalUser.setStatus(APPROVED);
            portalUser.setEnabled(true);
            My.jpa().mwPortalUser().edit(portalUser);
        }
        return portalUser;
    }

    private UserLoginCertificate createUserLoginCertificate(LoginDAO loginDAO, User user) 
            throws FileNotFoundException, IOException, CryptographyException, CertificateException, NoSuchAlgorithmException, KeyManagementException, 
            KeyStoreException, NonexistentEntityException, MSDataException  {
        UserLoginCertificate userLoginCertificate = loginDAO.findUserLoginCertificateByUsername(username);
        if (userLoginCertificate == null) {
            
            // first load the ca key
            byte[] combinedPrivateKeyAndCertPemBytes;
            try (FileInputStream cakeyIn = new FileInputStream(My.configuration().getCaKeystoreFile())) {
                combinedPrivateKeyAndCertPemBytes = IOUtils.toByteArray(cakeyIn);
            }
            PrivateKey cakey = RsaUtil.decodePemPrivateKey(new String(combinedPrivateKeyAndCertPemBytes));
            X509Certificate cacert = X509Util.decodePemCertificate(new String(combinedPrivateKeyAndCertPemBytes));
            
            // now create the user certificate
            
            KeyPair keyPair = RsaUtil.generateRsaKeyPair(RsaUtil.MINIMUM_RSA_KEY_SIZE);
            /*
            X509Certificate certificate = X509Builder.factory()
                    .selfSigned(String.format("CN=%s", username), keyPair)
                    .expires(365, TimeUnit.DAYS)
                    .build();
            */
            X509Builder builder = X509Builder.factory();
            X509Certificate certificate = builder
                    .commonName(username)
                    .subjectPublicKey(keyPair.getPublic())
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
                certificateBuilderFaults = builder.getFaults();
                for(Fault fault : certificateBuilderFaults) {
                    log.error(fault.toString());
                }
                throw new CertificateException("Cannot generate certificate");
            }
            
            userLoginCertificate = new UserLoginCertificate();
            userLoginCertificate.setId(new UUID());
            userLoginCertificate.setCertificate(certificate.getEncoded());
//            userLoginCertificate.setComment("automatically created by setup"); // now it needs to be a list of roles
            userLoginCertificate.setEnabled(true);
            userLoginCertificate.setExpires(certificate.getNotAfter());
            userLoginCertificate.setSha1Hash(Sha1Digest.digestOf(certificate.getEncoded()).toByteArray());
            userLoginCertificate.setSha256Hash(Sha256Digest.digestOf(certificate.getEncoded()).toByteArray());
            userLoginCertificate.setStatus(Status.APPROVED);
            userLoginCertificate.setUserId(user.getId());
            loginDAO.insertUserLoginCertificate(userLoginCertificate.getId(), userLoginCertificate.getUserId(), userLoginCertificate.getCertificate(), userLoginCertificate.getSha1Hash(), userLoginCertificate.getSha256Hash(), userLoginCertificate.getExpires(), userLoginCertificate.isEnabled(), userLoginCertificate.getStatus(), userLoginCertificate.getComment());
            log.debug("Created user login certificate with sha256 {}", Sha256Digest.valueOf(userLoginCertificate.getSha256Hash()).toHexString());
            // now we have to store the private key somewhere.... for now we will create a portal user keystore so the admin user can use these privileges when logged in to portal
            MwPortalUser portalUser = createMwPortalUser(keyPair.getPrivate(), certificate, cacert);
            log.debug("Created the portal user {} successfully", portalUser.getUsername());
        }
        return userLoginCertificate;
    }

    private List<RolePermission> createAdminUserCertificatePermissions(LoginDAO loginDAO, Role adminRole, UserLoginCertificate userLoginCertificate) {
        List<Role> adminUserCertificateRoles = loginDAO.findRolesByUserLoginCertificateId(userLoginCertificate.getId());
        HashSet<UUID> adminUserCertificateRoleIdList = new HashSet<>(); // it's a list but it will only be populated with ONE role id -- the administrator role id
        if (adminUserCertificateRoles.isEmpty()) {
            loginDAO.insertUserLoginCertificateRole(userLoginCertificate.getId(), adminRole.getId());
            adminUserCertificateRoleIdList.add(adminRole.getId());
        } else {
            // admin user already has some roles, check if one of them is the admin role -- if the admin user is missing the admin role we will automatically add it
            for (Role role : adminUserCertificateRoles) {
                if (role.getRoleName().equalsIgnoreCase(ADMINISTRATOR_ROLE)) {
                    adminUserCertificateRoleIdList.add(role.getId());
                }
            }
            if (adminUserCertificateRoleIdList.isEmpty()) {
                loginDAO.insertUserLoginCertificateRole(userLoginCertificate.getId(), adminRole.getId());
                adminUserCertificateRoleIdList.add(adminRole.getId());
            }
        }
        // already handled above by password block... this would link to same role which would have same permissions.... so this block is a no-op
        List<RolePermission> adminCertificateRolePermissions = loginDAO.findRolePermissionsByCertificateRoleIds(toStrings(adminUserCertificateRoleIdList));
        if (adminCertificateRolePermissions.isEmpty()) {
            RolePermission adminPermission = new RolePermission();
            adminPermission.setRoleId(adminRole.getId());
            adminPermission.setPermitDomain("*");
            adminPermission.setPermitAction("*");
            adminPermission.setPermitSelection("*");
            loginDAO.insertRolePermission(adminPermission.getRoleId(), adminPermission.getPermitDomain(), adminPermission.getPermitAction(), adminPermission.getPermitSelection());
        }
        return adminCertificateRolePermissions;
    }

    @Override
    protected void execute() throws Exception {
        try (LoginDAO loginDAO = MyJdbi.authz()) {
            User user = createUser(loginDAO);
            Role adminRole = createAdminRole(loginDAO);
            
            // password-based login
            UserLoginPassword userLoginPassword = createUserLoginPassword(loginDAO, user);
            List<RolePermission> adminPasswordRolePermissions = createAdminUserPasswordPermissions(loginDAO, adminRole, userLoginPassword);
            log.debug("Added {} password roles for admin user.", adminPasswordRolePermissions.size());

            // now prepare the x509 certificate login
            UserLoginCertificate userLoginCertificate = createUserLoginCertificate(loginDAO, user);
            List<RolePermission> adminCertificateRolePermissions = createAdminUserCertificatePermissions(loginDAO, adminRole, userLoginCertificate);
            log.debug("Added {} certificate roles for admin user.", adminCertificateRolePermissions.size());

        }
    }

    private void storeAdminPassword() throws IOException {
        // save the password to a file so the admin user can read it ; because it shouldn't be stored in the permanent configuration
        File privateDir = new File(Folders.configuration() + File.separator + "private");
        if (!privateDir.exists()) {
            privateDir.mkdirs();
        }
        if (Platform.isUnix()) {
            Runtime.getRuntime().exec("chmod 700 " + privateDir.getAbsolutePath());
        }
        File passwordFile = privateDir.toPath().resolve("password.txt").toFile();
        FileUtils.writeStringToFile(passwordFile, ""); // first create an empty file so we can set permissions before writing the password to it
        if (Platform.isUnix()) {
            Runtime.getRuntime().exec("chmod 600 " + passwordFile.getAbsolutePath());
        }
        FileUtils.writeStringToFile(passwordFile, String.format("%s\n", password));
    }
    
    private X509Certificate getSamlCertificate() throws KeyManagementException, KeyStoreException {
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
    private X509Certificate getTlsCertificate() throws KeyManagementException, KeyStoreException  {
        SimpleKeystore tlsKeystore = new SimpleKeystore(My.configuration().getTlsKeystoreFile(), My.configuration().getTlsKeystorePassword());   //"changeit");
        for (String alias : tlsKeystore.aliases()) {
            log.debug("TLS Keystore alias: {}", alias);
            // make sure it has a SAML private key and certificate inside
            try {
                if ("tomcat".equals(alias) || "s1as".equals(alias)) {
                    RsaCredentialX509 credential = tlsKeystore.getRsaCredentialX509(alias, My.configuration().getTlsKeystorePassword());   //"changeit");
                    log.debug("TLS certificate: {}", credential.getCertificate().getSubjectX500Principal().getName());
                    return credential.getCertificate();
                } else if (!"glassfish-instance".equals(alias)) {
                    log.warn("Cannot find TLS certificate with correct matching alias.");
                }
            } catch (IOException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableEntryException | CertificateEncodingException | CryptographyException e) {
                log.debug("Cannot read TLS key from keystore", e);
//                validation("Cannot read SAML key from keystore"); // we are assuming the keystore only has one private key entry ... 
            }
        }
        return null;
    }
    
}
