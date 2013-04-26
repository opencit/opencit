/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This resource class allows you to specify different resources for input
 * and output. This allows to conveniently make a copy of a working file
 * or provide a working file as a read-only copy by setting a different
 * output destination, all without having to modify the code that is using
 * the resource.
 * 
 * Example:
 * 
 * FileResource in = new FileResource(readOnlyFile);
 * ByteArrayResource out = new ByteArrayResource();
 * Resource readonly = new CopyResource(in, out);
 * workWithResource(readonly);
 * // if the method workWithResource makes changes and saves, then the
 * // changes will be saved to the "out" resource, leaving the readOnlyFile
 * // unmodified.
 * 
 * @author jbuhacoff
 */
public class CopyResource implements Resource {
    private Resource in;
    private Resource out;
    
    public CopyResource(Resource in, Resource out) {
        this.in = in;
        this.out = out;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return in.getInputStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return out.getOutputStream();
    }
}
