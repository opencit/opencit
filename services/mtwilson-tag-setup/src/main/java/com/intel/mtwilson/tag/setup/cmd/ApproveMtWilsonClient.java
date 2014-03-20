/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tag.setup.cmd;

import com.intel.mtwilson.tag.setup.TagCommand;
import com.intel.mtwilson.My;
import com.intel.mtwilson.ms.controller.ApiClientX509JpaController;
import com.intel.mtwilson.ms.data.ApiClientX509;
import com.intel.mtwilson.setup.SetupException;
import java.util.Properties;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.configuration.MapConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This command exports a file from the database to the filesystem
 * @author jbuhacoff
 */
public class ApproveMtWilsonClient extends TagCommand {
    private static Logger log = LoggerFactory.getLogger(ApproveMtWilsonClient.class);
    
    @Override
    public void execute(String[] args) throws Exception {
        
        // check if username/password is provided on command line or already configured ...
        String fingerprint = null;
        
        if( getOptions().containsKey("fingerprint") ) {
            fingerprint = getOptions().getString("fingerprint", "");
        }
        
//        // already configured
//        if( fingerprint == null ) {
//            fingerprint = My.configuration().getApiClientFingerprint().toString().getBytes(); // Global.configuration().getMtWilsonURL();
//        }
        
//        // defaults
//        if( fingerprint == null ) {
//            fingerprint = "".getBytes();
//        }

        if( fingerprint == null ) {
            throw new SetupException("Please specify a fingerprint for the API client user to be approved.");
        }

        try {
            System.out.println(String.format("Searching for client by fingerprint: %s", fingerprint));
            ApiClientX509JpaController x509jpaController = My.jpa().mwApiClientX509();
            ApiClientX509 client = x509jpaController.findApiClientX509ByFingerprint(Hex.decodeHex(fingerprint.toCharArray()));
            if( client == null ) {
                log.error("Cannot find client record with fingerprint {}", fingerprint);
                throw new IllegalStateException("Cannot find client record with fingerprint "+fingerprint);
            }
            client.setStatus("Approved");
            client.setEnabled(true);
            x509jpaController.edit(client);
        }
        catch(Exception e) {
            throw new SetupException("Cannot update API Client record: "+e.getMessage(), e);
        }
    }
    
    
    public static void main(String args[]) throws Exception {
        ApproveMtWilsonClient cmd = new ApproveMtWilsonClient();
        cmd.setOptions(new MapConfiguration(new Properties()));
        cmd.execute(new String[] { });
    }    
    
}
