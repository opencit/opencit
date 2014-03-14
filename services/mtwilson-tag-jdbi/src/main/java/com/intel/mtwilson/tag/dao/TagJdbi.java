/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tag.dao;

import com.intel.mtwilson.tag.dao.jdbi.CertificateRequestDAO;
import com.intel.mtwilson.tag.dao.jdbi.CertificateDAO;
import com.intel.mtwilson.tag.dao.jdbi.TpmPasswordDAO;
import com.intel.mtwilson.tag.dao.jdbi.KvAttributeDAO;
import com.intel.mtwilson.tag.dao.jdbi.SelectionDAO;
import com.intel.mtwilson.tag.dao.jdbi.SelectionKvAttributeDAO;
import com.intel.mtwilson.tag.dao.jdbi.ConfigurationDAO;
import com.intel.mtwilson.My;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.apache.commons.dbcp.BasicDataSource;
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
    public static Connection conn = null;
    public static DataSource ds = null;

    public static DataSource getDataSource() {        
        try {
            if (ds == null) {
                String driver = My.jdbc().driver();
                String dbUrl = My.jdbc().url();                
                BasicDataSource dataSource = new BasicDataSource();
                dataSource.setDriverClassName(driver); // or com.mysql.jdbc.Driver  for mysql
                dataSource.setUrl(dbUrl);
                dataSource.setUsername(My.configuration().getDatabaseUsername());
                dataSource.setPassword(My.configuration().getDatabasePassword());
                ds = dataSource;
            }
        } catch (Exception ex) {
            log.error("Error connecting to the database. {}", ex.getMessage());
        }
        return ds;
    }

    public static Connection getConnection() {
        try {
            if (conn == null) {
                conn = getDataSource().getConnection();
            }
            return conn;
        } catch (Exception ex) {
            log.error("Error connection to the database. {}", ex.getMessage());
        }
        return null;
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

    public static DSLContext jooq() throws SQLException, IOException {
        // omits the schema name from generated sql ; when we connect to the database we already specify a schema so this settings avoid 
        // redundancy in the sql and allows the administrator to change the database name without breaking the application
        Settings settings = new Settings().withRenderSchema(false).withRenderNameStyle(RenderNameStyle.LOWER);
        SQLDialect dbDialect = (My.jdbc().driver().contains("mysql")) ? SQLDialect.MYSQL : SQLDialect.POSTGRES;
        // throws SQLException; Note that the DSLContext doesn't close the connection. We'll have to do that ourselves.
        DSLContext jooq = DSL.using(TagJdbi.getConnection(), dbDialect, settings);
        return jooq;
    }
}
