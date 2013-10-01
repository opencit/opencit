/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy.dynamic;

/**
 * Represents a variable value (such as host's known UUID) in a dynamic expected PCR calculation.
 * Variables are applied to expressions in order to fill place-holders required
 * for evaluation.
 * @since 1.2
 * @author jbuhacoff
 */
public class Variable {
    public String name;
    public Class type;
}
