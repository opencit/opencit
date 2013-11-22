/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.rfc822;

import java.io.IOException;

/**
 *
 * @author jbuhacoff
 */
public interface Encoder {
    byte[] encode(byte[] data) throws IOException;
}
