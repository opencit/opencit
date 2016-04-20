/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.cmd;

import com.intel.mountwilson.as.common.ASConfig;
import com.intel.mtwilson.as.controller.TblHostSpecificManifestJpaController;
import com.intel.mtwilson.as.controller.TblHostsJpaController;
import com.intel.mtwilson.as.controller.exceptions.IllegalOrphanException;
import com.intel.mtwilson.as.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.as.data.TblHostSpecificManifest;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.console.Command;
import com.intel.mtwilson.My;
import com.intel.mtwilson.setup.SetupException;
import java.io.IOException;
import java.util.List;
import org.apache.commons.configuration.Configuration;

/**
 *
 * @author jbuhacoff
 */
public class EraseHostRegistrationData implements Command {
  
    private Configuration options = null;
    @Override
    public void setOptions(Configuration options) {
        this.options = options;
    }

    /**
     * Creates a new API Client in current directory, registers it with Mt Wilson (on localhost or as configured), and then checks the database for the expected record to validate that it's being created.
     * @param args
     * @throws Exception 
     */
    @Override
    public void execute(String[] args) throws Exception {
        Configuration serviceConf = ASConfig.getConfiguration();
        deleteHostRegistrationRecords(serviceConf);
    }
    
    private void deleteHostRegistrationRecords(Configuration conf) throws SetupException, CryptographyException, IllegalOrphanException, NonexistentEntityException, IOException {
        TblHostSpecificManifestJpaController hsmJpa = My.jpa().mwHostSpecificManifest();
        List<TblHostSpecificManifest> hsmList = hsmJpa.findTblHostSpecificManifestEntities();
        for(TblHostSpecificManifest hsm : hsmList) {
            hsmJpa.destroy(hsm.getId());
        }
//        byte[] dek = Base64.decodeBase64(ASConfig.getConfiguration().getString("mtwilson.as.dek"));
        TblHostsJpaController jpa = My.jpa().mwHosts();
        List<TblHosts> list = jpa.findTblHostsEntities();
        for(TblHosts host : list) {
            System.out.println("Deleting host #"+host.getId()+": "+host.getName());
            jpa.destroy(host.getId());
        }
        
    }

}
