/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro;

import com.intel.dcsg.cpg.io.UUID;

/**
 *
 * @author jbuhacoff
 */
public class UserId {
    private UUID userId;

    public UserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getUserId() {
        return userId;
    }
    
}
