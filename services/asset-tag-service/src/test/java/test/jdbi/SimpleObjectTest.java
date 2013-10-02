/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.jdbi;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import javax.sql.DataSource;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * References:
 * Validation queries: http://stackoverflow.com/questions/3668506/efficient-sql-test-query-or-validation-query-that-will-work-across-all-or-most
 * 
 * @author jbuhacoff
 */
public class SimpleObjectTest {
    private static Logger log = LoggerFactory.getLogger(SimpleObjectTest.class);
    public static String driver = "org.apache.derby.jdbc.EmbeddedDriver";
    public static String protocol = "jdbc:derby:";
    public static DataSource ds = null;
    
    @BeforeClass
    public static void startDatabase() throws Exception {
        // find the temporary directory
        File temp = File.createTempFile("derby", ".tmp");
        log.debug("temp file in: "+temp.getAbsolutePath()+" ; parent in "+temp.getParent());
        System.setProperty("derby.system.home", temp.getParent()); // System.getProperty("user.home")+File.separator+".derby");
        Class.forName(driver).newInstance();
    }
    
    @AfterClass
    public static void stopDatabase() throws Exception {
//        DriverManager.getConnection("jdbc:derby:MyDbTest;shutdown=true");  // shut down a specific database
//        DriverManager.getConnection("jdbc:derby:;shutdown=true"); // shut down all databaes and the derby engine  ; throws SQLException "Derby system shutdown."
    }
    
    private DataSource getDataSource() {
        BasicDataSource dataSource = new BasicDataSource();

        dataSource.setDriverClassName(driver); // or com.mysql.jdbc.Driver  for mysql
//        dataSource.setUsername("username");
//        dataSource.setPassword("password");
        dataSource.setUrl("jdbc:derby:mytestdb;create=true"); // automatically creates derby in-memory db,   or use "jdbc:mysql://<host>:<port>/<database>"  for a mysql db
        dataSource.setMaxActive(10);
        dataSource.setMaxIdle(5);
        dataSource.setInitialSize(5);
        dataSource.setValidationQuery("VALUES 1");  // derby-specific query,  for mysql /postgresl / microsoft sql / sqlite / and h2  use "select 1"
        return dataSource;
    }
    private Connection getConnection() throws SQLException {
        //return DriverManager.getConnection(protocol + "derbyDB;create=true", new Properties());
        if( ds == null ) {
            ds = getDataSource();
        }
        return ds.getConnection(); // also a username/password option is available
    }
    
    @Test
    public void testCreateInMemoryDbWithDriverManager() throws SQLException {
        Connection c = DriverManager.getConnection(protocol + "derbyDB;create=true", new Properties());
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("VALUES 1");
        if( rs.next() ) {
            System.out.println("database ok");
        }
        rs.close();
        s.close();
        c.close();
    }
    
    @Test
    public void testCreateInMemoryDb() {
        ds = getDataSource();
        DBI dbi = new DBI(ds);
        SimpleObjectDAO dao = dbi.open(SimpleObjectDAO.class);

        dao.create();
        dao.insert(2, "Aaron");

        String name = dao.findNameById(2);
        assertEquals(name, "Aaron"); 

        dao.close();
    }
}
