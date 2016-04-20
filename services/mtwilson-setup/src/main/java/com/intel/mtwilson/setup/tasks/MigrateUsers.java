/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.tasks;

import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.crypto.Sha256Digest;
import com.intel.dcsg.cpg.i18n.LocaleUtil;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.ms.controller.exceptions.IllegalOrphanException;
import com.intel.mtwilson.ms.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.ms.data.ApiClientX509;
import com.intel.mtwilson.ms.data.ApiRoleX509;
import com.intel.mtwilson.ms.data.MwPortalUser;
import com.intel.mtwilson.setup.DatabaseSetupTask;
import com.intel.mtwilson.shiro.jdbi.LoginDAO;
import com.intel.mtwilson.shiro.jdbi.MyJdbi;
import com.intel.mtwilson.user.management.rest.v2.model.Role;
import com.intel.mtwilson.user.management.rest.v2.model.Status;
import com.intel.mtwilson.user.management.rest.v2.model.User;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginCertificate;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginPassword;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Requires 20140213235800_mtwilson20_add_permission_table.sql to be already
 * executed against the database because that script creates the new user, role,
 * and permission tables.
 *
 * Note: cannot really convert existing portal users to mw_user with salted
 * passwords because mw_portal_user didn't store the plaintext password (that's
 * good) it stored a keystore and the login test was whether the user's input
 * password could be used to unlock the keystore.
 * 
 * Administration note:  this setup task MOVES data from mtwilson 1.2 tables
 * to mtwilson 2.0 tables;  it's safe to run multiple times because it will only
 * migrate data that wasn't already migrated, but if there's a programmer error
 * here the old data may be lost;  so backup the mtwilson 1.2 database before
 * running setup. 
 * 
 * @author jbuhacoff
 */
