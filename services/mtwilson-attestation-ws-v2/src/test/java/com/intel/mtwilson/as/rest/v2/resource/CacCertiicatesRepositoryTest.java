/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.as.rest.v2.resource;

import org.junit.Test;
import com.intel.mtwilson.as.rest.v2.model.*;
import com.intel.mtwilson.as.rest.v2.repository.CaCertificateRepository;
import java.util.List;

/**
 *
 * @author jbuhacoff
 */
public class CacCertiicatesRepositoryTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CacCertiicatesRepositoryTest.class);

    @Test
    public void testSearchEkCertificates() {
        CaCertificateFilterCriteria criteria = new CaCertificateFilterCriteria();
        criteria.domain = "ek";
        CaCertificateRepository repository = new CaCertificateRepository();
        CaCertificateCollection searchResults = repository.search(criteria);
        List<CaCertificate> documents = searchResults.getCaCertificates();
        for(CaCertificate item : documents) {
            log.debug("search result: {}", item.getX509Certificate().getSubjectX500Principal().getName());
        }
    }
}
