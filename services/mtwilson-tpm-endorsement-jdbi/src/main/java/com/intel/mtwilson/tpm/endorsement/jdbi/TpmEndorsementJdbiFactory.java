/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tpm.endorsement.jdbi;

import com.intel.mtwilson.jdbi.util.JdbiUtil;

/**
 *
 * @author jbuhacoff
 */
public class TpmEndorsementJdbiFactory {

    public static TpmEndorsementDAO tpmEndorsementDAO() {
        return JdbiUtil.getDBI().open(TpmEndorsementDAO.class);
    }
}
