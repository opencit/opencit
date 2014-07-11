/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tag.model;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.repository.Locator;
import javax.ws.rs.PathParam;

/**
 *
 * @author jbuhacoff
 */
public class FileLocator implements Locator<File> {
    
    @PathParam("id")
    public UUID id;
    
    @Override
    public void copyTo(File item) {
        if (id != null) {
            item.setId(id);
        }
    }
    
}
