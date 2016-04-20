/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.cmd;

import com.intel.mtwilson.My;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.dcsg.cpg.console.Command;
import com.intel.mtwilson.setup.SetupContext;
import java.util.List;
import org.apache.commons.configuration.Configuration;

/**
 *
 * @author jbuhacoff
 */
public class FindHost implements Command {
  

    private Configuration options = null;
    @Override
    public void setOptions(Configuration options) {
        this.options = options;
    }

    @Override
    public void execute(String[] args) throws Exception {

        if( args.length < 1) { throw new IllegalArgumentException("Usage: FindHost <name>"); }   
        String hostname = args[0];
        
        List<TxtHostRecord> results = My.client().queryForHosts(hostname);
        System.out.println(String.format("Found %d matching records", results.size()));
        for(TxtHostRecord txtHostRecord : results) {
            System.out.println(txtHostRecord.AddOn_Connection_String);
        }
    }

    

}
