/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.feature.dao;

import com.intel.dcsg.cpg.jpa.PersistenceManager;
import com.intel.mtwilson.My;
import com.intel.mtwilson.MyPersistenceManager;
import com.intel.mtwilson.feature.dao.jdbi.FeaturePermissionDAO;
import java.io.IOException;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FeatureJdbi {

    private static Logger log = LoggerFactory.getLogger(FeatureJdbi.class);
    public static DataSource ds = null;
    
    synchronized public static void createDataSource() throws IOException {
        if( ds == null ) {
            ds = PersistenceManager.createDataSource(MyPersistenceManager.getASDataJpaProperties(My.configuration()));
       }
    }

    public static DataSource getDataSource() throws SQLException {        
        if (ds == null) {
            try {
                createDataSource();
            }
            catch(IOException e) {
                throw new SQLException(e);
            }
        }
        return ds;
    }

    public static FeaturePermissionDAO featurePermissionDao() throws SQLException {
        DBI dbi = new DBI(getDataSource());
        return dbi.open(FeaturePermissionDAO.class);
    }

}
