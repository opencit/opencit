/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.io;

import com.intel.dcsg.cpg.i18n.LocaleUtil;
import java.util.Locale;
import org.junit.Test;
import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class LocaleUtilTest {
    private static Logger log = LoggerFactory.getLogger(LocaleUtilTest.class);
    
    @Test
    public void testLocaleToTag() {
        assertEquals("en", LocaleUtil.toLanguageTag(new Locale("en")));
        assertEquals("en-US", LocaleUtil.toLanguageTag(new Locale("en", "US")));
        assertEquals("US", LocaleUtil.toLanguageTag(new Locale("", "US")));
    }

    @Test
    public void testTagToLocale() {
        assertEquals(new Locale("en"), LocaleUtil.forLanguageTag("en"));
        assertEquals(new Locale("en","US"), LocaleUtil.forLanguageTag("en-US"));
        assertEquals(new Locale("", "US"), LocaleUtil.forLanguageTag("US"));
    }

}
