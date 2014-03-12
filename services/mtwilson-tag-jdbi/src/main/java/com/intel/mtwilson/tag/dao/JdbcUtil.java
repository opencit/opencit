/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tag.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class JdbcUtil {

    private static Logger log = LoggerFactory.getLogger(JdbcUtil.class);

    /**
     * Does NOT close the result set.
     *
     * @param rs
     * @throws SQLException
     */
    public static void describeResultSet(ResultSet rs) throws SQLException {
        int columns = rs.getMetaData().getColumnCount();
        log.debug("Result set has {} columns", columns);
        for(int i=1; i<=columns; i++) {
            log.debug(String.format("Column: %s  Data type: %s", rs.getMetaData().getColumnName(i), rs.getMetaData().getColumnTypeName(i)));
        }
    }
}
