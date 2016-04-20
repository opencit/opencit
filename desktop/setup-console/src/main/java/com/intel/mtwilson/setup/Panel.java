/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup;

//import java.util.Iterator;

import com.intel.dcsg.cpg.validation.Model;


/**
 * just a draft
 * Replace with an XML model that is fully navigable? That way the UI
 * has complete control over how it presents it... could be single page, or
 * wizard, etc.
 * @author jbuhacoff
 */
public interface Panel {
    String title();
    boolean isOptional(); // true if you can skip it
    boolean isComplete();
//    Iterator<Node> children(); // child nodes, in order
    Model[] content();
}
