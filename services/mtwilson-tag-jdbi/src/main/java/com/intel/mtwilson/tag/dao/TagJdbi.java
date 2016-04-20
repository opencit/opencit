/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tag.dao;

import com.intel.dcsg.cpg.jpa.PersistenceManager;
import com.intel.mtwilson.tag.dao.jdbi.CertificateRequestDAO;
import com.intel.mtwilson.tag.dao.jdbi.CertificateDAO;
import com.intel.mtwilson.tag.dao.jdbi.TpmPasswordDAO;
import com.intel.mtwilson.tag.dao.jdbi.KvAttributeDAO;
import com.intel.mtwilson.tag.dao.jdbi.SelectionDAO;
import com.intel.mtwilson.tag.dao.jdbi.SelectionKvAttributeDAO;
import com.intel.mtwilson.tag.dao.jdbi.ConfigurationDAO;
import com.intel.mtwilson.My;
import com.intel.mtwilson.tag.dao.jdbi.FileDAO;
import com.intel.mtwilson.MyPersistenceManager;
import com.intel.mtwilson.jooq.util.JooqContainer;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.RenderNameStyle;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TagJdbi {

    private static Logger log = LoggerFactory.getLogger(TagJdbi.class);
    public static DataSource ds = null;

    synchronized public static void createDataSource() throws IOException {
        if (ds == null) {
            ds = PersistenceManager.createDataSource(MyPersistenceManager.getASDataJpaProperties(My.configuration()));
        }
    }

    public static DataSource getDataSource() throws SQLException {
        if (ds == null) {
            try {
                createDataSource();
            } catch (IOException e) {
                throw new SQLException(e);
            }
        }
        return ds;
    }

    public static KvAttributeDAO kvAttributeDao() throws SQLException {
        DBI dbi = new DBI(getDataSource());
        return dbi.open(KvAttributeDAO.class);
    }

    public static CertificateRequestDAO certificateRequestDao() throws SQLException {
        DBI dbi = new DBI(getDataSource());
        return dbi.open(CertificateRequestDAO.class);
    }

    public static CertificateDAO certificateDao() throws SQLException {
        DBI dbi = new DBI(getDataSource());
        return dbi.open(CertificateDAO.class);
    }

    public static SelectionDAO selectionDao() throws SQLException {
        DBI dbi = new DBI(getDataSource());
        return dbi.open(SelectionDAO.class);
    }

    public static SelectionKvAttributeDAO selectionKvAttributeDao() throws SQLException {
        DBI dbi = new DBI(getDataSource());
        return dbi.open(SelectionKvAttributeDAO.class);
    }

    public static ConfigurationDAO configurationDao() throws SQLException {
        DBI dbi = new DBI(getDataSource());
        return dbi.open(ConfigurationDAO.class);
    }

    public static TpmPasswordDAO tpmPasswordDao() throws SQLException {
        DBI dbi = new DBI(getDataSource());
        return dbi.open(TpmPasswordDAO.class);
    }

    public static FileDAO fileDao() throws SQLException {
        DBI dbi = new DBI(getDataSource());
        return dbi.open(FileDAO.class);
    }

    /**
     * CODE QUALITY: All usage of this method should be in the following form:
     * <pre>
     * try(JooqContainer jc = TagJdbi.jooq()) {
     * DSLContext jooq = jc.getDslContext();
     * // code
     * }
     * </pre>
     *
     * This ensures the jooq database connection is automatically released
     * at the end of the block (either closed or returned to the pool)
     * 
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static JooqContainer jooq() throws SQLException, IOException {
        // omits the schema name from generated sql ; when we connect to the database we already specify a schema so this settings avoid 
        // redundancy in the sql and allows the administrator to change the database name without breaking the application
        Settings settings = new Settings().withRenderSchema(false).withRenderNameStyle(RenderNameStyle.LOWER);
        SQLDialect dbDialect = getSqlDialect();
        // throws SQLException; Note that the DSLContext doesn't close the connection. We'll have to do that ourselves.
        Connection connection = TagJdbi.getDataSource().getConnection();
        DSLContext jooq = DSL.using(connection, dbDialect, settings);

        return new JooqContainer(jooq, connection);
    }

    public static SQLDialect getSqlDialect() throws IOException {
        String protocol = My.configuration().getDatabaseProtocol();
        if ("mysql".equalsIgnoreCase(protocol)) {
            return SQLDialect.MYSQL;
        }
        if ("postgresql".equalsIgnoreCase(protocol) || "postgres".equalsIgnoreCase(protocol)) {
            return SQLDialect.POSTGRES;
        }
        if ("derby".equalsIgnoreCase(protocol)) {
            return SQLDialect.DERBY;
        }
        return SQLDialect.valueOf(protocol);
    }
}
