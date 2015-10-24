/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tpm.endorsement.jdbi;

import com.intel.mtwilson.My;
import com.intel.mtwilson.jdbi.util.JdbiUtil;

/**
 *
 * @author jbuhacoff
 */
public class TpmEndorsementJdbiFactory {

    public static TpmEndorsementDAO tpmEndorsementDAO() {
        try {
            return JdbiUtil.getDBI(My.jdbc().connection()).open(TpmEndorsementDAO.class);
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
