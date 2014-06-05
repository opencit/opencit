/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.cmd;

import com.intel.mtwilson.ms.common.MSConfig;
import com.intel.dcsg.cpg.console.Command;
import com.intel.mtwilson.setup.SetupContext;
import com.intel.mtwilson.setup.SetupException;
import com.intel.mtwilson.setup.SetupWizard;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.commons.configuration.Configuration;

/**
 *
 * @author jbuhacoff
 */
public class ListApiClients implements Command {
 
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
        Configuration serviceConf = MSConfig.getConfiguration();
        listApiClientRecords(serviceConf);
    }
    
    private boolean listApiClientRecords(Configuration conf) throws SetupException, IOException {
        boolean found = false;
        SetupWizard wizard = new SetupWizard(conf);
        try {
            try (Connection c = wizard.getMSDatabaseConnection();
                    PreparedStatement s = c.prepareStatement("SELECT ID,name,hex(enabled) as enabled,status,hex(fingerprint) as fingerprint, comment FROM api_client_x509");
                    ResultSet rs = s.executeQuery()) {
                while (rs.next()) {
                    System.out.println("---");
                    System.out.println("  ID: " + rs.getInt("ID"));
                    System.out.println("  Name: " + rs.getString("name"));
                    System.out.println("  Enabled: " + rs.getString("enabled"));
                    System.out.println("  Status: " + rs.getString("status"));
                    System.out.println("  Fingerprint: " + rs.getString("fingerprint"));
                    System.out.println("  Comment: " + rs.getString("comment"));
                }
            }
            return found;
        }
        catch(SQLException e) {
            throw new SetupException("Cannot query API Client records: "+e.getMessage(), e);
        } 
    }

}
