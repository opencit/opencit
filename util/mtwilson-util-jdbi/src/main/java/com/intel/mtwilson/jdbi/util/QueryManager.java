/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jdbi.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Facilitates obtaining a database vendor-specific 
 * @author jbuhacoff
 */
public class QueryManager {
    
        private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(QueryManager.class);

//        private String queryResourceFilename;
        private String driverName;
        private Properties sql;
        public QueryManager(String sqlResourcePath, String driverName) throws IOException {
            try(InputStream in = getClass().getResourceAsStream(sqlResourcePath)) { // for example  "/tag-jdbi.properties");
            sql = new Properties();
            sql.load(in);
            this.driverName = driverName;
            }
        }
        public String getQuery(String queryName) {
            String query = sql.getProperty(String.format("%s.%s", queryName, driverName));
            log.debug("query.driver = {}", query);
            if( query == null ) {
                query = sql.getProperty(queryName);
                log.debug("query = {}", query);
            }
            return query; // may be null
        }
    }
