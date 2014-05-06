/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.cmd;

import com.intel.mtwilson.security.rest.v2.model.UserKeystore;
import com.intel.mtwilson.security.rest.v2.model.UserLoginCertificate;
import com.intel.mtwilson.security.rest.v2.model.UserLoginPassword;
import com.intel.mtwilson.security.rest.v2.model.User;
import com.intel.mtwilson.ms.common.MSConfig;
import com.intel.mtwilson.ms.controller.ApiClientX509JpaController;
import com.intel.mtwilson.ms.controller.ApiRoleX509JpaController;
import com.intel.mtwilson.ms.controller.MwPortalUserJpaController;
import com.intel.mtwilson.ms.data.ApiClientX509;
import com.intel.mtwilson.ms.data.ApiRoleX509;
import com.intel.mtwilson.ms.data.MwPortalUser;
import com.intel.mtwilson.ms.MSPersistenceManager;
import com.intel.dcsg.cpg.console.Command;
import com.intel.mtwilson.shiro.jdbi.LoginDAO;
import com.intel.mtwilson.shiro.jdbi.MyJdbi;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManagerFactory;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Accepts one option: --all indicates that even admin and
 * ManagementServiceAutomation should be removed.
 *
 * @author jbuhacoff
 */
public class EraseUserAccounts implements Command {

    private Logger log = LoggerFactory.getLogger(getClass());
  
    private MSPersistenceManager pm;
    private EntityManagerFactory em;

    private Configuration options = null;

    @Override
    public void setOptions(Configuration options) {
        this.options = options;
    }

    /**
     * Deletes all user accounts EXCEPT the admin user and the
     * ManagementServiceAutomation user
     *
     * @param args
     * @throws Exception
     */
    @Override
    public void execute(String[] args) throws Exception {
        //Configuration serviceConf = MSConfig.getConfiguration();
        pm = new MSPersistenceManager();
        em = pm.getEntityManagerFactory("MSDataPU");
        if(options.containsKey("user")) {
            String username = options.getString("user");
            System.out.println("deleting user " + username);
            deleteUser(username);
            deleteApiClient(username);
        }else {
            deletePortalUsers();
            deleteApiClients();
            deleteUsers();
        }
    }

    private void deleteUser(String username) {
        try {
             MwPortalUserJpaController jpa  = new MwPortalUserJpaController(em); //My.jpa().mwPortalUser();
             MwPortalUser portalUser = jpa.findMwPortalUserByUserName(username);
             jpa.destroy(portalUser.getId());
//             System.out.println("Deleted " + portalUser.getUsername());
        }catch (Exception ex) {
            System.err.println("Exception occured: \r\n\r\n" + ex.getMessage());
        }
    }
    
    private void deleteUsers() throws Exception {
        try(LoginDAO dao = MyJdbi.authz()) {
        List<User> users = dao.findAllUsers();
        for(User user : users) {
            UserKeystore userKeystore = dao.findUserKeystoreByUserId(user.getId());
            if( userKeystore != null ) {
                dao.deleteUserKeystoreById(userKeystore.getId());
            }
            UserLoginPassword userLoginPassword = dao.findUserLoginPasswordByUserId(user.getId());
            if( userLoginPassword != null ) {
                dao.deleteUserLoginPasswordRolesByUserLoginPasswordId(userLoginPassword.getId());
                dao.deleteUserLoginPasswordById(userLoginPassword.getId());
            }
            UserLoginCertificate userLoginCertificate = dao.findUserLoginCertificateByUserId(user.getId());
            if( userLoginCertificate != null ) {
                dao.deleteUserLoginCertificateRolesByUserLoginCertificateId(userLoginCertificate.getId());
                dao.deleteUserLoginCertificateById(userLoginCertificate.getId());
            }
            dao.deleteUser(user.getId());
        }
        }
    }
    
