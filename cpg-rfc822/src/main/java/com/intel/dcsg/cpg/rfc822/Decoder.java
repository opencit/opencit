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
public interface Decoder {
    byte[] decode(byte[] encoded) throws IOException;
}
