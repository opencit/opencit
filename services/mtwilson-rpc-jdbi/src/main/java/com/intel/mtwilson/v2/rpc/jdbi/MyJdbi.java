/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.v2.rpc.jdbi;

import com.intel.mtwilson.My;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import javax.sql.DataSource;
import org.apache.commons.dbcp.BasicDataSource;
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.skife.jdbi.v2.tweak.ConnectionFactory;
/**
 * References:
 * Validation queries: http://stackoverflow.com/questions/3668506/efficient-sql-test-query-or-validation-query-that-will-work-across-all-or-most
 * 
 * @author jbuhacoff
 */
public class MyJdbi {
    private static Logger log = LoggerFactory.getLogger(MyJdbi.class);
  
  /*
  public static <T> T openDAO(T clazz) {
    DBI dbi = new DBI(getDataSource());
    T dao = dbi.open(clazz.getClass());
    return null;
  }*/
 public static RpcDAO rpc() throws SQLException {
//     createTables();
    DBI dbi = new DBI(new ExistingConnectionFactory());
    
    return dbi.open(RpcDAO.class);
  } 

 
 public static class ExistingConnectionFactory implements ConnectionFactory {
        @Override
        public Connection openConnection() throws SQLException {
            try {
                return My.jdbc().connection();
            }
            catch(Exception e) {
                throw new RuntimeException(e);
            }
        }    
}
 
 
}
