/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.configuration;

import com.intel.dcsg.cpg.configuration.Configuration;
import java.io.IOException;

/**
 *
 * @author jbuhacoff
 */
public interface ConfigurationProvider {
    Configuration load() throws IOException;
    void save(Configuration configuration) throws IOException;
}
