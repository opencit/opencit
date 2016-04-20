/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent;

import com.intel.dcsg.cpg.console.HyphenatedCommandFinder;

/**
 *
 * @author jbuhacoff
 */
public class TrustagentCommandFinder extends HyphenatedCommandFinder {

    public TrustagentCommandFinder() {
        super("com.intel.mtwilson.trustagent.cmd");
    }
}