    private void deletePortalUsers() throws com.intel.mtwilson.ms.controller.exceptions.NonexistentEntityException {
        try {
            boolean deleteAll = options.getBoolean("all", false);
            boolean verbose = options.getBoolean("verbose", false);
            MwPortalUserJpaController jpa = new MwPortalUserJpaController(em);
            List<MwPortalUser> list = jpa.findMwPortalUserEntities();
            Configuration serviceConf = MSConfig.getConfiguration();
            String msUser = serviceConf.getString("mtwilson.api.key.alias");
            String adminUser = System.getenv("MC_FIRST_USERNAME");

            int count = 0;
            for (MwPortalUser record : list) {
                if (deleteAll || (!deleteAll && !record.getUsername().equals(msUser) && !record.getUsername().equals(adminUser))) {
                    jpa.destroy(record.getId());
                    count++;
                    if (verbose) {
                        System.out.println("Deleting portal user " + record.getUsername() + "..... Done");
                    }
                }
            }
            System.out.println(count + " portal users deleted.");
        } catch (Exception ex) {
            System.err.println("Exception occured: \r\n\r\n" + ex.toString());
        }
    }

    private void deleteApiClients() throws com.intel.mtwilson.ms.controller.exceptions.NonexistentEntityException, com.intel.mtwilson.ms.controller.exceptions.IllegalOrphanException {
        try {
            boolean deleteAll = options.getBoolean("all", false);
            boolean verbose = options.getBoolean("verbose", false);
            ApiClientX509JpaController jpa = new ApiClientX509JpaController(em);
            ApiRoleX509JpaController rolejpa = new ApiRoleX509JpaController(em);
            List<ApiClientX509> list = jpa.findApiClientX509Entities();
            Configuration serviceConf = MSConfig.getConfiguration();
            String msUser = serviceConf.getString("mtwilson.api.key.alias");
            String adminUser = System.getenv("MC_FIRST_USERNAME");

            int count = 0;
            for (ApiClientX509 record : list) {
                if (deleteAll || (!deleteAll && !record.getName().contains("CN=" + msUser + ",") && !record.getName().contains("CN=" + adminUser + ","))) {
                    Collection<ApiRoleX509> roles = record.getApiRoleX509Collection();
                    for (ApiRoleX509 role : roles) {
                        rolejpa.destroy(role.getApiRoleX509PK());
                    }
                    jpa.destroy(record.getId());
                    count++;
                    String apiName = record.getName().substring(3, record.getName().indexOf(","));
                    if (verbose) {
                        System.out.println("Deleting API client " + apiName + "..... Done");
                    }
                }
            }
            System.out.println(count + " API clients deleted.");
        } catch (Exception ex) {
            System.err.println("Exception occured: \r\n\r\n" + ex.toString());  
        }
    }
    
    private void deleteApiClient(String username) throws com.intel.mtwilson.ms.controller.exceptions.NonexistentEntityException, com.intel.mtwilson.ms.controller.exceptions.IllegalOrphanException {
        try {
            //boolean deleteAll = options.getBoolean("all", false);
            boolean verbose = options.getBoolean("verbose", false);
            ApiClientX509JpaController jpa = new ApiClientX509JpaController(em);
            ApiRoleX509JpaController rolejpa = new ApiRoleX509JpaController(em);
            List<ApiClientX509> list = jpa.findApiClientX509Entities();

            int count = 0;
            for (ApiClientX509 record : list) {
                if (record.getName().contains("CN=" + username + ",")) {
                    Collection<ApiRoleX509> roles = record.getApiRoleX509Collection();
                    for (ApiRoleX509 role : roles) {
                        rolejpa.destroy(role.getApiRoleX509PK());
                    }
                    jpa.destroy(record.getId());
                    count++;
                    String apiName = record.getName().substring(3, record.getName().indexOf(","));
                    if (verbose) {
                        System.out.println("Deleting API client " + apiName + "..... Done");
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println("Exception occured: \r\n\r\n" + ex.toString());
        }
    }
}
