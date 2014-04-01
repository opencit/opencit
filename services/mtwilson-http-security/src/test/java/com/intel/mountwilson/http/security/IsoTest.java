/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mountwilson.http.security;

import com.intel.dcsg.cpg.iso8601.Iso8601Date;
import java.text.ParseException;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class IsoTest {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Test
    public void testIso8601Format() {
        log.debug(new Iso8601Date(new Date(System.currentTimeMillis())).toString());
        log.debug(new Iso8601Date(System.currentTimeMillis()).toString());
        assertTrue(true);
    }
    
    @Test
    public void testIso8601ParseWithTimezoneOffsetWithColon() throws ParseException {
        log.debug(Iso8601Date.valueOf("2012-03-05T14:04:48-08:00").toString());
        assertTrue(true);
    }

    @Test
    public void testIso8601ParseWithTimezoneOffsetWithoutColon() throws ParseException {
        log.debug(Iso8601Date.valueOf("2012-03-05T14:04:48-0800").toString());
        assertTrue(true);
    }

    @Test
    public void testIso8601ParseWithoutTimezoneOffset() throws ParseException {
        log.debug(Iso8601Date.valueOf("2012-03-05T14:04:48").toString());
        assertTrue(true);
    }
    
}
