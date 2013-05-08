/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.cmd;

import com.intel.mountwilson.as.common.ASConfig;
import com.intel.mtwilson.MyPersistenceManager;
import com.intel.mtwilson.as.controller.MwMleSourceJpaController;
import com.intel.mtwilson.as.controller.TblHostSpecificManifestJpaController;
import com.intel.mtwilson.as.controller.TblHostsJpaController;
import com.intel.mtwilson.as.controller.TblMleJpaController;
import com.intel.mtwilson.as.controller.TblModuleManifestJpaController;
import com.intel.mtwilson.as.controller.TblOemJpaController;
import com.intel.mtwilson.as.controller.TblOsJpaController;
import com.intel.mtwilson.as.controller.TblPcrManifestJpaController;
import com.intel.mtwilson.as.controller.exceptions.IllegalOrphanException;
import com.intel.mtwilson.as.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.as.data.MwMleSource;
import com.intel.mtwilson.as.data.TblHostSpecificManifest;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.as.data.TblMle;
import com.intel.mtwilson.as.data.TblModuleManifest;
import com.intel.mtwilson.as.data.TblOem;
import com.intel.mtwilson.as.data.TblOs;
import com.intel.mtwilson.as.data.TblPcrManifest;
import com.intel.mtwilson.as.helper.ASPersistenceManager;
import com.intel.mtwilson.crypto.CryptographyException;
import com.intel.mtwilson.ms.common.MSConfig;
import com.intel.mtwilson.ms.controller.ApiClientX509JpaController;
import com.intel.mtwilson.ms.controller.ApiRoleX509JpaController;
import com.intel.mtwilson.ms.controller.MwPortalUserJpaController;
import com.intel.mtwilson.ms.data.ApiClientX509;
import com.intel.mtwilson.ms.data.ApiRoleX509;
import com.intel.mtwilson.ms.data.MwPortalUser;
import com.intel.mtwilson.ms.helper.MSPersistenceManager;
import com.intel.mtwilson.setup.Command;
import com.intel.mtwilson.setup.SetupContext;
import com.intel.mtwilson.setup.SetupException;
import com.intel.mtwilson.setup.SetupWizard;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManagerFactory;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.Configuration;

/**
 * Accepts one option:  --all   indicates that even admin and ManagementServiceAutomation should be removed.
 * @author jbuhacoff
 */
public class EraseUserAccounts implements Command {
    private SetupContext ctx = null;
    private MSPersistenceManager pm;
    private EntityManagerFactory em;
    
    @Override
    public void setContext(SetupContext ctx) {
        this.ctx = ctx;
    }

    private Configuration options = null;
    @Override
    public void setOptions(Configuration options) {
        this.options = options;
    }

    /**
     * Deletes all user accounts EXCEPT the admin user and the ManagementServiceAutomation user
     * @param args
     * @throws Exception 
     */
    @Override
    public void execute(String[] args) throws Exception {
        Configuration serviceConf = MSConfig.getConfiguration();
        pm = new MSPersistenceManager();
        em = pm.getEntityManagerFactory("ASDataPU");
        deletePortalUsers();
        deleteApiClients();
    }
    
    private void deletePortalUsers() throws com.intel.mtwilson.ms.controller.exceptions.NonexistentEntityException {
        boolean deleteAll = options.getBoolean("all", false);
        MwPortalUserJpaController jpa = new MwPortalUserJpaController(em);
        List<MwPortalUser> list = jpa.findMwPortalUserEntities();
        for(MwPortalUser record : list) {
            if( deleteAll || (!deleteAll && !record.getUsername().equals("ManagementServiceAutomation") && !record.getUsername().equals("admin")) ) {
                jpa.destroy(record.getId());
            }
        }
    }
    
    private void deleteApiClients() throws com.intel.mtwilson.ms.controller.exceptions.NonexistentEntityException, com.intel.mtwilson.ms.controller.exceptions.IllegalOrphanException {
        boolean deleteAll = options.getBoolean("all", false);
        ApiClientX509JpaController jpa = new ApiClientX509JpaController(em);
        ApiRoleX509JpaController rolejpa = new ApiRoleX509JpaController(em);
        List<ApiClientX509> list = jpa.findApiClientX509Entities();
        for(ApiClientX509 record : list) {
            if( deleteAll || (!deleteAll && !record.getName().contains("CN=ManagementServiceAutomation") && !record.getName().contains("CN=admin")) ) {
                Collection<ApiRoleX509> roles = record.getApiRoleX509Collection();
                for(ApiRoleX509 role : roles) {
                    rolejpa.destroy(role.getApiRoleX509PK());
                }
                jpa.destroy(record.getId());
            }
        }
    }
    
}
