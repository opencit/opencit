/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.as.rest.v2.resource;

import com.intel.mtwilson.My;
import com.intel.mtwilson.as.rest.v2.model.UserCertificate;
import java.io.IOException;
import java.util.Map;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class UserCertificatesTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserCertificatesTest.class);
    
    @Test
    public void testRetrieveUserCertificate() throws IOException {
        log.debug("mtwilson.db.host = {} configured in {}", My.configuration().getDatabaseHost(), My.configuration().getSource("mtwilson.db.host"));
        log.debug("mtwilson.db.port = {} configured in {}", My.configuration().getDatabasePort(), My.configuration().getSource("mtwilson.db.port"));
        log.debug("mtwilson.db.user = {} configured in {}", My.configuration().getDatabaseUsername(), My.configuration().getSource("mtwilson.db.user"));
        log.debug("mtwilson.db.password = {} configured in {}", My.configuration().getDatabasePassword(), My.configuration().getSource("mtwilson.db.password"));
        log.debug("mtwilson.db.driver = {} configured in {}", My.configuration().getDatabaseDriver(), My.configuration().getSource("mtwilson.db.driver"));
        log.debug("mtwilson.db.schema = {} configured in {}", My.configuration().getDatabaseSchema(), My.configuration().getSource("mtwilson.db.schema"));
        log.debug("jdbc url = {}", My.jdbc().url());
        UserCertificates userCertificates = new UserCertificates();
        UserCertificate userCertificate = userCertificates.retrieve("7f2a647d-8172-44a6-b15a-30eaa42580e7");
        log.debug("Retrieved user certificate: {}", userCertificate.getName());
    }
}
