/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.extensions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class ExtensionProviderTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExtensionProviderTest.class);

    @Test
    public void testFindProviders() {
        Collection<ExtensionProvider> providers = new ArrayList<>();
        ServiceLoaderExtensionProvider sl = new ServiceLoaderExtensionProvider();
        Iterator<String> providerNamesIterator = sl.find(ExtensionProvider.class);
        while (providerNamesIterator.hasNext()) {
            String providerName = providerNamesIterator.next();
            log.debug("Found extension provider: {}", providerName);
            try {
                ExtensionProvider provider = sl.create(ExtensionProvider.class, providerName);
                providers.add(provider);
            } catch (ExtensionNotFoundException e) {
                log.error("Cannot use extension provider: {}", providerName, e);
            }
        }
    }
}
