/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls.policy.jdbi;

import com.intel.mtwilson.jdbi.util.JdbiUtil;

/**
 *
 * @author jbuhacoff
 */
public class TlsPolicyJdbiFactory {

    public static TlsPolicyDAO tlsPolicyDAO() {
        return JdbiUtil.getDBI().open(TlsPolicyDAO.class);
    }
}
