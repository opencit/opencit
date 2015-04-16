/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.validation;

import java.util.Collection;

/**
 * Indicates the the implementing object may keep track of faults during
 * its processing and will make them available via the {@code getFaults()}
 * method. The state in which faults may become available is implementation-
 * dependent. For example, a class implementing both Runnable and Faults may
 * add faults during its {@code run()} method so it wouldn't make sense to
 * check the faults before calling {@code run()}.
 * @author jbuhacoff
 */
public interface Faults {
    Collection<Fault> getFaults();
}
