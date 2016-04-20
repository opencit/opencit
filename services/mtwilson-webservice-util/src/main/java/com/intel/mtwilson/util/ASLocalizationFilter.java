/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util;

import com.intel.mtwilson.My;
import com.intel.dcsg.cpg.i18n.LocalizableResponseFilter;
import java.io.IOException;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;

/**
 *
 * @author jbuhacoff
 */
@Priority(Priorities.ENTITY_CODER)
public class ASLocalizationFilter extends LocalizableResponseFilter {
    public ASLocalizationFilter() throws IOException {
        setAvailableLocales(My.configuration().getAvailableLocales());
    }
}
