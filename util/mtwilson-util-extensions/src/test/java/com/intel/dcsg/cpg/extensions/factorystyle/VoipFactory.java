/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.extensions.factorystyle;

/**
 *
 * @author jbuhacoff
 */
public class VoipFactory {
    public Voip create() {
        // pretend the "provider" was configured somewhere, like with My.configuration().getProvider()
        return new Voip("Example Inc.");
    }
}
