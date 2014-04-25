/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.dbcp.apache;

import com.intel.mtwilson.util.dbcp.apache.LoggingDataSource;
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
public class DbcpLoggingTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DbcpLoggingTest.class);
    private static LoggingDataSource ds;

    @BeforeClass
    public static void createDataSource() throws IOException {
        ds = new LoggingDataSource();
        ds.setAccessToUnderlyingConnectionAllowed(true);
        ds.setDriverClassName(My.jdbc().driver()); // something like com.mysql.jdbc.Driver  for mysql
        ds.setUrl(My.jdbc().url());
        ds.setUsername(My.configuration().getDatabaseUsername());
        ds.setPassword(My.configuration().getDatabasePassword());
        ds.setMaxActive(3);
        ds.setMaxIdle(5);
        ds.setValidationQuery("select 1");
//        ds.setValidationQueryTimeout(30); // 30 second timeout ;  // commented out because using it causes this exception: org.postgresql.jdbc4.Jdbc4Statement.setQueryTimeout(int) is not yet implemented
        ds.setTestWhileIdle(true);
        ds.setTimeBetweenEvictionRunsMillis(1000 * 60); // run once a minute to evict stale connections
    }

    /**
     * Example output:
     *
     * 14:07:10.315 [main] DEBUG c.i.m.util.dbcp.LoggingDataSource -
     * getConnection
     *
     * 14:07:10.505 [main] DEBUG c.i.m.util.dbcp.LoggingObjectPool -
     * borrowObject
     *
     * 14:07:10.621 [main] DEBUG c.i.m.u.dbcp.CustomPoolingDataSource - wrapping
     * connection
     *
     * 14:07:10.625 [main] DEBUG c.i.m.util.dbcp.LoggingConnection - constructor
     *
     * 14:07:10.625 [main] DEBUG c.i.m.util.dbcp.LoggingConnection -
     * createStatement
     *
     * 14:07:10.700 [main] DEBUG c.i.m.util.dbcp.DbcpLoggingTest - got result
     * row default
     *
     * 14:07:10.701 [main] DEBUG c.i.m.util.dbcp.DbcpLoggingTest - got result
     * row other
     *
     * 14:07:10.701 [main] DEBUG c.i.m.util.dbcp.LoggingConnection - close
     *
     * Notice that returnObject is not called...
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
