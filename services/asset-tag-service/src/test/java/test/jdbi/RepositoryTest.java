/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.jdbi;

import com.intel.mtwilson.atag.model.Tag;
import com.intel.mtwilson.atag.model.CertificateRequestTagValue;
import com.intel.mtwilson.atag.model.CertificateRequest;
import com.intel.mtwilson.atag.dao.jdbi.RdfTripleDAO;
import com.intel.mtwilson.atag.dao.jdbi.CertificateDAO;
import com.intel.mtwilson.atag.dao.jdbi.TagDAO;
import com.intel.mtwilson.atag.dao.jdbi.TagValueDAO;
import com.intel.mtwilson.atag.dao.jdbi.CertificateRequestDAO;
import com.intel.mtwilson.atag.dao.jdbi.CertificateRequestTagValueDAO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.intel.mtwilson.atag.Derby;
import com.intel.dcsg.cpg.io.UUID;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.sql.DataSource;
import org.apache.commons.lang3.StringUtils;
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
public class RepositoryTest {
    private static Logger log = LoggerFactory.getLogger(RepositoryTest.class);
    public static String driver = "org.apache.derby.jdbc.EmbeddedDriver";
    public static String protocol = "jdbc:derby:";
    public static DataSource ds = null;
    
    public static void main(String[] args) throws Exception {
        startDatabase();
        new RepositoryTest().testDescribeDatabase();
        stopDatabase();
    }
    
    @BeforeClass
    public static void startDatabase() throws Exception {
        /*
        // find the temporary directory
        File temp = File.createTempFile("derby", ".tmp");
        System.out.println("temp file in: "+temp.getAbsolutePath()+" ; parent in "+temp.getParent());
        System.setProperty("derby.system.home", temp.getParent()); // System.getProperty("user.home")+File.separator+".derby");
        Class.forName(driver).newInstance();
        */
        ds = Derby.getDataSource();
        
        testCreateInMemoryDbWithDriverManager(); // make sure database exists
        dropAllTablesInDb(); // start fresh... without any tables
        testCreateInMemoryDb(); // create the tables
    }
    
    @AfterClass
    public static void stopDatabase() throws Exception {
//        DriverManager.getConnection("jdbc:derby:MyDbTest;shutdown=true");  // shut down a specific database
        try {
        DriverManager.getConnection("jdbc:derby:;shutdown=true"); // shut down all databaes and the derby engine  ; throws SQLException "Derby system shutdown."
        }
        catch(Exception e) {
            // A clean shutdown always throws SQL exception XJ015, which can be ignored.
            // http://db.apache.org/derby/papers/DerbyTut/embedded_intro.html
            log.trace("Derby system shutdown", e);
        }
    }
    /*
    private static DataSource getDataSource() {
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
    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(protocol + "derbyDB;create=true", new Properties());
        /*
        if( ds == null ) {
            ds = getDataSource();
        }
        return ds.getConnection(); // also a username/password option is available
        * /
    }*/
    
    private static void testCreateInMemoryDbWithDriverManager() throws SQLException {
        Connection c = Derby.getConnection(); //DriverManager.getConnection(protocol + "derbyDB;create=true", new Properties());
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("VALUES 1");
        if( rs.next() ) {
            System.out.println("database ok");
        }
        rs.close();
        s.close();
//        c.close(); // don't close the connection because we didn't open it.
    }
    
    private static void dropAllTablesInDb() throws SQLException {
        Connection c = Derby.getConnection();
        Set<String> tables = Derby.listTablesAndViews(c);
        for(String table : tables) {
            Statement s = c.createStatement();
            s.executeUpdate("DROP TABLE "+table);
            s.close();
        }
    }
    
//    @Test
    public static void testCreateInMemoryDb() throws SQLException {
        ds = Derby.getDataSource();
        DBI dbi = new DBI(ds);
        
        // tag
        TagDAO tagDao = dbi.open(TagDAO.class);
        if( !Derby.tableExists("tag") ) { tagDao.create(); }        
        tagDao.close();
        
        // tag value
        TagValueDAO tagValueDao = dbi.open(TagValueDAO.class);
        if( !Derby.tableExists("tag_value") ) { tagValueDao.create(); }
        tagValueDao.close();
        
        // rdf triple
        RdfTripleDAO rdfTripleDao = dbi.open(RdfTripleDAO.class);
        if( !Derby.tableExists("rdf_triple") ) { rdfTripleDao.create(); }
        rdfTripleDao.close();
        
        // certificate request
        CertificateRequestDAO certificateRequestDao = dbi.open(CertificateRequestDAO.class);
        if( !Derby.tableExists("certificate_request") ) { certificateRequestDao.create(); }
        certificateRequestDao.close();
        
        // certificate request tag value
        CertificateRequestTagValueDAO certificateRequestTagValueDao = dbi.open(CertificateRequestTagValueDAO.class);
        if( !Derby.tableExists("certificate_request_tag_value") ) { certificateRequestTagValueDao.create(); }
        certificateRequestTagValueDao.close();

        // certificate
        CertificateDAO certificateDao = dbi.open(CertificateDAO.class);
        if( !Derby.tableExists("certificate") ) { certificateDao.create(); }
        certificateRequestTagValueDao.close();
        
    }
    
