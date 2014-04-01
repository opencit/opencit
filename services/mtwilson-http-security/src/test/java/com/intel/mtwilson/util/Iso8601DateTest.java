/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util;

import com.intel.dcsg.cpg.iso8601.Iso8601Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class Iso8601DateTest {
    private static final Logger log = LoggerFactory.getLogger(Iso8601DateTest.class);
    
    
    private static final String rYear = "(?:[0-9]{4})";
    private static final String rMonth = "(?:0[1-9]|1[0-2])";
    private static final String rDay = "(?:0[1-9]|[1-2][0-9]|3[0-1])";
    private static final String rHour = "(?:[0-1][0-9]|2[0-3])";
    private static final String rMinute = "(?:[0-5][0-9])";
    private static final String rSecond = "(?:[0-5][0-9]|60)"; // the 60 is for leap second
    private static final String rFraction = "(?:\\056[0-9]+)"; // octal 46 is hex 0x2e for period "." (ascii 46)
    private static final String rTimezoneWithZ = "[Zz]";
    private static final String rTimezoneWithColon = rHour+":"+rMinute;
    private static final String rTimezoneWithoutColon = rHour+rMinute;
    private static final String rTimezone = "(?:"+rTimezoneWithZ+"|[+-]"+rTimezoneWithColon+"|[+-]"+rTimezoneWithoutColon+")";
    private static final String rTime = "(?:"+rHour+":"+rMinute+":"+rSecond+rFraction+"|"+rHour+":"+rMinute+":"+rSecond+"|"+rHour+":"+rMinute+")";
    private static final String rCompleteTime = "(?:"+rTime+rTimezone+"?)";
    private static final String rDate = rYear+"-"+rMonth+"-"+rDay;
    private static final String rDatetime = rDate+"[Tt]"+rCompleteTime;
    
    private static final Pattern pDatetime = Pattern.compile(rDatetime);
    private static final Pattern pTimezone = Pattern.compile(rTimezone+"$"); // anchor to end of line
    
    private void parse(String text) {
        log.debug("Trying to parse: "+text);
        Iso8601Date date = Iso8601Date.valueOf(text);
        log.debug("Parse result: "+date.toString());
    }
    
    @Test
    public void testParseDate() {
        parse("1999-12-31T23:59");
        parse("1999-12-31T23:59:00");
        parse("1999-12-31T23:59:00.000");
        parse("1999-12-31T23:59:00Z");
        parse("1999-12-31T23:59:00.000Z");
        parse("1999-12-31T23:59:00-0000");
        parse("1999-12-31T23:59:00.000-0000");
        parse("1999-12-31T23:59:00-0800");
        parse("1999-12-31T23:59:00.000-0800");
    }
}
