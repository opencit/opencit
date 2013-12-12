/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.io;

import com.intel.dcsg.cpg.validation.InputModel;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

/**
 * A simple class to help with validating hex string inputs.
 * 
 * You can reuse the same HexInput instance to validate many inputs:
 * HexInput hex = new HexInput();
 * while(true) {
 *   hex.setInput(inputString);
 *   if( hex.isValid() ) {
 *     byte[] value = hex.value();
 *     log.debug("Got {} bytes", value.length);
 *   }
 * }
 * 
 * @since 0.1.2
 * @author jbuhacoff
 */
public class HexInput extends InputModel<byte[]> {

    @Override
    protected byte[] convert(String hexInput) {
        String hex = HexUtil.trim(hexInput);
        try {
            byte[] value = Hex.decodeHex(hex.toCharArray());
            return value;
        }
        catch(DecoderException e) {
            fault(e, "Invalid hex input: %s", hexInput);
        }
        return null;
    }
    
}
