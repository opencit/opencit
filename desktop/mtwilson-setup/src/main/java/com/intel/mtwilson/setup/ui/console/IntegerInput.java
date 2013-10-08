/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.ui.console;

import com.intel.mtwilson.datatypes.Hostname;
import com.intel.mtwilson.validation.InputModel;
import com.intel.mtwilson.setup.model.YesNo;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import org.apache.commons.lang3.Range;
/**
 *
 * @author jbuhacoff
 */
public class IntegerInput extends InputModel<Integer> {

    private final Range<Integer> range;
    public IntegerInput() { 
        range = null; 
    }
    public IntegerInput(Range<Integer> intRange) {
        this.range = intRange;
    }
    public IntegerInput(int min, int max) {
        this.range = Range.between(min, max);
    }
    
    @Override
    protected Integer convert(String input) {
        try {
            Integer value = Integer.valueOf(input);
            if( range == null ) {
                return value;
            }
            else {
                if( range.contains(value) ) {
                    return value;
                }
                else {
                    fault("Not in range [%d, %d]: %d", range.getMinimum(), range.getMaximum(), value);
                }
            }
        }
        catch(java.lang.NumberFormatException e) {
            fault(e, "Not a number: %s", input);
        }
        return null;
    }
    

}
