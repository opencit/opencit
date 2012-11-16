/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.as.helper;

/**
 * Look for required configuration settings. 
 * Generate default settings for missing configuration, if possible.
 * Write the generated settings back to the configuration file. 
 * 
 * For database, first look in mtwilson.as.db.* and then in mtwilson.db.* FOR EACH proeprty separately.
 * So admin can define common database server in mtwilson.db.host and then different database names
 * for each service in mtwilson.as.db.name and mtwilson.ms.db.name etc. 
 * 
 * @author jbuhacoff
 */
public class ASConfiguration {
    
}
