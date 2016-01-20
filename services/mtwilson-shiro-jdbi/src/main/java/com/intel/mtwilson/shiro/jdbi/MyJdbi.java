/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.jdbi;

import com.intel.mtwilson.My;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
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
    
    private static DBI dbi = null;
    
    /**
     * Must close connection when done!  for example: 
     * try(LoginDAO loginDAO = MyJdbi.authz()) {
     * // do things... java7 will automatically call close() on loginDAO when done or on exception
     * }
     * @return
     * @throws SQLException 
     */
 public static LoginDAO authz() throws SQLException {
//     createTables();
    return getDBI().open(LoginDAO.class);
  } 
 
 private static DBI getDBI() {
     log.debug("MyJdbi (mtwilson-shiro-jdbi) static DBI instance: {}", dbi);
     if( dbi == null ) {
        dbi = new DBI(new ExistingConnectionFactory());
        log.debug("MyJdbi (mtwilson-shiro-jdbi) created new DBI instance: {}", dbi);
     }
     return dbi;
 }

 
 public static class ExistingConnectionFactory implements ConnectionFactory {
        @Override
        public Connection openConnection() throws SQLException {
            try {
                Connection connection = My.jdbc().connection();
                log.debug("MyJdbi (mtwilson-shiro-jdbi) connection: {}", connection);
                return connection;
            }
            catch(IOException | ClassNotFoundException | SQLException e) {
                throw new RuntimeException(e);
            }
        }    
}
 
 
}
