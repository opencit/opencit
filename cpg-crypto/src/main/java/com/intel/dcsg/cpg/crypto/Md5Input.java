/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto;

import com.intel.dcsg.cpg.io.HexInput;
import com.intel.dcsg.cpg.validation.ObjectModel;
import java.util.List;

/**
 *
 * @since 0.1
 * @author jbuhacoff
 */
public class Md5Input extends ObjectModel {
    private HexInput hexInput = new HexInput();
    private String hex;
    public Md5Input(String hex) {
        this.hex = hex;
    }
    
    @Override
    protected void validate() {
        hexInput.setInput(hex);
        if( !hexInput.isValid() ) {
            fault(hexInput, "Invalid hex input: %s", hex);
            return;
        }
        //Md5Digest md5 = new Md5Digest(hex);
        //Md5Digest.valueOf(digest)
    }
    
}
