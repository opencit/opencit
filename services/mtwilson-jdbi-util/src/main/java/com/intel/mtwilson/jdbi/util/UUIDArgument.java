/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jdbi.util;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.Argument;
import org.skife.jdbi.v2.tweak.ArgumentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class allows UUID instances to be passed directly to DAO queries as
 * parameters. It automatically converts the UUID to either a 16-byte binary
 * field or a 36 byte text field (32 bytes hex and 4 hyphens)
 *
 * References: https://groups.google.com/forum/#!topic/jdbi/VxEmvWwshso
 * http://jdbi.org/sql_object_api_argument_binding/
 * http://skife.org/jdbi/java/library/sql/2011/03/16/jdbi-sql-objects.html
 * http://stackoverflow.com/questions/12022452/does-jdbi-accept-uuid-parameters
 * https://groups.google.com/forum/?fromgroups=#!searchin/jdbi/argument$20factory/jdbi/ooFw_s183jM/WLwNBJuemYEJ
 * https://groups.google.com/forum/#!topic/jdbi/YvVP1bwqYcg
 *
 * @author jbuhacoff
 */
public class UUIDArgument implements ArgumentFactory<UUID> {

    private Logger log = LoggerFactory.getLogger(getClass());

    /*
    private String driverName;

    public UUIDArgument() {
        try {
            driverName = My.configuration().getDatabaseProtocol();
        } catch (Exception e) {
            log.error("Cannot read configured database protocol: {}", e.getMessage());
            driverName = null;
        }
    }
    */

    @Override
    public boolean accepts(Class<?> type, Object value, StatementContext ctx) {
        return value != null && UUID.class.isAssignableFrom(value.getClass());
    }

    @Override
    public Argument build(Class<?> type, final UUID value, StatementContext ctx) {
        return new Argument() {
            @Override
            public void apply(int position, PreparedStatement statement, StatementContext ctx) throws SQLException {
                statement.setString(position, value.toString());
                /*
                try {
//                    log.debug("configured driver name {}", driverName);
                    if( ctx.getAttribute("driver") != null ) {
                        driverName = (String)ctx.getAttribute("driver");
//                        log.debug("statement context driver name {}", driverName);
                    }
                    int parameterType = statement.getParameterMetaData().getParameterType(position);
                    int precision = statement.getParameterMetaData().getPrecision(position);
                    if ((parameterType == java.sql.Types.BINARY || parameterType == java.sql.Types.VARBINARY) ) {  // && precision >= 16 
                        statement.setBytes(position, value.toByteArray().getBytes()); // mysql  binary(16) or postgresql bytea
                        return;
                    }
                    if ((parameterType == java.sql.Types.CHAR || parameterType == java.sql.Types.VARCHAR) && precision >= 36) {
                        statement.setString(position, value.toString()); // any database  char(36) which is hex UUID format with hyphens
                        return;
                    }
                    if ((parameterType == java.sql.Types.CHAR || parameterType == java.sql.Types.VARCHAR) && precision == 32) {
                        // hex format without hyphens
                        statement.setString(position, value.toHexString()); // any database char(32) which is hex  UUID format without hyphens 
                        return;
                    }
                    if ("postgresql".equalsIgnoreCase(driverName)) {
//                        log.debug("parameter type name: {}", statement.getParameterMetaData().getParameterTypeName(position)); // output is paramter type name: uuid
                        if ("uuid".equalsIgnoreCase(statement.getParameterMetaData().getParameterTypeName(position))) {
                            statement.setObject(position, value.uuidValue()); // postgresql uuid
                            return;
                        }
                    }
//////                log.debug("sql object type: {}", ctx.getSqlObjectType().getName()); // output: com.intel.dcsg.cpg.atag.dao.TagDAO, or null if using the Handle API instead of the SqlObject)
//                    log.debug("parameter type is binary: {}", parameterType == java.sql.Types.BINARY || parameterType == java.sql.Types.VARBINARY);
//                    log.debug("parameter type is text: {}", parameterType == java.sql.Types.CHAR || parameterType == java.sql.Types.VARCHAR);
//                    log.debug("parameter size: {}", statement.getParameterMetaData().getPrecision(position)); // for example  16  when the type is CHAR(16) FOR BIT DATA
                    statement.setString(position, value.toString()); // any database  UUID format with hyphens is default
                    return;
                } catch (SQLException e) {
                    // probably java.sql.SQLException: Parameter metadata not available for the given statement
                    log.debug("Error while auto-detecting UUID type: {}", e.getMessage());
                }
                // since we cannot use getParameterMetaData(), guess using only the database driver:  for postgresql we'll use the uuid object, for mysql we'll assume binary(16), for anything else we'll guess char(36)
                if ("postgresql".equalsIgnoreCase(driverName)) {
                    statement.setObject(position, value.uuidValue()); // postgresql uuid
                } else if ("mysql".equalsIgnoreCase(driverName)) {
                    statement.setBytes(position, value.toByteArray().getBytes()); // mysql  binary(16) or postgresql bytea
                } else {
                    statement.setString(position, value.toString()); // any database  char(36) which is hex UUID format with hyphens
                }
                */
            }

            /**
             * Useful for seeing the actual uuid in error messages, for example
             * in SQL exceptions, even if the exception is not related to the
             * UUID , like if some other parameter had an error, but UUID was in
             * the statement so it's value is shown.
             */
            @Override
            public String toString() {
                return value.toString();
            }
        };
    }
}
