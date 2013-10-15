/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.cmd;

import com.intel.dcsg.cpg.console.Command;
import com.intel.mtwilson.atag.AtagCommand;
import com.intel.mtwilson.atag.dao.Derby;
import com.intel.mtwilson.atag.dao.jdbi.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Set;
import javax.sql.DataSource;
import org.apache.commons.configuration.MapConfiguration;
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class Setup extends AtagCommand {
    private static Logger log = LoggerFactory.getLogger(Setup.class);
    private com.intel.mtwilson.atag.dao.CreateDatabase creator = new com.intel.mtwilson.atag.dao.CreateDatabase();
    
    @Override
    public void execute(String[] args) throws Exception {
        // tasks to complete: create-database, init-database, create-ca-key, and create-mtwilson-client
        CreateDatabase createDatabase = new CreateDatabase();
        InitDatabase initDatabase = new InitDatabase();
        CreateCaKey createCaKey = new CreateCaKey();
        Command[] tasks = new Command[] { createDatabase, initDatabase, createCaKey };
        for(int i=0; i<tasks.length; i++) {
            tasks[i].setOptions(getOptions());
            tasks[i].execute(new String[0]);
        }
    }
    

 
    public static void main(String args[]) throws Exception {
        Setup cmd = new Setup();
        cmd.setOptions(new MapConfiguration(new Properties()));
        cmd.execute(new String[0]);
        
    }    
}
