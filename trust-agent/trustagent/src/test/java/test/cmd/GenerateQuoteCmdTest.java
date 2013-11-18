/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.cmd;

import com.intel.mountwilson.common.TAException;
import com.intel.mountwilson.trustagent.commands.GenerateQuoteCmd;
import com.intel.mountwilson.trustagent.data.TADataContext;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class GenerateQuoteCmdTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GenerateQuoteCmdTest.class);

    @Test
    public void testGenerateQuoteCmd() throws TAException {
        TADataContext context = new TADataContext();
        GenerateQuoteCmd cmd = new GenerateQuoteCmd(context);
        cmd.execute();
    }
}
