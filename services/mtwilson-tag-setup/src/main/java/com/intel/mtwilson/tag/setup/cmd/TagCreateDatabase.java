package com.intel.mtwilson.tag.setup.cmd;

///*
// * Copyright (C) 2013 Intel Corporation
// * All rights reserved.
// */
//package com.intel.mtwilson.tag.setup;
//
//import com.intel.mtwilson.atag.AtagCommand;
//import com.intel.mtwilson.atag.dao.Derby;
//import com.intel.mtwilson.atag.dao.jdbi.*;
//import java.sql.Connection;
//import java.sql.SQLException;
//import java.sql.Statement;
//import java.util.Properties;
//import java.util.Set;
//import javax.sql.DataSource;
//import org.apache.commons.configuration.MapConfiguration;
//import org.skife.jdbi.v2.DBI;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
///**
// *
// * @author jbuhacoff
// */
//public class CreateDatabase extends AtagCommand {
//    private static Logger log = LoggerFactory.getLogger(CreateDatabase.class);
//    private com.intel.mtwilson.atag.dao.CreateDatabase creator = new com.intel.mtwilson.atag.dao.CreateDatabase();
//    
//    @Override
//    public void execute(String[] args) throws Exception {
//        creator.setDropTablesEnabled(getOptions().getBoolean("drop", false));
//        creator.execute(args);
//    }
//    
//
// 
//    public static void main(String args[]) throws Exception {
//        CreateDatabase cmd = new CreateDatabase();
//        cmd.setOptions(new MapConfiguration(new Properties()));
//        cmd.execute(new String[0]);
//        
//    }    
//}
