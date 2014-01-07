/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.ms.helper;

import com.intel.mtwilson.My;
import com.intel.mtwilson.util.LocalizationResponseFilter;
import java.io.IOException;

/**
 *
 * @author jbuhacoff
 */
public class MSLocalizationFilter extends LocalizationResponseFilter {
    public MSLocalizationFilter() throws IOException {
        setAvailableLocales(My.configuration().getAvailableLocales());
    }
}
