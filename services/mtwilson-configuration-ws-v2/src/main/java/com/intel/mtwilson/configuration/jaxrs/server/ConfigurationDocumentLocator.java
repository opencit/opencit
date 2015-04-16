/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.configuration.jaxrs.server;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.configuration.jaxrs.ConfigurationDocument;
import com.intel.mtwilson.repository.Locator;

/**
 *
 * @author jbuhacoff
 */
public class ConfigurationDocumentLocator implements Locator<ConfigurationDocument> {
    public UUID id;

    @Override
    public void copyTo(ConfigurationDocument item) {
        item.setId(id);
    }
}
