/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Definition of a Resource here is something that can be re-used. Therefore
 * the getInputStream must be repeatable and it must be possible to repeatedly
 * call getOutputStream to obtain an OutputStream to write to.
 * 
 * XXX Should we add a getURL() method so users can identify the resource?
 * The creator of the resource would set the URL.
 * 
 * @author jbuhacoff
 */
public interface Resource {
    /**
     * If the resource does not have any data, this method may return null or
     * it may throw an EOFException.
     * If the resource represents a file and the file is not found, calling
     * this method may throw a FileNotFoundException.  
     * @return
     * @throws IOException 
     */
    InputStream getInputStream() throws IOException;
    
    /**
     * You must close the OutputStream after writing to ensure that the
     * contents are written to their destination.
     * @return
     * @throws IOException 
     */
    OutputStream getOutputStream() throws IOException;
}