    @Test
    public void testDescribeDatabase() throws SQLException {
        Connection c = Derby.getConnection();
        
        ds = Derby.getDataSource();
        Set<String> tables = Derby.listTablesAndViews(Derby.getConnection());
        for(String table : tables) {
            log.debug("TABLE: {}", table);
            ResultSet rs = c.getMetaData().getColumns(null, null, table, null);
            int columns = rs.getMetaData().getColumnCount();
            while(rs.next()) {
                /*
                for(int i=1; i<=columns; i++) {
                    log.debug("COLUMN: "+rs.getMetaData().getColumnName(i)+" = "+rs.getString(1));
                }
                */
                /*
                 * Sample output:
2013-08-04 19:13:15,757 DEBUG [main] t.j.RepositoryTest [RepositoryTest.java:132] TABLE: TAG_VALUE
2013-08-04 19:13:15,896 DEBUG [main] t.j.RepositoryTest [RepositoryTest.java:140] Column: ID  Data type: BIGINT
2013-08-04 19:13:15,897 DEBUG [main] t.j.RepositoryTest [RepositoryTest.java:140] Column: TAGID  Data type: BIGINT
2013-08-04 19:13:15,899 DEBUG [main] t.j.RepositoryTest [RepositoryTest.java:140] Column: VALUE  Data type: VARCHAR
2013-08-04 19:13:15,900 DEBUG [main] t.j.RepositoryTest [RepositoryTest.java:132] TABLE: TAG
2013-08-04 19:13:15,931 DEBUG [main] t.j.RepositoryTest [RepositoryTest.java:140] Column: ID  Data type: BIGINT
2013-08-04 19:13:15,933 DEBUG [main] t.j.RepositoryTest [RepositoryTest.java:140] Column: UUID  Data type: CHAR
2013-08-04 19:13:15,936 DEBUG [main] t.j.RepositoryTest [RepositoryTest.java:140] Column: NAME  Data type: VARCHAR
2013-08-04 19:13:15,937 DEBUG [main] t.j.RepositoryTest [RepositoryTest.java:140] Column: OID  Data type: VARCHAR
                 */
                log.debug(String.format("Column: %s  Data type: %s", rs.getString("COLUMN_NAME"), rs.getString("TYPE_NAME")));
            }
            rs.close();
        }
    }
    
    @Test
    public void testTag() throws SQLException {
        ds = Derby.getDataSource();
        DBI dbi = new DBI(ds);
        TagDAO dao = dbi.open(TagDAO.class);

        UUID uuid = new UUID();
        long id = dao.insert(uuid, "location", "1.1.1.1");

        String name = dao.findNameById(id+12391); // non-existing id, since we only added id #1 above
        assertNull(name);
        
        name = dao.findNameById(id); // this one should exist
        assertEquals("location", name); 

        Tag tag = dao.findByUuid(uuid);
        assertEquals("location", tag.getName());
        assertEquals(uuid.toString(), tag.getUuid().toString());
        assertEquals(uuid.toHexString(), tag.getUuid().toHexString());
        assertEquals("1.1.1.1", tag.getOid());
        assertEquals(id, tag.getId());
        
        dao.close();
    }
    
    @Test
    public void testTagValue() throws SQLException {
//        ds = Derby.getDataSource();
//        DBI dbi = new DBI(ds);
//        TagDAO tagDao = dbi.open(TagDAO.class);
//        TagValueDAO dao = dbi.open(TagValueDAO.class);
        TagDAO tagDao = Derby.tagDao();
        TagValueDAO dao = Derby.tagValueDao();

        UUID tagUuid = new UUID();
        long tagId = tagDao.insert(tagUuid, "first name", "1.1.1.1");
        
        // generate some names to insert as values
        String[] firstNames = new String[] { "John", "Mike", "Bob", "Alice", "Clara", "Zoey" };
        log.debug("There are {} values to batch insert", firstNames.length);
        
        int[] results = dao.insert(tagId, Arrays.asList(firstNames));
        
        int inserted = 0; 
        for(int i=0; i<results.length; i++) { inserted += results[i]; }
        
        log.debug("Batch inserted {} values", inserted);

        
        
        dao.close();
        tagDao.close();
    }
    
