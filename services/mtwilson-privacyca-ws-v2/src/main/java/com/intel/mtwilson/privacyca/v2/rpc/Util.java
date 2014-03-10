/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.privacyca.v2.rpc;

import static gov.niarl.his.privacyca.TpmUtils.byteArrayToHexString;
import static gov.niarl.his.privacyca.TpmUtils.hexStringToByteArray;
import java.util.BitSet;

/**
 *
 * @author jbuhacoff
 */
public class Util {
    public static class TpmIdentityProofOptions {
        public boolean TrousersModeIV = false;
        public boolean TrousersModeSymkeyEncscheme = false;
        public boolean TrousersModeBlankOeap = false;
    }
    public static String encodeTpmIdentityProofOptionsToHex(TpmIdentityProofOptions options) {
        return byteArrayToHexString(encodeTpmIdentityProofOptionsToByteArray(options));
    }
    public static byte[] encodeTpmIdentityProofOptionsToByteArray(TpmIdentityProofOptions options) {
        BitSet bits = new BitSet(3);
        bits.clear();
        bits.set(0, options.TrousersModeIV);
        bits.set(1, options.TrousersModeSymkeyEncscheme);
        bits.set(2, options.TrousersModeBlankOeap);
        return bits.toByteArray();
    }
    public static TpmIdentityProofOptions decodeTpmIdentityProofOptionsFromHex(String encodedOptions) {
        return decodeTpmIdentityProofOptionsFromByteArray(hexStringToByteArray(encodedOptions));
    }
    public static TpmIdentityProofOptions decodeTpmIdentityProofOptionsFromByteArray(byte[] encodedOptions) {
        BitSet bits = BitSet.valueOf(encodedOptions);
        TpmIdentityProofOptions options = new TpmIdentityProofOptions();
        options.TrousersModeIV = bits.get(0);
        options.TrousersModeSymkeyEncscheme = bits.get(1);
        options.TrousersModeBlankOeap = bits.get(2);
        return options;
    }    
}
