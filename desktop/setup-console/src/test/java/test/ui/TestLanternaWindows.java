/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package test.ui;
import com.intel.mtwilson.setup.SetupException;
import com.intel.mtwilson.setup.cmd.Wizard;
import java.io.IOException;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class TestLanternaWindows {
    @Test
    public void testWindow() throws Exception {
        Wizard w = new Wizard();
        w.execute(null);
        System.out.println("hit enter to continue"); System.in.read();
    }
}
