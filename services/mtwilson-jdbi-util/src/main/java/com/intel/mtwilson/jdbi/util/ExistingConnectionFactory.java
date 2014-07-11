/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jdbi.util;

import com.intel.mtwilson.My;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import org.skife.jdbi.v2.tweak.ConnectionFactory;

/**
 *
 * @author jbuhacoff
 */
public class ExistingConnectionFactory implements ConnectionFactory {

    @Override
    public Connection openConnection() throws SQLException {
        try {
            return My.jdbc().connection();
        } catch (IOException | ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
