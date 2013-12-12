/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.module;

import java.util.List;

/**
 *
 * @author jbuhacoff
 */
public interface ModuleRepository {
    List<Module> listModules();
    boolean contains(String artifact);
}