public class MigrateUsers extends DatabaseSetupTask {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MigrateUsers.class);
    private String algorithmName = null;
    private Integer iterations = null;
    HashMap<String, Role> roleCache = new HashMap<>();

    public String getAlgorithmName() {
        return algorithmName;
    }

    /**
     *
     * @param algorithmName from the list in
     * http://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#MessageDigest
     * or from a custom cryptography provider
     */
    public void setAlgorithmName(String algorithmName) {
        this.algorithmName = algorithmName;
    }

    public Integer getIterations() {
        return iterations;
    }

    public void setIterations(Integer iterations) {
        this.iterations = iterations;
    }

    @Override
    protected void configure() throws Exception {
        // no special configuration is necessary for this task ?  default is
        // to use sha-256 and 1000 iterations
        if (algorithmName == null) {
            algorithmName = "SHA-256";
        }
        try {
            MessageDigest md = MessageDigest.getInstance(algorithmName);
            log.debug("Hash algorithm {} is available", md.getAlgorithm());
            if (iterations == null) {
                // determine a suitable number of iterations.
                // the following will determine how many hash iterations can be
                // performed in approximately one second on the local system and
                // that number will be used as the default iteration count;
                // if the number exceeds Integer.MAX_VALUE then a warning will be issued
                // and Integer.MAX_VALUE will be used. 
//                iterations = benchmarkIterationCount(md, 1000); // 1000 ms = 1 second
//                log.debug("Hash benchmark indicates {} iterations should be used", iterations);
                iterations = 1; // the benchmark works but an HP EliteBook 8560w purchased in 2011-2012 can process more than Integer.MAX_VALUE  SHA-256 iterations in under one second...  so benchmarking doesn't even make sense right now.  using a value of 1 iteration as a nominal default for now.
            }
        } catch (NoSuchAlgorithmException e) {
            configuration("Algorithm %s is not available", algorithmName);
        }
        if (iterations == null || iterations < 1) {
            configuration("Iteration count must be positive");
        }
        // now check for the required database tables to be present
        try (Connection c = My.jdbc().connection()) {
            // data migrated from these mtwilson 1.2 tables:
            requireTable(c, "mw_portal_user");
            requireTable(c, "mw_api_client_x509");
            requireTable(c, "mw_api_role_x509");
            // to these mtwilson 2.0 tables:
            requireTable(c, "mw_role");
//            requireTable(c, "mw_role_permission"); 
            requireTable(c, "mw_user");
            requireTable(c, "mw_user_keystore");
            requireTable(c, "mw_user_login_password");
            requireTable(c, "mw_user_login_password_role");
        }
    }

    @Override
    protected void validate() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void execute() throws Exception {
        roleCache.clear();
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        List<MwPortalUser> portalUsers = My.jpa().mwPortalUser().findMwPortalUserEntities(); // getting "the type of mwPortalUser() is erroneous"
        for (MwPortalUser portalUser : portalUsers) {
            try {
                migratePortalUser(portalUser);
            } catch (Exception e) {
                log.error("Cannot migrate portal user: {}", portalUser.getUsername(), e);
                validation("Cannot migrate portal user: %s", portalUser.getUsername());
            }
        }
        List<ApiClientX509> apiClients = My.jpa().mwApiClientX509().findApiClientX509Entities();
        for (ApiClientX509 apiClient : apiClients) {
            try {
                migrateApiClient(apiClient, null);
            } catch (Exception e) {
                log.error("Cannot migrate X509 client: {}", apiClient.getName());
                validation("Cannot migrate X509 client: %s", apiClient.getName());
            }
        }
    }

    /**
     * @param cipherInfo the password-based cipher to test (algorithm name, salt
     * size)
     * @param elapsedTimeTarget the minimum average delay, in milliseconds; the
     * function will try increasingly larger iteration counts until the
     * encryption operations takes MORE THAN this amount of milliseconds on
     * average (5 data points in the average)
     * @return
     * @throws CryptographyException
     */
    /*private int benchmarkIterationCount(MessageDigest md, double elapsedTimeTarget) {
        Random rnd = new Random(); // don't need a secure random since we are not generating keys for production use here -- only to test encryption speed
        // generate random input for the trial
        byte[] plaintextInput = new byte[1024];
        rnd.nextBytes(plaintextInput);
        // now find out how many iterations it takes in order to "spin" for elapsedTimeTarget (in milliseconds) 
        int iterationCount = 1;
        double avgElapsedTime = 0.0;
        while (iterationCount < Integer.MAX_VALUE / 2 && avgElapsedTime < elapsedTimeTarget) {
            iterationCount *= 2;
            avgElapsedTime = 0.0;
            for (int i = 0; i < 5; i++) { // 5 trials for each value of iteration count, in order to smooth out outliers due to transient spikes in system load 
                long startTime = System.currentTimeMillis();
                byte[] result = md.digest(plaintextInput);
                plaintextInput = result;
                long currentTime = System.currentTimeMillis();
                long elapsedTime = currentTime - startTime;
                avgElapsedTime = 1.0 * (avgElapsedTime * i + elapsedTime) / (i + 1);
            }
            log.debug("iterations: {}   avg elapsed time: {}", new Object[]{iterationCount, avgElapsedTime});
        }
        if (iterationCount > Integer.MAX_VALUE / 2) {
            log.warn("Hashing is fast and the number of iterations will only be a minor deterrent to a brute force attack."); //  maybe recommend using a random number of iterations for each user in order to make the iteration count itself a search space for attackers conducting a "blind" (no access to the password database) brute force attack?
            iterationCount = Integer.MAX_VALUE;
        }
        return iterationCount;
    }*/

    private void migratePortalUser(MwPortalUser portalUser) throws SQLException, IOException, NonexistentEntityException, IllegalOrphanException {
        try (LoginDAO loginDAO = MyJdbi.authz()) {
            // create new user record
            User user = new User();
            user.setId(new UUID());
            user.setUsername(portalUser.getUsername());
            user.setLocale(LocaleUtil.forLanguageTag(portalUser.getLocale()));
            user.setComment(String.format("%s\nmw_portal_user.id=%d\nmw_portal_user.uuid=%s", (portalUser.getComment() == null ? "" : portalUser.getComment()), portalUser.getId(), portalUser.getUuid_hex()));

            loginDAO.insertUser(user.getId(), user.getUsername(), LocaleUtil.toLanguageTag(user.getLocale()), user.getComment());

            // create new password login record  --  will be disabled because we don't know the user's password
            UserLoginPassword userLoginPassword = new UserLoginPassword();
            userLoginPassword.setId(new UUID());
            userLoginPassword.setUserId(user.getId());
            userLoginPassword.setEnabled(false);
//            userLoginPassword.setExpires(null); // we're not using this feature right now but an admin could force users to rotate their passwords
            userLoginPassword.setAlgorithm("SHA-256");
            userLoginPassword.setIterations(iterations);
            userLoginPassword.setPasswordHash(new byte[0]); // we don't know the password right now 
            userLoginPassword.setSalt(new byte[0]); // will be generated at the time the password is set
            userLoginPassword.setStatus(Status.PENDING);
            userLoginPassword.setComment(user.getComment());
            loginDAO.insertUserLoginPassword(userLoginPassword.getId(), userLoginPassword.getUserId(), userLoginPassword.getPasswordHash(), 
                    userLoginPassword.getSalt(), userLoginPassword.getIterations(), userLoginPassword.getAlgorithm(), userLoginPassword.getExpires(), 
                    userLoginPassword.isEnabled(), userLoginPassword.getStatus(), userLoginPassword.getComment());

            // look up the user's certificates and  permissions
            List<ApiClientX509> apiClients = My.jpa().mwApiClientX509().findApiClientX509ByNameLike(String.format("CN=%s", user.getUsername()));
            for (ApiClientX509 apiClient : apiClients) {
                migrateApiClient(apiClient, user);
            }
            
            // now delete the portal user record so if migration is executed again, we won't create duplicate records
            My.jpa().mwPortalUser().destroy(portalUser.getId());
        }
    }

    private void migrateApiClient(ApiClientX509 apiClient, User user) throws SQLException, IOException, NonexistentEntityException, IllegalOrphanException {
        try (LoginDAO loginDAO = MyJdbi.authz()) {
            UserLoginPassword userLoginPassword = null;
            if (user == null) {
                user = new User();
                user.setId(new UUID());
                user.setComment(String.format("Automatically created for existing mw_api_client_x509: %s", apiClient.getName()));
                user.setLocale(LocaleUtil.forLanguageTag(apiClient.getLocale()));
                user.setUsername(apiClient.getUserNameFromName());
                loginDAO.insertUser(user.getId(), user.getUsername(), LocaleUtil.toLanguageTag(user.getLocale()), user.getComment());

            } else {
                userLoginPassword = loginDAO.findUserLoginPasswordByUserId(user.getId());            // we'll use this later when adding roles
            }
            // migrate the user's login certificate 
            UserLoginCertificate userLoginCertificate = new UserLoginCertificate();
            userLoginCertificate.setId(new UUID());
            userLoginCertificate.setUserId(user.getId());
            userLoginCertificate.setCertificate(apiClient.getCertificate());
            userLoginCertificate.setComment(String.format("Imported from mw_api_client_x509.id=%d", apiClient.getId()));
            userLoginCertificate.setEnabled(apiClient.getEnabled());
//                userLoginCertificate.setExpires(null); // we don't use this right now, but we need to implement the notBefore and notAfter for all login types anyway, and wwhen we do that we can copy those from the certificate here
            userLoginCertificate.setSha1Hash(Sha1Digest.digestOf(apiClient.getCertificate()).toByteArray());
            userLoginCertificate.setSha256Hash(Sha256Digest.digestOf(apiClient.getCertificate()).toByteArray());
            try {
                userLoginCertificate.setStatus(Status.valueOf(apiClient.getStatus()));
            } catch (IllegalArgumentException e) {
                log.debug("Invalid mw_api_client_x509 status: {}", apiClient.getStatus());
                userLoginCertificate.setStatus(Status.PENDING);
            }

            loginDAO.insertUserLoginCertificate(userLoginCertificate.getId(), userLoginCertificate.getUserId(), userLoginCertificate.getCertificate(), userLoginCertificate.getSha1Hash(), userLoginCertificate.getSha256Hash(), userLoginCertificate.getExpires(), userLoginCertificate.isEnabled(), userLoginCertificate.getStatus(), userLoginCertificate.getComment());


            // migrate the user's permissions to  certificate login 

            Collection<ApiRoleX509> apiClientRoles = apiClient.getApiRoleX509Collection();
            for (ApiRoleX509 apiClientRole : apiClientRoles) {
                String roleName = apiClientRole.getApiRoleX509PK().getRole();
                // check if the role was already created, caching what we already looked up
                Role role = getRole(roleName);

//            UserLoginCertificateRole userLoginCertificateRole = new UserLoginCertificateRole();
//            userLoginCertificateRole.setLoginCertificateId(userLoginCertificate.getId());
//            userLoginCertificateRole.setRoleId(role.getId());

                loginDAO.insertUserLoginCertificateRole(userLoginCertificate.getId(), role.getId());

                // if there are any password login entries for the same user,  add the same roles there
                if (userLoginPassword != null) {
                    loginDAO.insertUserLoginPasswordRole(userLoginPassword.getId(), role.getId());

                }
                
                // delete the permission record to prevent duplicate records if migration is executed again
                My.jpa().mwApiRoleX509().destroy(apiClientRole.getApiRoleX509PK());
            }
            
            // now delete the portal user record so if migration is executed again, we won't create duplicate records
            My.jpa().mwApiClientX509().destroy(apiClient.getId());
            
        }
    }

    private Role getRole(String roleName) throws SQLException, IOException {
        Role role;
        if (roleCache.containsKey(roleName)) {
            role = roleCache.get(roleName);
        } else {
            try (LoginDAO loginDAO = MyJdbi.authz()) {
                role = loginDAO.findRoleByName(roleName);
                roleCache.put(roleName, role);
            }
        }
        return role;
    }
}
