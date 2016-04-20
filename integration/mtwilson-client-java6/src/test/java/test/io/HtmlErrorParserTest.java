/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package test.io;

import com.intel.mtwilson.HtmlErrorParser;
import java.io.IOException;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class HtmlErrorParserTest {
    @Test
    public void testGlassfishError500() throws IOException {
        String html = IOUtils.toString(getClass().getResourceAsStream("/glassfish-error-500.html"));
        HtmlErrorParser errorParser = new HtmlErrorParser(html);
        System.out.println("ServerName: "+errorParser.getServerName());
        System.out.println("RootCause: "+errorParser.getRootCause());
    }
}
