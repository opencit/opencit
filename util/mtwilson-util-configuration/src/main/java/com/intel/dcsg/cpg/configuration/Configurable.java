/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.configuration;

/**
 * Classes implementing this interface indicate they can be configured before
 * using them, or reconfigured during use without needing to create a new
 * instance (which can be important if they were injected and the calling code
 * does not know how to or should not create new instances).
 * 
 * For example, a class could implement Configurable, Runnable so a set of
 * tasks can be configured and executed like this:
 * <pre>
 * for(Task task : tasks) {
 *   task.configure(conf);
 *   task.run();
 * }
 * </pre>
 * 
 * @author jbuhacoff
 */
public interface Configurable {
    void configure(Configuration configuration);
}
