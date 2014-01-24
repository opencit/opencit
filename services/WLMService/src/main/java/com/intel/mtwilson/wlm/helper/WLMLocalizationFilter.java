/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.wlm.helper;

import com.intel.mtwilson.My;
import com.intel.dcsg.cpg.i18n.LocalizableResponseFilter;
import java.io.IOException;

/**
 *
 * @author jbuhacoff
 */
public class WLMLocalizationFilter extends LocalizableResponseFilter {
    public WLMLocalizationFilter() throws IOException {
        setAvailableLocales(My.configuration().getAvailableLocales());
    }
}
