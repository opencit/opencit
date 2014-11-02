/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.validation;

import java.math.BigDecimal;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class ValidationUtilTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ValidationUtilTest.class);
    
    public static class Pojo {
        public String name = "test";
        @Unchecked
        public String crazy = "horse";
        
        public byte[] data = null;
    }
    
    /**
     * Example output:
     * <pre>
01:11:31.050 [main] DEBUG c.i.d.c.v.ValidationUtilTest - tostring = com.intel.dcsg.cpg.validation.ValidationUtilTest$Pojo@6b51ad98
01:11:31.070 [main] DEBUG c.i.d.cpg.validation.ValidationUtil - Starting to validate class com.intel.dcsg.cpg.validation.ValidationUtilTest$Pojo
01:11:31.070 [main] DEBUG c.i.d.cpg.validation.ValidationUtil - isBuiltInType com.intel.dcsg.cpg.validation.ValidationUtilTest$Pojo ? false
01:11:31.070 [main] DEBUG c.i.d.cpg.validation.ValidationUtil - Verifying string field : name
01:11:31.070 [main] DEBUG c.i.d.cpg.validation.ValidationUtil - Verifying string value : test
01:11:31.070 [main] DEBUG c.i.d.cpg.validation.ValidationUtil - Validating test against regex ^(?:[a-zA-Z0-9\[\]$@(){}_\.\, |-]+)$
01:11:31.070 [main] DEBUG c.i.d.cpg.validation.ValidationUtil - Verifying string field : crazy
01:11:31.070 [main] DEBUG c.i.d.cpg.validation.ValidationUtil - Verifying string value : horse
01:11:31.080 [main] DEBUG c.i.d.cpg.validation.ValidationUtil - Field crazy in com.intel.dcsg.cpg.validation.ValidationUtilTest$Pojo is unchecked
01:11:31.080 [main] DEBUG c.i.d.cpg.validation.ValidationUtil - isBuiltInType java.lang.String ? true
01:11:31.080 [main] DEBUG c.i.d.cpg.validation.ValidationUtil - isBuiltInType java.lang.String ? true
01:11:31.080 [main] DEBUG c.i.d.cpg.validation.ValidationUtil - Verifying string method : toString
01:11:31.080 [main] DEBUG c.i.d.cpg.validation.ValidationUtil - Verifying method return value : com.intel.dcsg.cpg.validation.ValidationUtilTest$Pojo@67439515
01:11:31.080 [main] DEBUG c.i.d.cpg.validation.ValidationUtil - Validating com.intel.dcsg.cpg.validation.ValidationUtilTest$Pojo@67439515 against regex ^(?:[a-zA-Z0-9\[\]$@(){}_\.\, |-]+)$
01:11:31.080 [main] DEBUG c.i.d.cpg.validation.ValidationUtil - isBuiltInType void ? true
01:11:31.080 [main] DEBUG c.i.d.cpg.validation.ValidationUtil - isBuiltInType void ? true
01:11:31.080 [main] DEBUG c.i.d.cpg.validation.ValidationUtil - isBuiltInType void ? true
01:11:31.080 [main] DEBUG c.i.d.cpg.validation.ValidationUtil - isBuiltInType boolean ? true
01:11:31.080 [main] DEBUG c.i.d.cpg.validation.ValidationUtil - isBuiltInType java.lang.String ? true
01:11:31.080 [main] DEBUG c.i.d.cpg.validation.ValidationUtil - isBuiltInType int ? true
01:11:31.080 [main] DEBUG c.i.d.cpg.validation.ValidationUtil - isBuiltInType java.lang.Class ? true
01:11:31.080 [main] DEBUG c.i.d.cpg.validation.ValidationUtil - isBuiltInType void ? true
01:11:31.080 [main] DEBUG c.i.d.cpg.validation.ValidationUtil - isBuiltInType void ? true
     * </pre>
     */
    @Test
    public void testValidation() {
        log.debug("tostring = {}", new Pojo().toString());
        ValidationUtil.validate(new Pojo());
    }
    
    @Test
    public void testUuidValidation() {
        log.debug("c7da729e-b0de-4bc6-9487-22a63eaa684f valid? {}", ValidationUtil.isValidWithRegex("c7da729e-b0de-4bc6-9487-22a63eaa684f", RegexPatterns.UUID));
        log.debug("xc7da729e-b0de-4bc6-9487-22a63eaa684f valid? {}", ValidationUtil.isValidWithRegex("xc7da729e-b0de-4bc6-9487-22a63eaa684f", RegexPatterns.UUID));
        log.debug("yda729e-b0de-4bc6-9487-22a63eaa684f valid? {}", ValidationUtil.isValidWithRegex("yda729e-b0de-4bc6-9487-22a63eaa684f", RegexPatterns.UUID));
    }

    @Test
    public void testValidationWithPrimitiveArray() {
        Pojo pojo = new Pojo();
        pojo.crazy = "horse";
        pojo.name = "don";
        pojo.data = new byte[] { 0, 1, 2, 3, 4 };
        log.debug("tostring = {}", new Pojo().toString());
        ValidationUtil.validate(pojo);
    }

}
