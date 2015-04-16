/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jackson.validation;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;
import com.intel.dcsg.cpg.validation.Fault;

/**
 *
 * @author rksavino
 */
public class ValidationModule extends Module {

    @Override
    public String getModuleName() {
        return "ValidationModule";
    }

    @Override
    public Version version() {
        return new Version(1, 0, 0, "com.intel.mtwilson.util", "mtwilson-util-jackson-validation", null);
    }

    @Override
    public void setupModule(SetupContext sc) {
        sc.setMixInAnnotations(Fault.class, FaultMixIn.class);
    }
}
