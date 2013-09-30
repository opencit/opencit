/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.pool;

import com.intel.dcsg.cpg.performance.Task;
import com.intel.dcsg.cpg.performance.report.PerformanceInfo;
import static com.intel.dcsg.cpg.performance.report.PerformanceUtil.measureMultipleConcurrentTasks;
import com.intel.mtwilson.My;
import static java.lang.String.format;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;
import javax.sql.DataSource;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DataSourceConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.dbcp.managed.BasicManagedDataSource;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.junit.Test;
import org.objectweb.jotm.Current;
//import test.derby.Derby;

/**
 *
 * @author jbuhacoff
 */
public class ConnectionPoolTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConnectionPoolTest.class);

    private static DataSource createDataSource(Properties jpaProperties) {
        BasicManagedDataSource ds = new BasicManagedDataSource();
        Current tm = new Current();
        ds.setAccessToUnderlyingConnectionAllowed(true);
        ds.setConnectionInitSqls(Collections.EMPTY_LIST);
        ds.setDefaultAutoCommit(true);
//        ds.setDefaultCatalog("mw_as"); // not needed when using the url...
        ds.setDefaultReadOnly(false);
//        ds.setDefaultTransactionIsolation(0);
        ds.setDriverClassLoader(ClassLoader.getSystemClassLoader());
        ds.setDriverClassName(jpaProperties.getProperty("javax.persistence.jdbc.driver"));
        ds.setInitialSize(10);
        ds.setLogAbandoned(true);
//        ds.setLogWriter(null); // null disables logging; TODO: see if we can get a PrintWriter from slf4j... and for some reason calls createDataSource() whic hdoesn't make sense
//        ds.setLoginTimeout(30); // in seconds ;   not supported by basicdatasource... and for some reason calls createDataSource() whic hdoesn't make sense
        ds.setMaxActive(1); // max 50 active connections to database
        ds.setMinIdle(1); // min 5 idle connections in the pool
        ds.setMaxIdle(1); // max 10 idle connections in the pool
        ds.setMaxOpenPreparedStatements(-1); // no limit
        ds.setMaxWait(-1); // wait indefinitely for a new connection from the pool
        ds.setMinEvictableIdleTimeMillis(1000*5); // (milliseconds) connection may be idle up to 5 seconds before being evicted
        ds.setNumTestsPerEvictionRun(10); // how many connections to test each time
        ds.setPassword(jpaProperties.getProperty("javax.persistence.jdbc.password"));
        ds.setPoolPreparedStatements(true);
        ds.setRemoveAbandoned(true);
        ds.setRemoveAbandonedTimeout(5); // (seconds) connection may be abandoned for up to 5 seconds  before being removed
        ds.setTestOnBorrow(true);
        ds.setTestOnReturn(false);
        ds.setTestWhileIdle(true);
        ds.setTimeBetweenEvictionRunsMillis(1000*5); // (milliseconds) check which idle connections should be evicted once 5 seconds
        ds.setUrl(jpaProperties.getProperty("javax.persistence.jdbc.url"));
        ds.setUsername(jpaProperties.getProperty("javax.persistence.jdbc.user"));
        ds.setValidationQuery("SELECT 1");
        ds.setValidationQueryTimeout(2); // (seconds) how long to wait on a result for the validation query before giving up
//        DataSourceConnectionFactory connectionFactory = new DataSourceConnectionFactory(dataSource, dbUsername, dbPassowrd);
//        PoolableConnectionFactory dbcpFactory = new PoolableConnectionFactory(connectionFactory, pool, validationQuery, validationQueryTimeoutSeconds, false, false);
//        poolingDataSource = new PoolingDataSource(pool);
        ds.setTransactionManager(tm);
        return ds;
    }
    
    /**
     * test database: mysql 
     * started with 5 processes listed in mysql's "show processlist"
     * 
     * before:
mysql> show processlist;
+-------+------+-----------------+-------+---------+------+-------+------------------+
| Id    | User | Host            | db    | Command | Time | State | Info             |
+-------+------+-----------------+-------+---------+------+-------+------------------+
| 54196 | root | localhost       | NULL  | Query   |    0 | NULL  | show processlist |
| 54197 | root | localhost:40701 | mw_as | Sleep   |   33 |       | NULL             |
| 54198 | root | localhost:40702 | mw_as | Sleep   |   33 |       | NULL             |
| 54199 | root | localhost:40703 | mw_as | Sleep   |   33 |       | NULL             |
| 54200 | root | localhost:40704 | mw_as | Sleep   |   33 |       | NULL             |
| 54201 | root | localhost:40705 | mw_as | Sleep   |   33 |       | NULL             |
+-------+------+-----------------+-------+---------+------+-------+------------------+
6 rows in set (0.00 sec)
     * 
     * during:
mysql> show processlist;
+-------+------+----------------------------------------+-------+---------+------+-------+------------------+
| Id    | User | Host                                   | db    | Command | Time | State | Info             |
+-------+------+----------------------------------------+-------+---------+------+-------+------------------+
| 54196 | root | localhost                              | NULL  | Query   |    0 | NULL  | show processlist |
| 54197 | root | localhost:40701                        | mw_as | Sleep   |   15 |       | NULL             |
| 54198 | root | localhost:40702                        | mw_as | Sleep   |   15 |       | NULL             |
| 54199 | root | localhost:40703                        | mw_as | Sleep   |   15 |       | NULL             |
| 54200 | root | localhost:40704                        | mw_as | Sleep   |   15 |       | NULL             |
| 54201 | root | localhost:40705                        | mw_as | Sleep   |   15 |       | NULL             |
| 54225 | root | jbuhacof-mobl.amr.corp.intel.com:39425 | mw_as | Sleep   |    3 |       | NULL             |
| 54226 | root | jbuhacof-mobl.amr.corp.intel.com:39426 | mw_as | Sleep   |    3 |       | NULL             |
| 54227 | root | jbuhacof-mobl.amr.corp.intel.com:39427 | mw_as | Sleep   |    2 |       | NULL             |
| 54228 | root | jbuhacof-mobl.amr.corp.intel.com:39428 | mw_as | Sleep   |    2 |       | NULL             |
| 54229 | root | jbuhacof-mobl.amr.corp.intel.com:39429 | mw_as | Sleep   |    1 |       | NULL             |
| 54230 | root | jbuhacof-mobl.amr.corp.intel.com:39430 | mw_as | Sleep   |    1 |       | NULL             |
| 54231 | root | jbuhacof-mobl.amr.corp.intel.com:39431 | mw_as | Sleep   |    1 |       | NULL             |
| 54232 | root | jbuhacof-mobl.amr.corp.intel.com:39432 | mw_as | Sleep   |    0 |       | NULL             |
| 54233 | root | jbuhacof-mobl.amr.corp.intel.com:39433 | mw_as | Sleep   |    0 |       | NULL             |
+-------+------+----------------------------------------+-------+---------+------+-------+------------------+
15 rows in set (0.00 sec)
     * 
     * after:
mysql> show processlist;
+-------+------+-----------------+-------+---------+------+-------+------------------+
| Id    | User | Host            | db    | Command | Time | State | Info             |
+-------+------+-----------------+-------+---------+------+-------+------------------+
| 54196 | root | localhost       | NULL  | Query   |    0 | NULL  | show processlist |
| 54197 | root | localhost:40701 | mw_as | Sleep   |   44 |       | NULL             |
| 54198 | root | localhost:40702 | mw_as | Sleep   |   44 |       | NULL             |
| 54199 | root | localhost:40703 | mw_as | Sleep   |   44 |       | NULL             |
| 54200 | root | localhost:40704 | mw_as | Sleep   |   44 |       | NULL             |
| 54201 | root | localhost:40705 | mw_as | Sleep   |   44 |       | NULL             |
+-------+------+-----------------+-------+---------+------+-------+------------------+
6 rows in set (0.00 sec)

     * 
     * @throws Exception 
     */
    private DataSource createBasicDataSource() throws Exception {
        Properties p = new Properties();
        p.setProperty("javax.persistence.jdbc.driver", My.configuration().getDatabaseDriver());
        p.setProperty("javax.persistence.jdbc.url", String.format("jdbc:%s://%s:%s/%s", 
                My.configuration().getDatabaseProtocol(), 
                My.configuration().getDatabaseHost(),
                My.configuration().getDatabasePort(),
                My.configuration().getDatabaseSchema()));
        p.setProperty("javax.persistence.jdbc.user", My.configuration().getDatabaseUsername());
        p.setProperty("javax.persistence.jdbc.password", My.configuration().getDatabasePassword());
        DataSource ds = createDataSource(p);
       return ds;
    }

    
    /**
     * seems to work with mysql - only 1-2 connections are opened to handle all 20 tasks
     */
    @Test
    public void testMeasureConcurrentTask() throws Exception {
        DataSource ds = createBasicDataSource();
        
        int n = 20;
        ArrayList<SimpleDatabaseTask> tasks = new ArrayList<SimpleDatabaseTask>(n);
        for(int i=0; i<n; i++) {
            tasks.add(new SimpleDatabaseTask(String.valueOf(i+1), ds));
        }
        PerformanceInfo info = measureMultipleConcurrentTasks(tasks, 5); // 5 second timeout
        printPerformanceInfo(info);
        for(int i=0; i<n; i++) {
            printTaskStatus(tasks.get(i));
        }
    }
    
    /**
     * In this test, the individual tasks do NOT call close() on the connection when done to simulate
     * bad code behavior. The test is to see if the pool will automatically close the connection after
     * it's idle too long.
     */
    @Test
    public void testMeasureConcurrentTaskWithoutClosing() throws Exception {
        DataSource ds = createBasicDataSource();
        
        int n = 20;
        ArrayList<NonclosingDatabaseTask> tasks = new ArrayList<NonclosingDatabaseTask>(n);
        for(int i=0; i<n; i++) {
            tasks.add(new NonclosingDatabaseTask(String.valueOf(i+1), ds));
        }
        PerformanceInfo info = measureMultipleConcurrentTasks(tasks, 5); // 5 second timeout
        printPerformanceInfo(info);
        for(int i=0; i<n; i++) {
            printTaskStatus(tasks.get(i));
        }
    }
    
    
    
    /*

    public static void createTables() throws SQLException  {
        DBI dbi = new DBI(getDataSource());
        TagDAO tagDao = dbi.open(TagDAO.class);
        if( !Derby.tableExists("tag") ) { tagDao.create(); }        // throws SQLException
        tagDao.close();
        
        TagValueDAO tagValueDao = dbi.open(TagValueDAO.class);
        if( !Derby.tableExists("tag_value") ) { tagValueDao.create(); }      // throws SQLException
        tagValueDao.close();
        
    }
       * 
     */
    
    private static class SimpleDatabaseTask extends Task {
        private final DataSource ds;
        public SimpleDatabaseTask(String id, DataSource ds) {
            super(id); // or can use the hashcode of the messsage as the id: String.valueOf(message.hashCode())
            this.ds = ds;
        }
        @Override
        public void execute() throws Exception {
            log.debug(format("HelloWorldTask[%s] executing query", getId()));
            Connection c = ds.getConnection();
            Statement s = c.createStatement();
            s.executeQuery("SELECT 1");
            s.close();
            c.close();
        }
        public String getMessage() { return "no result"; }
    }

    private static class NonclosingDatabaseTask extends Task {
        private final DataSource ds;
        public NonclosingDatabaseTask(String id, DataSource ds) {
            super(id); // or can use the hashcode of the messsage as the id: String.valueOf(message.hashCode())
            this.ds = ds;
        }
        @Override
        public void execute() throws Exception {
            log.debug(format("HelloWorldTask[%s] executing query", getId()));
            Connection c = ds.getConnection();
            Statement s = c.createStatement();
            s.executeQuery("SELECT 1");
            s.close();
//            c.close(); // the NonclosingDatabaseTask intentionally does not close the connection when doen to simulate bad behavior
        }
        public String getMessage() { return "no result"; }
    }
    
    private static void printTaskStatus(Task task) {
        log.debug("Task results: {}", task.getId());
        if( task.isDone() ) {
            log.debug("+ completed [{} ms]", task.getStopTime() - task.getStartTime());
        }
        else if( task.isError() ) {
            log.debug("+ error: {} [{} ms]", task.getCause().toString(), task.getStopTime() - task.getStartTime());
        }
        else {
            log.debug("+ timeout");
        }
    }

    private static void printPerformanceInfo(PerformanceInfo info) {
        log.debug("Number of executions: {}", info.getData().length);
        log.debug("Average time: {}", info.getAverage());
        log.debug("Min time: {}", info.getMin());
        log.debug("Max time: {}", info.getMax());
    }
    
}

