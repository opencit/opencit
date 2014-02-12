/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.datatypes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class JacksonTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JacksonTest.class);

    
    @Test
    public void testWritePojo() throws Exception {
        ApiClientCreateRequest pojo = new ApiClientCreateRequest();
        pojo.setRoles(new String[] { "a", "b", "c" });
        pojo.setCertificate(new byte[] { 0, 1, 2, 3 });
        ObjectMapper mapper = new ObjectMapper();
        log.debug("pojo: {}", mapper.writeValueAsString(pojo));
        // output:  pojo: {"X509Certificate":"AAECAw==","Roles":["a","b","c"]}
    }
    
    @Test
    public void testWritePojoWithPropertyNamingStrategy() throws Exception {
        ApiClientCreateRequest pojo = new ApiClientCreateRequest();
        pojo.setRoles(new String[] { "a", "b", "c" });
        pojo.setCertificate(new byte[] { 0, 1, 2, 3 });
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(new PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy());
        log.debug("pojo: {}", mapper.writeValueAsString(pojo));
        // output:  pojo: {"x509_certificate":"AAECAw==","roles":["a","b","c"]}
    }

}
