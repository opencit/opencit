/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.module;

/**
 * This event is posted before a module is deactivated, to allow consumers to release resources before
 * they become invalid.
 * @author jbuhacoff
 */
public class ModuleDeactivationEvent implements ModuleEvent {
    // XXX tentative
    public static final String MODULE_DEACTIVATION_EVENT = "module-deactivation-event";

    public ModuleDeactivationEvent(Module module, Object component) {
        
    }

    // XXX tentative
    @Override
    public String getName() {
        return MODULE_DEACTIVATION_EVENT;
    }
}
