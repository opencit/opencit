package com.intel.mtwilson.util.dbcp;

/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
import com.intel.dcsg.cpg.util.jdbc.PoolingDataSource;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.performance.AlarmClock;
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
01:13:54.302 [main] DEBUG c.i.m.util.dbcp.AbstractObjectPool - Creating new object for pool
01:13:54.678 [main] DEBUG c.i.m.util.dbcp.PooledConnection - [7530484849248823296] constructor wrapping jdbc:postgresql://10.1.71.56:5432/mw_as, UserName=root, PostgreSQL Native Driver
01:13:54.679 [main] DEBUG c.i.m.util.dbcp.AbstractObjectPool - Borrowing object from pool: PooledConnection[7530484849248823296] wrapping jdbc:postgresql://10.1.71.56:5432/mw_as, UserName=root, PostgreSQL Native Driver / 216787636
01:13:54.679 [main] DEBUG c.i.m.util.dbcp.PooledConnection - [7530484849248823296] createStatement
01:13:54.704 [main] DEBUG c.i.m.u.dbcp.HybridDbcpLoggingTest - got result row default
01:13:54.705 [main] DEBUG c.i.m.u.dbcp.HybridDbcpLoggingTest - got result row other
01:13:54.705 [main] DEBUG c.i.m.util.dbcp.PooledConnection - [7530484849248823296] close
01:13:54.705 [main] DEBUG c.i.m.util.dbcp.AbstractObjectPool - Returning object to pool: PooledConnection[7530484849248823296] wrapping jdbc:postgresql://10.1.71.56:5432/mw_as, UserName=root, PostgreSQL Native Driver / 216787636
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
        for (int i = 0; i < 100; i++) {
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
