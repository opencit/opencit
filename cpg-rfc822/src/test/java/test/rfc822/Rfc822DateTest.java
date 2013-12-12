/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package test.rfc822;

import com.intel.dcsg.cpg.rfc822.Rfc822Date;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class Rfc822DateTest {
    private Logger log = LoggerFactory.getLogger(getClass());
    
    @Test
    public void testParseDate() throws IOException {
        Date date = Rfc822Date.parse("Mon Jul 22 17:21:56 EDT 2013"); // sample output of 'date' command in linux and java's date.toString()
        log.debug("Parsed date: {}", date.toString());
    }
    
    @Test
    public void testParseDateWithSimpleDateFormatter() throws ParseException {
        SimpleDateFormat f = new SimpleDateFormat();
        Date date = f.parse("Mon Jul 22 17:21:56 EDT 2013"); // sample output of 'date' command in linux and java's date.toString()
        log.debug("Parsed date: {}", date.toString());
    }

    
    
    @Test
    public void testParseDateR() throws IOException {
        Date date = Rfc822Date.parse("Thu, 15 Aug 2013 21:37:18 -0700"); // sample output of 'date -R' command in linux; -R means rfc 2822 mode
        log.debug("Parsed date: {}", date.toString());
    }

}
