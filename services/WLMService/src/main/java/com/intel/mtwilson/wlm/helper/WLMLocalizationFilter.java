/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.wlm.helper;

import com.intel.mtwilson.My;
import com.intel.mtwilson.util.LocalizationResponseFilter;
import java.io.IOException;

/**
 *
 * @author jbuhacoff
 */
public class WLMLocalizationFilter extends LocalizationResponseFilter {
    public WLMLocalizationFilter() throws IOException {
        setAvailableLocales(My.configuration().getAvailableLocales());
    }
}
