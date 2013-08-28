/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag;

import com.intel.mtwilson.atag.dao.jdbi.*;
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
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * References:
 * Validation queries: http://stackoverflow.com/questions/3668506/efficient-sql-test-query-or-validation-query-that-will-work-across-all-or-most
 * 
 * @author jbuhacoff
 */
public class Derby {
    private static Logger log = LoggerFactory.getLogger(Derby.class);
    public static String driver = "org.apache.derby.jdbc.EmbeddedDriver";
    //public static String protocol = "jdbc:derby:";
    
    /**
     * NOTE: this url is repeated in the pom.xml where jooq needs it to connect
     * to the database to grab the schema and automatically generate sources
     */
    public static String protocol = "jdbc:derby:directory:target/derby/mytestdb;create=true"; // places it in directory "target/derby" under current directory (good for junit testing)
    public static Connection c = null;
    public static DataSource ds = null;
    private static boolean isLoaded = false;
    private static boolean isSchemaCreated = false;
    
    public static void startDatabase() throws SQLException {
        if( !isLoaded ) {
            try {
            // find the temporary directory
//            File temp = File.createTempFile("derby", ".tmp");
//            System.out.println("temp file in: "+temp.getAbsolutePath()+" ; parent in "+temp.getParent());
//            System.setProperty("derby.system.home", temp.getParent()); // System.getProperty("user.home")+File.separator+".derby");
            Class.forName(driver).newInstance();
            isLoaded = true;
            }
            catch(Exception e) {
                throw new SQLException("Cannot load Derby driver", e);
            }
        }
    }
    
    public static void stopDatabase() throws Exception {
//        DriverManager.getConnection("jdbc:derby:MyDbTest;shutdown=true");  // shut down a specific database
//        DriverManager.getConnection("jdbc:derby:;shutdown=true"); // shut down all databaes and the derby engine  ; throws SQLException "Derby system shutdown."
    }
    
    public static DataSource getDataSource() throws SQLException {
        if( !isLoaded ) { startDatabase(); }
        if( ds == null ) {
        BasicDataSource dataSource = new BasicDataSource();

        dataSource.setDriverClassName(driver); // or com.mysql.jdbc.Driver  for mysql
//        dataSource.setUsername("username");
//        dataSource.setPassword("password");
        //dataSource.setUrl("jdbc:derby:mytestdb;create=true"); // automatically creates derby in-memory db,   or use "jdbc:mysql://<host>:<port>/<database>"  for a mysql db
        dataSource.setUrl(protocol); // creates it in the "target/derby" folder which is for temporary files, good for junit tests
        dataSource.setMaxActive(10);
        dataSource.setMaxIdle(5);
        dataSource.setInitialSize(5);
        dataSource.setValidationQuery("VALUES 1");  // derby-specific query,  for mysql /postgresl / microsoft sql / sqlite / and h2  use "select 1"
        ds = dataSource;
        }
        return ds;
    }
    public static Connection getConnection() throws SQLException {
        if( c == null ) {
            c = getDataSource().getConnection(); // also a username/password option is available
        }
        return c;
        //return DriverManager.getConnection(protocol + "derbyDB;create=true", new Properties());
//        return getDataSource().getConnection(); 
    }
    
    public static void testDatabaseConnection() throws SQLException {
        Connection c = DriverManager.getConnection(protocol, new Properties());
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("VALUES 1");
        if( rs.next() ) {
            log.info("Database connection is ok");
        }
        rs.close();
        s.close();
        c.close();
    }

    public static boolean tableExists(String tableName) throws SQLException {
        Set<String> availableTables = listTablesAndViews(getConnection());
        return availableTables.contains(tableName) || availableTables.contains(tableName.toUpperCase()); // derby tables names tend to be all caps
    }
    
    public static Set<String> listTablesAndViews(Connection targetDBConn) throws SQLException
  {
    HashSet<String> set = new HashSet<String>();
    DatabaseMetaData dbmeta = targetDBConn.getMetaData();
    readDBTable(set, dbmeta, "TABLE", null);
    readDBTable(set, dbmeta, "VIEW", null);
    return set;
  }

  private static void readDBTable(Set<String> set, DatabaseMetaData dbmeta, String searchCriteria, String schema)
      throws SQLException
  {
    ResultSet rs = dbmeta.getTables(null, schema, null, new String[]
    { searchCriteria });
    while (rs.next())
    {
        log.trace("readDBTable Table: {}" , rs.getString("TABLE_NAME"));
      set.add(rs.getString("TABLE_NAME"));
    }
  }    
  
    public static void createTables() throws SQLException  {
        DBI dbi = new DBI(getDataSource());
        TagDAO tagDao = dbi.open(TagDAO.class);
        if( !Derby.tableExists("tag") ) { tagDao.create(); }        // throws SQLException
        tagDao.close();
        
        TagValueDAO tagValueDao = dbi.open(TagValueDAO.class);
        if( !Derby.tableExists("tag_value") ) { tagValueDao.create(); }      // throws SQLException
        tagValueDao.close();
        
    }
  
    // helper methods for jdbi
  
  /*
  public static <T> T openDAO(T clazz) {
    DBI dbi = new DBI(getDataSource());
    T dao = dbi.open(clazz.getClass());
    return null;
  }*/
 public static TagDAO tagDao() throws SQLException {
//     createTables();
    DBI dbi = new DBI(getDataSource());
    return dbi.open(TagDAO.class);
  } 
 public static TagValueDAO tagValueDao() throws SQLException {
//     createTables();
    DBI dbi = new DBI(getDataSource());
    return dbi.open(TagValueDAO.class);
  } 
 public static RdfTripleDAO rdfTripleDao() throws SQLException {
//     createTables();
    DBI dbi = new DBI(getDataSource());
    return dbi.open(RdfTripleDAO.class);
  } 
 public static CertificateRequestDAO certificateRequestDao() throws SQLException {
//     createTables();
    DBI dbi = new DBI(getDataSource());
    return dbi.open(CertificateRequestDAO.class);
  } 
 public static CertificateRequestTagValueDAO certificateRequestTagValueDao() throws SQLException {
//     createTables();
    DBI dbi = new DBI(getDataSource());
    return dbi.open(CertificateRequestTagValueDAO.class);
  } 
 public static CertificateRequestApprovalDAO certificateRequestApprovalDao() throws SQLException {
//     createTables();
    DBI dbi = new DBI(getDataSource());
    return dbi.open(CertificateRequestApprovalDAO.class);
  } 
 public static CertificateDAO certificateDao() throws SQLException {
//     createTables();
    DBI dbi = new DBI(getDataSource());
    return dbi.open(CertificateDAO.class);
  } 
 public static SelectionDAO selectionDao() throws SQLException {
//     createTables();
    DBI dbi = new DBI(getDataSource());
    return dbi.open(SelectionDAO.class);
  } 
 public static SelectionTagValueDAO selectionTagValueDao() throws SQLException {
//     createTables();
    DBI dbi = new DBI(getDataSource());
    return dbi.open(SelectionTagValueDAO.class);
  } 
 public static ConfigurationDAO configurationDao() throws SQLException {
//     createTables();
    DBI dbi = new DBI(getDataSource());
    return dbi.open(ConfigurationDAO.class);
  } 
 
 
  // helper methods for JOOQ
 
 public static DSLContext jooq() throws SQLException {
        DSLContext jooq = DSL.using(Derby.getConnection(), SQLDialect.DERBY); // throws SQLException; Note that the DSLContext doesn't close the connection. We'll have to do that ourselves.
        return jooq;
 }
}