    // this report function is also in TagApiTest2
    private void report(CertificateRequest[] certificateRequests) throws JsonProcessingException {
        if( certificateRequests == null ) { log.debug("Report: certificate-requests is null"); }
        log.debug("Report: {} certificate-requests", certificateRequests.length);
        for(CertificateRequest certificateRequest : certificateRequests) {
            log.debug("uuid: {}", certificateRequest.getUuid());
            log.debug("subject: {}", certificateRequest.getSubject());
            ArrayList<String> tagpairs = new ArrayList<String>();
            List<CertificateRequestTagValue> tags = certificateRequest.getTags();
            if( tags == null ) {
                log.debug("tags property is null");
            }
            else {
                for(CertificateRequestTagValue tag : tags) {
                    tagpairs.add(String.format("(%s: %s)", tag.getName(), tag.getValue()));
                }
                log.debug("certificate-request: {}", String.format("uuid:%s  subject:%s  tags: %s", certificateRequest.getUuid(), certificateRequest.getSubject(), StringUtils.join(tagpairs, " ")));
            }
        }
    }
    private void report(CertificateRequestTagValue[] certificateRequestTagValues) throws JsonProcessingException {
        if( certificateRequestTagValues == null ) { log.debug("Report: certificate-request-tag-values is null"); }
        log.debug("Report: {} certificate-request-tag-value", certificateRequestTagValues.length);
        for(CertificateRequestTagValue certificateRequestTagValue : certificateRequestTagValues) {
            log.debug("certificate-request-tag-value: {}", String.format("name:%s  oid:%s  value: %s", certificateRequestTagValue.getName(), certificateRequestTagValue.getOid(), certificateRequestTagValue.getValue() ));
        }
    }
    private void report(List<CertificateRequestTagValue> certificateRequestTagValues) throws JsonProcessingException {
        if( certificateRequestTagValues == null ) { log.debug("Report: certificate-request-tag-values is null"); }
        log.debug("Report: {} certificate-request-tag-value", certificateRequestTagValues.size());
        for(CertificateRequestTagValue certificateRequestTagValue : certificateRequestTagValues) {
            log.debug("certificate-request-tag-value: {}", String.format("name:%s  oid:%s  value: %s", certificateRequestTagValue.getName(), certificateRequestTagValue.getOid(), certificateRequestTagValue.getValue() ));
        }
    }
    
    /*
    // this one is commented out because a certificate request is not going to work w/o first inserting tags and tag values... see TagApiTest2 for the full test
    @Test
    public void testCertificateRequest() throws SQLException, JsonProcessingException {
        ds = Derby.getDataSource();
        DBI dbi = new DBI(ds);
        log.debug("inserting certificate requests");
       CertificateRequest req1 = new CertificateRequest("host1", Arrays.asList(new CertificateRequestTagValue[] { new CertificateRequestTagValue("country", null, "US"), new CertificateRequestTagValue("state", null, "CA") }));
        CertificateRequest req2 = new CertificateRequest("host2", Arrays.asList(new CertificateRequestTagValue[] { new CertificateRequestTagValue("country", null, "US"), new CertificateRequestTagValue("state", null, "CA") }));
         log.debug("searching certificate requests");
        CertificateRequestDAO certificateRequestDao = dbi.open(CertificateRequestDAO.class);
        long id1 = certificateRequestDao.insert(new UUID(), "host1"); 
        long id2 = certificateRequestDao.insert(new UUID(), "host2"); 
        report(new CertificateRequest[] { certificateRequestDao.findById(id1), certificateRequestDao.findById(id2) });
        certificateRequestDao.close();
        log.debug("searching certificate request tag values");
        CertificateRequestTagValueDAO certificateRequestTagValueDao = dbi.open(CertificateRequestTagValueDAO.class);
        List<CertificateRequestTagValue> tvs1 = certificateRequestTagValueDao.findByCertificateRequestIdWithValues(5);
        report(tvs1);
        certificateRequestTagValueDao.close();
        
    }
        */
}
