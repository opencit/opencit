/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.util;

import com.intel.dcsg.cpg.util.DepthFirstTreeSearch;
import com.intel.dcsg.cpg.util.FileTree;
import com.intel.dcsg.cpg.util.Visitor;
import java.io.File;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class DepthFirstTreeSearchTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DepthFirstTreeSearchTest.class);

    @Test
    public void testFileTreeSearch() {
        FileTree tree = new FileTree();
        DepthFirstTreeSearch<File> search = new DepthFirstTreeSearch<File>();
        Visitor<File> visitor = new Visitor<File>() {
            public void visit(File item) {
                log.debug("visit: {}", item.getAbsolutePath());
            }
        };
        search.search(tree, new File(System.getProperty("user.home")+File.separator+".mystery-hill"), visitor);
    }
}
