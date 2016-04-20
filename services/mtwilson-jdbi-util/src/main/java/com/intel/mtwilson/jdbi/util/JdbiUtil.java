/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jdbi.util;

import org.skife.jdbi.v2.DBI;

/**
 *
 * @author jbuhacoff
 */
public class JdbiUtil {
    private static DBI dbi = null;
    
    public static DBI getDBI() {
        if (dbi == null) {
            dbi = new DBI(new ExistingConnectionFactory());
        }
        return dbi;
    }
}
