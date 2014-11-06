/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jdbi.util;

import com.intel.dcsg.cpg.io.UUID;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import org.skife.jdbi.v2.StatementContext;

/**
 *
 * @author jbuhacoff
 */
public class UUIDMapper {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UUIDMapper.class);
    
    public UUID getUUID(ResultSet rs, StatementContext sc, String fieldName) throws SQLException {
//        log.debug("driver is {}", sc.getAttribute("driver"));
        // find the column type for the given field name
        ResultSetMetaData meta = rs.getMetaData();
        int index = 0;
        int max = meta.getColumnCount();
        for(int i=1; i<=max; i++) {
//            log.debug("column {} is type {}  ({})", meta.getColumnName(i), meta.getColumnTypeName(i), meta.getColumnType(i)); //  
//            int type = meta.getColumnType(i);
//            if( type == java.sql.Types.BINARY) {
//                log.debug("column {} is binary", meta.getColumnName(i));
//            }
            if( fieldName.equalsIgnoreCase(meta.getColumnName(i)))  {
                index = i;
            }
        }
        if( index == 0 ) {
//            log.error("UUID column {} not found", fieldName);
            throw new SQLException("UUID column not found: "+fieldName);
        }
        int type = meta.getColumnType(index); // postgresql uuid  (1111)    mysql  BINARY (-2) .   other mysql names for getColumnTypeName:  mysql: BINARY, VARCHAR, BLOB, BIT, INT, DATE
        if( type == java.sql.Types.BINARY || type == java.sql.Types.VARBINARY ) {
            return UUID.valueOf(rs.getBytes(index));
        }
        if( type == java.sql.Types.CHAR || type == java.sql.Types.VARCHAR ) {
            return UUID.valueOf(rs.getString(index));
        }
        String typeName = meta.getColumnTypeName(index);
        if( "uuid".equalsIgnoreCase(typeName) ) { // postgresql  "uuid" column 
//            return UUID.valueOf(rs.getObject(index, java.util.UUID.class)); // java.lang.AbstractMethodError: org.apache.commons.dbcp.DelegatingResultSet.getObject(ILjava/lang/Class;)Ljava/lang/Object;
            return UUID.valueOf((java.util.UUID)rs.getObject(index));
        }
        log.debug("Found UUID column at index {} but type is not supported", index);
        return null;
    }
}
