package com.intel.mtwilson.util.dbcp;

/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
import com.intel.dcsg.cpg.util.jdbc.PoolingDataSource;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.performance.AlarmClock;
import com.intel.dcsg.cpg.util.jdbc.retry.RetryingConnection;
import com.intel.mtwilson.My;
import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.BeforeClass;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

/**
 *
 * @author jbuhacoff
 */
public class HybridDbcpLoggingTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HybridDbcpLoggingTest.class);
    private static DataSource ds;

    private static void configureBasicDataSource(BasicDataSource bds) throws IOException {
        bds.setAccessToUnderlyingConnectionAllowed(true);
        bds.setDriverClassName(My.jdbc().driver()); // something like com.mysql.jdbc.Driver  for mysql
        bds.setUrl(My.jdbc().url());
        bds.setUsername(My.configuration().getDatabaseUsername());
        bds.setPassword(My.configuration().getDatabasePassword());
        bds.setMaxActive(3);
        bds.setMaxIdle(5);
        bds.setValidationQuery("select 1");
//        ds.setValidationQueryTimeout(30); // 30 second timeout ;  // commented out because using it causes this exception: org.postgresql.jdbc4.Jdbc4Statement.setQueryTimeout(int) is not yet implemented
        bds.setTestWhileIdle(true);
        bds.setTimeBetweenEvictionRunsMillis(1000 * 60); // run once a minute to evict stale connections
    }

    @BeforeClass
    public static void createHybridPoolingDataSourceWithManagedPool3() throws IOException {
        BasicDataSource basicDataSource = new BasicDataSource();
        configureBasicDataSource(basicDataSource);
        com.intel.dcsg.cpg.util.jdbc.ValidatingConnectionPool connectionPool = new com.intel.dcsg.cpg.util.jdbc.ValidatingConnectionPool();
        connectionPool.setDataSource(basicDataSource);
        connectionPool.setValidateOnBorrow(true);
        connectionPool.setValidateOnReturn(true);
        connectionPool.setValidationQuery("SELECT 1");
        ds = new PoolingDataSource(connectionPool);
    }

    /**
     * Example output:
     * <pre>
     * 01:13:54.302 [main] DEBUG c.i.m.util.dbcp.AbstractObjectPool - Creating new object for pool
     * 01:13:54.678 [main] DEBUG c.i.m.util.dbcp.PooledConnection - [7530484849248823296] constructor wrapping jdbc:postgresql://10.1.71.56:5432/mw_as, UserName=root, PostgreSQL Native Driver
     * 01:13:54.679 [main] DEBUG c.i.m.util.dbcp.AbstractObjectPool - Borrowing object from pool: PooledConnection[7530484849248823296] wrapping jdbc:postgresql://10.1.71.56:5432/mw_as, UserName=root, PostgreSQL Native Driver / 216787636
     * 01:13:54.679 [main] DEBUG c.i.m.util.dbcp.PooledConnection - [7530484849248823296] createStatement
     * 01:13:54.704 [main] DEBUG c.i.m.u.dbcp.HybridDbcpLoggingTest - got result row default
     * 01:13:54.705 [main] DEBUG c.i.m.u.dbcp.HybridDbcpLoggingTest - got result row other
     * 01:13:54.705 [main] DEBUG c.i.m.util.dbcp.PooledConnection - [7530484849248823296] close
     * 01:13:54.705 [main] DEBUG c.i.m.util.dbcp.AbstractObjectPool - Returning object to pool: PooledConnection[7530484849248823296] wrapping jdbc:postgresql://10.1.71.56:5432/mw_as, UserName=root, PostgreSQL Native Driver / 216787636
     * </pre>
     *
     * @throws SQLException
     */
    @Test
    public void testLogConnectionOpenClose() throws SQLException {
        try (Connection c = ds.getConnection()) {
            try (Statement s = c.createStatement()) {
                try (ResultSet rs = s.executeQuery("select id, name, description from mw_tag_selection")) {
                    while (rs.next()) {
                        log.debug("got result row {}", rs.getString("name"));
                    }
                }
            }
        }
    }

    @Test
    public void testLogConnectionOpenClose2() throws SQLException {
        try (Connection c1 = ds.getConnection();
                Connection c2 = ds.getConnection()) {
            try (Statement s1 = c1.createStatement()) {
                try (ResultSet rs1 = s1.executeQuery("select id, name, description from mw_tag_selection")) {
                    while (rs1.next()) {
                        log.debug("got result row {}", rs1.getString("name"));
                    }
                }
            }
            try (Statement s2 = c2.createStatement()) {
                try (ResultSet rs2 = s2.executeQuery("select id, name, description from mw_tag_selection")) {
                    while (rs2.next()) {
                        log.debug("got result row {}", rs2.getString("name"));
                    }
                }
            }
        }
    }

    /**
     *
     * Settings:
     * <pre>
     * ds.setMaxActive(20);
     * ds.setMaxIdle(5);
     * ds.setValidationQuery("select 1");
     * ds.setTestWhileIdle(true);
     * ds.setTimeBetweenEvictionRunsMillis(1000*60);
     * </pre>
     *
     * Result: A new connection is created each time so at the end of the test
     * there are 10 connections, and they are only closed when the jvm exits.
     *
     *
     *
     * @throws Exception
     */
    @Test
    public void testMultipleConnectionOpenClose() throws Exception {
        AlarmClock alarm = new AlarmClock(5, TimeUnit.SECONDS);
        for (int i = 0; i < 10; i++) {
            log.debug("testing connection {}", (i + 1));
            testLogConnectionOpenClose();
            //alarm.sleep();
        }
    }

    @Test
    public void testMultipleConnectionOpenClose2() throws Exception {
        AlarmClock alarm = new AlarmClock(5, TimeUnit.SECONDS);
        for (int i = 0; i < 10; i++) {
            log.debug("testing connection {}", (i + 1));
            testLogConnectionOpenClose2();
            alarm.sleep();
        }
    }

    /**
     * This test is interactive: 1) run the test; it will open a connection and
     * wait X seconds 2) restart the database server to drop the connection 3)
     * after X seconds the test will try another query
     *
     * Example output:
     * <pre>
     * 12:14:22.828 [main] DEBUG c.i.m.u.dbcp.HybridDbcpLoggingTest - opening connection
     * 12:14:22.829 [main] DEBUG c.i.d.c.o.AbstractObjectPool - Creating new object for pool, currently borrowed 0
     * 12:14:22.829 [main] DEBUG c.i.d.cpg.util.jdbc.ConnectionPool - creating new object for pool, now created 1 trashed 0 total 1
     * 12:14:23.831 [main] DEBUG c.i.d.cpg.util.jdbc.PooledConnection - [3783430681094712320] constructor wrapping jdbc:mysql://10.1.71.56:3306/mw_as, UserName=root@10.254.189.232, MySQL-AB JDBC Driver
     * 12:14:23.855 [main] DEBUG c.i.d.c.o.AbstractObjectPool - Borrowing object from pool: PooledConnection[3783430681094712320] wrapping jdbc:mysql://10.1.71.56:3306/mw_as, UserName=root@10.254.189.232, MySQL-AB JDBC Driver / 562503067
     * 12:14:23.877 [main] DEBUG c.i.d.c.u.j.ValidatingConnectionPool - Validating connection PooledConnection[3783430681094712320] wrapping jdbc:mysql://10.1.71.56:3306/mw_as, UserName=root@10.254.189.232, MySQL-AB JDBC Driver
     * 12:14:23.877 [main] DEBUG c.i.d.cpg.util.jdbc.PooledConnection - [3783430681094712320] createStatement
     * 12:14:23.900 [main] DEBUG c.i.d.c.u.j.ValidatingConnectionPool - Validation result
     * 12:14:23.900 [main] DEBUG c.i.m.u.dbcp.HybridDbcpLoggingTest - trying first statement
     * 12:14:23.900 [main] DEBUG c.i.d.cpg.util.jdbc.PooledConnection - [3783430681094712320] createStatement
     * 12:14:23.927 [main] DEBUG c.i.m.u.dbcp.HybridDbcpLoggingTest - got result row default
     * 12:14:23.927 [main] DEBUG c.i.m.u.dbcp.HybridDbcpLoggingTest - got result row dlafkj
     * 12:14:23.927 [main] DEBUG c.i.m.u.dbcp.HybridDbcpLoggingTest - pausing before next statement
     * 12:14:38.934 [main] DEBUG c.i.m.u.dbcp.HybridDbcpLoggingTest - trying second statement
     * 12:14:38.934 [main] DEBUG c.i.d.cpg.util.jdbc.PooledConnection - [3783430681094712320] createStatement
     * 12:14:38.937 [main] DEBUG c.i.d.cpg.util.jdbc.PooledConnection - [3783430681094712320] close
     * 12:14:38.938 [main] DEBUG c.i.d.c.u.j.ValidatingConnectionPool - Validating connection PooledConnection[3783430681094712320] wrapping connection is closed
     * 12:14:38.938 [main] DEBUG c.i.d.c.u.j.ValidatingConnectionPool - Revoking invalid object on return
     * 12:14:38.938 [main] DEBUG c.i.d.c.o.AbstractObjectPool - Marking revoked object to remove from pool: PooledConnection[3783430681094712320] wrapping connection is closed / 562503067
     * 12:14:38.938 [main] DEBUG c.i.d.c.o.AbstractObjectPool - Removed revoked object from pool: PooledConnection[3783430681094712320] wrapping connection is closed / 562503067
     * 12:14:38.938 [main] DEBUG c.i.d.cpg.util.jdbc.ConnectionPool - unwrapping connection to close
     *
     * </pre>
     *
     * And the exception thrown for the second statement is:
     * <pre>
     * com.mysql.jdbc.exceptions.jdbc4.CommunicationsException: Communications link failure
     *
     * The last packet successfully received from the server was 15,009 milliseconds ago.  The last packet sent successfully to the server was 1 milliseconds ago.
     * at sun.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
     * at sun.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:57)
     * at sun.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:45)
     * at java.lang.reflect.Constructor.newInstance(Constructor.java:526)
     * at com.mysql.jdbc.Util.handleNewInstance(Util.java:411)
     * at com.mysql.jdbc.SQLError.createCommunicationsException(SQLError.java:1117)
     * at com.mysql.jdbc.MysqlIO.reuseAndReadPacket(MysqlIO.java:3589)
     * at com.mysql.jdbc.MysqlIO.reuseAndReadPacket(MysqlIO.java:3478)
     * at com.mysql.jdbc.MysqlIO.checkErrorPacket(MysqlIO.java:4019)
     * at com.mysql.jdbc.MysqlIO.sendCommand(MysqlIO.java:2490)
     * at com.mysql.jdbc.MysqlIO.sqlQueryDirect(MysqlIO.java:2651)
     * at com.mysql.jdbc.ConnectionImpl.execSQL(ConnectionImpl.java:2728)
     * at com.mysql.jdbc.ConnectionImpl.execSQL(ConnectionImpl.java:2678)
     * at com.mysql.jdbc.StatementImpl.executeQuery(StatementImpl.java:1612)
     * at org.apache.commons.dbcp.DelegatingStatement.executeQuery(DelegatingStatement.java:208)
     * at org.apache.commons.dbcp.DelegatingStatement.executeQuery(DelegatingStatement.java:208)
     * at com.intel.mtwilson.util.dbcp.HybridDbcpLoggingTest.testLogConnectionOpenWaitRetry(HybridDbcpLoggingTest.java:175)
     * at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
     * at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
     * at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
     * at java.lang.reflect.Method.invoke(Method.java:606)
     * at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:47)
     * at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)
     * at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:44)
     * at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)
     * at org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:271)
     * at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:70)
     * at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:50)
     * at org.junit.runners.ParentRunner$3.run(ParentRunner.java:238)
     * at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:63)
     * at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:236)
     * at org.junit.runners.ParentRunner.access$000(ParentRunner.java:53)
     * at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:229)
     * at org.junit.internal.runners.statements.RunBefores.evaluate(RunBefores.java:26)
     * at org.junit.runners.ParentRunner.run(ParentRunner.java:309)
     * at org.apache.maven.surefire.junit4.JUnit4Provider.execute(JUnit4Provider.java:242)
     * at org.apache.maven.surefire.junit4.JUnit4Provider.executeTestSet(JUnit4Provider.java:137)
     * at org.apache.maven.surefire.junit4.JUnit4Provider.invoke(JUnit4Provider.java:112)
     * at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
     * at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
     * at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
     * at java.lang.reflect.Method.invoke(Method.java:606)
     * at org.apache.maven.surefire.util.ReflectionUtils.invokeMethodWithArray(ReflectionUtils.java:189)
     * at org.apache.maven.surefire.booter.ProviderFactory$ProviderProxy.invoke(ProviderFactory.java:165)
     * at org.apache.maven.surefire.booter.ProviderFactory.invokeProvider(ProviderFactory.java:85)
     * at org.apache.maven.surefire.booter.ForkedBooter.runSuitesInProcess(ForkedBooter.java:115)
     * at org.apache.maven.surefire.booter.ForkedBooter.main(ForkedBooter.java:75)
     * Suppressed: java.lang.IllegalStateException: Cannot close connection
     * at com.intel.dcsg.cpg.util.jdbc.ConnectionPool.trashObject(ConnectionPool.java:62)
     * at com.intel.dcsg.cpg.util.jdbc.ConnectionPool.trashObject(ConnectionPool.java:16)
     * at com.intel.dcsg.cpg.objectpool.AbstractObjectPool.returnObject(AbstractObjectPool.java:63)
     * at com.intel.dcsg.cpg.util.jdbc.ValidatingConnectionPool.returnObject(ValidatingConnectionPool.java:71)
     * at com.intel.dcsg.cpg.util.jdbc.ValidatingConnectionPool.returnObject(ValidatingConnectionPool.java:18)
     * at com.intel.dcsg.cpg.util.jdbc.PooledConnection.close(PooledConnection.java:34)
     * at com.intel.mtwilson.util.dbcp.HybridDbcpLoggingTest.testLogConnectionOpenWaitRetry(HybridDbcpLoggingTest.java:181)
     * ... 30 more
     * Caused by: java.sql.SQLException: Already closed.
     * at org.apache.commons.dbcp.PoolableConnection.close(PoolableConnection.java:114)
     * at org.apache.commons.dbcp.PoolingDataSource$PoolGuardConnectionWrapper.close(PoolingDataSource.java:191)
     * at com.intel.dcsg.cpg.util.jdbc.ConnectionPool.trashObject(ConnectionPool.java:51)
     * ... 36 more
     * Caused by: java.io.EOFException: Can not read response from server. Expected to read 4 bytes, read 0 bytes before connection was unexpectedly lost.
     * at com.mysql.jdbc.MysqlIO.readFully(MysqlIO.java:3039)
     * at com.mysql.jdbc.MysqlIO.reuseAndReadPacket(MysqlIO.java:3489)
     * ... 40 more
     *
     * </pre>
     *
     * @throws SQLException
     */
    @Test
    public void testLogConnectionOpenWaitRetry() throws SQLException {
        log.debug("opening connection");
        //try (Connection c = ds.getConnection()) {
        try(RetryingConnection c = new RetryingConnection(ds.getConnection(), ds)) {
            log.debug("trying first statement");
            try (Statement s = c.createStatement()) {
                try (ResultSet rs = s.executeQuery("select id, name, description from mw_tag_selection")) {
                    while (rs.next()) {
                        log.debug("got result row {}", rs.getString("name"));
                    }
                }
            }
            log.debug("pausing before next statement");
            AlarmClock wait = new AlarmClock(15, TimeUnit.SECONDS);
            wait.sleep();
            log.debug("trying second statement");
            try (Statement s = c.createStatement()) {
                try (ResultSet rs = s.executeQuery("select id, name, description from mw_tag_selection")) {
                    while (rs.next()) {
                        log.debug("got result row {}", rs.getString("name"));
                    }
                }
            }
        }
        log.debug("done");
    }

    /**
     * Example output:
     *
     * 14:11:14.415 [main] DEBUG c.i.m.util.dbcp.LoggingDataSource -
     * getConnection
     *
     * 14:11:14.594 [main] DEBUG c.i.m.util.dbcp.LoggingObjectPool -
     * borrowObject
     *
     * 14:11:14.701 [main] DEBUG c.i.m.u.dbcp.CustomPoolingDataSource - wrapping
     * connection
     *
     * 14:11:14.702 [main] DEBUG c.i.m.util.dbcp.LoggingConnection - constructor
     *
     * 14:11:14.910 [main] DEBUG c.i.m.util.dbcp.LoggingConnection -
     * prepareStatement select id, name, description from mw_tag_selection
     *
     * 14:11:14.964 [main] DEBUG c.i.m.util.dbcp.DbcpLoggingTest - got result
     * row com.intel.mtwilson.util.dbcp.DbcpLoggingTest$Selection@3918e589
     *
     * 14:11:14.964 [main] DEBUG c.i.m.util.dbcp.DbcpLoggingTest - got result
     * row com.intel.mtwilson.util.dbcp.DbcpLoggingTest$Selection@1185a2a8
     *
     * 14:11:14.964 [main] DEBUG c.i.m.util.dbcp.LoggingConnection - close
     *
     * @throws SQLException
     */
    @Test
    public void testLogDbiOpenClose() throws SQLException {
        DBI dbi = new DBI(ds);
        try (SelectionDAO dao = dbi.open(SelectionDAO.class)) {
            List<Selection> selections = dao.findAll();
            for (Selection selection : selections) {
                log.debug("got result row {}", selection);
            }
        }
    }

    public static class Selection {

        private String id;
        private String name;
        private String description;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    @RegisterMapper(SelectionResultMapper.class)
    public static interface SelectionDAO extends Closeable {

        @SqlQuery("select id, name, description from mw_tag_selection")
        List<Selection> findAll();

        @SqlQuery("select id, name, description from mw_tag_selection where id=:id")
        Selection findById(@Bind("id") UUID id);

        @SqlQuery("select id, name, description from mw_tag_selection where name=:name")
        Selection findByName(@Bind("name") String name);

        @Override
        void close();
    }

    public static class SelectionResultMapper implements ResultSetMapper<Selection> {

        private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SelectionResultMapper.class);

        @Override
        public Selection map(int i, ResultSet rs, StatementContext sc) throws SQLException {
            Selection selection = new Selection();
            selection.setId(rs.getString("id"));
            selection.setName(rs.getString("name"));
            selection.setDescription(rs.getString("description"));
            return selection;
        }
    }
}
