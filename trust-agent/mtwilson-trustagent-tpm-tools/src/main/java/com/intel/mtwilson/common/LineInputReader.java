/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jbuhacoff
 */
public class LineInputReader implements Runnable {
    private final BufferedReader reader;
    private final ArrayList<String> lines = new ArrayList<>();
    
    public LineInputReader(InputStream in) {
        reader = new BufferedReader(new InputStreamReader(in));
    }
    
    
    @Override
    public void run() {
        try {
            // read until we reach EOF or an error
            while(true) {
                String line = reader.readLine(); // returns null at EOF
                if( line == null ) { break; }
                lines.add(line);
            }
            reader.close();
        }
        catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getLines() {
        return lines;
    }
    
    
}
