/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.extensions;

import com.intel.mtwilson.setup.tasks.UpdateExtensionsCacheFile;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class UpdateExtensionsCacheFileTest {
    @Test
    public void testUpdateExtensionsCacheFile() throws Exception {
        UpdateExtensionsCacheFile task = new UpdateExtensionsCacheFile();
        task.run();
    }
}
