/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jdbi.util;

import java.util.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.Argument;
import org.skife.jdbi.v2.tweak.ArgumentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Timestamp;

/**
 * Converts java.util.Date to java.sql.Timestamp
 *
 * @author jbuhacoff
 */
public class DateArgument implements ArgumentFactory<Date> {

    private Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public boolean accepts(Class<?> type, Object value, StatementContext ctx) {
        return value != null && Date.class.isAssignableFrom(value.getClass());
    }

    @Override
    public Argument build(Class<?> type, final Date value, StatementContext ctx) {
        return new Argument() {
            @Override
            public void apply(int position, PreparedStatement statement, StatementContext ctx) throws SQLException {
                statement.setTimestamp(position, new Timestamp(value.getTime()));

            }
            @Override
            public String toString() {
                return value.toString();
            }
        };
    }
}
