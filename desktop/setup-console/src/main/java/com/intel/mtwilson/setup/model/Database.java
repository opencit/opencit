/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.model;

import com.intel.mtwilson.datatypes.Hostname;
import com.intel.mtwilson.datatypes.InternetAddress;

/**
 *
 * @author jbuhacoff
 */
public class Database {
    public DatabaseType type;
    public InternetAddress hostname; // can also contain an ip address
    public String driver; // infer from database type ??? or infer the type from the driver name ??? that's ok for default but it must be customizable
    public Integer port; // need to set default according to type (mysql:3306, postgres:????)
    public String username;
    public String password;
}
