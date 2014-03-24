/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.setup;

import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.crypto.Sha256Digest;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.io.ByteArrayResource;
import com.intel.dcsg.cpg.io.Platform;
import com.intel.mtwilson.MyFilesystem;
import com.intel.mtwilson.setup.LocalSetupTask;
import com.intel.mtwilson.shiro.authc.password.PasswordCredentialsMatcher;
import com.intel.mtwilson.shiro.jdbi.LoginDAO;
import com.intel.mtwilson.shiro.jdbi.MyJdbi;
import com.intel.mtwilson.shiro.jdbi.model.*;
import java.io.File;
import java.util.List;
import org.apache.commons.io.FileUtils;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.x509.X509Builder;
import com.intel.mtwilson.My;
import com.intel.mtwilson.ms.data.MwPortalUser;
import java.io.IOException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author jbuhacoff
 */
public class CreateAdminUser extends LocalSetupTask {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CreateAdminUser.class);
    public static final String ADMINISTRATOR_ROLE = "administrator"; // TODO: move this to mtwilson-shiro-util ?
    private String username;
    private String password;
    private LoginDAO loginDAO;
//    private boolean isNewPassword = false;

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
//        isNewPassword = true;
        return RandomUtil.randomBase64String(8).replace("=", "_"); // TODO INSECURE: use a larger alphabet with more special characters
    }

    @Override
    protected void configure() throws Exception {
        username = getUsername();
        password = getPassword();
    }

    List<UUID> getRoleUuidList(List<Role> roles) {
        ArrayList<UUID> uuids = new ArrayList<>();
        for (Role role : roles) {
            uuids.add(role.getId());
        }
        return uuids;
    }

    @Override
    protected void validate() throws Exception {
        // ensure we have an admin user created with permissions assigned
        try (LoginDAO loginDAO = MyJdbi.authz()) {
            UserLoginPassword userLoginPassword = loginDAO.findUserLoginPasswordByUsername(username);
            if (userLoginPassword == null) {
                validation("User does not exist or does not have a password: %s", username);
                return;
            }
            List<Role> passwordRoles = loginDAO.findRolesByUserLoginPasswordId(userLoginPassword.getId());
            List<UUID> passwordRoleIds = getRoleUuidList(passwordRoles);
            List<RolePermission> passwordRolePermissions = loginDAO.findRolePermissionsByPasswordRoleIds(passwordRoleIds);
            if (passwordRolePermissions == null || passwordRolePermissions.isEmpty()) {
                validation("User does not have password permissions assigned: %s", username);
            }
            // now check for the admin user's x509 login
            UserLoginCertificate userLoginCertificate = loginDAO.findUserLoginCertificateByUsername(username);
            if (userLoginCertificate == null) {
                validation("User does not exist or does not have a certificate");
                return;
            }
            List<Role> certificateRoles = loginDAO.findRolesByUserLoginCertificateId(userLoginCertificate.getId());
            List<UUID> certificateRoleIds = getRoleUuidList(certificateRoles);
            List<RolePermission> certificateRolePermissions = loginDAO.findRolePermissionsByCertificateRoleIds(certificateRoleIds);
            if (certificateRolePermissions == null || certificateRolePermissions.isEmpty()) {
                validation("User does not have certificate permissions assigned: %s", username);
            }
            // now check for admin permissions - at least one login method must have them  (password, hmac, and/or certificate) 
            boolean isAdminPermissionAssigned = false;
            for (RolePermission rolePermission : passwordRolePermissions) {
                if (rolePermission.getPermitDomain().equals("*") && rolePermission.getPermitAction().equals("*") && rolePermission.getPermitSelection().equals("*")) {
                    isAdminPermissionAssigned = true;
                }
            }
            for (RolePermission rolePermission : certificateRolePermissions) {
                if (rolePermission.getPermitDomain().equals("*") && rolePermission.getPermitAction().equals("*") && rolePermission.getPermitSelection().equals("*")) {
                    isAdminPermissionAssigned = true;
                }
            }
            if (!isAdminPermissionAssigned) {
                validation("User does not have admin permissions assigned: %s", username); // eithe rpassword or certificate
            }

        }
    }

    private User createUser(LoginDAO loginDAO) {
        User user = loginDAO.findUserByName(username);
        if (user == null) {
            user = new User();
            user.setId(new UUID());
            user.setComment("automatically created by setup");
            user.setEnabled(true);
            user.setStatus(Status.APPROVED);
            user.setUsername(username);
            loginDAO.insertUser(user);
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
            userLoginPassword.setPasswordHash(PasswordCredentialsMatcher.passwordHash(password.getBytes(), userLoginPassword));
            userLoginPassword.setEnabled(true);
            loginDAO.insertUserLoginPassword(userLoginPassword.getId(), userLoginPassword.getUserId(), userLoginPassword.getPasswordHash(), userLoginPassword.getSalt(), userLoginPassword.getIterations(), userLoginPassword.getAlgorithm(), userLoginPassword.getExpires(), userLoginPassword.isEnabled());
            // now we have to store the password somewhere so the admin can read it and know it; but it doesn't belong in the server configuration so we save it in a file which the admin can read and delete (or the setup application can automatically delete it)
            storeAdminPassword();
        }
        return userLoginPassword;
    }

    private List<RolePermission> createAdminUserPasswordPermissions(LoginDAO loginDAO, Role adminRole, UserLoginPassword userLoginPassword) {
        List<Role> adminUserPasswordRoles = loginDAO.findRolesByUserLoginPasswordId(userLoginPassword.getId());
        ArrayList<UUID> adminUserPasswordRoleIdList = new ArrayList<>(); // it's a list but it will only be populated with ONE role id -- the administrator role id
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
        List<RolePermission> adminPasswordRolePermissions = loginDAO.findRolePermissionsByPasswordRoleIds(adminUserPasswordRoleIdList);
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

    private MwPortalUser createMwPortalUser(PrivateKey privateKey, X509Certificate certificate) throws Exception {
        MwPortalUser portalUser = My.jpa().mwPortalUser().findMwPortalUserByUserName(username);
        if (portalUser == null) {
            log.debug("Creating new mw_portal_user entry with keystore");
            ByteArrayResource resource = new ByteArrayResource();
            SimpleKeystore keystore = new SimpleKeystore(resource, password);
            keystore.addKeyPairX509(privateKey, certificate, username, password);
            keystore.save();
            portalUser = new MwPortalUser();
            portalUser.setComment("created automatically by setup");
            portalUser.setUsername(username);
            portalUser.setUuid_hex(new UUID().toString());
            portalUser.setStatus("APPROVED");
            portalUser.setEnabled(true);
            portalUser.setKeystore(resource.toByteArray());
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
            portalUser.setStatus("APPROVED");
            portalUser.setEnabled(true);
            My.jpa().mwPortalUser().edit(portalUser);
        }
        return portalUser;
    }

    private UserLoginCertificate createUserLoginCertificate(LoginDAO loginDAO, User user) throws Exception {
        UserLoginCertificate userLoginCertificate = loginDAO.findUserLoginCertificateByUsername(username);
        if (userLoginCertificate == null) {
            KeyPair keyPair = RsaUtil.generateRsaKeyPair(RsaUtil.MINIMUM_RSA_KEY_SIZE);
            X509Certificate certificate = X509Builder.factory().selfSigned(String.format("CN=%s", username), keyPair).expires(365, TimeUnit.DAYS).build();
            userLoginCertificate = new UserLoginCertificate();
            userLoginCertificate.setId(new UUID());
            userLoginCertificate.setCertificate(certificate.getEncoded());
            userLoginCertificate.setComment("automatically created by setup");
            userLoginCertificate.setEnabled(true);
            userLoginCertificate.setExpires(certificate.getNotAfter());
            userLoginCertificate.setSha1Hash(Sha1Digest.digestOf(certificate.getEncoded()).toByteArray());
            userLoginCertificate.setSha256Hash(Sha256Digest.digestOf(certificate.getEncoded()).toByteArray());
            userLoginCertificate.setStatus(Status.APPROVED);
            userLoginCertificate.setUserId(user.getId());
            loginDAO.insertUserLoginCertificate(userLoginCertificate.getId(), userLoginCertificate.getUserId(), userLoginCertificate.getCertificate(), userLoginCertificate.getSha1Hash(), userLoginCertificate.getSha256Hash(), userLoginCertificate.getExpires(), userLoginCertificate.isEnabled(), userLoginCertificate.getStatus(), userLoginCertificate.getComment());
            // now we have to store the private key somewhere.... for now we will create a portal user keystore so the admin user can use these privileges when logged in to portal
            MwPortalUser portalUser = createMwPortalUser(keyPair.getPrivate(), certificate);
        }
        return userLoginCertificate;
    }

    private List<RolePermission> createAdminUserCertificatePermissions(LoginDAO loginDAO, Role adminRole, UserLoginCertificate userLoginCertificate) {
        List<Role> adminUserCertificateRoles = loginDAO.findRolesByUserLoginCertificateId(userLoginCertificate.getId());
        ArrayList<UUID> adminUserCertificateRoleIdList = new ArrayList<>(); // it's a list but it will only be populated with ONE role id -- the administrator role id
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
        List<RolePermission> adminCertificateRolePermissions = loginDAO.findRolePermissionsByCertificateRoleIds(adminUserCertificateRoleIdList);
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

            // now prepare the x509 certificate login
            UserLoginCertificate userLoginCertificate = createUserLoginCertificate(loginDAO, user);
            List<RolePermission> adminCertificateRolePermissions = createAdminUserCertificatePermissions(loginDAO, adminRole, userLoginCertificate);

        }
    }

    private void storeAdminPassword() throws IOException {
        // save the password to a file so the admin user can read it ; because it shouldn't be stored in the permanent configuration
        File privateDir = new File(MyFilesystem.getApplicationFilesystem().getConfigurationPath() + File.separator + "private");
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
}
