/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.as.helper;

import com.intel.mtwilson.My;
import com.intel.dcsg.cpg.i18n.LocalizableResponseFilter;
import java.io.IOException;

/**
 *
 * @author jbuhacoff
 */
public class ASLocalizationFilter extends LocalizableResponseFilter {
    public ASLocalizationFilter() throws IOException {
        setAvailableLocales(My.configuration().getAvailableLocales());
    }
}
