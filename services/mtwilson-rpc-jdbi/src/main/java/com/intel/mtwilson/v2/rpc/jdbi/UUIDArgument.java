/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.v2.rpc.jdbi;

import com.intel.dcsg.cpg.io.UUID;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.Argument;
import org.skife.jdbi.v2.tweak.ArgumentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * XXX TODO:  this class should be moved to cpg-io under a jdbi package (where jdbi has a provided/optional scope) or to 
 * a new cpg-jdbi module if it looks like jdbi is going to be a mainstay of the quick prototypes.
 * 
 * This class allows UUID instances to be passed directly to DAO queries as parameters.
 * It automatically converts the UUID to either a 16-byte binary field or a 36 byte text field (32 bytes hex and 4 hyphens)
 * 
 * References:
 * https://groups.google.com/forum/#!topic/jdbi/VxEmvWwshso
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

    @Override
    public boolean accepts(Class<?> type, Object value, StatementContext ctx) {
        return value != null && value instanceof UUID;  //type.isAssignableFrom(UUID.class);
    }

    @Override
    public Argument build(Class<?> type, final UUID value, StatementContext ctx) {
        return new Argument() {

            @Override
            public void apply(int position, PreparedStatement statement, StatementContext ctx) throws SQLException {
//                log.debug("sql object type: {}", ctx.getSqlObjectType().getName()); // output: com.intel.dcsg.cpg.atag.dao.TagDAO
//                log.debug("parameter type name: {}", statement.getParameterMetaData().getParameterTypeName(position)); // output: CHAR () FOR BIT DATA  (notice the size is not there, it's a generic type name... size is obtained with getPrecision below)
                int parameterType = statement.getParameterMetaData().getParameterType(position);
//                log.debug("parameter type is binary: {}", parameterType == java.sql.Types.BINARY || parameterType == java.sql.Types.VARBINARY);  
//                log.debug("parameter type is text: {}", parameterType == java.sql.Types.CHAR || parameterType == java.sql.Types.VARCHAR);  
//                log.debug("parameter size: {}", statement.getParameterMetaData().getPrecision(position)); // for example  16  when the type is CHAR(16) FOR BIT DATA
                int precision = statement.getParameterMetaData().getPrecision(position);
                if( (parameterType == java.sql.Types.BINARY || parameterType == java.sql.Types.VARBINARY) && precision >= 16 ) {
                    statement.setBytes(position, value.toByteArray().getBytes());
                }
                else if( (parameterType == java.sql.Types.CHAR || parameterType == java.sql.Types.VARCHAR) && precision >= 36 ) {
                    statement.setString(position, value.toString()); // UUID format with hyphens is 36 characters                    
                }
                else if( (parameterType == java.sql.Types.CHAR || parameterType == java.sql.Types.VARCHAR) && precision == 32 ) {
                    // hex format without hyphens
                    statement.setString(position, value.toHexString()); // UUID format without hyphens is 32 hex characters
                }
                else { // parameter type is char or varchar... or else we'll probably get an error!
                    statement.setString(position, value.toString()); // UUID format with hyphens is default
                }
            }
            
            /**
             * Useful for seeing the actual uuid in error messages, for example in SQL exceptions,  even if the exception is not related to the UUID  , like if some other parameter had an error, but UUID was in the statement so it's value is shown. 
             */
            @Override
            public String toString() {
                return value.toString();
            }
        };
    }
    
}
